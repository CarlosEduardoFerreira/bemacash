package com.kaching123.pos.printer;

import com.kaching123.pos.Action;

@Deprecated
public class AutomaticLineFeedAction extends Action {

	private static final byte[] COMMAND_ENABLE_BYTES = new byte[]{ESC, _byte('z'), 0x1};
	private static final byte[] COMMAND_DISABLE_BYTES = new byte[]{ESC, _byte('z'), 0x0};

	private boolean enable;
	
	public AutomaticLineFeedAction(boolean enable){
		this.enable = enable;
	}
	
	@Override
	protected byte[] getCommand() {
		return enable ? COMMAND_ENABLE_BYTES : COMMAND_DISABLE_BYTES;
	}

}
