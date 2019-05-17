package com.amap.location.demo.rpc;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author maple on 2019/5/14 10:12.
 * @version v1.0
 * @see 1040441325@qq.com
 */
public class Ap implements Parcelable {
    private String bssid;
    private String ssid;
    private int level;
    private double latitude;
    private double longitude;
    private String address;
    private int accuracy;
    private String locationType;

    public Ap() {
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    protected Ap(Parcel in) {
        this.bssid = in.readString();
        this.ssid = in.readString();
        this.level = in.readInt();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.address = in.readString();
        this.accuracy = in.readInt();
        this.locationType = in.readString();

    }

    public static final Creator<Ap> CREATOR = new Creator<Ap>() {
        @Override
        public Ap createFromParcel(Parcel in) {
            return new Ap(in);
        }

        @Override
        public Ap[] newArray(int size) {
            return new Ap[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.bssid);
        dest.writeString(this.ssid);
        dest.writeInt(this.level);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeString(this.address);
        dest.writeInt(this.accuracy);
        dest.writeString(this.locationType);
    }

    @Override
    public String toString() {
        return "Ap{" +
                "bssid='" + bssid + '\'' +
                ", ssid='" + ssid + '\'' +
                ", level=" + level +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", address='" + address + '\'' +
                ", accuracy=" + accuracy +
                ", locationType='" + locationType + '\'' +
                '}';
    }
}
