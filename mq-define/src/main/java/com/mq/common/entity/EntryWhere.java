package com.mq.common.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class EntryWhere implements Serializable {
	private int sn;
	private String prefix;
	private String key1;
	private String key2;
	private String op;
	private String valType;
	private String val1;
	private String val2;
	private String suffix;
    public EntryWhere(){}

    public EntryWhere(int sn, String key2, String op, String val2) {
		super();
		this.sn = sn;
		this.key2 = key2;
		this.op = op;
		this.val2 = val2;
	}
    
    public EntryWhere(int sn, String prefix, String key1, String key2,
			String op, String valType, String val1, String val2, String suffix) {
		super();
		this.sn = sn;
		this.prefix = prefix;
		this.key1 = key1;
		this.key2 = key2;
		this.op = op;
		this.valType = valType;
		this.val1 = val1;
		this.val2 = val2;
		this.suffix = suffix;
	}
}
