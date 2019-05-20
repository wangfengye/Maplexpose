package com.maple.maplexpose;

import com.amap.location.demo.rpc.Ap;

import java.util.ArrayList;
import java.util.List;

/**
 * @author maple on 2019/5/9 16:11.
 * @version v1.0
 * @see 1040441325@qq.com
 */
public class APList {
    private int code;
    private List<Ap> data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public List<Ap> getData() {
        return data == null ? new ArrayList<Ap>() : data;
    }

    public void setData(List<Ap> data) {
        this.data = data;
    }

    public void add(Ap ap) {
        if (data == null) data = new ArrayList<>();
        data.add(ap);
    }

    public void clear() {
        if (data != null) data.clear();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("APList\n");
        if (data == null) return builder.toString();
        for (int i = 0; i < data.size(); i++) {
            builder.append(data.get(i).toString()).append("\n");
        }
        return builder.toString();
    }
}
