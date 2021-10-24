package com.mq.common.entity;

import lombok.Data;

@Data
public class EntryOrder extends BaseObject {
	private int sn;
	private String key1;
	private String key2;
	private String op;

    public EntryOrder() {}

    public EntryOrder(int sn, String key1, String key2, String op) {
		super();
		this.sn = sn;
		this.key1 = key1;
		this.key2 = key2;
		this.op = op;
	}

}
