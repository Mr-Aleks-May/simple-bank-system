package com.system.bank.simple.models;

import java.math.BigDecimal;

public class BankAccountInfo {
	private long id;
	private int customerId;
	private short currency;
	private BigDecimal balance;

	public BankAccountInfo() {
		super();
	}

	public BankAccountInfo(long id, int customerId, short currency, BigDecimal balance) {
		super();
		this.id = id;
		this.customerId = customerId;
		this.currency = currency;
		this.balance = balance;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getCustomerId() {
		return customerId;
	}

	public void setCustomerId(int customerId) {
		this.customerId = customerId;
	}

	public short getCurrency() {
		return currency;
	}

	public void setCurrency(short currency) {
		this.currency = currency;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	@Override
	public String toString() {
		return "BankAccountInfo [id=" + id + ", customerId=" + customerId + ", currency=" + currency + ", balance="
				+ balance + "]";
	}
}
