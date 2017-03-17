package com.kaching123.pos.printer;

import com.kaching123.pos.Action;

/**
 * [Function] Perform partial paper cut.
 * [Format] ASCII ESC i
 * Hexadecimal 1B 69
 * Decimal 27 105
 *
 * [Function] Perform partial paper cut.
 * Hexadecimal 1D 56 01 49
 *
 * @author gdubina
 *
 */
public class FullPaperCutAction2 extends Action {

	private static final byte[] COMMAND_BYTES = new byte[]{GS, 0x56, N1, 0x49};
	
	@Override
	public byte[] getCommand() {
		return COMMAND_BYTES;
	}

}
