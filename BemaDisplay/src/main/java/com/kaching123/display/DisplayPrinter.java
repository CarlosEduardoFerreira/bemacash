package com.kaching123.display;

import java.io.IOException;

public interface DisplayPrinter {

	void write(byte[] bytes) throws IOException;
	
	void close() throws IOException;

    boolean isUSBDisplayer();
}
