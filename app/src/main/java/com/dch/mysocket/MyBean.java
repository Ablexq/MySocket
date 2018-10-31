package com.dch.mysocket;

public class MyBean {

    private String msg;
    private int type;

    public MyBean(String msg, int type) {
        this.msg = msg;
        this.type = type;
    }

    @Override
    public String toString() {
        return "MyBean{" +
                "msg='" + msg + '\'' +
                ", type=" + type +
                '}';
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
