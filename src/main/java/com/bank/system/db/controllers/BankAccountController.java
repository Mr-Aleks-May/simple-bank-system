package com.bank.system.db.controllers;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.bank.system.controllers.Logger;
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
	 * @return Return: 0 - if successfuly register, 200 - if customer already exist.
	 *         In case of error return -1.
	 */
	public Response signup(String email, String password) {
		if (isCustomerExist(email)) {
			return new Response().add("status", 200);
		}

		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword());) {

			connection.setAutoCommit(false);

			try (PreparedStatement stmt1 = connection
					.prepareStatement("INSERT INTO customers(email, password) VALUES(?, ?);");) {
				stmt1.setString(1, email);
				stmt1.setString(2, password);
				stmt1.executeUpdate();

				try (PreparedStatement stmt2 = connection
						.prepareStatement("SELECT id FROM customers WHERE email=? LIMIT 1;");) {
					stmt2.setString(1, email);
					ResultSet rs = stmt2.executeQuery();

					if (!rs.next()) {
						return new Response().add("status", 201);
					}

					int customer_id = rs.getInt("id");

					try (PreparedStatement stmt3 = connection.prepareStatement(
							"INSERT INTO primary_account(customer_id, balance, currency) VALUES(?, ?, ?);");) {
						stmt3.setInt(1, customer_id);
						stmt3.setBigDecimal(2, new BigDecimal(0));
						stmt3.setInt(3, 980);
						stmt3.executeUpdate();

						connection.commit();

						return new Response().add("status", 0);
					}
				}
			} catch (SQLException e) {
				connection.rollback();
				Logger.log(e.getClass().getName() + ": " + e.getMessage());

				return new Response().add("status", -1);
			}
		} catch (SQLException e) {
			Logger.log(e.getClass().getName() + ": " + e.getMessage());

			return new Response().add("status", -1);
		}

	}

	public Response signin(String email, String password) {
		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword());) {

			connection.setAutoCommit(false);

			try (PreparedStatement stmt1 = connection
					.prepareStatement("SELECT id, email, password FROM customers WHERE email=? LIMIT 1;");) {
				stmt1.setString(1, email);
				ResultSet rs = stmt1.executeQuery();

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
					try (PreparedStatement stmt2 = connection
							.prepareStatement("UPDATE tokens set token=? WHERE customer_id=?;");) {
						stmt2.setInt(2, customer_id);
						stmt2.setString(1, token);
						stmt2.executeUpdate();
					}
				} else {
					try (PreparedStatement stmt2 = connection
							.prepareStatement("INSERT INTO tokens(customer_id, token) VALUES(?, ?);");) {
						stmt2.setInt(1, customer_id);
						stmt2.setString(2, token);
						stmt2.executeUpdate();
					}
				}

				connection.commit();

				return new Response().add("status", 0).add("token", token);
			} catch (SQLException e) {
				connection.rollback();
				Logger.log(e.getClass().getName() + ": " + e.getMessage());

				return new Response().add("status", -1);
			}

		} catch (SQLException e) {
			Logger.log(e.getClass().getName() + ": " + e.getMessage());

			return new Response().add("status", -1);
		}
	}

	private boolean isCustomerExist(String email) {
		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword());) {

			connection.setAutoCommit(false);

			try (PreparedStatement stmt1 = connection
					.prepareStatement("SELECT email FROM customers WHERE email=? LIMIT 1;");) {
				stmt1.setString(1, email);
				ResultSet rs = stmt1.executeQuery();

				boolean isEntityAlreadyExist = rs.next();

				connection.commit();

				if (isEntityAlreadyExist) {
					return true;
				}
			} catch (SQLException e) {
				connection.rollback();
				Logger.log(e.getClass().getName() + ": " + e.getMessage());
			}
		} catch (SQLException e) {
			Logger.log(e.getClass().getName() + ": " + e.getMessage());
		}

		return false;
	}

	private boolean isTokenExist(int customer_id) {
		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword());) {

			connection.setAutoCommit(false);

			try (PreparedStatement stmt1 = connection
					.prepareStatement("SELECT customer_id FROM tokens WHERE customer_id=? LIMIT 1;");) {
				stmt1.setInt(1, customer_id);
				ResultSet rs = stmt1.executeQuery();

				boolean isEntityAlreadyExist = rs.next();

				connection.commit();

				if (isEntityAlreadyExist) {
					return true;
				}
			} catch (SQLException e) {
				connection.rollback();
				Logger.log(e.getClass().getName() + ": " + e.getMessage());
			}
		} catch (SQLException e) {
			Logger.log(e.getClass().getName() + ": " + e.getMessage());
		}

		return false;
	}

	public int findCustomerAccountIdBy(String token) {
		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword());) {

			connection.setAutoCommit(false);

			try (PreparedStatement stmt1 = connection
					.prepareStatement("SELECT customer_id FROM tokens WHERE token=? LIMIT 1;");) {
				stmt1.setString(1, token);
				ResultSet rs = stmt1.executeQuery();

				if (rs.next()) {
					int customerId = rs.getInt("customer_id");
					stmt1.close();

					connection.commit();

					return customerId;
				}
			} catch (SQLException e) {
				connection.rollback();
				Logger.log(e.getClass().getName() + ": " + e.getMessage());
			}
		} catch (SQLException e) {
			Logger.log(e.getClass().getName() + ": " + e.getMessage());
		}

		return -1;
	}

	public Response deposit(String token, String account, BigDecimal amount) {
		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword());) {

			connection.setAutoCommit(false);

			try (PreparedStatement stmt1 = connection
					.prepareStatement("SELECT customer_id FROM tokens WHERE token=? LIMIT 1;");) {
				stmt1.setString(1, token);
				ResultSet rs = stmt1.executeQuery();

				if (!rs.next()) {
					return new Response().add("status", 200);
				}

				int customer_id = rs.getInt("customer_id");

				try (PreparedStatement smt2 = connection
						.prepareStatement("SELECT * FROM primary_account WHERE customer_id=? LIMIT 1;");) {
					smt2.setInt(1, customer_id);

					rs = smt2.executeQuery();

					if (!rs.next()) {
						return new Response().add("status", 201);
					}

					BigDecimal balance = rs.getBigDecimal("balance");
					balance = balance.add(amount);

					try (PreparedStatement stmt3 = connection
							.prepareStatement("UPDATE primary_account SET balance=? WHERE customer_id=?;");) {
						stmt3.setBigDecimal(1, balance);

						stmt3.setInt(2, customer_id);
						stmt3.executeUpdate();

						try (PreparedStatement stmt4 = connection.prepareStatement(
								"INSERT INTO primary_account_transactions(customer_id, name, type, balance_after, amount) VALUES(?, ?, ?, ?, ?);");) {
							stmt4.setInt(1, customer_id);
							stmt4.setString(2, "deposit " + java.time.LocalDateTime.now().getNano());
							stmt4.setInt(3, 0);
							stmt4.setBigDecimal(4, balance);
							stmt4.setBigDecimal(5, amount);
							stmt4.executeUpdate();

							connection.commit();

							return new Response().add("status", 0).add("balance", balance);
						}
					}
				}
			} catch (SQLException e) {
				connection.rollback();
				Logger.log(e.getClass().getName() + ": " + e.getMessage());

				return new Response().add("status", -1);
			}
		} catch (SQLException e) {
			Logger.log(e.getClass().getName() + ": " + e.getMessage());

			return new Response().add("status", -1);
		}
	}

	public Response withdraw(String token, String account, BigDecimal amount) {
		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword());) {

			connection.setAutoCommit(false);

			try (PreparedStatement stmt1 = connection
					.prepareStatement("SELECT customer_id FROM tokens WHERE token=? LIMIT 1;");) {
				stmt1.setString(1, token);
				ResultSet rs = stmt1.executeQuery();

				if (!rs.next()) {
					return new Response().add("status", 200);
				}

				int customer_id = rs.getInt("customer_id");

				try (PreparedStatement smt2 = connection
						.prepareStatement("SELECT * FROM primary_account WHERE customer_id=? LIMIT 1;");) {
					smt2.setInt(1, customer_id);

					rs = smt2.executeQuery();

					if (!rs.next()) {
						return new Response().add("status", 201);
					}

					BigDecimal balance = rs.getBigDecimal("balance");
					if ((balance = balance.subtract(amount)).compareTo(BigDecimal.ZERO) < 0) {
						return new Response().add("status", 202).add("balance", balance.add(amount));
					}

					try (PreparedStatement stmt3 = connection
							.prepareStatement("UPDATE primary_account SET balance=? WHERE customer_id=?;");) {
						stmt3.setBigDecimal(1, balance);

						stmt3.setInt(2, customer_id);
						stmt3.executeUpdate();

						try (PreparedStatement stmt4 = connection.prepareStatement(
								"INSERT INTO primary_account_transactions(customer_id, name, type, balance_after, amount) VALUES(?, ?, ?, ?, ?);");) {
							stmt4.setInt(1, customer_id);
							stmt4.setString(2, "withdraw " + java.time.LocalDateTime.now().getNano());
							stmt4.setInt(3, 1);
							stmt4.setBigDecimal(4, balance);
							stmt4.setBigDecimal(5, amount);
							stmt4.executeUpdate();

							connection.commit();

							return new Response().add("status", 0).add("balance", balance);
						}
					}
				}
			} catch (SQLException e) {
				connection.rollback();
				Logger.log(e.getClass().getName() + ": " + e.getMessage());

				return new Response().add("status", -1);
			}
		} catch (Exception e) {
			Logger.log(e.getClass().getName() + ": " + e.getMessage());

			return new Response().add("status", -1);
		}
	}

	public Response getBalance(String token, String account) {
		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword());) {

			connection.setAutoCommit(false);

			try (PreparedStatement stmt1 = connection.prepareStatement(
					"SELECT * FROM primary_account WHERE customer_id IN (SELECT customer_id FROM tokens WHERE token=? LIMIT 1) LIMIT 1;");) {

				stmt1.setString(1, token);
				ResultSet rs = stmt1.executeQuery();

				if (!rs.next()) {
					return new Response().add("status", 200);
				}

				BigDecimal balance = rs.getBigDecimal("balance");

				connection.commit();

				return new Response().add("status", 0).add("balance", balance);
			} catch (SQLException e) {
				connection.rollback();
				Logger.log(e.getClass().getName() + ": " + e.getMessage());

				return new Response().add("status", -1);
			}
		} catch (Exception e) {
			Logger.log(e.getClass().getName() + ": " + e.getMessage());

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

			connection.setAutoCommit(false);

			try (PreparedStatement stmt1 = connection.prepareStatement(
					"SELECT * FROM primary_account_transactions WHERE customer_id IN (SELECT customer_id FROM tokens WHERE token=? LIMIT 1) AND  date BETWEEN ? AND ?;");) {
				stmt1.setString(1, token);
				stmt1.setDate(2, from);
				stmt1.setDate(3, to);
				ResultSet rs = stmt1.executeQuery();

				connection.commit();

				return new Response().add("transaction", rs).add("status", 0);
			} catch (SQLException e) {
				connection.rollback();
				Logger.log(e.getClass().getName() + ": " + e.getMessage());

				return new Response().add("status", -1);
			}
		} catch (Exception e) {
			Logger.log(e.getClass().getName() + ": " + e.getMessage());

			return new Response().add("status", -1);
		}
	}
}
