package com.data.entity;

/**
 * 列的属性
 */
public class ColumnEntity {
	/**列名*/
	private String columnName;
	/** 数据类型 */
	private String dataType;
	/** 列名类型 */
	private String jdbcType;
	/**列名备注*/
	private String comments;

	/**字段名称（java属性）*/
	private String  fieldName;
	/**属性类型*/
	private String javaType;
	/**属性类型 类称*/
	private String javaTypeFull;
	/**字段名称（java方法名）*/
	private String  fieldMethodName;
	/** getter名称 */
	private String getter;
	/** setter名称 */
	private String setter;

	/**auto_increment*/
	private String extra;
	/**是否可以为空*/
	private boolean nullable;
	/**是否为主键*/
	private boolean isPrimaryKey;

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getJdbcType() {
		return jdbcType;
	}

	public void setJdbcType(String jdbcType) {
		this.jdbcType = jdbcType;
	}

	public String getJavaType() {
		return javaType;
	}

	public void setJavaType(String javaType) {
		this.javaType = javaType;
	}

	public String getJavaTypeFull() {
		return javaTypeFull;
	}

	public void setJavaTypeFull(String javaTypeFull) {
		this.javaTypeFull = javaTypeFull;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

	public boolean isNullable() {
		return nullable;
	}

	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

	public boolean isPrimaryKey() {
		return isPrimaryKey;
	}

	public void setPrimaryKey(boolean primaryKey) {
		isPrimaryKey = primaryKey;
	}

	public String getFieldMethodName() {
		return fieldMethodName;
	}

	public void setFieldMethodName(String fieldMethodName) {
		this.fieldMethodName = fieldMethodName;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getGetter() {
		return getter;
	}

	public void setGetter(String getter) {
		this.getter = getter;
	}

	public String getSetter() {
		return setter;
	}

	public void setSetter(String setter) {
		this.setter = setter;
	}
}
