package com.kaching123.display;

import java.io.IOException;

public abstract class Action extends BaseAction<Void> {

    @Override
    public Void execute(DisplayPrinter printer) throws IOException {
        printer.write(getCommand());
        return null;
    }

    public byte[] utf2cp850(String line, boolean isSerialPortDisplay) {
        byte[] cp850BytesCal = new byte[line.length()];
        for (int i = 0; i < line.length(); i++) {
            String temp = line.substring(i, i + 1);
            if (CP850Converter.string2CP850Byte.containsKey(temp)) {
                cp850BytesCal[i] = CP850Converter.string2CP850Byte.get(temp);
                System.arraycopy(cp850BytesCal, 0, cp850BytesCal, 0, i);
            }
        }
        byte[] finalBytes = new byte[line.length() + 3];
        // C0X1B, C0X25, C0X02,  set Bematech device CP850
        if (isSerialPortDisplay) {
            finalBytes[0] = E_C0X1B;
            finalBytes[1] = E_C0X74;
            finalBytes[2] = E_C0X02;
        } else {
            finalBytes[0] = LC_C0X1B;
            finalBytes[1] = LC_C0X25;
            finalBytes[2] = LC_C0X02;
        }

        System.arraycopy(cp850BytesCal, 0, finalBytes, 3, cp850BytesCal.length);
        return finalBytes;
    }
}
