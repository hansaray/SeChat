package com.simpla.sechat.Objects;

public class FriendsObject {

    private String uid;
    private String nickname;
    private String image;
    private String code;
    private int type;

    public FriendsObject(String uid, String nickname, String image, String code,int type) {
        this.uid = uid;
        this.nickname = nickname;
        this.image = image;
        this.code = code;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
