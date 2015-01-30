package com.kaching123.pos;

import java.io.IOException;
//testing

public abstract class Action extends BaseAction<Void>{

	@Override
	public Void execute(PosPrinter printer) throws IOException {
		printer.write(getCommand());
		return null;
	}
}
