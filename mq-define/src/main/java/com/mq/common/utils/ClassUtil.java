package com.mq.common.utils;


import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

import java.io.IOException;
import java.util.*;


/**
 * class工具类
 */
@Slf4j
public class ClassUtil {


    //扫描  scanPackages 下的文件匹配符
    public static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";


    public static List<Class<?>> getAllClassByInterface(Class c) {

        List<Class<?>> returnClassList = null;
        if (c.isInterface()) {
            String packageName = c.getPackage().getName();
            List<Class<?>> packageClass = findPackageClass(packageName);
            if (packageClass != null) {
                returnClassList = new ArrayList();
                Iterator iterator = packageClass.iterator();

                while(iterator.hasNext()) {
                    Class classes = (Class)iterator.next();
                    if (c.isAssignableFrom(classes) && !c.equals(classes)) {
                        returnClassList.add(classes);
                    }
                }
            }
        }

        return returnClassList;
    }


    /**
     * 找到scanPackages下全部类信息
     * @param scanPackages 扫包路径,多个路径用","分割，支持ant风格
     */
    public static List<Class<?>> findPackageClass(String scanPackages)
    {
        if (StringUtils.isEmpty(scanPackages))
        {
            return new ArrayList<>();
        }
        // 排重包路径，避免父子路径重复扫描
        Set<String> packages = checkPackages(scanPackages);
        //获取Spring资源解析器
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        //创建Spring中用来读取resource为class的工具类
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

        Set<Class> fullClazzSet = Sets.newHashSet();

        for (String basePackage : packages)
        {
            if (StringUtils.isEmpty(basePackage))
            {
                continue;
            }
            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                    + ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage))
                    + "/" + DEFAULT_RESOURCE_PATTERN;
            try
            {
                //获取packageSearchPath下的Resource，这里得到的Resource是Class信息
                Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
                for (Resource resource : resources)
                {
                    //检查resource，这里的resource都是class
                    Class classz = loadClass(metadataReaderFactory, resource);
                    fullClazzSet.add(classz);
                }
            }
            catch (Exception e)
            {
                log.error("获取包下面的类信息失败,package:" + basePackage, e);
            }
        }
        return Arrays.asList(fullClazzSet.toArray(new Class<?>[fullClazzSet.size()] ));
    }

    /**
     * 排重、检测package父子关系，避免多次扫描
     * @param scanPackages 扫包路径
     * @return 返回全部有效的包路径信息
     */
    private static Set<String> checkPackages(String scanPackages)
    {
        if (StringUtils.isEmpty(scanPackages))
        {
            return Sets.newHashSet();
        }
        Set<String> packages = Sets.newHashSet();
        //排重路径
        Collections.addAll(packages, scanPackages.split(","));
        for (String packageStr : packages.toArray(new String[packages.size()]))
        {
            if (StringUtils.isEmpty(packageStr) || packageStr.equals(".") || packageStr.startsWith("."))
            {
                continue;
            }
            if (packageStr.endsWith("."))
            {
                packageStr = packageStr.substring(0, packageStr.length() - 1);
            }
            Iterator<String> packageIte = packages.iterator();
            boolean needAdd = true;
            while (packageIte.hasNext())
            {
                String pack = packageIte.next();
                if (packageStr.startsWith(pack + "."))
                {
                    //如果待加入的路径是已经加入的pack的子集，不加入
                    needAdd = false;
                }
                else if (pack.startsWith(packageStr + "."))
                {
                    //如果待加入的路径是已经加入的pack的父集，删除已加入的pack
                    packageIte.remove();
                }
            }
            if (needAdd)
            {
                packages.add(packageStr);
            }
        }
        return packages;
    }

    /**
     * 加载资源，根据resource获取class
     * @param metadataReaderFactory spring中用来读取resource为class的工具
     * @param resource 这里的资源就是一个Class
     */
    private static Class loadClass(MetadataReaderFactory metadataReaderFactory, Resource resource)
            throws IOException
    {
        try
        {
            if (resource.isReadable())
            {
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                if (metadataReader != null)
                {
                    String className = metadataReader.getClassMetadata().getClassName();
                    return  Class.forName(className);
                }
            }
        }
        catch (Exception e)
        {
            log.error("根据Spring resource获取类名称失败", e);
        }
        return null;
    }

}
