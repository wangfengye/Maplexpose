// LocManager.aidl
package com.amap.location.demo.rpc;
import com.amap.location.demo.rpc.Ap;
// Declare any non-default types here with import statements

interface LocManager {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    List<Ap> loc();
    Ap testA(in Ap ap);
    void testB(out Ap ap);
    void testC(inout Ap ap);
}
