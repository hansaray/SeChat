package com.simpla.sechat.Objects;

import java.io.Serializable;

public class LocationObject implements Serializable {
    private String locationName;
    private String latitude;
    private String longLatitude;
    private String uid;

    public LocationObject() {
    }

    public LocationObject(String locationName, String latitude, String longLatitude) {
        this.locationName = locationName;
        this.latitude = latitude;
        this.longLatitude = longLatitude;
    }

    public LocationObject(String locationName, String latitude, String longLatitude, String uid) {
        this.locationName = locationName;
        this.latitude = latitude;
        this.longLatitude = longLatitude;
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongLatitude() {
        return longLatitude;
    }

    public void setLongLatitude(String longLatitude) {
        this.longLatitude = longLatitude;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
}

