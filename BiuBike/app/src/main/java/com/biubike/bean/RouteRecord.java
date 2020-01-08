package com.biubike.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by gaolei on 17/2/27.
 */

public class RouteRecord implements Parcelable {
    private String cycleDate;
    private String cycleTime;
    private String cycleDistance;
    private long cycleStartTime;
    private long cycleEndTime;
    private String cyclePrice;
    private String cyclePoints;


    public long getCycleStartTime() {
        return cycleStartTime;
    }

    public void setCycleStartTime(long cycleStartTime) {
        this.cycleStartTime = cycleStartTime;
    }

    public long getCycleEndTime() {
        return cycleEndTime;
    }

    public void setCycleEndTime(long cycleEndTime) {
        this.cycleEndTime = cycleEndTime;
    }

    public String getCyclePrice() {
        return cyclePrice;
    }

    public void setCyclePrice(String cyclePrice) {
        this.cyclePrice = cyclePrice;
    }

    public String getCyclePoints() {
        return cyclePoints;
    }

    public void setCyclePoints(String cyclePoints) {
        this.cyclePoints = cyclePoints;
    }

    public String getCycleDistance() {
        return cycleDistance;
    }

    public void setCycleDistance(String cycleDistance) {
        this.cycleDistance = cycleDistance;
    }

    public String getCycleDate() {
        return cycleDate;
    }

    public void setCycleDate(String cycleDate) {
        this.cycleDate = cycleDate;
    }

    public String getCycleTime() {
        return cycleTime;
    }

    public void setCycleTime(String cycleTime) {
        this.cycleTime = cycleTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(cycleDate);
        parcel.writeString(cycleTime);
        parcel.writeString(cycleDistance);
        parcel.writeString(cyclePrice);
        parcel.writeLong(cycleStartTime);
        parcel.writeLong(cycleEndTime);
        parcel.writeString(cyclePoints);
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
        cycleDate = source.readString();
        cycleTime = source.readString();
        cycleDistance = source.readString();
        cyclePrice = source.readString();
        cycleStartTime = source.readLong();
        cycleEndTime = source.readLong();
        cyclePoints = source.readString();
    }
}
