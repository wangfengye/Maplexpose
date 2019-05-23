package com.maple.maplexpose.util;

/**
 * @author maple on 2019/5/23 13:47.
 * @version v1.0
 * @see 1040441325@qq.com
 * 保留固定行数的文字
 */
@SuppressWarnings({"unused", "SameParameterValue"})
public class FixLinesStr {
    private StringBuilder builder = new StringBuilder();
    private int mMaxLines = 24;
    private int mLines;

    public FixLinesStr() {
    }

    public FixLinesStr(int maxLines) {
        this.mMaxLines = maxLines;
    }

    /**
     *
     * @param data 新增文字
     * @return 当前可显示的文字
     */
    public String put(String data) {
        builder.append(data);
        mLines = mLines + getSubStr(data, "\n");
        if (mLines > mMaxLines) {
            for (int i = mLines - mMaxLines; i > 0; i--) {
                builder.delete(0, builder.indexOf("\n") + 1);
            }
            mLines = mMaxLines;
        }
        return builder.toString();
    }

    /**
     * 获取子字符串的数量
     *
     * @param str 目标字符串
     * @param chs 子字符
     * @return 数量
     */
    private int getSubStr(String str, String chs) {
        // 用空字符串替换所有要查找的字符串
        String destStr = str.replaceAll(chs, "");
        // 查找字符出现的个数 = （原字符串长度 - 替换后的字符串长度）/要查找的字符串长度
        return (str.length() - destStr.length()) / chs.length();
    }

    public void clear() {
        if (builder!=null)builder.setLength(0);
    }
}
