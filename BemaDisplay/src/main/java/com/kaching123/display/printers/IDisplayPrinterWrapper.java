package com.kaching123.display.printers;

import com.kaching123.display.DisplayPrinter;

import java.io.IOException;

/**
 * Created by pkabakov on 25.02.14.
 */
public interface IDisplayPrinterWrapper {

    public void print(DisplayPrinter displayPrinter) throws IOException;
    public void clear();

}
