package com.biubike.util;

import com.baidu.mapapi.model.LatLng;

/**
 * Created by gaolei on 17/3/
 */

public class LocationManager {

    LatLng currentLL;
    String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LatLng getCurrentLL() {
        return currentLL;
    }

    public void setCurrentLL(LatLng currentLL) {
        this.currentLL = currentLL;
    }

    public static LocationManager getInstance() {
        return SingletonFactory.singletonInstance;
    }

    private static class SingletonFactory {
        private static LocationManager singletonInstance = new LocationManager();
    }
}
