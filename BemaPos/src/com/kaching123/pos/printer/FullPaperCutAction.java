package com.kaching123.pos.printer;

import com.kaching123.pos.Action;

/**
 * [Function] Perform partial paper cut.
 * [Format] ASCII ESC m
 * Hexadecimal 1B 6D
 * Decimal 27 109
 *
 * @author gdubina
 *
 */
public class FullPaperCutAction extends Action {

	private static final byte[] COMMAND_BYTES = new byte[]{ESC, _byte('m')};
	
	@Override
	public byte[] getCommand() {
		return COMMAND_BYTES;
	}

}
