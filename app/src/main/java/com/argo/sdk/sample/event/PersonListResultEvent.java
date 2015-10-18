package com.argo.sdk.sample.event;

import com.argo.sdk.event.AppBaseEvent;

import java.util.List;

/**
 * Created by user on 10/11/15.
 */
public class PersonListResultEvent extends AppBaseEvent {

    private List<Object> list;
    private int page;

    public PersonListResultEvent(List<Object> list, int page) {
        this.list = list;
        this.page = page;
    }

    public List<Object> getList() {
        return list;
    }

    public void setList(List<Object> list) {
        this.list = list;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
