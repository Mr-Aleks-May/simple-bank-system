package com.system.bank.simple.models;

import java.math.BigDecimal;
import java.sql.Date;

public class Transaction {
	private int id;
	private int customerId;
	private long accountId;
	private String name;
	private short type;
	private short currency;
	private BigDecimal balanceAfter;
	private BigDecimal amount;
	private Date date;
	private String description;
	private String location;

	public Transaction() {
		super();
	}

	public Transaction(int id, int customerId, long accountId, String name, short type, short currency,
			BigDecimal balanceAfter, BigDecimal amount, Date date, String description, String location) {
		super();
		this.id = id;
		this.customerId = customerId;
		this.accountId = accountId;
		this.name = name;
		this.type = type;
		this.currency = currency;
		this.balanceAfter = balanceAfter;
		this.amount = amount;
		this.date = date;
		this.description = description;
		this.location = location;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCustomerId() {
		return customerId;
	}

	public void setCustomerId(int customerId) {
		this.customerId = customerId;
	}

	public long getAccountId() {
		return accountId;
	}

	public void setAccountId(long accountId) {
		this.accountId = accountId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
	}

	public short getCurrency() {
		return currency;
	}

	public void setCurrency(short currency) {
		this.currency = currency;
	}

	public BigDecimal getBalanceAfter() {
		return balanceAfter;
	}

	public void setBalanceAfter(BigDecimal balanceAfter) {
		this.balanceAfter = balanceAfter;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	@Override
	public String toString() {
		return "Transaction [id=" + id + ", customerId=" + customerId + ", accountId=" + accountId + ", name=" + name
				+ ", type=" + type + ", currency=" + currency + ", balanceAfter=" + balanceAfter + ", amount=" + amount
				+ ", date=" + date + ", description=" + description + ", location=" + location + "]";
	}
}
