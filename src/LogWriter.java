/* 
 * Log Writer interface
 *
 * Copyright 2011 Wappworks Studios
 */

public interface LogWriter {
	void setUpLogger(Object obj);

	void logAppend(String logText);
}