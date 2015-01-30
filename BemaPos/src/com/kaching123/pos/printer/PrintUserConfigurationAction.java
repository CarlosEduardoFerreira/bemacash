package com.kaching123.pos.printer;

import com.kaching123.pos.Action;

/**
 * GS F9h ) 0
 * Print user configuration
 *
 * @author gdubina
 *
 */
public class PrintUserConfigurationAction extends Action {

	private static final byte[] COMMAND_BYTES = new byte[]{GS, F9, _byte(')'), _byte('0')};
	
	@Override
	public byte[] getCommand() {
		return COMMAND_BYTES;
	}

}
