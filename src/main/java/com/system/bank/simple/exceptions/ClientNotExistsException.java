package com.system.bank.simple.exceptions;

public class ClientNotExistsException extends Exception {

	public ClientNotExistsException() {
		super();
	}

	public ClientNotExistsException(String message) {
		super(message);
	}
}
