package com.kaching123.pos.util;

import com.kaching123.pos.PosPrinter;

import java.io.IOException;

/**
 * Created by vkompaniets on 14.01.14.
 */
public interface IPrinter {
    public void print(PosPrinter posPrinter) throws IOException;
    public void emptyLine(int c);
    public void emptyLine();
    public void drawLine();
    public void drawDoubleLine();
    public void barcode(String text);
    public void logo();
}
