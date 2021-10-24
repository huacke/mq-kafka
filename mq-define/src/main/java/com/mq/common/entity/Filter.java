package com.mq.common.entity;

import com.mq.common.utils.FiledUtils;
import com.mq.common.utils.StringUtils;
import lombok.Data;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
public class Filter<T> extends BaseObject{

	public Filter() {
		super();
	}

    //查询参数对象
	protected T param;

    private List<EntryWhere> entryWhereList;

	public void addEntryWhereList(EntryWhere entry) {
		if(this.entryWhereList ==null){
			this.entryWhereList = new ArrayList<EntryWhere>();
		}
		this.entryWhereList.add(entry);
	}
	private List<EntryOrder> entryOrderList;

	public void addEntryOrderList(EntryOrder entry) {
		if(this.entryOrderList ==null){
			this.entryOrderList = new ArrayList<EntryOrder>();
		}
		this.entryOrderList.add(entry);
	}

	public Map<String,Object> getMap(){
		Map<String,Object> map = new HashMap<String,Object>();
		if(this.entryWhereList !=null && this.entryWhereList.size()>0){
			for(int k=0;k<this.entryWhereList.size();k++){
				map.put(entryWhereList.get(k).getKey2(), entryWhereList.get(k).getVal2());
			}
		}
		if(this.entryOrderList !=null && this.entryOrderList.size()>0){
			String orderByClause = "";
			for(int k=0;k<this.entryOrderList.size();k++){
				if (k != 0) {
					orderByClause = orderByClause + ",";
				}
				orderByClause = orderByClause + StringUtils.underscoreName(entryOrderList.get(k).getKey1())+ " " + entryOrderList.get(k).getKey2();
			}
			map.put("orderByClause", orderByClause);
		}
		if (param != null) {
            Map params = null;
            if (param instanceof Map) {
                params = (Map) param;
            } else {
                params = FiledUtils.transfer2Map(param);
            }
            map.putAll(params);
		}
		return map;
	}
}