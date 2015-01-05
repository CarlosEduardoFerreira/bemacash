package com.kaching123.display;

import java.io.IOException;

public abstract class BaseAction<T> {
    public static final byte C0X02 = (byte) 0x02;
    public static final byte C0X1B = (byte) 0x1b;
    public static final byte C0X25 = (byte) 0x25;
    public static final byte C0X74 = (byte) 0x74;

    protected abstract byte[] getCommand();

    public abstract T execute(DisplayPrinter printer) throws IOException;

    public static byte _byte(char ch) {
        return (byte) ch;
    }
}
