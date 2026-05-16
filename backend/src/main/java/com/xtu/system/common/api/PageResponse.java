package com.xtu.system.common.api;

import java.util.Collections;
import java.util.List;

public class PageResponse<T> {

    private List<T> list;
    private long pageNum;
    private long pageSize;
    private long total;

    public PageResponse() {
        this.list = Collections.emptyList();
    }

    public PageResponse(List<T> list, long pageNum, long pageSize, long total) {
        this.list = list;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
    }

    public static <T> PageResponse<T> of(List<T> list, long pageNum, long pageSize, long total) {
        return new PageResponse<>(list, pageNum, pageSize, total);
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public long getPageNum() {
        return pageNum;
    }

    public void setPageNum(long pageNum) {
        this.pageNum = pageNum;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
