package com.system.bank.simple.api.controllers;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.system.bank.simple.api.models.Response;
import com.system.bank.simple.controllers.IBankAccount;
import com.system.bank.simple.exceptions.AccountNotCreatedException;
import com.system.bank.simple.exceptions.AccountNotFoundException;
import com.system.bank.simple.exceptions.ClientAlreadyExistsException;
import com.system.bank.simple.exceptions.ClientNotExistsException;
import com.system.bank.simple.exceptions.InsufficientFundException;
import com.system.bank.simple.exceptions.InternalServerError;
import com.system.bank.simple.exceptions.TokenNotValidException;
import com.system.bank.simple.exceptions.WrongDateException;
import com.system.bank.simple.exceptions.WrongPasswordException;
import com.system.bank.simple.models.BankAccountInfo;
import com.system.bank.simple.models.Transaction;
import com.system.bank.simple.utils.Validator;

@RestController
public class RESTAccountController {

	@RequestMapping(value = "/api/signup", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	public String signup(@RequestParam(name = "email", required = true) String email,
			@RequestParam(name = "password", required = true) String password) {

		Response response = null;

		Validator vs = new Validator();
		if (!vs.isValidEmail(email)) {
			response = new Response().add("status", 600);
		} else if (password.length() != 32) {
			response = new Response().add("status", 601);
		} else {
			IBankAccount ba = new com.system.bank.simple.controllers.BankAccountController();

			try {
				ba.signup(email, password);
				response = new Response().add("status", 0);
			} catch (ClientAlreadyExistsException e) {
				response = new Response().add("status", 100);
			} catch (InternalServerError e) {
				response = new Response().add("status", 500);
			}

			try {
				ba.openAccountViaEmail(email, (short) 980);
			} catch (ClientNotExistsException | AccountNotCreatedException e) {
				response = new Response().add("status", 300);
			} catch (InternalServerError e) {
			}
		}

		return response.toJSON();
	}

	@RequestMapping(value = "/api/signin", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	public String signin(@RequestParam(name = "email", required = true) String email,
			@RequestParam(name = "password", required = true) String password) {

		Response response = null;

		Validator vs = new Validator();
		if (!vs.isValidEmail(email)) {
			response = new Response().add("status", 600);
		} else if (password.length() != 32) {
			response = new Response().add("status", 601);
		} else {
			IBankAccount ba = new com.system.bank.simple.controllers.BankAccountController();

			try {
				String token = ba.signin(email, password);
				response = new Response().add("status", 0).add("token", token);
			} catch (ClientNotExistsException e) {
				response = new Response().add("status", 300);
			} catch (WrongPasswordException e) {
				response = new Response().add("status", 301);
			} catch (InternalServerError e) {
				response = new Response().add("status", 500);
			}
		}

		return response.toJSON();
	}

	@RequestMapping(value = "/api/account/deposite", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	public String deposit(@RequestParam(name = "token", required = true) String token,
			@RequestParam(name = "account", required = true) long account,
			@RequestParam(name = "amount", required = true) BigDecimal amount) {

		Response response = null;

		if (token.length() != 32) {
			response = new Response().add("status", 602);
		} else {
			IBankAccount ba = new com.system.bank.simple.controllers.BankAccountController();

			try {
				BigDecimal balance = ba.deposit(token, account, amount);
				response = new Response().add("status", 0).add("balance", balance);
			} catch (TokenNotValidException e) {
				response = new Response().add("status", 302);
			} catch (AccountNotFoundException e) {
				response = new Response().add("status", 201);
			} catch (InternalServerError e) {
				response = new Response().add("status", 500);
			}
		}

		return response.toJSON();
	}

	@RequestMapping(value = "/api/account/withdraw", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	public String withdraw(@RequestParam(name = "token", required = true) String token,
			@RequestParam(name = "account", required = true) long account,
			@RequestParam(name = "amount", required = true) BigDecimal amount) {

		Response response = null;

		if (token.length() != 32) {
			response = new Response().add("status", 602);
		} else {
			IBankAccount ba = new com.system.bank.simple.controllers.BankAccountController();

			try {
				BigDecimal balance = ba.withdraw(token, account, amount);
				response = new Response().add("status", 0).add("balance", balance);
			} catch (TokenNotValidException e) {
				response = new Response().add("status", 302);
			} catch (AccountNotFoundException e) {
				response = new Response().add("status", 201);
			} catch (InsufficientFundException e) {
				response = new Response().add("status", 401);
			} catch (InternalServerError e) {
				response = new Response().add("status", 500);
			}
		}

		return response.toJSON();
	}

	@RequestMapping(value = "/api/account/get_balance", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	public String getBalance(@RequestParam(name = "token", required = true) String token,
			@RequestParam(name = "account", required = true) long account) {

		Response response = null;

		if (token.length() != 32) {
			response = new Response().add("status", 602);
		} else {
			IBankAccount ba = new com.system.bank.simple.controllers.BankAccountController();

			try {
				BigDecimal balance = ba.getBalance(token, account);
				response = new Response().add("status", 0).add("balance", balance);
			} catch (TokenNotValidException e) {
				response = new Response().add("status", 302);
			} catch (AccountNotFoundException e) {
				response = new Response().add("status", 201);
			} catch (InternalServerError e) {
				response = new Response().add("status", 500);
			}
		}

		return response.toJSON();
	}

	@RequestMapping(value = "/api/account/view/transactions", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	public String viewTransactions(@RequestParam(name = "token", required = true) String token,
			@RequestParam(name = "account", required = true) long account,
			@RequestParam(name = "from", required = true) @DateTimeFormat(pattern = "dd.MM.yyyy") Date from,
			@RequestParam(name = "to", required = true) @DateTimeFormat(pattern = "dd.MM.yyyy") Date to) {

		Response response = null;

		if (token.length() != 32) {
			response = new Response().add("status", 602);
		} else {
			IBankAccount ba = new com.system.bank.simple.controllers.BankAccountController();

			try {
				List<Transaction> transactions = ba.getTransactions(token, account, from, to);
				response = new Response().add("status", 0).add("transactions", transactions);
			} catch (TokenNotValidException e) {
				response = new Response().add("status", 302);
			} catch (WrongDateException e) {
				response = new Response().add("status", 603);
			} catch (InternalServerError e) {
				response = new Response().add("status", 500);
			}
		}

		return response.toJSON();
	}

	@RequestMapping(value = "/api/get_accounts", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	public String getCustomerAccounts(@RequestParam(name = "token", required = true) String token) {

		Response response = null;

		if (token.length() != 32) {
			response = new Response().add("status", 602);
		} else {
			IBankAccount ba = new com.system.bank.simple.controllers.BankAccountController();

			try {
				List<BankAccountInfo> accounts = ba.getBankAccounts(token);
				response = new Response().add("status", 0).add("accounts", accounts);
			} catch (TokenNotValidException e) {
				response = new Response().add("status", 302);
			} catch (InternalServerError e) {
				response = new Response().add("status", 500);
			}
		}

		return response.toJSON();
	}
}
