package com.kaching123.pos.printer;

import com.kaching123.pos.Action;

public class InitPrintAction extends Action {

	private static final byte[] COMMAND_BYTES = new byte[]{ESC, _byte('@')};
	
	@Override
	protected byte[] getCommand() {
		return COMMAND_BYTES;
	}

}
