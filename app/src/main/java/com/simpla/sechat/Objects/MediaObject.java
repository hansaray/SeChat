package com.simpla.sechat.Objects;

public class MediaObject {

    private String url;
    private String type;

    public MediaObject(String url, String type) {
        this.url = url;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
