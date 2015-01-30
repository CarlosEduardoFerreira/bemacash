package com.kaching123.pos.printer;

import com.kaching123.pos.Action;

/**
 * [Function] Perform partial paper cut.
 * [Format] ASCII ESC i
 * Hexadecimal 1B 69
 * Decimal 27 105
 *
 * @author gdubina
 *
 */
public class FullPaperCutAction2 extends Action {

	private static final byte[] COMMAND_BYTES = new byte[]{ESC, _byte('i')};
	
	@Override
	public byte[] getCommand() {
		return COMMAND_BYTES;
	}

}
