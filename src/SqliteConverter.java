/* 
 * SQLite Converter
 *
 * Copyright 2011 Wappworks Studios
 */


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;



@SuppressWarnings({ "rawtypes", "unchecked" })
public class SqliteConverter {

	public static final int XML = 1;
	public static final int JSON = 2;

	private LogWriter logWriter;
	private Connection dbConn = null;

	private Vector tableHandlers;

	public SqliteConverter(LogWriter inLogWriter) {
		logWriter = inLogWriter;
	}

	public Boolean init(File dbFile) {
		logWriter.logAppend("Sqlite database load commencing");

		try {
			Class.forName("org.sqlite.JDBC");
			dbConn = DriverManager.getConnection("jdbc:sqlite:"
					+ dbFile.getPath());

			tableHandlers = processSchema();
		} catch (ClassNotFoundException e) {
			logWriter
					.logAppend(String
							.format("Class not found exception occurred while initializing converter:\n%1$s",
									e.toString()));
			return (false);
		} catch (SQLException e) {
			logWriter
					.logAppend(String
							.format("Class not found exception occurred while initializing converter:\n%1$s",
									e.toString()));
			return (false);
		}

		logWriter.logAppend("Sqlite database load complete");

		return (true);
	}

	public Boolean deInit() {
		try {
			if (dbConn != null)
				dbConn.close();
		} catch (SQLException e) {
			return (false);
		}

		dbConn = null;
		tableHandlers = null;

		logWriter.logAppend("Sqlite database closed");

		return (true);
	}

	private Vector processSchema() throws SQLException {
		Vector tableHandlers = new Vector();

		// Get the table names
		Statement dbStat = dbConn.createStatement();
		ResultSet rs = dbStat
				.executeQuery("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;");
		try {
			while (rs.next()) {
				String tableName = rs.getString("name");
				logWriter.logAppend(String.format(
						"Processing schema for table '%1$s'", tableName));
				SqliteTableHandler tableHandler = new SqliteTableHandler(
						tableName, dbConn, logWriter);
				tableHandlers.add(tableHandler);
			}
		} finally {
			rs.close();
		}

		return (tableHandlers);
	}

	public Boolean export(File outFile, int format) {
		return (export(outFile, format, new SqliteConverterExportConfig()));
	}

	public final Boolean export$$Base(File outFile, int format,
			SqliteConverterExportConfig config) {
		return true;
	}


	private Boolean exportToJson(File jsonFile,
			SqliteConverterExportConfig config) {
		logWriter.logAppend("JSON export commencing");

		// Handle the export...
		try {
			FileWriter fStream = new FileWriter(jsonFile);
			BufferedWriter fOut = new BufferedWriter(fStream);
			JSONObject jsonOut = new JSONObject();

			exportTablesToJson(jsonOut, config);
			fOut.write(jsonOut.toString(2));

			fOut.close();
			fStream.close();
		} catch (IOException e) {
			logWriter
					.logAppend(String
							.format("I/O error encountered trying to output database to file '%1$s'",
									jsonFile.getAbsolutePath()));
			return (false);
		} catch (JSONException e) {
			logWriter.logAppend(String.format(
					"Error encoding database file '%1$s' into JSON",
					jsonFile.getAbsolutePath()));
		}
		logWriter.logAppend("JSON export complete");
		return (true);
	}

	private void exportTablesToJson(JSONObject jsonOut,
			SqliteConverterExportConfig config) {
		for (int i = 0; i < tableHandlers.size(); i++)
		// for( Object tableHandler : tableHandlers )
		{
			Object tableHandler = tableHandlers.elementAt(i);
			try {
				logWriter.logAppend(String.format(
						"Exporting contents for table '%1$s'",
						((SqliteTableHandler) tableHandler).getName()));
				((SqliteTableHandler) tableHandler).exportTableToJson(jsonOut,
						config);
			} catch (SQLException e) {
				logWriter.logAppend(String.format(
						"SQL exception occured trying to export table '%1$s'",
						((SqliteTableHandler) tableHandler).getName()));
			}
		}
	}

	public Boolean export(File outFile, int format,
			SqliteConverterExportConfig config) {
		export$$Base(outFile,format,config);
		if (format == JSON)
			return (exportToJson(outFile, config));
		else
			return false;
	}

}