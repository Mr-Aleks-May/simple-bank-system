package com.system.bank.simple.controllers;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.system.bank.simple.db.config.DBSettings;
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
import com.system.bank.simple.utils.Logger;

public class BankAccountController implements IBankAccount {

	@Override
	public void signup(String email, String password) throws ClientAlreadyExistsException, InternalServerError {
		if (isCustomerExist(email)) {
			throw new ClientAlreadyExistsException();
		}

		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword());) {

			connection.setAutoCommit(false);

			try (PreparedStatement stmt1 = connection
					.prepareStatement("INSERT INTO customers(email, password) VALUES(?, ?);");) {
				stmt1.setString(1, email);
				stmt1.setString(2, password);
				stmt1.executeUpdate();

				connection.commit();

			} catch (SQLException e) {
				connection.rollback();

				Logger.log(e.getClass().getName() + ": " + e.getMessage());
				throw new InternalServerError(e.getClass().getName() + ": " + e.getMessage());
			}
		} catch (SQLException e) {
			Logger.log(e.getClass().getName() + ": " + e.getMessage());
			throw new InternalServerError(e.getClass().getName() + ": " + e.getMessage());
		}
	}

	@Override
	public String signin(String email, String password)
			throws ClientNotExistsException, WrongPasswordException, InternalServerError {

		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword());) {

			connection.setAutoCommit(false);

			try (PreparedStatement stmt1 = connection
					.prepareStatement("SELECT id, email, password FROM customers WHERE email=? LIMIT 1;");) {
				stmt1.setString(1, email);

				try (ResultSet rs1 = stmt1.executeQuery();) {
					if (!rs1.next()) {
						throw new ClientNotExistsException();
					}

					int customer_id = rs1.getInt("id");
					String customerPassword = rs1.getString("password");

					if (!customerPassword.equals(password)) {
						throw new WrongPasswordException();
					}

					String token = new com.system.bank.simple.utils.Token().generateToken();

					if (isTokenExistForCustomer(customer_id)) {
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

					return token;
				}
			} catch (SQLException e) {
				connection.rollback();

				Logger.log(e.getClass().getName() + ": " + e.getMessage());
				throw new InternalServerError();
			}

		} catch (SQLException e) {
			Logger.log(e.getClass().getName() + ": " + e.getMessage());
			throw new InternalServerError();
		}
	}

	@Override
	public void openAccountViaEmail(String email, short currency)
			throws ClientNotExistsException, AccountNotCreatedException, InternalServerError {
		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword());) {

			connection.setAutoCommit(false);

			try (PreparedStatement stmt1 = connection
					.prepareStatement("SELECT id FROM customers WHERE email=? LIMIT 1;");) {
				stmt1.setString(1, email);

				try (ResultSet rs1 = stmt1.executeQuery();) {
					if (!rs1.next()) {
						throw new ClientNotExistsException();
					}

					int customer_id = rs1.getInt("id");

					try (PreparedStatement stmt2 = connection.prepareStatement(
							"INSERT INTO accounts(customer_id, currency, balance) VALUES(?, ?, ?);");) {
						stmt2.setInt(1, customer_id);
						stmt2.setShort(2, currency);
						stmt2.setBigDecimal(3, new BigDecimal(0));
						stmt2.executeUpdate();

						connection.commit();
					}
				}
			} catch (SQLException e) {
				connection.rollback();

				Logger.log(e.getClass().getName() + ": " + e.getMessage());
				throw new AccountNotCreatedException(e.getClass().getName() + ": " + e.getMessage());
			}
		} catch (SQLException e) {
			Logger.log(e.getClass().getName() + ": " + e.getMessage());
			throw new InternalServerError(e.getClass().getName() + ": " + e.getMessage());
		}
	}

	@Override
	public void openAccountViaToken(String token, short currency)
			throws TokenNotValidException, AccountNotCreatedException, InternalServerError {
		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword());) {

			connection.setAutoCommit(false);

			try (PreparedStatement stmt1 = connection
					.prepareStatement("SELECT customer_id FROM tokens WHERE token=?  LIMIT 1;");) {
				stmt1.setString(1, token);

				try (ResultSet rs1 = stmt1.executeQuery();) {
					if (!rs1.next()) {
						throw new TokenNotValidException();
					}

					int customer_id = rs1.getInt("id");

					try (PreparedStatement stmt2 = connection.prepareStatement(
							"INSERT INTO accounts(customer_id, currency, balance) VALUES(?, ?, ?);");) {
						stmt2.setInt(1, customer_id);
						stmt2.setShort(2, currency);
						stmt2.setBigDecimal(3, new BigDecimal(0));
						stmt2.executeUpdate();

						connection.commit();
					}
				}
			} catch (SQLException e) {
				connection.rollback();

				Logger.log(e.getClass().getName() + ": " + e.getMessage());
				throw new AccountNotCreatedException(e.getClass().getName() + ": " + e.getMessage());
			}
		} catch (SQLException e) {
			Logger.log(e.getClass().getName() + ": " + e.getMessage());
			throw new InternalServerError(e.getClass().getName() + ": " + e.getMessage());
		}
	}

	@Override
	public List<BankAccountInfo> getBankAccounts(String token) throws TokenNotValidException, InternalServerError {
		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword());) {

			try (PreparedStatement stmt1 = connection
					.prepareStatement("SELECT customer_id FROM tokens WHERE token=? LIMIT 1;");) {
				stmt1.setString(1, token);

				try (ResultSet rs1 = stmt1.executeQuery();) {
					if (!rs1.next()) {
						throw new TokenNotValidException();
					}

					int customerId = rs1.getInt("customer_id");

					try (PreparedStatement stmt2 = connection
							.prepareStatement("SELECT * FROM accounts WHERE customer_id=? LIMIT 1;");) {
						stmt2.setInt(1, customerId);

						try (ResultSet rs2 = stmt2.executeQuery();) {
							List<BankAccountInfo> accounts = new LinkedList<BankAccountInfo>();

							while (rs2.next()) {
								BankAccountInfo account = new BankAccountInfo();

								account.setId(rs2.getInt("id"));
								account.setCustomerId(rs2.getInt("customer_id"));
								account.setCurrency(rs2.getShort("currency"));
								account.setBalance(rs2.getBigDecimal("balance"));

								accounts.add(account);
							}

							return accounts;
						}
					}
				}
			} catch (SQLException e) {
				Logger.log(e.getClass().getName() + ": " + e.getMessage());
				throw new InternalServerError(e.getClass().getName() + ": " + e.getMessage());
			}
		} catch (SQLException e) {
			Logger.log(e.getClass().getName() + ": " + e.getMessage());
			throw new InternalServerError(e.getClass().getName() + ": " + e.getMessage());
		}
	}

	@Override
	public int findCustomerIdBy(String token) throws TokenNotValidException, InternalServerError {
		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword());) {

			try (PreparedStatement stmt1 = connection
					.prepareStatement("SELECT customer_id FROM tokens WHERE token=? LIMIT 1;");) {
				stmt1.setString(1, token);

				try (ResultSet rs1 = stmt1.executeQuery();) {
					if (!rs1.next()) {
						throw new TokenNotValidException();
					}

					int customerId = rs1.getInt("customer_id");

					return customerId;
				}
			} catch (SQLException e) {
				Logger.log(e.getClass().getName() + ": " + e.getMessage());
				throw new InternalServerError(e.getClass().getName() + ": " + e.getMessage());
			}
		} catch (SQLException e) {
			Logger.log(e.getClass().getName() + ": " + e.getMessage());
			throw new InternalServerError(e.getClass().getName() + ": " + e.getMessage());
		}
	}

	@Override
	public boolean isCustomerExist(String email) throws InternalServerError {
		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword());) {

			try (PreparedStatement stmt1 = connection
					.prepareStatement("SELECT email FROM customers WHERE email=? LIMIT 1;");) {
				stmt1.setString(1, email);

				try (ResultSet rs1 = stmt1.executeQuery();) {
					boolean isEntityAlreadyExist = rs1.next();

					if (isEntityAlreadyExist) {
						return true;
					}
				}
			} catch (SQLException e) {
				Logger.log(e.getClass().getName() + ": " + e.getMessage());
				throw new InternalServerError(e.getClass().getName() + ": " + e.getMessage());
			}
		} catch (SQLException e) {
			Logger.log(e.getClass().getName() + ": " + e.getMessage());
			throw new InternalServerError(e.getClass().getName() + ": " + e.getMessage());
		}

		return false;
	}

	@Override
	public boolean isTokenExistForCustomer(int customer_id) throws InternalServerError {
		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword());) {

			try (PreparedStatement stmt1 = connection
					.prepareStatement("SELECT token FROM tokens WHERE customer_id=? LIMIT 1;");) {
				stmt1.setInt(1, customer_id);

				try (ResultSet rs1 = stmt1.executeQuery();) {
					boolean isEntityAlreadyExist = rs1.next();

					if (isEntityAlreadyExist) {
						return true;
					}
				}
			} catch (SQLException e) {
				Logger.log(e.getClass().getName() + ": " + e.getMessage());
				throw new InternalServerError(e.getClass().getName() + ": " + e.getMessage());
			}
		} catch (SQLException e) {
			Logger.log(e.getClass().getName() + ": " + e.getMessage());
			throw new InternalServerError(e.getClass().getName() + ": " + e.getMessage());
		}

		return false;
	}

	@Override
	public boolean isTokenActive(String token) throws InternalServerError {
		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword());) {

			try (PreparedStatement stmt1 = connection
					.prepareStatement("SELECT customer_id FROM tokens WHERE token=? LIMIT 1;");) {
				stmt1.setString(1, token);

				try (ResultSet rs1 = stmt1.executeQuery();) {
					boolean isEntityAlreadyExist = rs1.next();

					if (isEntityAlreadyExist) {
						return true;
					}
				}
			} catch (SQLException e) {
				Logger.log(e.getClass().getName() + ": " + e.getMessage());
				throw new InternalServerError(e.getClass().getName() + ": " + e.getMessage());
			}
		} catch (SQLException e) {
			Logger.log(e.getClass().getName() + ": " + e.getMessage());
			throw new InternalServerError(e.getClass().getName() + ": " + e.getMessage());
		}

		return false;
	}

	@Override
	public BigDecimal deposit(String token, long account, BigDecimal amount)
			throws TokenNotValidException, AccountNotFoundException, InternalServerError {
		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword());) {

			connection.setAutoCommit(false);

			try (PreparedStatement stmt1 = connection
					.prepareStatement("SELECT customer_id FROM tokens WHERE token=? LIMIT 1;");) {
				stmt1.setString(1, token);

				try (ResultSet rs1 = stmt1.executeQuery();) {
					if (!rs1.next()) {
						throw new TokenNotValidException();
					}

					int customer_id = rs1.getInt("customer_id");

					try (PreparedStatement smt2 = connection
							.prepareStatement("SELECT * FROM accounts WHERE customer_id=? AND id=? LIMIT 1;");) {
						smt2.setInt(1, customer_id);
						smt2.setLong(2, account);

						try (ResultSet rs2 = smt2.executeQuery();) {
							if (!rs2.next()) {
								throw new AccountNotFoundException();
							}

							BigDecimal balance = rs2.getBigDecimal("balance");
							balance = balance.add(amount);

							try (PreparedStatement stmt3 = connection
									.prepareStatement("UPDATE accounts SET balance=? WHERE customer_id=?;");) {
								stmt3.setBigDecimal(1, balance);

								stmt3.setInt(2, customer_id);
								stmt3.executeUpdate();

								try (PreparedStatement stmt4 = connection.prepareStatement(
										"INSERT INTO transactions(customer_id, account_id, name, type, currency, balance_after, amount) VALUES(?, ?, ?, ?, ?, ?, ?);");) {
									stmt4.setInt(1, customer_id);
									stmt4.setLong(2, account);
									stmt4.setString(3, "deposit " + java.time.LocalDateTime.now().getNano());
									stmt4.setShort(4, (short)0);
									stmt4.setShort(5, (short)980);
									stmt4.setBigDecimal(6, balance);
									stmt4.setBigDecimal(7, amount);
									stmt4.executeUpdate();

									connection.commit();

									return balance;
								}
							}
						}
					}
				}
			} catch (SQLException e) {
				connection.rollback();

				Logger.log(e.getClass().getName() + ": " + e.getMessage());
				throw new InternalServerError(e.getClass().getName() + ": " + e.getMessage());
			}
		} catch (SQLException e) {
			Logger.log(e.getClass().getName() + ": " + e.getMessage());
			throw new InternalServerError(e.getClass().getName() + ": " + e.getMessage());
		}
	}

	@Override
	public BigDecimal withdraw(String token, long account, BigDecimal amount)
			throws TokenNotValidException, AccountNotFoundException, InsufficientFundException, InternalServerError {
		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword());) {

			connection.setAutoCommit(false);

			try (PreparedStatement stmt1 = connection
					.prepareStatement("SELECT customer_id FROM tokens WHERE token=? LIMIT 1;");) {
				stmt1.setString(1, token);

				try (ResultSet rs1 = stmt1.executeQuery();) {
					if (!rs1.next()) {
						throw new TokenNotValidException();
					}

					int customer_id = rs1.getInt("customer_id");

					try (PreparedStatement smt2 = connection
							.prepareStatement("SELECT * FROM accounts WHERE customer_id=? AND id=? LIMIT 1;");) {
						smt2.setInt(1, customer_id);
						smt2.setLong(2, account);

						try (ResultSet rs2 = smt2.executeQuery();) {
							if (!rs2.next()) {
								throw new AccountNotFoundException();
							}

							BigDecimal balance = rs2.getBigDecimal("balance");
							if ((balance = balance.subtract(amount)).compareTo(BigDecimal.ZERO) < 0) {
								throw new InsufficientFundException(balance.add(amount));
							}

							try (PreparedStatement stmt3 = connection
									.prepareStatement("UPDATE accounts SET balance=? WHERE customer_id=?;");) {
								stmt3.setBigDecimal(1, balance);

								stmt3.setInt(2, customer_id);
								stmt3.executeUpdate();

								try (PreparedStatement stmt4 = connection.prepareStatement(
										"INSERT INTO transactions(customer_id, account_id, name, type, currency, balance_after, amount) VALUES(?, ?, ?, ?, ?, ?, ?);");) {
									stmt4.setInt(1, customer_id);
									stmt4.setLong(2, account);
									stmt4.setString(3, "withdraw " + java.time.LocalDateTime.now().getNano());
									stmt4.setShort(4, (short)1);
									stmt4.setShort(5, (short)980);
									stmt4.setBigDecimal(6, balance);
									stmt4.setBigDecimal(7, amount);
									stmt4.executeUpdate();

									connection.commit();

									return balance;
								}
							}
						}
					}
				}
			} catch (SQLException e) {
				connection.rollback();

				Logger.log(e.getClass().getName() + ": " + e.getMessage());
				throw new InternalServerError(e.getClass().getName() + ": " + e.getMessage());
			}
		} catch (SQLException e) {
			Logger.log(e.getClass().getName() + ": " + e.getMessage());
			throw new InternalServerError(e.getClass().getName() + ": " + e.getMessage());
		}
	}

	@Override
	public BigDecimal getBalance(String token, long account)
			throws TokenNotValidException, AccountNotFoundException, InternalServerError {
		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword());) {

			try (PreparedStatement stmt1 = connection
					.prepareStatement("SELECT customer_id FROM tokens WHERE token=? LIMIT 1;");) {
				stmt1.setString(1, token);

				try (ResultSet rs1 = stmt1.executeQuery();) {
					if (!rs1.next()) {
						throw new TokenNotValidException();
					}

					int customer_id = rs1.getInt("customer_id");

					try (PreparedStatement stmt2 = connection
							.prepareStatement("SELECT * FROM accounts WHERE customer_id=? AND id=? LIMIT 1;");) {
						stmt2.setInt(1, customer_id);
						stmt2.setLong(2, account);

						try (ResultSet rs2 = stmt2.executeQuery();) {
							if (!rs2.next()) {
								throw new AccountNotFoundException("Account not found or token not valid.");
							}

							BigDecimal balance = rs2.getBigDecimal("balance");

							return balance;
						}
					}
				}
			} catch (SQLException e) {
				Logger.log(e.getClass().getName() + ": " + e.getMessage());
				throw new InternalServerError(e.getClass().getName() + ": " + e.getMessage());
			}
		} catch (SQLException e) {
			Logger.log(e.getClass().getName() + ": " + e.getMessage());
			throw new InternalServerError(e.getClass().getName() + ": " + e.getMessage());
		}
	}

	@Override
	public List<Transaction> getTransactions(String token, long account, Date from, Date to)
			throws TokenNotValidException, WrongDateException, InternalServerError {
		return getTransactions(token, account, new java.sql.Date(from.getTime()), new java.sql.Date(to.getTime()));
	}

///////////////////////|///////////////////////////
	@Override
	public List<Transaction> getTransactions(String token, long account, java.sql.Date from, java.sql.Date to)
			throws TokenNotValidException, WrongDateException, InternalServerError {
		if (!to.after(from) && !to.equals(from)) {
			throw new WrongDateException();
		}

		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword());) {

			try (PreparedStatement stmt1 = connection
					.prepareStatement("SELECT customer_id FROM tokens WHERE token=? LIMIT 1;");) {
				stmt1.setString(1, token);

				try (ResultSet rs1 = stmt1.executeQuery();) {
					if (!rs1.next()) {
						throw new TokenNotValidException();
					}

					int customer_id = rs1.getInt("customer_id");

					try (PreparedStatement stmt2 = connection.prepareStatement(
							"SELECT * FROM transactions WHERE customer_id=? AND  date BETWEEN ? AND ?;");) {
						stmt2.setInt(1, customer_id);
						stmt2.setDate(2, from);
						stmt2.setDate(3, to);

						try (ResultSet rs2 = stmt2.executeQuery();) {
							List<Transaction> transactions = new LinkedList<Transaction>();

							while (rs2.next()) {
								Transaction t = new Transaction();

								t.setId(rs2.getInt("id"));
								t.setCustomerId(rs2.getInt("customer_id"));
								t.setAccountId(rs2.getLong("account_id"));
								t.setName(rs2.getString("name"));
								t.setType(rs2.getShort("type"));
								t.setCurrency(rs2.getShort("currency"));
								t.setBalanceAfter(rs2.getBigDecimal("balance_after"));
								t.setAmount(rs2.getBigDecimal("amount"));
								t.setDate(rs2.getDate("date"));
								t.setDescription(rs2.getString("description"));
								t.setLocation(rs2.getString("location"));

								transactions.add(t);
							}

							return transactions;
						}
					}
				} catch (SQLException e) {
					Logger.log(e.getClass().getName() + ": " + e.getMessage());
					throw new InternalServerError(e.getClass().getName() + ": " + e.getMessage());
				}
			}
		} catch (SQLException e) {
			Logger.log(e.getClass().getName() + ": " + e.getMessage());
			throw new InternalServerError(e.getClass().getName() + ": " + e.getMessage());
		}
	}
}
