package com.mq.kafka.partition;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;
import java.util.List;
import java.util.Map;

/**
 * @author huacke
 * @version v1.0
 * @ClassName KeyHashPartitioner
 * @Description 基于主键hash取模的方式，同一个key会分发到同一个分区
 */
public class KeyHashPartitioner implements Partitioner {
    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        List<PartitionInfo> partitionInfos = cluster.availablePartitionsForTopic(topic);
        int size = partitionInfos.size();
        int hashCode = Math.abs(HashCodeBuilder.reflectionHashCode(key));
        return hashCode%(size);
    }
    @Override
    public void close() {
    }
    @Override
    public void configure(Map<String, ?> configs) {
    }
}
