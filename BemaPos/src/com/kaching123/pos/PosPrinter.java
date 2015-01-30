package com.kaching123.pos;

import java.io.IOException;

public interface PosPrinter {

    void write(byte[] bytes) throws IOException;

    byte[] read(int len) throws IOException;

    void close() throws IOException;

    boolean supportExtendedStatus();

    byte getBasicStatus() throws IOException;
}
