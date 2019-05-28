package com.maple.maplexpose;

import com.alibaba.fastjson.JSON;
import com.amap.location.demo.rpc.Ap;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        APList apList = new APList();
        apList.add(new Ap());
        System.out.print(JSON.toJSON(apList));
        assertEquals(4, 2 + 2);
    }
}