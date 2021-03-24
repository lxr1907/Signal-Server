package org.whispersystems.textsecuregcm.mybatis.entity;

import java.io.Serializable;

public class BaseModel implements Serializable {

    private Integer pageSize = 10;// 每页大小
    private Integer pageNo = 1;// 当前页数
    private Long fromRec;// 当前查询的索引位置
    private String orderBy;// 排序

    public String getOrderBy() {
        return this.orderBy;
    }

    public BaseModel setOrderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public Integer getPageSize() {
        return this.pageSize;
    }

    public BaseModel setPageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }

        this.pageSize = pageSize;
        return this;
    }

    public Integer getPageNo() {
        return this.pageNo;
    }

    public BaseModel setPageNo(Integer pageNo) {
        if (pageNo == null || pageNo < 1) {
            pageNo = 1;
        }

        this.pageNo = pageNo;
        return this;
    }

    public Long getFromRec() {
        if (this.pageNo != null && this.pageSize != null) {
            this.fromRec = Long.valueOf(String.valueOf((this.pageNo - 1) * this.pageSize));
        }
        return this.fromRec;
    }

    public BaseModel setFromRec(Long fromRec) {
        this.fromRec = fromRec;
        return this;
    }

    public void setPageFieldToNull() {
        this.pageSize = null;
        this.pageNo = null;
        this.fromRec = null;
    }
}
