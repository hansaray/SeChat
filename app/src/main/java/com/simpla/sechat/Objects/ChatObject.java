package com.simpla.sechat.Objects;

public class ChatObject {
    private Boolean seen;
    private String last_message;
    private long time;
    private String user_id;
    private String type;
    private int control;
    private String name;

    public ChatObject() {
    }

    public ChatObject(Boolean seen, String last_message, long time, String user_id, int control) {
        this.seen = seen;
        this.last_message = last_message;
        this.time = time;
        this.user_id = user_id;
        this.type = type;
        this.control = control;
    }

    public ChatObject(Boolean seen, String last_message, long time) {
        this.seen = seen;
        this.last_message = last_message;
        this.time = time;
    }

    public ChatObject(Boolean seen, String last_message, long time, String user_id) {
        this.seen = seen;
        this.last_message = last_message;
        this.time = time;
        this.user_id = user_id;
    }

    public ChatObject(Boolean seen, String last_message, long time, String user_id, String type) {
        this.seen = seen;
        this.last_message = last_message;
        this.time = time;
        this.user_id = user_id;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getControl() {
        return control;
    }

    public void setControl(int control) {
        this.control = control;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public String getLast_message() {
        return last_message;
    }

    public void setLast_message(String last_message) {
        this.last_message = last_message;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
