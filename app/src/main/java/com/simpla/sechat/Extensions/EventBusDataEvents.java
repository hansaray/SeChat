package com.simpla.sechat.Extensions;

import android.net.Uri;

public class EventBusDataEvents {

    public static class sendMediaPosition{
        private String position;

        public sendMediaPosition(String position) {
            this.position = position;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }
    }

    public static class sendUri{
        private Uri uri;
        private String path;

        public sendUri(Uri uri, String path) {
            this.uri = uri;
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public Uri getUri() {
            return uri;
        }

        public void setUri(Uri uri) {
            this.uri = uri;
        }
    }

    public static class sendFile{
        private String path;
        private boolean isItImage;

        public sendFile(String path, boolean isItImage) {
            this.path = path;
            this.isItImage = isItImage;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean isItImage() {
            return isItImage;
        }

        public void setItImage(boolean itImage) {
            isItImage = itImage;
        }
    }

    public static class sendInfo{
        private String uid;
        int activity;

        public sendInfo(String uid, int activity) {
            this.uid = uid;
            this.activity = activity;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public int getActivity() {
            return activity;
        }

        public void setActivity(int activity) {
            this.activity = activity;
        }
    }

    public static class sendMediaToMesssage{
        private String path;
        private boolean isItImage;

        public sendMediaToMesssage(String path, boolean isItImage) {
            this.path = path;
            this.isItImage = isItImage;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean isItImage() {
            return isItImage;
        }

        public void setItImage(boolean itImage) {
            isItImage = itImage;
        }
    }
}
