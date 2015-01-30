package com.kaching123.pos.drawer;

import com.kaching123.pos.Action;

/**
 * DLE DC4 fn m t (fn = 1)
 * Generate pulse at real-time
 * @author hamst_000
 *
 */
public class OpenDrawerAction extends Action {

	private static final byte[] COMMAND_BYTES = new byte[]{DLE, 0x14, 0x01, 0x00, 0x04};
	
	@Override
	public byte[] getCommand() {
		return COMMAND_BYTES;
	}

}
