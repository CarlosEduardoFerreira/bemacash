package com.kaching123.pos.printer;

import com.kaching123.pos.ActionWithAnswer;

/**
 * GS F9h ` n
 * Get printer information
 *
 * @author gdubina
 *
 */
public class GetPrinterNameAction extends ActionWithAnswer<String>{

	private static final byte[] COMMAND_BYTES = new byte[]{GS, F9, 0x27, 0x30};
	
	public GetPrinterNameAction() {
		super(10);
	}

	@Override
	public byte[] getCommand() {
		return COMMAND_BYTES;
	}

	@Override
	public String parseAnswer(byte[] bytes) {
		return new String(bytes);
	}

}
