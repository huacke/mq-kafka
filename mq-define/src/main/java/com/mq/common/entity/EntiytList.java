package com.mq.common.entity;

import com.mq.common.entity.page.EntryPage;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页对象
 */
@Data
public class EntiytList<T> extends EntryPage {
    private List<T> rows;
    public EntiytList() {
        super();
    }
    public EntiytList(List<T> rows) {
        super();
        this.rows = new ArrayList<T>();
        this.rows.addAll(rows);
    }
    public void addRows(T row) {
        if(this.rows ==null){
            this.rows = new ArrayList<T>();
        }
        this.rows.add(row);
    }
}
