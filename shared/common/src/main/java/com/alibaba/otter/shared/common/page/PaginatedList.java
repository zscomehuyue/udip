package com.alibaba.otter.shared.common.page;

/**
 * Created with IntelliJ IDEA.
 * User: yfzhangsheng
 */
public class PaginatedList<T> extends WrapperArrayList<T> implements PageList<T> {

    private static final long serialVersionUID = -6761941552387789226L;

    public static final int PAGESIZE_DEFAULT = 10;

    private int pageSize;

    private int pageNo;

    private int total;

    private int totalPage;

    private int startRow;

    private int endRow;

    public PaginatedList() {
        repaginate();
    }

    //FIXME must set total
    public PaginatedList(int pageNo, int pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        repaginate();
    }

    public boolean isFirstPage() {
        return pageNo <= 1;
    }

    public boolean isMiddlePage() {
        return !(isFirstPage() || isLastPage());
    }

    public boolean isLastPage() {
        return pageNo >= totalPage;
    }

    public boolean isNextPageAvailable() {
        return !isLastPage();
    }

    public boolean isPreviousPageAvailable() {
        return !isFirstPage();
    }

    public int getNextPage() {
        if (isLastPage()) {
            return total;
        } else {
            return pageNo + 1;
        }
    }

    public int getPreviousPage() {
        if (isFirstPage()) {
            return 1;
        } else {
            return pageNo - 1;
        }
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        repaginate();
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
        repaginate();
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
        repaginate();
    }


    public int getTotalPage() {
        return totalPage;
    }


    public int getStartRow() {
        return startRow;
    }


    public int getEndRow() {
        return endRow;
    }

    private void repaginate() {
        if (pageSize < 1) {
            pageSize = PAGESIZE_DEFAULT;
        }
        if (pageNo < 1) {
            pageNo = 1;
        }
        if (total > 0) {
            totalPage = total / pageSize + (total % pageSize > 0 ? 1 : 0);
            if (pageNo > totalPage) {
                pageNo = totalPage;
            }
            endRow = pageSize;
            startRow = (pageNo - 1) * pageSize;
            if (endRow > total) {
                endRow = total;
            }
        }
    }

    public static void main(String[] s) {
        PaginatedList page = new PaginatedList();
        page.setPageNo(0);
        page.setPageSize(1000);
        System.out.println(page.getStartRow());
        page.setPageNo(1);
        page.setPageSize(1000);
        System.out.println(page.getStartRow());
//
    }
}
