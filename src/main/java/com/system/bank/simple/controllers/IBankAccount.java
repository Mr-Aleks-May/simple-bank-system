package com.system.bank.simple.controllers;

import java.math.BigDecimal;
import java.util.List;

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

public interface IBankAccount {

	/***
	 * Register new customer (in DB).
	 * 
	 * @param email    - email address.
	 * @param password - password (in form of md5 hash, with length 32).
	 * @throws ClientAlreadyExistsException
	 * @throws InternalServerError
	 */
	void signup(String email, String password) throws ClientAlreadyExistsException, InternalServerError;

	/***
	 * Checking email (login) and password, and if they valid return access token.
	 * 
	 * @param email    - email address.
	 * @param password - password (in form of md5 hash, with length 32).
	 * @return access token.
	 * @throws ClientNotExistsException
	 * @throws WrongPasswordException
	 * @throws InternalServerError
	 */
	String signin(String email, String password)
			throws ClientNotExistsException, WrongPasswordException, InternalServerError;

	/***
	 * Open new account for user.
	 * 
	 * @param token - access token.
	 * @throws TokenNotValidException
	 * @throws AccountNotCreatedException
	 * @throws InternalServerError
	 */
	void openAccountViaEmail(String email, short currency)
			throws ClientNotExistsException, AccountNotCreatedException, InternalServerError;

	/***
	 * Open new account for user.
	 * 
	 * @param token - access token.
	 * @throws TokenNotValidException
	 * @throws AccountNotCreatedException
	 * @throws InternalServerError
	 */
	void openAccountViaToken(String token, short currency)
			throws TokenNotValidException, AccountNotCreatedException, InternalServerError;

	/***
	 * Return all user accounts.
	 * 
	 * @param token - access token.
	 * @return all user account.
	 * @throws TokenNotValidException
	 * @throws InternalServerError
	 */
	List<BankAccountInfo> getBankAccounts(String token) throws TokenNotValidException, InternalServerError;

	/***
	 * Search for Customer Id by token.
	 * 
	 * @param token - access token.
	 * @return Customer Id.
	 * @throws TokenNotValidException
	 * @throws ClientNotExistsException
	 * @throws InternalServerError
	 */
	int findCustomerIdBy(String token) throws TokenNotValidException, InternalServerError;

	/***
	 * Check if customer already register in DB.
	 * 
	 * @param email - email address.
	 * @return if customer exists return true.
	 * @throws ClientNotExistsException
	 * @throws InternalServerError
	 */
	boolean isCustomerExist(String email) throws InternalServerError;

	/***
	 * Check if token is active.
	 * 
	 * @param token - access token.
	 * @return if token active return true.
	 * @throws InternalServerError
	 */
	boolean isTokenActive(String token) throws InternalServerError;

	/***
	 * Check if token already exist for customer by Customer Id.
	 * 
	 * @param customer_id
	 * @return if token already exist for customer return true.
	 * @throws InternalServerError
	 */
	boolean isTokenExistForCustomer(int customer_id) throws InternalServerError;

	/***
	 * Deposit money on customer account.
	 * 
	 * @param token   - access token.
	 * @param account - account id.
	 * @param amount  - amount of money that will be deposited.
	 * @return current account balance (after performing deposit).
	 * @throws TokenNotValidException
	 * @throws AccountNotFoundException
	 * @throws InternalServerError
	 */
	BigDecimal deposit(String token, long account, BigDecimal amount)
			throws TokenNotValidException, AccountNotFoundException, InternalServerError;

	/***
	 * Withdraw money from customer account.
	 * 
	 * @param token   - access token.
	 * @param account - account id.
	 * @param amount  - amount of money that will be withdrawed.
	 * @return current account balance (after performing withdraw).
	 * @throws TokenNotValidException
	 * @throws AccountNotFoundException
	 * @throws InsufficientFundException
	 * @throws InternalServerError
	 */
	BigDecimal withdraw(String token, long account, BigDecimal amount)
			throws TokenNotValidException, AccountNotFoundException, InsufficientFundException, InternalServerError;

	/***
	 * Get balance of customer account by account Id.
	 * 
	 * @param token   - access token.
	 * @param account - account id.
	 * @return cash on balance.
	 * @throws TokenNotValidException
	 * @throws AccountNotFoundException
	 * @throws InternalServerError
	 */
	BigDecimal getBalance(String token, long account)
			throws TokenNotValidException, AccountNotFoundException, InternalServerError;

	/***
	 * Transaction on account by specified time period
	 * 
	 * @param token   - access token.
	 * @param account - account id.
	 * @param from    - date from which you want to watch transaction. (from <= to)
	 * @param to      - date to which you wand to watch transactions.
	 * @return list of transactions.
	 * @throws TokenNotValidException
	 * @throws WrongDateException
	 * @throws InternalServerError
	 */
	List<Transaction> getTransactions(String token, long account, java.util.Date from, java.util.Date to)
			throws TokenNotValidException, WrongDateException, InternalServerError;

	/***
	 * Transaction on account of specified time period
	 * 
	 * @param token   - access token.
	 * @param account - account id.
	 * @param from    - date from which you want to watch transaction. (from <= to)
	 * @param to      - date to which you wand to watch transactions.
	 * @return list of transactions.
	 * @throws TokenNotValidException
	 * @throws WrongDateException
	 * @throws InternalServerError
	 */
	List<Transaction> getTransactions(String token, long account, java.sql.Date from, java.sql.Date to)
			throws TokenNotValidException, WrongDateException, InternalServerError;
}