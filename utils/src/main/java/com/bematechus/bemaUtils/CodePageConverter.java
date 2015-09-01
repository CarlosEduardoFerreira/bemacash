package com.bematechus.bemaUtils;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Created by b1107005 on 5/24/2015.
 */
class CodePageConverter {
    protected HashMap<Character, Byte> map = new HashMap<Character, Byte>();


    public byte[] convert(String line) {
        ArrayList<Byte> bytes = new ArrayList<Byte>(line.length() + 10);
        int i=0;

        for ( Character ch : line.toCharArray())
        {
            if (map.containsKey(ch)) {
                bytes.add( map.get(ch));

            }
            else if ( ch == '\n') {

                bytes.add( (byte) 0x0d);
                bytes.add((byte) 0x0a);
                i++;
            }
            else
                bytes.add( (byte) 0x20);

            i++;
        }

        byte [] finalArray = new byte[i];
        i=0;
       for ( Byte b : bytes)
       {
           finalArray[i++] = b;
       }
        return finalArray;
    }
}
