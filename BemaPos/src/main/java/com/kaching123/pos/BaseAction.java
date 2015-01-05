package com.kaching123.pos;

import java.io.IOException;

public abstract class BaseAction<T> {

    public static final byte ESC = 0x1B;
    public static final byte GS = 0x1D;
    public static final byte DLE = 0x10;
    public static final byte FS = 0x1C;

    public static final byte F8 = (byte) 0xf8;
    public static final byte F9 = (byte) 0xf9;

    public static final byte C0X37 = (byte) 0x37;
    public static final byte C0X02 = (byte) 0x02;
    public static final byte C0X1B = (byte) 0x1b;
    public static final byte C0X74 = (byte) 0x74;

    //public static final byte CR = 0x09;
    public static final byte LF = 0x0A;
    public static final byte NUL = 0x00;

    protected abstract byte[] getCommand();

    public abstract T execute(PosPrinter printer) throws IOException;

    public static byte _byte(char ch) {
        return (byte) ch;
    }
}
