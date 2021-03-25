package com.simpla.sechat.Objects;

public class MessageObject {

    private String message;
    private Boolean seen;
    private long time;
    private String type;
    private String user_id;

    public MessageObject() {
    }

    public MessageObject(String message, Boolean seen, long time, String type, String user_id) {
        this.message = message;
        this.seen = seen;
        this.time = time;
        this.type = type;
        this.user_id = user_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

}
