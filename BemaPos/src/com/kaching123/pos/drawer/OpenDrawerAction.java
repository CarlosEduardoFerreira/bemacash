package com.kaching123.pos.drawer;

import com.kaching123.pos.Action;

/**
 * DLE DC4 fn m t (fn = 1)
 * Generate pulse at real-time
 * @author hamst_000
 *
 */
public class OpenDrawerAction extends Action {

	//private static final byte[] COMMAND_BYTES = new byte[]{DLE, 0x14, 0x01, 0x00, 0x04};
    private static final byte[] COMMAND_BYTES = new byte[]{ESC, 0x70, 0x00, 0x33, 0x33};
    //"<1B><70><00><33><33>"
	
	@Override
	public byte[] getCommand() {
		return COMMAND_BYTES;
	}

}
