package com.kaching123.pos.printer;

import com.kaching123.pos.Action;

public class PrintAndPaperFeedAction extends Action implements Countable {

	private static final byte[] COMMAND_BYTES = new byte[]{0x0A};
	
	@Override
	protected byte[] getCommand() {
		return COMMAND_BYTES;
	}

	@Override
	public int getLineCount() {
		return 1;
	}
}
