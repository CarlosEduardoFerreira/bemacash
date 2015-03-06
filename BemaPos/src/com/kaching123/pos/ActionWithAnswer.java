package com.kaching123.pos;

import java.io.IOException;

public abstract class ActionWithAnswer<T> extends BaseAction<T> {

	protected final int answerLength;
	
	public ActionWithAnswer(int answerLength) {
		super();
		this.answerLength = answerLength;
	}

	@Override
	public T execute(PosPrinter printer) throws IOException {
		printer.write(getCommand());
		byte[] bytes = printer.read(answerLength);
		return parseAnswer(bytes);
	}
	
	public abstract T parseAnswer(byte[] bytes);

}
