package com.system.bank.simple.utils;

import java.util.Random;

public class Token {
	private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
	private static final int ALPHABET_LENGTH = ALPHABET.length();
	private static final int TOKEN_LENTH = 32;

	public String generateToken() {
		return generateToken(TOKEN_LENTH);
	}

	public String generateToken(int length) {
		StringBuilder sb = new StringBuilder(length);
		Random rand = new Random();

		for (int i = 0; i < length; i++) {
			int pos = rand.nextInt(ALPHABET_LENGTH);
			sb.append(ALPHABET.charAt(pos));
		}

		return sb.toString();
	}
}
