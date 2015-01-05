package com.kaching123.display.actions;

import com.kaching123.display.Action;

import java.nio.charset.Charset;

public class PrintTextAction extends Action {

    private static Charset ASCII_CHARSET = Charset.forName("US-ASCII");

    private byte bytes[];
    private String text;

    public PrintTextAction(String text) {
        this.text = text;
//        bytes = text.getBytes(ASCII_CHARSET);
        bytes = utf2cp850(text);
    }

    @Override
    public byte[] getCommand() {
        return bytes;
    }

    @Override
    public String toString() {
        return text;
    }
}
