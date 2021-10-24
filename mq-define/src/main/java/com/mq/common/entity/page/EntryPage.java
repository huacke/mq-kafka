package com.mq.common.entity.page;

import com.mq.common.entity.Filter;
import lombok.Data;

@Data
public class EntryPage<T> extends Filter<T> {

    private Integer pageNum=1;
    private Integer pageSize=20;
    private Integer total=0;
    private Integer pages=0;
    private Integer startRow=0;
    private Integer endRow=0;

    public EntryPage() {
        super();
    }


    public EntryPage(EntryPage input) {
        super();
        this.param = (T)input.getParam();
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

    public void setPageNum(Integer pageNum) {
        if (pageNum == null || 0 == pageNum.intValue()) {
            this.pageNum = 1;
        } else {
            this.pageNum = pageNum;
        }
    }

    public void setTotal(Integer total) {
        this.total = total;
        this.pages = (Integer) (total / this.pageSize + ((total % this.pageSize == 0) ? 0 : 1));
    }
}