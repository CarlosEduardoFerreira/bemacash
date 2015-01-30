package com.kaching123.pos.util;

/**
 * Created by gdubina on 16.12.13.
 */
public final class ByteUtil {

    private ByteUtil(){};

    public static int _bit(byte status, int pos){
        return status >> pos & 1;
    }
}
