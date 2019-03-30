package com.alibaba.otter.shared.common.page;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: yfzhangsheng
 */
public interface PageList<T> extends WrapperList<T>,Serializable {

    boolean isMiddlePage();

    boolean isLastPage();

    boolean isNextPageAvailable();

    boolean isPreviousPageAvailable();

    int getPageSize();

    void setPageSize(int pageSize);

    int getPageNo();

    void setPageNo(int index);

    int getTotal();

    void setTotal(int total);

    int getTotalPage();

    int getStartRow();

    int getEndRow();

    int getNextPage();

    int getPreviousPage();

    boolean isFirstPage();

    boolean isEmpty();
}
