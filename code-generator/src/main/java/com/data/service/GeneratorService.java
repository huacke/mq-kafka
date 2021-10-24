
package com.data.service;

import com.data.dao.GeneratorDao;
import com.data.utils.GenUtils;
import com.data.utils.PageUtils;
import com.data.utils.Query;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成器
 */
@Service
public class GeneratorService {

	@Autowired
	private GeneratorDao generatorDao;

	public List<Map<String, Object>>  queryDatabases(Map<String,Object> query) {
		return generatorDao.queryDatabases(query);
	}
	public PageUtils queryList(Query query) {
		Page<?> page = PageHelper.startPage(query.getPage(), query.getLimit());
		List<Map<String, Object>> list = generatorDao.queryList(query);

		return new PageUtils(list, (int)page.getTotal(), query.getLimit(), query.getPage());
	}

	public Map<String, String> queryTable(Map<String,Object> query) {
		return generatorDao.queryTable(query);
	}

	public List<Map<String, String>> queryColumns(Map<String,Object> query) {
		return generatorDao.queryColumns(query);
	}

	public byte[] generatorCode(String[] tableNames,String chemaName,String basePackage,String moduleName) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ZipOutputStream zip = new ZipOutputStream(outputStream);
		Map<String,Object> query = new HashMap<>();
		for(String tableName : tableNames){
			query.put("tableName",tableName);
			query.put("schemaName",chemaName);
			//查询表信息
			Map<String, String> table = queryTable(query);
			//查询列信息
			List<Map<String, String>> columns = queryColumns(query);
			//生成代码
			GenUtils.generatorCode(table, columns,basePackage,moduleName, zip);
		}
		IOUtils.closeQuietly(zip);
		return outputStream.toByteArray();
	}
}
