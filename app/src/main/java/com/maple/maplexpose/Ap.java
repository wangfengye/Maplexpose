package com.maple.maplexpose;

/**
 * @author maple on 2019/5/9 14:20.
 * @version v1.0
 * @see 1040441325@qq.com
 * 伪造的wifi列表
 */
public class Ap {
    private String bssid;
    private String ssid;
    private int level;

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

    @Override
    public String toString() {
        return "Ap{" +
                "bssid='" + bssid + '\'' +
                ", ssid='" + ssid + '\'' +
                ", level=" + level +
                '}';
    }
}
