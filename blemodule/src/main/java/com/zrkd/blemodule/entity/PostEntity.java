package com.zrkd.blemodule.entity;

/**
 * @author  用于 Eventbus 传输
 */
public class PostEntity {
    /**
     *  事件tag,方便处理，对号入座   暂定： send 发送
     */
    private String tag;
    /**
     * 事件类型，可自定义
     */
    private int whtat;
    /**
     * 事件内容
     */
    private String event;

    public PostEntity() {
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getWhtat() {
        return whtat;
    }

    public void setWhtat(int whtat) {
        this.whtat = whtat;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    @Override
    public String toString() {
        return "PostEntity{" +
                "tag='" + tag + '\'' +
                ", whtat=" + whtat +
                ", event='" + event + '\'' +
                '}';
    }

}
