package com.bank.system.api.controllers;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bank.system.controllers.Validations;
import com.bank.system.db.controllers.BankAccountController;
import com.bank.system.models.Response;

@RestController
public class AccountController {

	@RequestMapping(value = "/api/signup", method = RequestMethod.POST)
	public String signup(@RequestParam(name = "email", required = true) String email,
			@RequestParam(name = "password", required = true) String password) {

		Response response = null;

		Validations vs = new Validations();
		if (!vs.isValidEmail(email)) {
			response = new Response().add("status", 100);
		} else if (password.length() != 32) {
			response = new Response().add("status", 101);
		} else {
			BankAccountController ba = new BankAccountController();
			response = ba.signup(email, password);
		}

		return response.toJSON();
	}

	@RequestMapping(value = "/api/signin", method = RequestMethod.POST)
	public String signin(@RequestParam(name = "email", required = true) String email,
			@RequestParam(name = "password", required = true) String password) {

		Response response = null;

		Validations vs = new Validations();
		if (!vs.isValidEmail(email)) {
			response = new Response().add("status", 100);
		} else if (password.length() != 32) {
			response = new Response().add("status", 101);
		} else {
			BankAccountController ba = new BankAccountController();
			response = ba.signin(email, password);
		}

		return response.toJSON();
	}

	@RequestMapping(value = "/api/account/deposite", method = RequestMethod.POST)
	public String deposit(@RequestParam(name = "token", required = true) String token,
			@RequestParam(name = "account", required = true) String account,
			@RequestParam(name = "amount", required = true) BigDecimal amount) {

		Response response = null;

		if (token.length() != 32) {
			response = new Response().add("status", 100);
		} else {
			BankAccountController ba = new BankAccountController();
			response = ba.deposit(token, account, amount);
		}

		return response.toJSON();
	}

	@RequestMapping(value = "/api/account/withdraw", method = RequestMethod.POST)
	public String withdraw(@RequestParam(name = "token", required = true) String token,
			@RequestParam(name = "account", required = true) String account,
			@RequestParam(name = "amount", required = true) BigDecimal amount) {

		Response response = null;

		if (token.length() != 32) {
			response = new Response().add("status", 100);
		} else {
			BankAccountController ba = new BankAccountController();
			response = ba.withdraw(token, account, amount);
		}

		return response.toJSON();
	}

	@RequestMapping(value = "/api/account/getBalance", method = RequestMethod.POST)
	public String getBalance(@RequestParam(name = "token", required = true) String token,
			@RequestParam(name = "account", required = true) String account) {

		Response response = null;

		if (token.length() != 32) {
			response = new Response().add("status", 100);
		} else {
			BankAccountController ba = new BankAccountController();
			response = ba.getBalance(token, account);
		}

		return response.toJSON();
	}

	@RequestMapping(value = "/api/account/view/transactions", method = RequestMethod.POST)
	public String viewTransactions(@RequestParam(name = "token", required = true) String token,
			@RequestParam(name = "account", required = true) String account,
			@RequestParam(name = "from", required = true) @DateTimeFormat(pattern = "dd.MM.yyyy") Date from,
			@RequestParam(name = "to", required = true) @DateTimeFormat(pattern = "dd.MM.yyyy") Date to) {

		Response response = null;

		if (token.length() != 32) {
			response = new Response().add("status", 100);
		} else {
			BankAccountController ba = new BankAccountController();
			response = ba.getTransactions(token, account, from, to);
		}

		return response.toJSON();
	}
}
