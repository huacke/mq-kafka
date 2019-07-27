package com.mq.common.entity;

import com.mq.common.entity.page.EntryPage;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页对象
 */
@SuppressWarnings("serial")
@XmlRootElement
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
    @XmlElement(name = "row")
    @XmlElementWrapper
    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
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
