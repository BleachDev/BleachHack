package org.bleachhack.util.doom.m;

import java.io.IOException;

public interface ISyncLogger {

	public void debugStart() throws IOException;
	public void debugEnd();
	public void sync(String format, Object ... args);
}

