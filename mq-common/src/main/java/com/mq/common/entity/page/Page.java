package com.mq.common.entity.page;
/*
	The MIT License (MIT)

	Copyright (c) 2014 abel533@gmail.com

	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included in
	all copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
	THE SOFTWARE.
*/

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Mybatis 
 *
 * @author liuzh/abel533/isea533
 * @version 3.2.2
 * @url http://git.oschina.net/free/Mybatis_PageHelper
 */
@SuppressWarnings("serial")
@XmlRootElement
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
			this.pageNum = DEFAULT_CURRNET_PAGE;
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

	/**
	 * 
	 * @return 鍋忕Щ閲�
	 */
	public Integer getOffset() {
		return (pageNum - 1) * pageSize;
	}

	/**
	 * 
	 * @return 鎬婚〉鏁�
	 */
	public Integer getTotalPageNum() {
		return total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
	}

    @XmlAttribute
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