package com.iris.increff.exception;

public class ApiException extends Exception {

	//code and message
	private static final long serialVersionUID = 1L;
	public ApiException(String string) {
		super(string);
	}

}
