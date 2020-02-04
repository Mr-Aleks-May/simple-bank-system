package com.system.bank.simple.utils;

public class Logger {

	private static Object lock = new Object();

	public static void log(String message) {
		logToConsole(message);
	}

	public static void logToConsole(String message) {
		synchronized (lock) {
			System.err.println(message);
		}
	}
}
