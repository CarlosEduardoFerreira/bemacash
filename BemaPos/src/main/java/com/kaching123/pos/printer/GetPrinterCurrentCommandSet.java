package com.kaching123.pos.printer;

import com.kaching123.pos.ActionWithAnswer;

public class GetPrinterCurrentCommandSet extends ActionWithAnswer<String>{

	private static final byte[] COMMAND_BYTES = new byte[]{GS, F9, _byte('C'), 0x00};

	public GetPrinterCurrentCommandSet() {
		super(1);
	}
	
	@Override
	public String parseAnswer(byte[] bytes) {
		return new String(bytes);
	}

	@Override
	protected byte[] getCommand() {
		return COMMAND_BYTES;
	}

}
