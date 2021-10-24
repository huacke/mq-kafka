package com.data.utils;

import com.data.entity.ColumnEntity;
import com.data.entity.TableEntity;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.WordUtils;
import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.ClasspathResourceLoader;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成器   工具类
 */
public class GenUtils {

    public static List<String> getTemplates(){

        List<String> templates = new ArrayList<String>();
        templates.add("/POJO.java.btl");
        templates.add("/Entity.java.btl");
        templates.add("/Example.java.btl");
        templates.add("/Mapper.xml.btl");
        templates.add("/Mapper.java.btl");
        templates.add("/IRepository.java.btl");
        templates.add("/RepositoryImpl.java.btl");
        templates.add("/Dao.java.btl");
        templates.add("/Dao.xml.btl");
        templates.add("/Service.java.btl");
        templates.add("/ServiceImpl.java.btl");
        templates.add("/Controller.java.btl");

        return templates;
    }

    public  static TableEntity getTableModel (Map<String, String> table, List<Map<String, String>> columns){
        //配置信息
        boolean hasBigDecimal = false;
        //表信息
        TableEntity tableEntity = new TableEntity();
        tableEntity.setTableName(table.get("tableName"));
        tableEntity.setComments(table.get("tableComment"));
        //表名转换成Java类名
        String className = tableToJava(tableEntity.getTableName(), "t_");
        tableEntity.setClassName(className);
        tableEntity.setObjectName(StringUtils.uncapitalize(className));
        //列信息
        List<ColumnEntity> columsList = new ArrayList<>();
        for(Map<String, String> column : columns){
            ColumnEntity columnEntity = new ColumnEntity();
            String columnName = column.get("columnName");
            columnEntity.setColumnName(columnName);
            String dataType = column.get("dataType");
            columnEntity.setDataType(dataType);
            // 设置字段对应的对象属性名
            String fieldName = StringUtils.lineToHump(columnName);
            columnEntity.setFieldName(fieldName);
            // 设置JAVA数据类型
            String javaType = DataTypeUtils.getJavaType(columnEntity.getDataType());
            columnEntity.setJavaType(javaType);
            columnEntity.setJavaTypeFull(DataTypeUtils.getJavaTypeClassz(javaType).getName());
            // 设置JDBC数据类型
            String jdbcType = DataTypeUtils.getJdbcType(columnEntity.getDataType());
            columnEntity.setJdbcType(jdbcType);

            // 设置属性设置和获取方法
            String setter = "set" + StringUtils.capitalize(fieldName);
            columnEntity.setSetter(setter);
            String getter = "get" + StringUtils.capitalize(fieldName);
            columnEntity.setGetter(getter);

            columnEntity.setComments(column.get("columnComment"));
            columnEntity.setExtra(column.get("extra"));
            columnEntity.setNullable("YES".equalsIgnoreCase(column.get("nullable")));
            //列名转换成Java属性名
            String attrName = columnToJava(columnEntity.getColumnName());
            columnEntity.setFieldMethodName(attrName);

            if (!hasBigDecimal && javaType.equals("BigDecimal" )) {
                hasBigDecimal = true;
            }
            //是否主键
            if ("PRI".equalsIgnoreCase(column.get("columnKey")) && tableEntity.getPk() == null) {
                tableEntity.setPk(columnEntity);
                columnEntity.setPrimaryKey(true);
            }
            columsList.add(columnEntity);
        }
        tableEntity.setHasBigDecimal(hasBigDecimal);
        tableEntity.setColumns(columsList);

        //没主键，则第一个字段为主键
        if (tableEntity.getPk() == null) {
            tableEntity.setPk(tableEntity.getColumns().get(0));
        }
        return  tableEntity;
    }

    public static  Map<String, Object> getContext(TableEntity tableEntity,String basePackage,String moduleName){
        //封装模板数据
        Map<String, Object> map = new HashMap<>();
        String className = tableEntity.getClassName();
        map.put("tableName", tableEntity.getTableName());
        map.put("tableComment", tableEntity.getComments());
        map.put("pk", tableEntity.getPk());
        map.put("className", className);
        String entityObjectName = "t"+StringUtils.capitalize(tableEntity.getObjectName());
        map.put("entityObjectName", entityObjectName);
        map.put("objectName", tableEntity.getObjectName());
        map.put("pathName", tableEntity.getObjectName().toLowerCase());
        map.put("columns", tableEntity.getColumns());
        map.put("hasBigDecimal", tableEntity.isHasBigDecimal());
        map.put("mainPath", basePackage);
        map.put("package", basePackage);
        map.put("moduleName", moduleName);
        map.put("bizCode", StringUtils.upperCase(moduleName));

        String basePackagePath = basePackage+".";
        String pojoClassName = tableEntity.getClassName();
        String pojoClassObjectName = StringUtils.uncapitalize(pojoClassName);
        String entityClassName = "T"+tableEntity.getClassName();
        String entityClassObjectName = StringUtils.uncapitalize(entityClassName);
        String exampleClassName = className+"Example";
        String mapperClassName = className+"Mapper";
        String daoClassName = className+"Dao";
        String daoClassObjectName = StringUtils.uncapitalize(daoClassName);
        String iRepositoryClassName = "I"+className+"Repository";
        String iRepositoryObjectName = StringUtils.uncapitalize(iRepositoryClassName);
        String repositoryImplClassName = className+"RepositoryImpl";
        String serviceClassName = className+"Service";
        String serviceObjectClassName = StringUtils.uncapitalize(serviceClassName);
        String serviceImplClassName = className+"ServiceImpl";
        String controllerClassName = className+"Controller";

        map.put("pojoClassName", pojoClassName);
        map.put("pojoClassObjectName", pojoClassObjectName);
        map.put("entityClassName", entityClassName);
        map.put("entityClassObjectName", entityClassObjectName);
        map.put("exampleClassName",exampleClassName);
        map.put("mapperClassName",mapperClassName);
        map.put("daoClassName",daoClassName);
        map.put("daoClassObjectName",daoClassObjectName);
        map.put("serviceClassName",serviceClassName);
        map.put("serviceObjectClassName",serviceObjectClassName);
        map.put("serviceImplClassName",serviceImplClassName);
        map.put("iRepositoryClassName",iRepositoryClassName);
        map.put("iRepositoryObjectName",iRepositoryObjectName);
        map.put("repositoryImplClassName",repositoryImplClassName);
        map.put("controllerClassName",controllerClassName);

        map.put("pojoPackageName",basePackagePath+moduleName+".biz.bean");
        map.put("entityPackageName",basePackagePath+moduleName+".dao.mapper.gen.entity");
        map.put("examplePackageName",basePackagePath+moduleName+".dao.mapper.gen.entity");
        map.put("mapperPackageName",basePackagePath+moduleName+".dao.mapper.gen");
        map.put("daoPackageName",basePackagePath+moduleName+".dao.mapper.ext");
        map.put("iRepositoryPackageName",basePackagePath+moduleName+".biz.repo");
        map.put("repositoryImplPackageName",basePackagePath+moduleName+".biz.repo.impl");
        map.put("servicePackageName",basePackagePath+moduleName+".biz.api");
        map.put("serviceImplPackageName",basePackagePath+moduleName+".biz.impl");
        map.put("controllerPackageName",basePackagePath+moduleName+".srs.impl");

        map.put("author", "generator");
        map.put("email", "generator@163.com");
        map.put("datetime", DateUtils.format(new Date(), DateUtils.DATE_TIME_PATTERN));
        return map;
    }
    /**
     * 生成代码
     */
    public static void generatorCode(Map<String, String> table,
                                     List<Map<String, String>> columns,String basePackage,String moduleName, ZipOutputStream zip){
        ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader("templates");
        try {
            Configuration configuration = Configuration.defaultConfiguration();
            GroupTemplate groupTemplate = new GroupTemplate(resourceLoader, configuration);
            TableEntity tableEntity = getTableModel(table,columns);
            Map<String, Object> map = getContext(tableEntity,basePackage,moduleName);
            //获取模板列表
            List<String> templates = getTemplates();
            for (String template : templates) {
                //渲染模板
                try {
                    Template tpl = groupTemplate.getTemplate(template);
                    String fileName = getFileName(template,map);
                    if(fileName!=null){
                        tpl.binding(map);
                        StringWriter sw = new StringWriter();
                        tpl.renderTo(sw);
                        //添加到zip
                        zip.putNextEntry(new ZipEntry(fileName));
                        IOUtils.write(sw.toString(), zip, "UTF-8" );
                        IOUtils.closeQuietly(sw);
                        zip.closeEntry();
                    }
                } catch (IOException e) {
                    throw new GenException("渲染模板失败，表名：" + tableEntity.getTableName(),e);
                }
            }
        }catch (Exception e){
            throw new GenException("生成代码异常",e);
        }
    }


    /**
     * 列名转换成Java属性名
     */
    public static String columnToJava(String columnName) {
        return WordUtils.capitalizeFully(columnName, new char[]{'_'}).replace("_", "" );
    }

    /**
     * 表名转换成Java类名
     */
    public static String tableToJava(String tableName, String tablePrefix) {
        if (!StringUtils.isEmpty(tablePrefix)) {
            tableName = tableName.replaceFirst(tablePrefix, "" );
        }
        return columnToJava(tableName);
    }

    /**
     * 获取文件名
     */
    public static String getFileName(String template, Map<String, Object> context) {

        String packagePath =  "src" + File.separator +"main" + File.separator + "java" + File.separator;
        String resourcesPath =  "src" + File.separator +"main" + File.separator + "resources" + File.separator;
        String  pojoClassName = context.get("pojoClassName").toString();
        String  entityClassName = context.get("entityClassName").toString();
        String  exampleClassName = context.get("exampleClassName").toString();
        String  mapperClassName = context.get("mapperClassName").toString();
        String  daoClassName = context.get("daoClassName").toString();
        String  iRepositoryClassName = context.get("iRepositoryClassName").toString();
        String  repositoryImplClassName = context.get("repositoryImplClassName").toString();
        String  serviceClassName = context.get("serviceClassName").toString();
        String  serviceImplClassName = context.get("serviceImplClassName").toString();
        String  controllerClassName = context.get("controllerClassName").toString();


        String  pojoPackageName = StringUtils.transPackageNameToPath(context.get("pojoPackageName").toString());
        String  entityPackageName = StringUtils.transPackageNameToPath(context.get("entityPackageName").toString());
        String  examplePackageName = StringUtils.transPackageNameToPath(context.get("examplePackageName").toString());
        String  mapperPackageName = StringUtils.transPackageNameToPath(context.get("mapperPackageName").toString());
        String  daoPackageName = StringUtils.transPackageNameToPath(context.get("daoPackageName").toString());
        String iRepositoryPackageName = StringUtils.transPackageNameToPath(context.get("iRepositoryPackageName").toString());
        String repositoryImplPackageName = StringUtils.transPackageNameToPath(context.get("repositoryImplPackageName").toString());
        String  servicePackageName = StringUtils.transPackageNameToPath(context.get("servicePackageName").toString());
        String  serviceImplPackageName = StringUtils.transPackageNameToPath(context.get("serviceImplPackageName").toString());
        String  controllerPackageName = StringUtils.transPackageNameToPath(context.get("controllerPackageName").toString());


        if (template.contains("POJO.java.btl" )) {
            return packagePath +File.separator+pojoPackageName+File.separator+pojoClassName+".java";
        }
        if (template.contains("Entity.java.btl" )) {
            return packagePath +File.separator+entityPackageName+File.separator+entityClassName+".java";
        }
        if (template.contains("Example.java.btl" )) {
            return packagePath +File.separator+examplePackageName+File.separator+exampleClassName+".java";
        }
        if (template.contains("Mapper.java.btl" )) {
            return packagePath +File.separator+mapperPackageName+File.separator+mapperClassName+".java";
        }
        if (template.contains("Dao.java.btl" )) {
            return packagePath +File.separator+daoPackageName+File.separator+daoClassName+".java";
        }
        if (template.contains("IRepository.java.btl" )) {
            return packagePath +File.separator+iRepositoryPackageName+File.separator+iRepositoryClassName+".java";
        }
        if (template.contains("RepositoryImpl.java.btl" )) {
            return packagePath +File.separator+repositoryImplPackageName+File.separator+repositoryImplClassName+".java";
        }
        if (template.contains("Service.java.btl" )) {
            return packagePath +File.separator+servicePackageName+File.separator+serviceClassName+".java";
        }
        if (template.contains("ServiceImpl.java.btl" )) {
            return packagePath +File.separator+serviceImplPackageName+File.separator+serviceImplClassName+".java";
        }
        if (template.contains("Controller.java.btl" )) {
            return packagePath +File.separator+controllerPackageName+File.separator+controllerClassName+".java";
        }
        if (template.contains("Dao.xml.btl" )) {
            return resourcesPath +File.separator+daoPackageName+File.separator+daoClassName+".xml";
        }
        if (template.contains("Mapper.xml.btl" )) {
            return resourcesPath +File.separator+mapperPackageName+File.separator+mapperClassName+".xml";
        }
        return null;
    }
}
