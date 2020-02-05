package com.system.bank.simple.db.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.system.bank.simple.utils.File;
import com.system.bank.simple.utils.Logger;

public class DBConfig {

	private static DBConfig dbconfig = null;

	private DBConfig() {
		dbconfig = this;
	}

	public static DBConfig create() {
		if (dbconfig == null) {
			new DBConfig();
		}

		return dbconfig;
	}

	public static DBConfig initDB() {
		String querys = new File().readFromFile("./init.sql");

		try (Connection connection = DriverManager.getConnection(DBSettings.getAdrress(), DBSettings.getUser(),
				DBSettings.getPassword());) {

			connection.setAutoCommit(false);

			try (PreparedStatement stmt1 = connection.prepareStatement(querys);) {
				stmt1.executeUpdate();

				connection.commit();
			} catch (SQLException e) {
				connection.rollback();

				Logger.log(e.getClass().getName() + ": " + e.getMessage());
			}
		} catch (SQLException e) {
			Logger.log(e.getClass().getName() + ": " + e.getMessage());
		}

		return dbconfig;
	}

}
