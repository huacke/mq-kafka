package com.mq.common.entity.page;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;


@XmlRootElement
public class EntryPage  implements Serializable{
	private static final long serialVersionUID = -3077892126485785496L;
	private Integer pageNum=1;
	private Integer pageSize=20;
	private Integer total;
    private Integer pages;
    private Integer startRow;
    private Integer endRow;

	public EntryPage() {
		super();
	}


    public EntryPage(EntryPage input) {
		super();
        this.pageNum = input.pageNum;
        this.pageSize = input.pageSize;
        this.total = input.total;
        this.startRow = input.startRow;
        this.endRow = input.endRow;
    }

	public EntryPage(Integer pageSize) {
		super();
		this.total = 0;
		this.pageNum = 1;
		this.pageSize = pageSize;
	}

    public EntryPage(Integer pageNum, Integer pageSize) {
		super();
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.startRow = pageNum > 0 ? (pageNum - 1) * pageSize : 0;
        this.endRow = pageNum * pageSize;
    }

    public EntryPage(Integer pageNum, Integer pageSize, Integer total) {
		super();
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.startRow = pageNum > 0 ? (pageNum - 1) * pageSize : 0;
        this.endRow = pageNum * pageSize;
    }

    @XmlAttribute
    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    @XmlAttribute
    public Integer getEndRow() {
        return endRow;
    }

    public void setEndRow(Integer endRow) {
        this.endRow = endRow;
    }

    @XmlAttribute
    public Integer getPageNum() {
        return pageNum;
    }

	public void setPageNum(Integer pageNum) {
		if (pageNum == null || 0 == pageNum.intValue()) {
			this.pageNum = 1;
		} else {
			this.pageNum = pageNum;
		}
	}

    @XmlAttribute
    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    @XmlAttribute
    public Integer getStartRow() {
        return startRow;
    }

    public void setStartRow(Integer startRow) {
        this.startRow = startRow;
    }

    @XmlAttribute
    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
        this.pages = (Integer) (total / this.pageSize + ((total % this.pageSize == 0) ? 0 : 1));
    }

    @Override
    public String toString() {
        return "Page{" +
                "pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", startRow=" + startRow +
                ", endRow=" + endRow +
                ", total=" + total +
                ", pages=" + pages +
                '}';
    }
}