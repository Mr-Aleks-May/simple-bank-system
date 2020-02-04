package com.system.bank.simple.db.config;

public class DBSettings {
	public static String adrress = "jdbc:postgresql://localhost:5432/bank";
	public static String user = "postgres";
	public static String password = "qwerty1";

	public static String getAdrress() {
		return adrress;
	}

	public static String getUser() {
		return user;
	}

	public static String getPassword() {
		return password;
	}
}
