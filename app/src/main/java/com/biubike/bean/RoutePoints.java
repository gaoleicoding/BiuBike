package com.biubike.bean;

import java.util.ArrayList;

/**
 * Created by gaolei on 17/3/26.
 */

public class RoutePoints {
    public ArrayList<RoutePoint> routeList;
    public String time, distance, price;

    public RoutePoints(ArrayList<RoutePoint> routeList, String time, String distance, String price) {
        this.routeList = routeList;
        this.time = time;
        this.distance = distance;
        this.price = price;
    }

    public ArrayList<RoutePoint> getRouteList() {
        return routeList;
    }

    public void setRouteList(ArrayList<RoutePoint> routeList) {
        this.routeList = routeList;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
