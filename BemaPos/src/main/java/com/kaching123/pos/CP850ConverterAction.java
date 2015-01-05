package com.kaching123.pos;

/**
 * Created by teli.yin on 10/20/2014.
 */
public abstract class CP850ConverterAction extends Action {

    public byte[] utf2cp850(String line) {
        byte[] cp850BytesCal = new byte[line.length()];
        for (int i = 0; i < line.length(); i++) {
            String temp = line.substring(i, i + 1);
            if (CP850Converter.string2CP850Byte.containsKey(temp)) {
                cp850BytesCal[i] = CP850Converter.string2CP850Byte.get(temp);
                System.arraycopy(cp850BytesCal, 0, cp850BytesCal, 0, i);
            }
        }
        byte[] finalBytes = new byte[line.length() + 3];
        // 0x1b, 0x74, 0x02,  set Printer CP850
        finalBytes[0] = C0X1B;
        finalBytes[1] = C0X74;
        finalBytes[2] = C0X02;
        System.arraycopy(cp850BytesCal, 0, finalBytes, 3, cp850BytesCal.length);
        return finalBytes;
    }

}
