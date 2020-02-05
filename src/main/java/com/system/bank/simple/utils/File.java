package com.system.bank.simple.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class File {

	public String readFromFile(String path) {
		StringBuilder sb = new StringBuilder();
		String line = null;

		try (BufferedReader br = new BufferedReader(new FileReader(path));) {
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append(System.getProperty("line.separator"));
			}
			sb.deleteCharAt(sb.length() - 1);
		} catch (IOException e) {
			Logger.log(e.getClass().getName() + ": " + e.getMessage());
		}

		return sb.toString();
	}
}
