/* 
 * Log-to-console Log Writer
 *
 * Copyright 2011 Wappworks Studios
 */

import javax.swing.JTextArea;

public class LogWriterWindow implements LogWriter {
	private JTextArea log;

	@Override
	public void setUpLogger(Object obj)
	{
		log = (JTextArea)obj;
	}
	
	public void logAppend(String logText) {
		log.append(logText + "\n");
		log.setCaretPosition(log.getDocument().getLength());
	}

}