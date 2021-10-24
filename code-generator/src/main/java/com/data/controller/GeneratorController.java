package com.data.controller;

import com.data.service.GeneratorService;
import com.data.utils.PageUtils;
import com.data.utils.Query;
import com.data.utils.ResponseResult;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 代码生成器
 */
@Controller
@RequestMapping("/sys/generator")
public class GeneratorController {
	@Autowired
	private GeneratorService sysGeneratorService;

	/**
	 * 数据库列表
	 */
	@ResponseBody
	@RequestMapping("/databases")
	public ResponseResult databases(@RequestParam Map<String, Object> params){
		return ResponseResult.ok().put("data",sysGeneratorService.queryDatabases(params));
	}
	/**
	 * 表结构列表
	 */
	@ResponseBody
	@RequestMapping("/list")
	public ResponseResult list(@RequestParam Map<String, Object> params){
		PageUtils pageUtil = sysGeneratorService.queryList(new Query(params));
		
		return ResponseResult.ok().put("page", pageUtil);
	}

	/**
	 * 生成代码
	 */
	@RequestMapping("/code")
	public void code(String tables,String schemaName,String basePackage,String moduleName,HttpServletResponse response) throws IOException{

		byte[] data = sysGeneratorService.generatorCode(tables.split(","),schemaName,basePackage,moduleName);
		response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=\""+moduleName+".zip\"");
        response.addHeader("Content-Length", "" + data.length);  
        response.setContentType("application/octet-stream; charset=UTF-8");  
  
        IOUtils.write(data, response.getOutputStream());  
	}
}
