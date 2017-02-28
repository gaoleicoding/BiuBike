package com.biubike.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by gaolei on 17/2/27.
 */

public class RouteRecord implements Parcelable {
    public String cycle_date;
    public String cycle_time;
    public String cycle_distance;

    public String getCycle_price() {
        return cycle_price;
    }

    public void setCycle_price(String cycle_price) {
        this.cycle_price = cycle_price;
    }

    public String getCycle_points() {
        return cycle_points;
    }

    public void setCycle_points(String cycle_points) {
        this.cycle_points = cycle_points;
    }

    public String getCycle_distance() {
        return cycle_distance;
    }

    public void setCycle_distance(String cycle_distance) {
        this.cycle_distance = cycle_distance;
    }

    public String getCycle_date() {
        return cycle_date;
    }

    public void setCycle_date(String cycle_date) {
        this.cycle_date = cycle_date;
    }

    public String getCycle_time() {
        return cycle_time;
    }

    public void setCycle_time(String cycle_time) {
        this.cycle_time = cycle_time;
    }

    public String cycle_price;
    public String cycle_points;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(cycle_date);
        parcel.writeString(cycle_time);
        parcel.writeString(cycle_distance);
        parcel.writeString(cycle_price);
        parcel.writeString(cycle_points);
    }

    public static final Parcelable.Creator<RouteRecord> CREATOR = new Creator<RouteRecord>() {
        @Override
        public RouteRecord createFromParcel(Parcel source) {

            return new RouteRecord(source);
        }

        @Override
        public RouteRecord[] newArray(int size) {
// TODO Auto-generated method stub
            return new RouteRecord[size];
        }
    };

    public RouteRecord() {
    }

    private RouteRecord(Parcel source) {
        cycle_date = source.readString();
        cycle_time = source.readString();
        cycle_distance = source.readString();
        cycle_price = source.readString();
        cycle_points = source.readString();
    }
}
