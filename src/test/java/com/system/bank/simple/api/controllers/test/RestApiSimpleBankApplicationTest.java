package com.system.bank.simple.api.controllers.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.google.gson.Gson;
import com.system.bank.simple.db.config.DBConfig;
import com.system.bank.simple.models.BankAccountInfo;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class RestApiSimpleBankApplicationTest {
	@Autowired
	private MockMvc mockMvc;

	@BeforeAll
	public static void config() {
		DBConfig.create();
		DBConfig.initDB();
	}

	@Test
	@Order(1)
	public void signupTest() throws Exception {
		/*************************
		 ****** SUCCESSFUL *******
		 *************************/
		// user successfully register
		mockMvc.perform(post("/api/signup?email=api.signup.user@mail.com&password=5f4dcc3b5aa765d61d8327deb882cf99"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(0));

		/*********************
		 ******* FAIL ********
		 *********************/
		// wrong email
		mockMvc.perform(post("/api/signup?email=api.signup.user&&@mail.com&password=5f4dcc3b5aa765d61d8327deb882cf99"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(600));

		// password less 32 symbols
		mockMvc.perform(post("/api/signup?email=api.signup.user@mail.com&password=5f4dcc3b5aa765d61d8327deb882cf9"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(601));

		// password bigger 32 symbols
		mockMvc.perform(post("/api/signup?email=api.signup.user@mail.com&password=5f4dcc3b5aa765d61d8327deb882cf990"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(601));

		// user already exists
		mockMvc.perform(post("/api/signup?email=api.signup.user@mail.com&password=5f4dcc3b5aa765d61d8327deb882cf99"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(100));
	}

	@Test
	@Order(2)
	public void signinTest() throws Exception {
		/********************
		 ****** CONFIG ******
		 ********************/
		// register new user for this test
		mockMvc.perform(post("/api/signup?email=api.signin.user@mail.com&password=5f4dcc3b5aa765d61d8327deb882cf99"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(0));

		/*************************
		 ****** SUCCESSFUL *******
		 *************************/
		// user successfully signin
		mockMvc.perform(post("/api/signin?email=api.signin.user@mail.com&password=5f4dcc3b5aa765d61d8327deb882cf99"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(0));

		/*********************
		 ******* FAIL ********
		 *********************/
		// wrong email
		mockMvc.perform(post("/api/signin?email=api.signin.user&&@mail.com&password=5f4dcc3b5aa765d61d8327deb882cf99"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(600));

		// password less 32 symbols
		mockMvc.perform(post("/api/signin?email=api.signin.user@mail.com&password=5f4dcc3b5aa765d61d8327deb882cf9"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(601));

		// password bigger 32 symbols
		mockMvc.perform(post("/api/signin?email=api.signin.user@mail.com&password=5f4dcc3b5aa765d61d8327deb882cf990"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(601));
	}

	@Test
	@Order(3)
	public void depositTest() throws Exception {
		/********************
		 ****** CONFIG ******
		 ********************/
		// register new user for this test
		mockMvc.perform(post("/api/signup?email=api.deposite.user@mail.com&password=5f4dcc3b5aa765d61d8327deb882cf99"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(0));

		// get response
		String responseJson = mockMvc
				.perform(post("/api/signin?email=api.deposite.user@mail.com&password=5f4dcc3b5aa765d61d8327deb882cf99"))
				.andReturn().getResponse().getContentAsString();

		// get token
		String token = new GsonJsonParser().parseMap(responseJson).get("token").toString();

		// get response
		responseJson = mockMvc.perform(post("/api/get_accounts?token=" + token)).andReturn().getResponse()
				.getContentAsString();

		List accounts = (List) new GsonJsonParser().parseMap(responseJson).get("accounts");

		long account = new Gson().fromJson(accounts.get(0).toString(), BankAccountInfo.class).getId();

		/*************************
		 ****** SUCCESSFUL *******
		 *************************/
		// customer successfully deposit money
		mockMvc.perform(post("/api/account/deposite?token=" + token + "&account=" + account + "&amount=100.66"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(0)).andExpect(jsonPath("$.balance").value(100.66));

		/*********************
		 ******* FAIL ********
		 *********************/
		// token less 32 symbols
		mockMvc.perform(post("/api/account/deposite?token=ykPQqt38yBDHeGi4OIK9obdRxtrX7r&account=1&amount=100.66"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(602));

		// token bigger 32 symbols
		mockMvc.perform(
				post("/api/account/deposite?token=ykPQqt38yBDHeGi4OIK9obdRxtrX7rwWtest&account=1&amount=100.66"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(602));

		// token not active
		mockMvc.perform(post("/api/account/deposite?token=testtesttesttesttesttesttesttest&account=1&amount=100.66"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(302));

		// number with letter (bad request)
		mockMvc.perform(post("/api/account/deposite?token=" + token + "&account=1&amount=100d.66"))
				.andExpect(status().isBadRequest());

		// account number with letter (bad request)
		mockMvc.perform(post("/api/account/deposite?token=" + token + "&account=1d&amount=100.66"))
				.andExpect(status().isBadRequest());

		// wrong account number
		mockMvc.perform(post("/api/account/deposite?token=" + token + "&account=00111234567890&amount=100.66"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(201));
	}

	@Test
	@Order(4)
	public void withdrawTest() throws Exception {
		/********************
		 ****** CONFIG ******
		 ********************/
		// register new user for this test
		mockMvc.perform(post("/api/signup?email=api.withdraw.user@mail.com&password=5f4dcc3b5aa765d61d8327deb882cf99"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(0));

		// get response
		String responseJson = mockMvc
				.perform(post("/api/signin?email=api.withdraw.user@mail.com&password=5f4dcc3b5aa765d61d8327deb882cf99"))
				.andReturn().getResponse().getContentAsString();

		// get token
		String token = new GsonJsonParser().parseMap(responseJson).get("token").toString();

		// get response
		responseJson = mockMvc.perform(post("/api/get_accounts?token=" + token)).andReturn().getResponse()
				.getContentAsString();

		List accounts = (List) new GsonJsonParser().parseMap(responseJson).get("accounts");

		long account = new Gson().fromJson(accounts.get(0).toString(), BankAccountInfo.class).getId();

		// customer successfully deposit money
		mockMvc.perform(post("/api/account/deposite?token=" + token + "&account=" + account + "&amount=100.66"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(0)).andExpect(jsonPath("$.balance").value(100.66));

		/*************************
		 ****** SUCCESSFUL *******
		 *************************/
		// customer successfully deposit money
		mockMvc.perform(post("/api/account/withdraw?token=" + token + "&account=" + account + "&amount=75.66"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(0)).andExpect(jsonPath("$.balance").value(25));

		/*********************
		 ******* FAIL ********
		 *********************/
		// token less 32 symbols
		mockMvc.perform(post("/api/account/withdraw?token=ykPQqt38yBDHeGi4OIK9obdRxtrX7r&account=1&amount=100.66"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(602));

		// token bigger 32 symbols
		mockMvc.perform(
				post("/api/account/withdraw?token=ykPQqt38yBDHeGi4OIK9obdRxtrX7rwWtest&account=1&amount=100.66"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(602));

		// token not active
		mockMvc.perform(post("/api/account/withdraw?token=testtesttesttesttesttesttesttest&account=1&amount=100.66"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(302));

		// insufficient fund
		mockMvc.perform(post("/api/account/withdraw?token=" + token + "&account=" + account + "&amount=75.66"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(401));

		// number with letter (bad request)
		mockMvc.perform(post("/api/account/withdraw?token=" + token + "&account=" + account + "&amount=75d.66"))
				.andExpect(status().isBadRequest());

		// account number with letter (bad request)
		mockMvc.perform(post("/api/account/withdraw?token=" + token + "&account=1d&amount=75.66"))
				.andExpect(status().isBadRequest());

		// wrong account number
		mockMvc.perform(post("/api/account/withdraw?token=" + token + "&account=00111234567890&amount=75.66"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(201));
	}

	@Test
	@Order(5)
	public void getBalanceTest() throws Exception {
		/********************
		 ****** CONFIG ******
		 ********************/
		// register new user for this test
		mockMvc.perform(post("/api/signup?email=api.get_balance.user@mail.com&password=5f4dcc3b5aa765d61d8327deb882cf99"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(0));

		// get response
		String responseJson = mockMvc
				.perform(post("/api/signin?email=api.get_balance.user@mail.com&password=5f4dcc3b5aa765d61d8327deb882cf99"))
				.andReturn().getResponse().getContentAsString();

		// get token
		String token = new GsonJsonParser().parseMap(responseJson).get("token").toString();

		// get response
		responseJson = mockMvc.perform(post("/api/get_accounts?token=" + token)).andReturn().getResponse()
				.getContentAsString();

		List accounts = (List) new GsonJsonParser().parseMap(responseJson).get("accounts");

		long account = new Gson().fromJson(accounts.get(0).toString(), BankAccountInfo.class).getId();

		// customer successfully deposit money
		mockMvc.perform(post("/api/account/deposite?token=" + token + "&account=" + account + "&amount=100.66"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(0)).andExpect(jsonPath("$.balance").value(100.66));

		// customer successfully withdraw money
		mockMvc.perform(post("/api/account/withdraw?token=" + token + "&account=" + account + "&amount=75.66"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(0)).andExpect(jsonPath("$.balance").value(25));

		/*************************
		 ****** SUCCESSFUL *******
		 *************************/
		// customer successfully deposit money
		mockMvc.perform(post("/api/account/get_balance?token=" + token + "&account=" + account))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(0)).andExpect(jsonPath("$.balance").value(25));

		/*********************
		 ******* FAIL ********
		 *********************/
		// token less 32 symbols
		mockMvc.perform(post("/api/account/get_balance?token=ykPQqt38yBDHeGi4OIK9obdRxtrX7r&account=" + account))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(602));

		// token bigger 32 symbols
		mockMvc.perform(post("/api/account/get_balance?token=ykPQqt38yBDHeGi4OIK9obdRxtrX7rwWtest&account=" + account))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(602));

		// token not active
		mockMvc.perform(post("/api/account/get_balance?token=testtesttesttesttesttesttesttest&account=" + account))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(302));

		// account number with letter (bad request)
		mockMvc.perform(post("/api/account/get_balance?token=" + token + "&account=" + account + "d"))
				.andExpect(status().isBadRequest());

		// wrong account number
		mockMvc.perform(post("/api/account/get_balance?token=" + token + "&account=00111234567890"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(201));
	}

	@Test
	@Order(6)
	public void getTransactionsTest() throws Exception {
		/********************
		 ****** CONFIG ******
		 ********************/
		// register new user for this test
		mockMvc.perform(post("/api/signup?email=api.transactions.user@mail.com&password=5f4dcc3b5aa765d61d8327deb882cf99"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(0));

		// get response
		String responseJson = mockMvc
				.perform(post("/api/signin?email=api.transactions.user@mail.com&password=5f4dcc3b5aa765d61d8327deb882cf99"))
				.andReturn().getResponse().getContentAsString();

		// get token
		String token = new GsonJsonParser().parseMap(responseJson).get("token").toString();

		// get response
		responseJson = mockMvc.perform(post("/api/get_accounts?token=" + token)).andReturn().getResponse()
				.getContentAsString();

		List accounts = (List) new GsonJsonParser().parseMap(responseJson).get("accounts");

		long account = new Gson().fromJson(accounts.get(0).toString(), BankAccountInfo.class).getId();

		// customer successfully deposit money
		mockMvc.perform(post("/api/account/deposite?token=" + token + "&account=" + account + " &amount=100.66"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(0)).andExpect(jsonPath("$.balance").value(100.66));

		// customer successfully deposit money
		mockMvc.perform(post("/api/account/withdraw?token=" + token + "&account=" + account + "&amount=75.66"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(0)).andExpect(jsonPath("$.balance").value(25));

		/*************************
		 ****** SUCCESSFUL *******
		 *************************/
		// get transactions
		mockMvc.perform(post("/api/account/view/transactions?token=" + token + "&account=" + account
				+ "&from=1.01.2020&to=2.2.2022")).andExpect(status().isOk())
				.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(0));

		/*********************
		 ******* FAIL ********
		 *********************/
		// token less 32 symbols
		mockMvc.perform(post("/api/account/view/transactions?token=ykPQqt38yBDHeGi4OIK9obdRx&account=" + account
				+ "&from=1.01.2020&to=2.2.2022")).andExpect(status().isOk())
				.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(602));

		// token bigger 32 symbols
		mockMvc.perform(post("/api/account/view/transactions?token=ykPQqt38yBDHeGi4OIK9obdRxtrX7rwWtest&account="
				+ account + "&from=1.01.2020&to=2.2.2022")).andExpect(status().isOk())
				.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(602));

		// token not active
		mockMvc.perform(post("/api/account/view/transactions?token=testtesttesttesttesttesttesttest&account=" + account
				+ "&from=1.01.2020&to=2.2.2022")).andExpect(status().isOk())
				.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(302));

		// account id with number
		mockMvc.perform(post("/api/account/view/transactions?token=" + token + "&account=" + account
				+ "d&from=1.01.2020&to=2.2.2022")).andExpect(status().isBadRequest());

		// wrong account number
		mockMvc.perform(post("/api/account/view/transactions?token=" + token
				+ "&account=000111234567890&from=1.01.2020&to=2.2.2022")).andExpect(status().isOk())
				.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(0));

		// date with letters
		mockMvc.perform(post("/api/account/view/transactions?token=" + token + "&account=" + account
				+ "&from=1.01d.2020&to=2.2.2022")).andExpect(status().isBadRequest());
	}

	@AfterAll
	public static void assertOutput() {
		DBConfig.initDB();
	}
}
