package com.system.bank.simple.controllers.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.SimpleDateFormat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.system.bank.simple.controllers.BankAccountController;
import com.system.bank.simple.db.config.DBConfig;
import com.system.bank.simple.exceptions.AccountNotFoundException;
import com.system.bank.simple.exceptions.ClientAlreadyExistsException;
import com.system.bank.simple.exceptions.ClientNotExistsException;
import com.system.bank.simple.exceptions.InsufficientFundException;
import com.system.bank.simple.exceptions.TokenNotValidException;
import com.system.bank.simple.exceptions.WrongDateException;
import com.system.bank.simple.exceptions.WrongPasswordException;

@TestMethodOrder(OrderAnnotation.class)
public class BankAccountControllerTest {

	@BeforeAll
	public static void config() {
		DBConfig.create();
		DBConfig.initDB();
	}

	@Test
	@Order(1)
	public void signupTest() {
		/********************
		 ****** CONFIG ******
		 ********************/
		BankAccountController ba = new BankAccountController();

		/*************************
		 ****** SUCCESSFUL *******
		 *************************/
		// customer successfully register
		assertDoesNotThrow(() -> {
			ba.signup("signup.user@mail.com", "5f4dcc3b5aa765d61d8327deb882cf99");
		});

		/*********************
		 ******* FAIL ********
		 *********************/
		// customer already exists
		assertThrows(ClientAlreadyExistsException.class, () -> {
			ba.signup("signup.user@mail.com", "5f4dcc3b5aa765d61d8327deb882cf99");
		});
	}

	@Test
	@Order(2)
	public void signinTest() throws Exception {
		/********************
		 ****** CONFIG ******
		 ********************/
		BankAccountController ba = new BankAccountController();

		// register customer
		ba.signup("signin.user@mail.com", "5f4dcc3b5aa765d61d8327deb882cf99");

		/*************************
		 ****** SUCCESSFUL *******
		 *************************/
		// customer successfully signin
		assertDoesNotThrow(() -> {
			ba.signin("signin.user@mail.com", "5f4dcc3b5aa765d61d8327deb882cf99");
		});

		/*********************
		 ******* FAIL ********
		 *********************/
		// user already exists
		assertThrows(ClientNotExistsException.class, () -> {
			ba.signin("wrong.signin.user@mail.com", "5f4dcc3b5aa765d61d8327deb882cf99");
		});
		// wrong password
		assertThrows(WrongPasswordException.class, () -> {
			ba.signin("signin.user@mail.com", "5f4dcc3b5aa765d61d8327deb882cf90");
		});
	}

	@Test
	@Order(3)
	public void depositTest() throws Exception {
		/********************
		 ****** CONFIG ******
		 ********************/
		BankAccountController ba = new BankAccountController();

		// register customer
		ba.signup("deposit.user@mail.com", "5f4dcc3b5aa765d61d8327deb882cf99");

		// signin and get token
		String token = ba.signin("deposit.user@mail.com", "5f4dcc3b5aa765d61d8327deb882cf99");

		ba.openAccountViaToken(token, (short) 980);
		long account = ba.getBankAccounts(token).get(0).getId();

		/*************************
		 ****** SUCCESSFUL *******
		 *************************/
		// successfully deposit cash on account
		assertDoesNotThrow(() -> {
			ba.deposit(token, account, new BigDecimal(100.66));
		});

		/*********************
		 ******* FAIL ********
		 *********************/
		// token not active
		assertThrows(TokenNotValidException.class, () -> {
			ba.deposit("testtesttesttesttesttesttesttest", account, new BigDecimal(100.66));
		});
		// wrong account
		assertThrows(AccountNotFoundException.class, () -> {
			ba.deposit(token, 111234567890L, new BigDecimal(100.66));
		});
	}

	@Test
	@Order(4)
	public void withdrawTest() throws Exception {
		/********************
		 ****** CONFIG ******
		 ********************/
		BankAccountController ba = new BankAccountController();

		// register customer
		ba.signup("withdraw.user@mail.com", "5f4dcc3b5aa765d61d8327deb882cf99");

		// signin and get token
		String token = ba.signin("withdraw.user@mail.com", "5f4dcc3b5aa765d61d8327deb882cf99");

		ba.openAccountViaToken(token, (short) 980);
		long account = ba.getBankAccounts(token).get(0).getId();

		// successfully deposit cash on account
		assertDoesNotThrow(() -> {
			ba.deposit(token, account, new BigDecimal(100.66));
		});

		/*************************
		 ****** SUCCESSFUL *******
		 *************************/
		// successfully withdraw cash from account
		assertDoesNotThrow(() -> {
			ba.withdraw(token, account, new BigDecimal(100.66));
		});

		/*********************
		 ******* FAIL ********
		 *********************/
		// token not active
		assertThrows(TokenNotValidException.class, () -> {
			ba.deposit("testtesttesttesttesttesttesttest", account, new BigDecimal(100.66));
		});
		// wrong account
		assertThrows(AccountNotFoundException.class, () -> {
			ba.deposit(token, 111234567890L, new BigDecimal(100.66));
		});
		// insufficient fund
		assertThrows(InsufficientFundException.class, () -> {
			ba.withdraw(token, account, new BigDecimal(75.66));
		});
	}

	@Test
	@Order(5)
	public void getBalanceTest() throws Exception {
		/********************
		 ****** CONFIG ******
		 ********************/
		BankAccountController ba = new BankAccountController();

		// register customer
		ba.signup("get_balance.user@mail.com", "5f4dcc3b5aa765d61d8327deb882cf99");

		// signin and get token
		String token = ba.signin("get_balance.user@mail.com", "5f4dcc3b5aa765d61d8327deb882cf99");

		ba.openAccountViaToken(token, (short) 980);
		long account = ba.getBankAccounts(token).get(0).getId();

		// successfully deposit cash on account
		assertDoesNotThrow(() -> {
			ba.deposit(token, account, new BigDecimal(100.66));
		});

		/*************************
		 ****** SUCCESSFUL *******
		 *************************/
		// successfully withdraw cash from account
		assertDoesNotThrow(() -> {
			ba.getBalance(token, account);
		});

		/*********************
		 ******* FAIL ********
		 *********************/
		// token not active
		assertThrows(TokenNotValidException.class, () -> {
			ba.getBalance("testtesttesttesttesttesttesttest", account);
		});
		// wrong account
		assertThrows(AccountNotFoundException.class, () -> {
			ba.getBalance(token, 111234567890L);
		});
	}

	@Test
	@Order(6)
	public void getTransactionsTest() throws Exception {
		/********************
		 ****** CONFIG ******
		 ********************/
		BankAccountController ba = new BankAccountController();

		// register customer
		ba.signup("transactions.user@mail.com", "5f4dcc3b5aa765d61d8327deb882cf99");

		// signin and get token
		String token = ba.signin("transactions.user@mail.com", "5f4dcc3b5aa765d61d8327deb882cf99");

		ba.openAccountViaToken(token, (short) 980);
		long account = ba.getBankAccounts(token).get(0).getId();

		// successfully deposit cash on account
		assertDoesNotThrow(() -> {
			ba.deposit(token, account, new BigDecimal(100.66));
		});

		/*************************
		 ****** SUCCESSFUL *******
		 *************************/
		// successfully get all transactions
		assertDoesNotThrow(() -> {
			ba.getTransactions(token, account, new SimpleDateFormat("MM-dd-yyyy").parse("2-2-2020"),
					new SimpleDateFormat("MM-dd-yyyy").parse("2-2-2022"));
		});

		// get list of transactions
		assertEquals(1, ba.getTransactions(token, account, new SimpleDateFormat("MM-dd-yyyy").parse("2-2-2020"),
				new SimpleDateFormat("MM-dd-yyyy").parse("2-2-2022")).size());

		/*********************
		 ******* FAIL ********
		 *********************/
		// token not active
		assertThrows(TokenNotValidException.class, () -> {
			ba.getTransactions("testtesttesttesttesttesttesttest", account,
					new SimpleDateFormat("MM-dd-yyyy").parse("2-2-2020"),
					new SimpleDateFormat("MM-dd-yyyy").parse("2-2-2022"));
		});
		// wrong date
		assertThrows(WrongDateException.class, () -> {
			ba.getTransactions(token, account, new SimpleDateFormat("MM-dd-yyyy").parse("2-2-2020"),
					new SimpleDateFormat("MM-dd-yyyy").parse("2-2-2019"));
		});
	}

	@Test
	@Order(7)
	public void getAccountsTest() throws Exception {
		/********************
		 ****** CONFIG ******
		 ********************/
		BankAccountController ba = new BankAccountController();

		// register customer
		ba.signup("get_accounts.user@mail.com", "5f4dcc3b5aa765d61d8327deb882cf99");

		// signin and get token
		String token = ba.signin("get_accounts.user@mail.com", "5f4dcc3b5aa765d61d8327deb882cf99");

		assertDoesNotThrow(() -> {
			ba.openAccountViaToken(token, (short) 980);
		});

		assertDoesNotThrow(() -> {
			ba.openAccountViaEmail("get_accounts.user@mail.com", (short) 980);
		});

		/*************************
		 ****** SUCCESSFUL *******
		 *************************/
		assertEquals(2, ba.getBankAccounts(token).size());

		/*********************
		 ******* FAIL ********
		 *********************/
		// token not active
		assertThrows(TokenNotValidException.class, () -> {
			ba.getBankAccounts("testtesttesttesttesttesttesttest");
		});
	}
}
