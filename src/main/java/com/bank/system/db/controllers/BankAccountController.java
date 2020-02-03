package com.bank.system.db.controllers;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
			return new Response().add("status", 200);
		}

		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword());) {

			PreparedStatement stmt = null;

			stmt = connection.prepareStatement("INSERT INTO customers(email, password) VALUES(?, ?);");
			stmt.setString(1, email);
			stmt.setString(2, password);
			stmt.executeUpdate();

			stmt.close();
			stmt = connection.prepareStatement("SELECT id FROM customers WHERE email=? LIMIT 1;");
			stmt.setString(1, email);
			ResultSet rs = stmt.executeQuery();

			if (!rs.next()) {
				return new Response().add("status", 201);
			}

			int customer_id = rs.getInt("id");

			stmt.close();
			stmt = connection
					.prepareStatement("INSERT INTO primary_account(customer_id, balance, currency) VALUES(?, ?, ?);");
			stmt.setInt(1, customer_id);
			stmt.setBigDecimal(2, new BigDecimal(0));
			stmt.setInt(3, 980);
			stmt.executeUpdate();
			stmt.close();

			return new Response().add("status", 0);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

			return new Response().add("status", -1);
		}
	}

	public Response signin(String email, String password) {
		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword());) {

			PreparedStatement stmt = null;

			stmt = connection.prepareStatement("SELECT id, email, password FROM customers WHERE email=? LIMIT 1;");
			stmt.setString(1, email);
			ResultSet rs = stmt.executeQuery();

			if (!rs.next()) {
				return new Response().add("status", 200);
			}

			int customer_id = rs.getInt("id");
			String customerPassword = rs.getString("password");

			if (!customerPassword.equals(password)) {
				return new Response().add("status", 201);
			}

			String token = new com.bank.system.controllers.Token().generateToken();
			if (isTokenExist(customer_id)) {
				stmt.close();
				stmt = connection.prepareStatement("UPDATE tokens set token=? WHERE customer_id=?;");
				stmt.setInt(2, customer_id);
				stmt.setString(1, token);
				stmt.executeUpdate();
				stmt.close();
			} else {
				stmt.close();
				stmt = connection.prepareStatement("INSERT INTO tokens(customer_id, token) VALUES(?, ?);");
				stmt.setInt(1, customer_id);
				stmt.setString(2, token);
				stmt.executeUpdate();
				stmt.close();
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
				DBSettings.getPassword());) {

			PreparedStatement stmt = null;

			stmt = connection.prepareStatement("SELECT email FROM customers WHERE email=? LIMIT 1;");
			stmt.setString(1, email);
			ResultSet rs = stmt.executeQuery();

			boolean isEntityAlreadyExist = rs.next();

			stmt.close();

			if (isEntityAlreadyExist) {
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
				DBSettings.getPassword());) {

			PreparedStatement stmt = null;

			stmt = connection.prepareStatement("SELECT customer_id FROM tokens WHERE customer_id=? LIMIT 1;");
			stmt.setInt(1, customer_id);
			ResultSet rs = stmt.executeQuery();

			boolean isEntityAlreadyExist = rs.next();

			stmt.close();

			if (isEntityAlreadyExist) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}

		return false;
	}

	public int findCustomerAccountIdBy(String token) {
		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword());) {

			PreparedStatement stmt = null;

			stmt = connection.prepareStatement("SELECT customer_id FROM tokens WHERE token=? LIMIT 1;");
			stmt.setString(1, token);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				int customerId = rs.getInt("customer_id");
				stmt.close();

				return customerId;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}

		return -1;
	}

	public Response deposit(String token, String account, BigDecimal amount) {
		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword());) {

			PreparedStatement stmt = null;

			stmt = connection.prepareStatement("SELECT customer_id FROM tokens WHERE token=? LIMIT 1;");
			stmt.setString(1, token);
			ResultSet rs = stmt.executeQuery();

			if (!rs.next()) {
				return new Response().add("status", 200);
			}

			int customer_id = rs.getInt("customer_id");

			stmt.close();
			stmt = connection.prepareStatement("SELECT * FROM primary_account WHERE customer_id=? LIMIT 1;");
			stmt.setInt(1, customer_id);
			rs = stmt.executeQuery();

			if (!rs.next()) {
				return new Response().add("status", 201);
			}

			BigDecimal balance = rs.getBigDecimal("balance");
			balance = balance.add(amount);

			stmt.close();
			stmt = connection.prepareStatement("UPDATE primary_account SET balance=? WHERE customer_id=?;");
			stmt.setBigDecimal(1, balance);
			stmt.setInt(2, customer_id);
			stmt.executeUpdate();

			stmt.close();
			stmt = connection.prepareStatement(
					"INSERT INTO primary_account_transactions(customer_id, name, type, balance_after, amount) VALUES(?, ?, ?, ?, ?);");
			stmt.setInt(1, customer_id);
			stmt.setString(2, "deposit " + java.time.LocalDateTime.now().getNano());
			stmt.setInt(3, 0);
			stmt.setBigDecimal(4, balance);
			stmt.setBigDecimal(5, amount);
			stmt.executeUpdate();

			stmt.close();

			return new Response().add("status", 0).add("balance", balance);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

			return new Response().add("status", -1);
		}
	}

	public Response withdraw(String token, String account, BigDecimal amount) {
		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword());) {

			PreparedStatement stmt = null;

			stmt = connection.prepareStatement("SELECT customer_id FROM tokens WHERE token=? LIMIT 1;");
			stmt.setString(1, token);
			ResultSet rs = stmt.executeQuery();

			if (!rs.next()) {
				return new Response().add("status", 200);
			}

			int customer_id = rs.getInt("customer_id");

			stmt.close();
			stmt = connection.prepareStatement("SELECT * FROM primary_account WHERE customer_id=? LIMIT 1;");
			stmt.setInt(1, customer_id);
			rs = stmt.executeQuery();

			if (!rs.next()) {
				return new Response().add("status", 201);
			}

			BigDecimal balance = rs.getBigDecimal("balance");
			if ((balance = balance.subtract(amount)).compareTo(BigDecimal.ZERO) < 0) {
				return new Response().add("status", 202).add("balance", balance);
			}

			stmt.close();
			stmt = connection.prepareStatement("UPDATE primary_account SET balance=? WHERE customer_id=?;");
			stmt.setBigDecimal(1, balance);
			stmt.setInt(2, customer_id);
			stmt.executeUpdate();

			stmt.close();
			stmt = connection.prepareStatement(
					"INSERT INTO primary_account_transactions(customer_id, name, type, balance_after, amount) VALUES(?, ?, ?, ?, ?);");
			stmt.setInt(1, customer_id);
			stmt.setString(2, "deposit " + java.time.LocalDateTime.now().getNano());
			stmt.setInt(3, 0);
			stmt.setBigDecimal(4, balance);
			stmt.setBigDecimal(5, amount);
			stmt.executeUpdate();

			stmt.close();

			return new Response().add("status", 0).add("balance", balance);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

			return new Response().add("status", -1);
		}
	}

	public Response getBalance(String token, String account) {
		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword());) {

			PreparedStatement stmt = null;

			stmt = connection.prepareStatement(
					"SELECT * FROM primary_account WHERE customer_id IN (SELECT customer_id FROM tokens WHERE token=? LIMIT 1) LIMIT 1;");
			stmt.setString(1, token);
			ResultSet rs = stmt.executeQuery();

			if (!rs.next()) {
				return new Response().add("status", 200);
			}

			BigDecimal balance = rs.getBigDecimal("balance");

			stmt.close();

			return new Response().add("status", 0).add("balance", balance);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

			return new Response().add("status", -1);
		}
	}

	public Response getTransactions(String token, String account, java.util.Date from, java.util.Date to) {
		return getTransactions(token, account, new java.sql.Date(from.getTime()), new java.sql.Date(to.getTime()));
	}

	public Response getTransactions(String token, String account, java.sql.Date from, java.sql.Date to) {
		if (!to.after(from) && !to.equals(from)) {
			return new Response().add("status", 200);
		}

		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword());) {

			PreparedStatement stmt = null;

			stmt = connection.prepareStatement(
					"SELECT * FROM primary_account_transactions WHERE customer_id IN (SELECT customer_id FROM tokens WHERE token=? LIMIT 1) AND  date BETWEEN ? AND ?;");
			stmt.setString(1, token);
			stmt.setDate(2, from);
			stmt.setDate(3, to);
			ResultSet rs = stmt.executeQuery();

			Response response = new Response().add("transaction", rs).add("status", 0);

			stmt.close();

			return response;
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

			return new Response().add("status", -1);
		}
	}
}
