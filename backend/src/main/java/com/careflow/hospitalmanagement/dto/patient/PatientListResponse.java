package com.careflow.hospitalmanagement.dto.patient;

import java.util.Collections;
import java.util.List;

public class PatientListResponse {

    private List<PatientDto> items = Collections.emptyList();
    private int totalCount;
    private int page;
    private int pageSize;

    public PatientListResponse() {}

    public PatientListResponse(List<PatientDto> items, int totalCount, int page, int pageSize) {
        this.items = items;
        this.totalCount = totalCount;
        this.page = page;
        this.pageSize = pageSize;
    }

    public List<PatientDto> getItems() {
        return items;
    }

    public void setItems(List<PatientDto> items) {
        this.items = items;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalPages() {
        return pageSize > 0 ? (int) Math.ceil((double) totalCount / pageSize) : 0;
    }
}
