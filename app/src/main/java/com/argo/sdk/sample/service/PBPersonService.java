package com.argo.sdk.sample.service;

/**
 * Created by user on 10/11/15.
 */
public interface PBPersonService {

    /**
     * 读取最新, 抛出事件 PersonListResultEvent
     * @param cursorId 时间戳
     */
    void findLatest(int cursorId);

    /**
     * 读取更多，抛出事件 PersonListResultEvent
     * @param page 页码
     * @param cursorId 时间戳
     */
    void findMore(int page, int cursorId);

    /**
     * 读取详细. 抛出事件 PersonDetailResultEvent
     * @param itemId 记录主键
     */
    void findBy(int itemId);

    /**
     * 从服务器加载详细. 抛出事件 PersonDetailResultEvent
     * @param itemId
     */
    void loadBy(int itemId);

    /**
     * 新建记录. 抛出事件 PersonCreateResultEvent
     * @param item 记录
     */
    void create(Object item);

    /**
     * 保存修改记录. 抛出事件 PersonSaveResultEvent
     * @param item 记录
     */
    void save(Object item);

    /**
     * 删除记录. 抛出事件 PersonRemoveResultEvent
     * @param item 记录
     */
    void remove(Object item);
}
