package com.bank.system.api.controllers;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bank.system.db.controllers.BankAccountController;
import com.bank.system.models.Response;

@RestController
public class AccountController {

	@RequestMapping(value = "/api/signup", method = RequestMethod.GET)
	public String signup(@RequestParam(name = "email", required = true) String email,
			@RequestParam(name = "password", required = true) String password) {

		BankAccountController ba = new BankAccountController();
		Response response = ba.signup(email, password);

		return "Email: " + email + " password: " + password + " " + response;
	}

	@RequestMapping(value = "/api/signin", method = RequestMethod.GET)
	public String signin(@RequestParam(name = "email", required = true) String email,
			@RequestParam(name = "password", required = true) String password) {

		BankAccountController ba = new BankAccountController();
		Response response = ba.signin(email, password);

		return response + "";
	}

	@RequestMapping(value = "/api/account/deposite", method = RequestMethod.GET)
	public String deposit(@RequestParam(name = "token", required = true) String token,
			@RequestParam(name = "account", required = true) String account,
			@RequestParam(name = "amount", required = true) String amount) {

		BigDecimal am = new BigDecimal(amount);

		BankAccountController ba = new BankAccountController();
		Response response = ba.deposit(token, account, am);

		return "You deposite on " + account + " " + amount + " " + response;
		// return "{\"token\":\"UYFJH567GHVBN\"}";
	}

	@RequestMapping(value = "/api/account/withdraw", method = RequestMethod.GET)
	public String withdraw(@RequestParam(name = "token", required = true) String token,
			@RequestParam(name = "account", required = true) String account,
			@RequestParam(name = "amount", required = true) String amount) {

		BigDecimal am = new BigDecimal(amount);

		BankAccountController ba = new BankAccountController();
		Response response = ba.withdraw(token, account, am);

		return response.toString();
	}

	@RequestMapping(value = "/api/account/getBalance", method = RequestMethod.GET)
	public String getBalance(@RequestParam(name = "token", required = true) String token,
			@RequestParam(name = "account", required = true) String account) {

		BankAccountController ba = new BankAccountController();
		Response response = ba.getBalance(token, account);

		return response.toString();
	}

	@RequestMapping(value = "/api/account/view/transactions", method = RequestMethod.GET)
	public String viewTransactions(@RequestParam(name = "token", required = true) String token,
			@RequestParam(name = "account", required = true) String account,
			@RequestParam(name = "from", required = true) String from,
			@RequestParam(name = "to", required = true) String to) {

		BankAccountController ba = new BankAccountController();
		try {
			Date f = new SimpleDateFormat("MM.dd.yyyy").parse(from);
			Date t = new SimpleDateFormat("MM.dd.yyyy").parse(to);
			
			Response response =	ba.getTransactions(token, account, f, t);

			return response.toString();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return "null";
	}
	

	//@RequestParam(name = "action", required = true) String action,
}
