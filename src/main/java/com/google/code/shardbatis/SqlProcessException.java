package com.google.code.shardbatis;

/**
 * @author sean.he
 * 
 */
public class SqlProcessException extends Exception {

	private static final long serialVersionUID = 1793760050084714190L;

	public SqlProcessException() {
		super();
	}

	public SqlProcessException(String msg) {
		super(msg);
	}

	public SqlProcessException(String msg, Throwable t) {
		super(msg, t);
	}

	public SqlProcessException(Throwable t) {
		super(t);
	}
}
