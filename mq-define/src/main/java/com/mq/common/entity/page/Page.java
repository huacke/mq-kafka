package com.mq.common.entity.page;


import lombok.Data;
import org.apache.ibatis.session.RowBounds;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Data
public class Page<T> extends ArrayList<T>  implements Serializable{

    private static final Integer SQL_COUNT = 0;
    private static final Integer NO_SQL_COUNT = -1;
	private static final int DEFAULT_CURRNET_PAGE = 1;
	private Integer pageNum=1;
	private Integer pageSize=20;
	private Integer total;
    private Integer pages;
    private Integer startRow;
    private Integer endRow;

	public Page() {}

    public Page(Page<T> input) {
        this.pageNum = input.pageNum;
        this.pageSize = input.pageSize;
        this.total = input.total;
        this.startRow = input.startRow;
        this.endRow = input.endRow;
    }

	public Page(Integer pageSize) {
		this.total = 0;
		this.pageNum = DEFAULT_CURRNET_PAGE;
		this.pageSize = pageSize;
	}

    public Page(Integer pageNum, Integer pageSize) {
        this(pageNum, pageSize, SQL_COUNT);
    }

    public Page(Integer pageNum, boolean count) {
        this(pageNum,  count ? Page.SQL_COUNT: Page.NO_SQL_COUNT);
    }

    public Page(Integer pageNum, Integer pageSize, boolean count) {
        this(pageNum, pageSize, count ? Page.SQL_COUNT : Page.NO_SQL_COUNT);
    }

    public Page(Integer pageNum, Integer pageSize, Integer total) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.startRow = pageNum > 0 ? (pageNum - 1) * pageSize : 0;
        this.endRow = pageNum * pageSize;
    }

    public Page(List<T> list) {    	
        if (list instanceof Page) {
        	Page<T> page = (Page<T>) list;
        	this.pageNum = page.getPageNum();
            this.pageSize = page.getPageSize();
            this.startRow = page.getStartRow();
            this.endRow = page.getEndRow();
            this.total = page.getTotal();
            this.pages = page.getPages();
        }
    }

    public Page(RowBounds rowBounds, boolean count) {
        this(rowBounds, count ? Page.SQL_COUNT : Page.NO_SQL_COUNT);
    }

    public Page(RowBounds rowBounds, Integer total) {
        this.pageSize = rowBounds.getLimit();
        this.startRow = rowBounds.getOffset();
        //RowBounds鏂瑰紡榛樿涓嶆眰count鎬绘暟锛屽鏋滄兂姹俢ount,鍙互淇敼杩欓噷涓篠QL_COUNT
        this.total = total;
        this.endRow = this.startRow + this.pageSize;
    }

	public void setPageNum(Integer pageNum) {
		if (pageNum == null || 0 == pageNum.intValue()) {
			this.pageNum = DEFAULT_CURRNET_PAGE;
		} else {
			this.pageNum = pageNum;
		}
	}

    public void setTotal(Integer total) {
        this.total = total;
        this.pages = (Integer) (total / this.pageSize + ((total % this.pageSize == 0) ? 0 : 1));
    }

	public Integer getOffset() {
		return (pageNum - 1) * pageSize;
	}

	public Integer getTotalPageNum() {
		return total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
	}

    public boolean isCount() {
        return this.total > NO_SQL_COUNT;
    }

    public void setPage(EntryPage page){
    	this.setPageNum(page.getPageNum());
    	this.setEndRow(page.getEndRow());
    	this.setPages(page.getPages());
    	this.setPageSize(page.getPageSize());
    	this.setStartRow(page.getStartRow());
    	this.setTotal(page.getTotal());
    }
    
    public void getPage(EntryPage page){
    	page.setPageNum(this.getPageNum());
    	page.setEndRow(this.getEndRow());
    	page.setPages(this.getPages());
    	page.setPageSize(this.getPageSize());
    	page.setStartRow(this.getStartRow());
    	page.setTotal(this.getTotal());
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