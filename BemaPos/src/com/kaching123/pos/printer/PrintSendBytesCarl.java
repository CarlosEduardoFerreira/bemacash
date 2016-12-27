package com.kaching123.pos.printer;

import com.kaching123.pos.Action;
import com.kaching123.pos.printer.Countable;

public class PrintSendBytesCarl extends Action implements Countable {

    private byte bytes[];
    private String line;

    public PrintSendBytesCarl(byte[] bts) {
        bytes = bts;
    }

    @Override
    public byte[] getCommand() {
        return bytes;
    }

    @Override
    public String toString() {
        return line;
    }

    @Override
    public int getLineCount() {
        return 1;
    }
}
