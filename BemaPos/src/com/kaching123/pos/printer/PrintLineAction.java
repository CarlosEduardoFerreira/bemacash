package com.kaching123.pos.printer;

import com.kaching123.pos.CP850ConverterAction;

public class PrintLineAction extends CP850ConverterAction implements Countable {

    private byte bytes[];
    private String line;

    public PrintLineAction(String line) {
        this.line = line;
        byte[] sBytes = utf2cp850(line);
        bytes = new byte[sBytes.length + 1];
        //bytes[0] = LF;
        System.arraycopy(sBytes, 0, bytes, 0, sBytes.length);
        bytes[sBytes.length] = LF;
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
