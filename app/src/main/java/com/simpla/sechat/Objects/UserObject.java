package com.simpla.sechat.Objects;

public class UserObject {

    private String nickname;
    private String email;
    private String imageURL;
    private String code;

    public UserObject(String nickname, String email, String imageURL) {
        this.nickname = nickname;
        this.email = email;
        this.imageURL = imageURL;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
