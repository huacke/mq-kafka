package com.mq.consumer.kafka.worker;

import com.mq.common.response.ResultHandleT;
import com.mq.common.utils.ReflectionUtils;
import com.mq.consumer.kafka.KafkaConsumerBuilder;
import com.mq.consumer.kafka.KafkaConsumerManager;
import com.mq.consumer.kafka.biz.bean.TopicGroup;
import com.mq.consumer.kafka.handler.KafkaMessageConsumeWorkHandler;
import com.mq.msg.Message;
import com.mq.msg.kafka.KafkaMessage;
import com.mq.msg.kafka.TopicDef;
import com.mq.utils.GsonHelper;
import com.mq.utils.ThreadUtil;
import com.mq.worker.AbstractMsgWorker;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author huacke
 * @version v1.0
 * @ClassName DefaultKafkaMsgWorker
 * 处理器使用多线程的方式去处理消息和业务逻辑
 * 因为kafka一个分区对应的一个分组（点对点模式，不过订阅模式是对应多个分组）同一时刻只会有一个消费者消费，所以一般一个分区分组设置一个消费者线程
 *
 * 为了避免因为业务逻辑执行时间过长导致消息session超时，然后导致kafka分区再均衡，采用了多线程去处理业务逻辑，可以设置超时。
 * 消费端应该根据自己业务逻辑大概执行的平均时间，设置合理的超时时间 sessionTimeoutMillis
 * 注意： sessionTimeoutMillis 对应的时间是consumer.poll()消费者从Kafka一次拉取然后把消息处理完的时间间隔，如果业务执行过慢导致超时
 * kafka会认为消费者挂了，然后引发分区消费者重新分配，有可能导致消息重复消费
 * workerExecutetTimeOutMillis，工作线程超时时间，可以根据自己业务平均处理时间合理设置
 *
 * @Description 默认消息处理器
 */
@Slf4j
public abstract class DefaultKafkaMsgWorker<R,T,M extends Message<T>> extends AbstractMsgWorker<R,M> implements InitializingBean, DisposableBean {

    @Autowired
    private KafkaConsumerManager manager;
    @Autowired
    private KafkaConsumerBuilder builder;

    //处理器简称
    private String classSimpleName = this.getClass().getSimpleName();
    //运行标记
    private final AtomicBoolean running = new AtomicBoolean(false);
    //消费者线程池线程数量
    private final AtomicInteger  consumerThreadNum = new AtomicInteger(0);
    //消费者实例集合
    private List<KafkaConsumer> consumers = Collections.synchronizedList(new ArrayList<>());
    //消费者线程池实例
    private ExecutorService consumerExecutor;
    //工作线程池实例
    private ExecutorService workerExecutor;
    //工作线程执行超时时间 默认25秒
    private long workerExecutetTimeOutMillis=25*1000;
    //消费者配置信息
    private Map<String, Object> consumerConfMap = new HashMap<>();
    //主题和消费组的映射,
    private Map<TopicDef.Topic, List<TopicGroup>> topicGroupMap = new HashMap<>();
    //每次从kafka poll获取批量消息的处理超时时间（两次poll的间隔时间）
    //特别要注意业务执行逻辑尽量要快，超时了会引起分区再均衡，而且无法提交offset
    private long pollIntervalTimeoutMillis=1800*1000;
    //每次批量从kfaka poll最多能拉取消息的数量
    private  int maxPollRecords=500;

    //消费线程总计
    private int totalConsumerSize =1;

    //消息数据类型
    private Class<T>  msgDataTypeClasszz = ReflectionUtils.getSuperClassGenricType(this.getClass(),1);

    /**
     * @Description 配置消费者核心参数
     * @param topicGroups 订阅的主题和消费组的映射列表，如果有多个分组，则对应的就是订阅模式
     * 因为kafka一个分区只能同时有一个消费者消费，建议设置成分区数量
     * 公式：topic分区数量
     * @param maxPollRecords 每次批量从kfaka poll最多能拉取消息的数量
     * @param pollIntervalTimeoutMillis 每次从kafka分区获取批量消息的处理超时时间
     * @param workerExecutetTimeOutMillis 工作线程执行超时时间
     * @param extConfMap 附加参数，可以自己定义消费者的配置参数
     **/
    protected  void doConfigure(List<TopicGroup> topicGroups,int maxPollRecords,long pollIntervalTimeoutMillis, long workerExecutetTimeOutMillis, Map<String,Object> extConfMap){

        Optional.ofNullable(topicGroups).ifPresent(it -> {
            Map<TopicDef.Topic, List<TopicGroup>> topicMapping = topicGroups.stream().filter(i -> i.getTopic() != null && i.getGroup() != null).collect(Collectors.groupingBy(TopicGroup::getTopic));
            Iterator<Map.Entry<TopicDef.Topic, List<TopicGroup>>> topicIterator = topicMapping.entrySet().iterator();
            while (topicIterator.hasNext()) {
                Map.Entry<TopicDef.Topic, List<TopicGroup>> currentTopic = topicIterator.next();
                List<TopicGroup> groupList = currentTopic.getValue();
                List<TopicGroup> distinctGroups = currentTopic.getValue().stream().distinct().collect(Collectors.toList());
                //去重处理
                if (!ObjectUtils.isEmpty(distinctGroups)) {
                    groupList.clear();
                    groupList.addAll(distinctGroups);
                }
            }
            this.topicGroupMap.putAll(topicMapping);
            long totalConsumerSize = topicGroupMap.entrySet().stream().map(tp -> tp.getValue()).flatMap(gp -> Arrays.stream(gp.toArray())).mapToInt(gpt -> ((TopicGroup) gpt).getConsumerSize()).sum();
            this.totalConsumerSize = totalConsumerSize > 0 ? (int) totalConsumerSize : this.totalConsumerSize;
        });
        if (extConfMap != null) { consumerConfMap.putAll(extConfMap); }
        if (maxPollRecords <= 0) { maxPollRecords = this.maxPollRecords; }
        if (pollIntervalTimeoutMillis <= 0) { pollIntervalTimeoutMillis = this.pollIntervalTimeoutMillis; }
        if (workerExecutetTimeOutMillis <= 0) { workerExecutetTimeOutMillis = this.workerExecutetTimeOutMillis; }
        this.pollIntervalTimeoutMillis = pollIntervalTimeoutMillis;
        this.workerExecutetTimeOutMillis = workerExecutetTimeOutMillis;
        this.maxPollRecords=maxPollRecords;
        consumerConfMap.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,this.maxPollRecords);
        consumerConfMap.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, (int)this.pollIntervalTimeoutMillis);
    }

    @Override
    public  R onWork(M msg) {
        return work(msg);
    }

    private  R work(M message) {
        return doWork(message);
    }

    /**
     * @Description 校验基本的参数
     **/
    private void validate() {
        Assert.notEmpty(topicGroupMap.keySet(), "topics is empty!");
        Assert.notEmpty(topicGroupMap.values(), "groups is empty!");
        Assert.isTrue(totalConsumerSize >= 1 ? true : false, "consumerSize must lagger 0!");
        Assert.isTrue(pollIntervalTimeoutMillis >= 1 ? true : false, "pollIntervalTimeoutMillis must lagger 0!");
        Assert.isTrue(workerExecutetTimeOutMillis >= 1 ? true : false, "workerExecutetTimeOutMillis must lagger 0!");
    }


    public void buildAndStartConsumer() {
        //循环每个消费分组
        topicGroupMap.entrySet().forEach(topicAndGorup->{
            String topic = topicAndGorup.getKey().name();
            List<TopicGroup> topicGroups = topicAndGorup.getValue();
            topicGroups.forEach(group->{
                IntStream.range(0, group.getConsumerSize()).forEach(it -> {
                    consumerConfMap.put(ConsumerConfig.GROUP_ID_CONFIG, group.getGroup().name());
                    //构建消费者
                    KafkaConsumer kafkaConsumer = builder.build(consumerConfMap);
                    consumers.add(kafkaConsumer);
                    //设置运行状态
                    running.set(true);
                    //使用多线程消费数据，每个消费者对应一个线程
                    consumerExecutor.submit(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            try {
                                Field groupIdField = kafkaConsumer.getClass().getDeclaredField("groupId");
                                if(groupIdField!=null){
                                    groupIdField.setAccessible(true);
                                    String groupId = (String)groupIdField.get(kafkaConsumer);
                                    Thread currentThread = Thread.currentThread();
                                    currentThread.setName(classSimpleName+"-ConsumerThreadPool-"+groupId+"-Consumer"+consumerThreadNum.addAndGet(1));
                                }
                                //开始启动消费者，消费数据
                                manager.startup(Arrays.asList(new String[]{topic}), kafkaConsumer,running, new KafkaMessageConsumeWorkHandler<T,R>() {
                                    @Override
                                    public ResultHandleT<R> doHandle(KafkaMessage message) {
                                        ResultHandleT<R> result =new ResultHandleT<>();
                                        try {
                                            Future<ResultHandleT<R>> workFuture = workerExecutor.submit(new Callable<ResultHandleT<R>>() {
                                                @Override
                                                public ResultHandleT call() throws Exception {
                                                    ResultHandleT<R> resultHandleT= new ResultHandleT();
                                                    Thread currentThread = Thread.currentThread();
                                                    long threadId = currentThread.getId();
                                                    String currentThreadName = currentThread.getName();
                                                    String threadName = classSimpleName + "-WorkerThreadPool-" + threadId;
                                                    if (!currentThreadName.equals(threadName)) {
                                                        currentThread.setName(threadName);
                                                    }
                                                    R excuteResult = null;
                                                    try {
                                                        T messageData = GsonHelper.fromJson(GsonHelper.toJson(message.getMsgData()), msgDataTypeClasszz);
                                                        message.getMessageBody().setMsgData(messageData);
                                                        excuteResult =  onWork((M)message);
                                                        resultHandleT.setData(excuteResult);
                                                    } catch (Exception e) {
                                                        log.error(String.format("%s Consumer excute bussiness work error cause by:", classSimpleName), e);
                                                        resultHandleT.setE(e);
                                                    }
                                                    return resultHandleT;
                                                }
                                            });
                                            //业务逻辑执行，超时根据设定的超时时间
                                            ResultHandleT<R> rs = workFuture.get(workerExecutetTimeOutMillis, TimeUnit.MILLISECONDS);
                                            if(rs!=null){
                                                result =rs;
                                            }
                                        } catch (Exception e) {
                                            result.setE(e);
                                            log.error(String.format("%s Consumer excute bussiness work error cause by:", classSimpleName), e);
                                        }
                                        return result;
                                    }
                                });
                            }catch (InterruptedException e){
                                Thread.currentThread().interrupt();
                            } catch (Exception e) {
                                log.error(String.format("%s startConsumer error,cause by:", classSimpleName), e);
                            }
                            return null;
                        }
                    });
                });
            });
        });

        return;
    }


    /* *
     * @Description 初始化消费者线程池
     **/
    private void initializeConsumerExecutor(int corePoolSize,int maxPoolSize,int keepAliveTimeSeconds) {
        if (null == consumerExecutor) {
            consumerExecutor= createWorkExecutor(corePoolSize,maxPoolSize,keepAliveTimeSeconds,new LinkedBlockingQueue(maxPoolSize+4),10);
        }
    }



    /* *
     * @Description 初始化工作线程池
     **/
    private void initializeWorkerExecutorExecutor(int corePoolSize,int maxPoolSize,int keepAliveTimeSeconds) {
        if (null == workerExecutor) {
            workerExecutor= createWorkExecutor(corePoolSize,maxPoolSize,keepAliveTimeSeconds,new LinkedBlockingQueue(20000),10);
        }
    }


    /* *
     * @Description 创建线程池
     **/
    private ExecutorService createWorkExecutor(int corePoolSize, int maxPoolSize, int keepAliveTimeSeconds, BlockingQueue blockingQueue,int priority) {

        ExecutorService consumerExecutor = new ThreadPoolExecutor(corePoolSize,
                maxPoolSize,
                keepAliveTimeSeconds,
                TimeUnit.SECONDS,
                blockingQueue, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setPriority(priority);
                return thread;
            }
        }, new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                //线程池任务队列满了之后，会执行此拒绝策略，此时会先先休眠一小段时间，然后再提交到任务队列重试
                if (!executor.isShutdown()) {
                    try {
                        Thread.sleep(50);
                    } catch (Exception e) {
                        log.error(String.format("%s 线程池使用拒绝策略时出错,原因： ", classSimpleName), e);
                    }
                    log.info(String.format("%s 线程池队列已满,开始尝试重新提交任务当前线程 ：%s ", classSimpleName,Thread.currentThread().getName()));
                    executor.submit(r);
                }
            }
        });
        return consumerExecutor;
    }

    /**
     * @Description 初始化消费者配置
     **/
    protected abstract void configure();
    /**
     * @Description 初始化配置
     **/
    private   void initCofing(){
        configure();
    }

    /* *
     * @Description 初始化Worker
     **/
    private void init() {
        //初始化配置
        initCofing();
        //校验
        validate();
        //初始化消费者线程执行器
        initializeConsumerExecutor(totalConsumerSize,totalConsumerSize,180);
        //初始化工作线程执行器
        initializeWorkerExecutorExecutor(ThreadUtil.getCpuPriorityThreadNum(null),ThreadUtil.getCpuPriorityThreadNum(null),180);
        //构造消费者实例，并启动
        buildAndStartConsumer();
    }


    private void releaseConsumers(){
        try {
            consumers.forEach(it -> {
                try {
                    it.wakeup();
                } catch (Exception e) {
                    log.error(String.format("%s 消费者wakeup出错，原因:", classSimpleName), e);
                }
            });
        } catch (Exception e) {
            log.error(String.format("%s 关闭消费者时出错，原因 : ", classSimpleName), e);
        }
    }


    private void releaseExecutor(ExecutorService executor,int type){

        String typeName ="";
        if(type==1){
            typeName ="消费";
        }else if(type==2){
            typeName ="工作";
        }
        //释放消费者线程池资源
        try {
            if (executor != null && !executor.isTerminated()) {
                log.info(String.format("%s 开始关闭%s线程池资源..................... ", classSimpleName,typeName));
                executor.shutdown();
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                }
                int retryCount = 200;
                int retry = 0;
                //循环检查线程池是否已经关闭，达到一定次数，然后再等待一段时间关闭线程池
                ThreadPoolExecutor exr = (ThreadPoolExecutor) executor;
                while (!executor.isTerminated() && retryCount > 0) {
                    retry++;
                    long taskCount = exr.getTaskCount();
                    long completedTaskCount = exr.getCompletedTaskCount();
                    long notCompletedTaskCount = taskCount-completedTaskCount;
                    log.info(String.format("%s 正在重试等待%s线程资源关闭,重试次数：%s,队列剩余容量：%s，当前未完成任务数：%s", classSimpleName,typeName, retry,exr.getQueue().remainingCapacity(),notCompletedTaskCount));
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                    }
                    retryCount--;
                }
                if(!executor.isTerminated()){
                    log.info(String.format("%s 重试等待%s线程资源超时，准备使用等待超时释放策略 .....", classSimpleName,typeName));
                    //重试多次失败，等待一段时间，关闭线程池
                    executor.awaitTermination(5, TimeUnit.MINUTES);
                }
                log.info(String.format("%s  释放%s线程池资源完毕！  ", classSimpleName,typeName));
            }
        } catch (Exception e) {
            log.info(String.format("%s 释放%s线程池资源时出错！原因 : ", classSimpleName,typeName), e);
        }

    }
    //释放消费者线程池占用资源
    private void releaseConsumerExecutor(){
        releaseExecutor(consumerExecutor,1);
    }

    //释放消费者工作线程占用资源
    private void releaseWorkerExecutor(){ releaseExecutor(workerExecutor,2); }

    //释放消费者管理器资源
    private void releaseConsumerManager(){ manager.release(); }

    /* *
     * @Description 释放worker占用的资源
     **/
    private void release() {

        log.info(String.format("%s 开始释放资源............", classSimpleName));

        //运行状态设置为无效
        running.set(false);

        //释放消费者资源
        releaseConsumers();

        //释放消费者线程池占用资源
        releaseConsumerExecutor();

        //释放消费者工作线程占用资源
        releaseWorkerExecutor();

        //释放消费者管理器资源
        releaseConsumerManager();


        log.info(String.format("%s 释放资源完毕！", classSimpleName));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //bean 初始化时，进行资源初始化
        init();
    }

    @Override
    public void destroy() throws Exception {
        //bean 销毁时进行资源释放
        release();
    }


}
