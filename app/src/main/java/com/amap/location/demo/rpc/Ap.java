package com.amap.location.demo.rpc;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * @author maple on 2019/5/14 10:12.
 * @version v1.0
 * @see 1040441325@qq.com
 */
public class Ap implements Parcelable {
    private int id;
    private int deviceId;

    private String bssid;
    private String ssid;
    private int level;
    private double latitude;
    private double longitude;
    private String address;
    private int accuracy;
    private String locationType;

    private String province;
    private String cityCode;
    private String adCode;
    private String debug;
    private String area;

    public Ap() {
    }

    protected Ap(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(deviceId);
        dest.writeString(bssid);
        dest.writeString(ssid);
        dest.writeInt(level);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(address);
        dest.writeInt(accuracy);
        dest.writeString(locationType);
        dest.writeString(province);
        dest.writeString(cityCode);
        dest.writeString(adCode);
        dest.writeString(debug);
        dest.writeString(area);
    }

    public void readFromParcel(Parcel in) {
        id = in.readInt();
        deviceId = in.readInt();
        bssid = in.readString();
        ssid = in.readString();
        level = in.readInt();
        latitude = in.readDouble();
        longitude = in.readDouble();
        address = in.readString();
        accuracy = in.readInt();
        locationType = in.readString();
        province = in.readString();
        cityCode = in.readString();
        adCode = in.readString();
        debug = in.readString();
        area = in.readString();

    }

    @Override
    public int describeContents() {
        return 0;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
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

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getAdCode() {
        return adCode;
    }

    public void setAdCode(String adCode) {
        this.adCode = adCode;
    }

    public String getDebug() {
        return debug;
    }

    public void setDebug(String debug) {
        this.debug = debug;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }
}
