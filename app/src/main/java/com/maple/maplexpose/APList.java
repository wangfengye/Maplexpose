package com.maple.maplexpose;

import java.util.ArrayList;
import java.util.List;

/**
 * @author maple on 2019/5/9 16:11.
 * @version v1.0
 * @see 1040441325@qq.com
 */
public class APList {
    private List<Ap> mAps;

    public List<Ap> getmAps() {
        return mAps == null ? new ArrayList<Ap>() : mAps;
    }

    public void setmAps(List<Ap> mAps) {
        this.mAps = mAps;
    }

    public void add(Ap ap) {
        if (mAps == null) mAps = new ArrayList<>();
        mAps.add(ap);
    }

    public void clear() {
        if (mAps != null) mAps.clear();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("APList\n");
        for (int i = 0; i < mAps.size(); i++) {
            builder.append(mAps.get(i).toString()).append("\n");
        }
        return builder.toString();
    }
}
