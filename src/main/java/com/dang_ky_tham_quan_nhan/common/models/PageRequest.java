package com.dang_ky_tham_quan_nhan.common.models;

import java.io.Serializable;

public class PageRequest implements Serializable {
    private int page = 1;
    private int size = 10;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
