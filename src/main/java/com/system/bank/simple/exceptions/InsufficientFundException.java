package com.system.bank.simple.exceptions;

import java.math.BigDecimal;

public class InsufficientFundException extends Exception {
	private BigDecimal current_balance;

	public InsufficientFundException() {
		super();
	}

	public InsufficientFundException(String message) {
		super(message);
	}

	public InsufficientFundException(BigDecimal current_balance) {
		this.current_balance = current_balance;
	}

	public BigDecimal getCurrentBalance() {
		return current_balance;
	}
}
