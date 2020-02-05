package com.system.bank.simple.api.controllers.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.system.bank.simple.db.config.DBConfig;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class TestSimpleBankApplication {
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
		// wrong email
		mockMvc.perform(post("/api/signup?email=user&&@mail.com&password=5f4dcc3b5aa765d61d8327deb882cf99"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(600));

		// password less 32 symbols
		mockMvc.perform(post("/api/signup?email=user@mail.com&password=5f4dcc3b5aa765d61d8327deb882cf9"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(601));

		// password bigger 32 symbols
		mockMvc.perform(post("/api/signup?email=user@mail.com&password=5f4dcc3b5aa765d61d8327deb882cf990"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(601));

		// user successfully register
		mockMvc.perform(post("/api/signup?email=user@mail.com&password=5f4dcc3b5aa765d61d8327deb882cf99"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(0));

		// user already exists
		mockMvc.perform(post("/api/signup?email=user@mail.com&password=5f4dcc3b5aa765d61d8327deb882cf99"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(100));
	}

	@Test
	@Order(2)
	public void signinTest() throws Exception {
		// wrong email
		mockMvc.perform(post("/api/signin?email=user&&@mail.com&password=5f4dcc3b5aa765d61d8327deb882cf99"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(600));

		// password less 32 symbols
		mockMvc.perform(post("/api/signin?email=user@mail.com&password=5f4dcc3b5aa765d61d8327deb882cf9"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(601));

		// password bigger 32 symbols
		mockMvc.perform(post("/api/signin?email=user@mail.com&password=5f4dcc3b5aa765d61d8327deb882cf990"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(601));

		// user successfully signin
		mockMvc.perform(post("/api/signin?email=user@mail.com&password=5f4dcc3b5aa765d61d8327deb882cf99"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(0));
	}

	@Test
	@Order(3)
	public void depositTest() throws Exception {
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

		// get response
		String json = mockMvc.perform(post("/api/signin?email=user@mail.com&password=5f4dcc3b5aa765d61d8327deb882cf99"))
				.andReturn().getResponse().getContentAsString();
		// get token
		String token = new GsonJsonParser().parseMap(json).get("token").toString();

		// customer successfully deposit money
		mockMvc.perform(post("/api/account/deposite?token=" + token + "&account=1&amount=100.66"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(0)).andExpect(jsonPath("$.balance").value(100.66));

		// number with letter (bad request)
		mockMvc.perform(post("/api/account/deposite?token=" + token + "&account=1&amount=100d.66"))
				.andExpect(status().isBadRequest());

		// account number with letter (bad request)
		mockMvc.perform(post("/api/account/deposite?token=" + token + "&account=1d&amount=100.66"))
				.andExpect(status().isBadRequest());

		// wrong account number
		mockMvc.perform(post("/api/account/deposite?token=" + token + "&account=00111&amount=100.66"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(201));
	}

	@Test
	@Order(4)
	public void withdrawTest() throws Exception {
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

		// get response
		String json = mockMvc.perform(post("/api/signin?email=user@mail.com&password=5f4dcc3b5aa765d61d8327deb882cf99"))
				.andReturn().getResponse().getContentAsString();
		// get token
		String token = new GsonJsonParser().parseMap(json).get("token").toString();

		// customer successfully deposit money
		mockMvc.perform(post("/api/account/withdraw?token=" + token + "&account=1&amount=75.66"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(0)).andExpect(jsonPath("$.balance").value(25));

		// wrong account number
		mockMvc.perform(post("/api/account/withdraw?token=" + token + "&account=1&amount=75.66"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(401));

		// number with letter (bad request)
		mockMvc.perform(post("/api/account/withdraw?token=" + token + "&account=1&amount=75d.66"))
				.andExpect(status().isBadRequest());

		// account number with letter (bad request)
		mockMvc.perform(post("/api/account/withdraw?token=" + token + "&account=1d&amount=75.66"))
				.andExpect(status().isBadRequest());

		// wrong account number
		mockMvc.perform(post("/api/account/withdraw?token=" + token + "&account=00111&amount=75.66"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(201));
	}

	@Test
	@Order(5)
	public void getBalanceTest() throws Exception {
		// token less 32 symbols
		mockMvc.perform(post("/api/account/get_balance?token=ykPQqt38yBDHeGi4OIK9obdRxtrX7r&account=1"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(602));

		// token bigger 32 symbols
		mockMvc.perform(post("/api/account/get_balance?token=ykPQqt38yBDHeGi4OIK9obdRxtrX7rwWtest&account=1"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(602));

		// token not active
		mockMvc.perform(post("/api/account/get_balance?token=testtesttesttesttesttesttesttest&account=1"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(302));

		// get response
		String json = mockMvc.perform(post("/api/signin?email=user@mail.com&password=5f4dcc3b5aa765d61d8327deb882cf99"))
				.andReturn().getResponse().getContentAsString();
		// get token
		String token = new GsonJsonParser().parseMap(json).get("token").toString();

		// customer successfully deposit money
		mockMvc.perform(post("/api/account/get_balance?token=" + token + "&account=1")).andExpect(status().isOk())
				.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(0)).andExpect(jsonPath("$.balance").value(25));

		// account number with letter (bad request)
		mockMvc.perform(post("/api/account/get_balance?token=" + token + "&account=1d"))
				.andExpect(status().isBadRequest());

		// wrong account number
		mockMvc.perform(post("/api/account/get_balance?token=" + token + "&account=00111")).andExpect(status().isOk())
				.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(201));
	}

	@Order(6)
	@Test
	public void getTransactionsTest() throws Exception {
		// token less 32 symbols
		mockMvc.perform(post(
				"/api/account/view/transactions?token=ykPQqt38yBDHeGi4OIK9obdRx&account=1&from=1.01.2020&to=2.2.2022"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(602));

		// token bigger 32 symbols
		mockMvc.perform(post(
				"/api/account/view/transactions?token=ykPQqt38yBDHeGi4OIK9obdRxtrX7rwWtest&account=1&from=1.01.2020&to=2.2.2022"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(602));

		// token not active
		mockMvc.perform(post(
				"/api/account/view/transactions?token=testtesttesttesttesttesttesttest&account=1&from=1.01.2020&to=2.2.2022"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(302));

		// get response
		String json = mockMvc.perform(post("/api/signin?email=user@mail.com&password=5f4dcc3b5aa765d61d8327deb882cf99"))
				.andReturn().getResponse().getContentAsString();
		// get token
		String token = new GsonJsonParser().parseMap(json).get("token").toString();

		// get transactions
		mockMvc.perform(post("/api/account/view/transactions?token=" + token + "&account=1&from=1.01.2020&to=2.2.2022"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(0));

		// account id with number
		mockMvc.perform(
				post("/api/account/view/transactions?token=" + token + "&account=1d&from=1.01.2020&to=2.2.2022"))
				.andExpect(status().isBadRequest());

		// wrong account number
		mockMvc.perform(
				post("/api/account/view/transactions?token=" + token + "&account=00011&from=1.01.2020&to=2.2.2022"))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status").value(0));

		// date with letters
		mockMvc.perform(
				post("/api/account/view/transactions?token=" + token + "&account=1&from=1.01d.2020&to=2.2.2022"))
				.andExpect(status().isBadRequest());
	}

	@AfterAll
	public static void assertOutput() {
	}
}
