package com.bank.system.db.controllers;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.bank.system.db.models.DBSettings;
import com.bank.system.models.BankAccountInfo;
import com.bank.system.models.Response;

public class BankAccountController {

	public List<BankAccountInfo> getBankAccounts() {

		return null;
	}

	/**
	 * Register customer in database.
	 * 
	 * @param email    - customer email address
	 * @param password - customer password (in form of md5 hash, with length 32)
	 * @return Return: 0 - if successfuly register, 1 - if customer already exist.
	 *         In case of error return -1.
	 */
	public Response signup(String email, String password) {
		if (isCustomerExist(email)) {
			return new Response().add("status", 1);
		}

		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword()); Statement stmt = connection.createStatement();) {

			String sql = String.format("INSERT INTO customers(email, password) VALUES('%s','%s');", email, password);
			stmt.executeUpdate(sql);

			sql = String.format("SELECT id FROM customers WHERE email='%s' LIMIT 1;", email);
			ResultSet rs = stmt.executeQuery(sql);

			if (!rs.next()) {
				return new Response().add("status", 2);
			}

			int customer_id = rs.getInt("id");

			sql = String.format("INSERT INTO primary_account(customer_id, balance, currency) VALUES('%s', '%d', '%s');",
					customer_id, 0, 980);
			stmt.executeUpdate(sql);

			return new Response().add("status", 0);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

			return new Response().add("status", -1);
		}
	}

	public Response signin(String email, String password) {
		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword()); Statement stmt = connection.createStatement();) {

			String sql = String.format("SELECT id, email, password FROM customers WHERE email='%s' LIMIT 1;", email);
			ResultSet rs = stmt.executeQuery(sql);

			if (!rs.next()) {
				return new Response().add("status", 1);
			}

			int customer_id = rs.getInt("id");
			String customerPassword = rs.getString("password");

			if (!customerPassword.equals(password)) {
				return new Response().add("status", 2);
			}

			String token = new com.bank.system.controllers.Token().generateToken();
			if (isTokenExist(customer_id)) {
				sql = String.format("UPDATE tokens set token='%2$s' WHERE customer_id='%1$d';", customer_id, token);
				stmt.executeUpdate(sql);
			} else {
				sql = String.format("INSERT INTO tokens(customer_id, token) VALUES('%d','%s');", customer_id, token);
				stmt.executeUpdate(sql);
			}

			return new Response().add("status", 0).add("token", token);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

			return new Response().add("status", -1);
		}
	}

	private boolean isCustomerExist(String email) {
		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword()); Statement stmt = connection.createStatement();) {

			String sql = String.format("SELECT email FROM customers WHERE email='%s' LIMIT 1;", email);
			ResultSet rs = stmt.executeQuery(sql);

			if (rs.next()) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}

		return false;
	}

	private boolean isTokenExist(int customer_id) {
		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword()); Statement stmt = connection.createStatement();) {

			String sql = String.format("SELECT customer_id FROM tokens WHERE customer_id='%d' LIMIT 1;", customer_id);
			ResultSet rs = stmt.executeQuery(sql);

			if (rs.next()) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}

		return false;
	}

	public BankAccountInfo findCustomerAccount(String token) {

		return null;
	}

	public Response deposit(String token, String account, BigDecimal amount) {
		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword()); Statement stmt = connection.createStatement();) {

			String sql = String.format("SELECT customer_id FROM tokens WHERE token='%s' LIMIT 1;", token);
			ResultSet rs = stmt.executeQuery(sql);

			if (!rs.next()) {
				return new Response().add("status", 1);
			}

			int customer_id = rs.getInt("customer_id");

			sql = String.format("SELECT * FROM primary_account WHERE customer_id='%d' LIMIT 1;", customer_id);
			rs = stmt.executeQuery(sql);

			if (!rs.next()) {
				return new Response().add("status", 2);
			}

			BigDecimal balance = rs.getBigDecimal("balance");
			balance = balance.add(amount);

			sql = String.format("UPDATE primary_account SET balance='%f' WHERE customer_id=%d;", balance, customer_id);
			stmt.executeUpdate(sql);

			sql = String.format(
					"INSERT INTO primary_account_transactions(customer_id, name, type, balance_after, amount) VALUES('%d', '%s', '%d', '%f', '%f');",
					customer_id, java.time.LocalDateTime.now().toString().substring(0, 20), 0, balance, amount);
			stmt.executeUpdate(sql);

			return new Response().add("status", 0).add("balance", balance);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

			return new Response().add("status", -1);
		}
	}

	public Response withdraw(String token, String account, BigDecimal amount) {
		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword()); Statement stmt = connection.createStatement();) {

			String sql = String.format("SELECT customer_id FROM tokens WHERE token='%s' LIMIT 1;", token);
			ResultSet rs = stmt.executeQuery(sql);

			if (!rs.next()) {
				return new Response().add("status", 1);
			}

			int customer_id = rs.getInt("customer_id");

			sql = String.format("SELECT * FROM primary_account WHERE customer_id='%d' LIMIT 1;", customer_id);
			rs = stmt.executeQuery(sql);

			if (!rs.next()) {
				return new Response().add("status", 2);
			}

			BigDecimal balance = rs.getBigDecimal("balance");
			if ((balance = balance.subtract(amount)).compareTo(BigDecimal.ZERO) < 0) {
				return new Response().add("status", 3).add("balance", balance);
			}

			sql = String.format("UPDATE primary_account SET balance='%f' WHERE customer_id=%d;", balance, customer_id);
			stmt.executeUpdate(sql);

			sql = String.format(
					"INSERT INTO primary_account_transactions(customer_id, name, type, balance_after, amount) VALUES('%d', '%s', '%d', '%f', '%f');",
					customer_id, java.time.LocalDateTime.now().toString().substring(0, 20), 1, balance, amount);
			stmt.executeUpdate(sql);

			return new Response().add("status", 0).add("balance", balance);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

			return new Response().add("status", -1);
		}
	}

	public Response getBalance(String token, String account) {
		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword()); Statement stmt = connection.createStatement();) {

			String sql = String.format(
					"SELECT * FROM primary_account WHERE customer_id IN (SELECT customer_id FROM tokens WHERE token='%s' LIMIT 1) LIMIT 1;",
					token);
			ResultSet rs = stmt.executeQuery(sql);

			if (!rs.next()) {
				return new Response().add("status", 1);
			}

			BigDecimal balance = rs.getBigDecimal("balance");

			return new Response().add("status", 0).add("balance", balance);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

			return new Response().add("status", -1);
		}
	}

	public Response getTransactions(String token, String account, Date from, Date to) {
		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword()); Statement stmt = connection.createStatement();) {

			String sql = String.format(
					"SELECT * FROM primary_account_transactions WHERE date BETWEEN to_date('%s','YYYY-MM-DD') AND to_date('%s','YYYY-MM-DD');",
					new SimpleDateFormat("yyyy-MM-dd").format(from), new SimpleDateFormat("yyyy-MM-dd").format(to),
					token);
			ResultSet rs = stmt.executeQuery(sql);

			if (!rs.next()) {
				return new Response().add("status", 1);
			}

			Response response = new Response();
			response.add("status", 0);

			do {
				response.add("transaction", rs.getBigDecimal("amount"));
				return response;
			} while (rs.next());

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

			return new Response().add("status", -1);
		}

		// return new Response().add("status", 0);
	}

//	System.out.println(Class.forName("org.postgresql.Driver"));

}
