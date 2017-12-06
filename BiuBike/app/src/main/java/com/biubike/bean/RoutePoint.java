package com.biubike.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by gaolei on 17/2/4.
 */

public class RoutePoint implements Parcelable {

    public int id;
    public long time;
    public double routeLat, routeLng, speed;

    public int getId() {
        return id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getRouteLat() {
        return routeLat;
    }

    public void setRouteLat(double routeLat) {
        this.routeLat = routeLat;
    }

    public double getRouteLng() {
        return routeLng;
    }

    public void setRouteLng(double routeLng) {
        this.routeLng = routeLng;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeDouble(routeLat);
        parcel.writeDouble(routeLng);
        parcel.writeDouble(speed);
        parcel.writeLong(time);
        parcel.writeInt(id);

    }

    public static final Parcelable.Creator<RoutePoint> CREATOR = new Creator<RoutePoint>() {
        @Override
        public RoutePoint createFromParcel(Parcel source) {
            RoutePoint routePoint = new RoutePoint();
            routePoint.id = source.readInt();
            routePoint.routeLat = source.readDouble();
            routePoint.routeLng = source.readDouble();
            routePoint.speed = source.readDouble();
            routePoint.time = source.readLong();
            return routePoint;
        }

        @Override
        public RoutePoint[] newArray(int size) {
// TODO Auto-generated method stub
            return new RoutePoint[size];
        }
    };
}
