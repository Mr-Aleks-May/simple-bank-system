package com.bank.system.controllers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validations {

	public boolean isValidEmail(String email) {
		String emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
				+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
		Pattern pattern = Pattern.compile(emailPattern);

		Matcher matcher = pattern.matcher(email);

		return matcher.matches();
	}

	public boolean isValidNumber(String number) {
		String numberPattern = "^(\\d+)$";

		Pattern pattern = Pattern.compile(numberPattern);
		Matcher matcher = pattern.matcher(number);

		return matcher.matches();
	}

	public boolean isValidDecimalNumber(String number) {
		String numberPattern = "^(\\d+.\\d+)$";

		Pattern pattern = Pattern.compile(numberPattern);
		Matcher matcher = pattern.matcher(number);

		return matcher.matches();
	}
}
