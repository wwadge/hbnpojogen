package com.felees.hbnpojogen;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.jvnet.inflector.Noun;

import com.felees.hbnpojogen.db.FieldObj;
import com.felees.hbnpojogen.obj.Clazz;
import com.felees.hbnpojogen.obj.PropertyObj;


/**
 * A variety of Helper methods
 *
 * @author wallacew
 *
 */
public class SyncUtils
implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1159482971877496597L;
	/** Constant */
	private static final String LIB = "lib";
	/** Constant */
	private static final String SRC = "SRC/";
	/** Constant */
	private static final String RESOURCES = "RESOURCES/";
	/** Constant */
	private static final String TESTRESOURCES = "TESTRESOURCES/";
	/** Constant */
	private static final String TEST = "TEST/";
	/** Constant */
	private static final String LIBRARIES = "LIBRARIES/";
	/** Constant */
	private static final String TOPLEVEL = "toplevel";
	/** Constant */
	private static final String PROJECTNAME = "PROJECTNAME";
	/** Constant */
	private static final String FKCOLUMN_NAME = "FKCOLUMN_NAME";
	/** Constant */
	private static final String TABLE_CAT = "TABLE_CAT";
	private static final String TABLE_SCHEM = "TABLE_SCHEM";
	/** Internal constant */
	private static final String JDBC_TABLE = "TABLE";
	/** Internal constant */
	private static final String JDBC_PKTABLE_NAME = "PKTABLE_NAME";
	/** Internal constant */
	private static final String JDBC_PKTABLE_CAT = "PKTABLE_CAT";
	/** Internal constant */
	private static final String JDBC_PKTABLE_SCHEM = "PKTABLE_SCHEM";
	/** Internal constant */
	private static final String JDBC_TABLE_NAME = "TABLE_NAME";
	/** For increased speed */
	private static ConcurrentHashMap<String, CommitResults> cache = new ConcurrentHashMap<String, CommitResults>();
	/** Found views. */
	private static Set<String> viewSet = new TreeSet<String>();




	/**
	 * Stupid MySql workaround (mysql is inconsistent in case handling)
	 *
	 * @param dbmd connection
	 * @param cat catalog
	 * @param tableName tableName
	 * @return the proper case of the table
	 * @throws SQLException
	 */
	private static String getTableNameInProperCase(final java.sql.DatabaseMetaData dbmd, String cat, String schema, String tableName)
	throws SQLException {
		ResultSet rs2 = dbmd.getTables(cat, schema, tableName, new String[] { JDBC_TABLE, "VIEW" });
		String result = "";
		// Get the table name
		if (rs2.next()) {
			  String catalog = rs2.getString(TABLE_CAT) == null ? cat : rs2.getString(TABLE_CAT);  //STANIMIR
			  String rs2Schema = rs2.getString(TABLE_SCHEM);
			  result = catalog + "." + (rs2Schema != null ? rs2Schema+"." : "") +rs2.getString(JDBC_TABLE_NAME);

		}
		rs2.close();
		return result;
	}





	/**
	 * Parses a DB and returns a commit/delete order.
	 *
	 * @param dbmd
	 * @param dbCatalogs
	 * @param singleSchema
	 * @param greedy
	 * @return CommitResults
	 * @throws SQLException
	 */
	public static CommitResults getCommitOrder(final java.sql.DatabaseMetaData dbmd, final TreeSet<String> dbCatalogs, final boolean singleSchema,
			final boolean greedy)
	throws SQLException {
		// This method can be improved (read: "should be shot in the head")

		CommitResults result = null;
		String checkTable;
		int tblCount = 0;

		if (singleSchema) {
			result = cache.get(dbCatalogs.iterator().next());
			if (result != null) {
				return result;
			}
		}


		result = new CommitResults();
		LinkedList<String> tables = new LinkedList<String>();
		TreeSet<String> parsedTables = new TreeSet<String>(new CaseInsensitiveComparator());

		TreeSet<String> tableDependencies = null;
		TreeMap<String, Boolean> tableDepsWithPossibleCycles = null;
		TreeSet<String> catalogs = new TreeSet<String>(new CaseInsensitiveComparator());

		catalogs.addAll(dbCatalogs);

		// Specify the type of object; in this case we want tables
		for (String dbCat : dbCatalogs) {
			ResultSet resultSet = dbmd.getTables(getTableCatalog(dbCat), getTableName(dbCat), "%", new String[] { JDBC_TABLE });

			// Get the table name
			while (resultSet.next()) {
				  String catalog = resultSet.getString(TABLE_CAT) == null ? dbCat : resultSet.getString(TABLE_CAT); //STANIMIR
				  tables.add(catalog + "." + resultSet.getString(JDBC_TABLE_NAME));

			}
		}

		for (String dbCat : dbCatalogs) {
			// fetch the views
			ResultSet resultSet = dbmd.getTables(getTableCatalog(dbCat), getTableName(dbCat), "%", new String[] { "VIEW" });

			// Get the table name
			while (resultSet.next()) {
				   String catalog = resultSet.getString(TABLE_CAT) == null ? dbCat : resultSet.getString(TABLE_CAT);  //STANIMIR
				   String name = catalog + "." + resultSet.getString(JDBC_TABLE_NAME);

				tables.add(name);
				viewSet.add(name);
			}
		}

		addCyclicTables(tables, dbmd);
		while (tables.size() > 0) {

			String tmp = tables.get(tblCount);
			String dbCatalog = tmp.substring(0, tmp.indexOf("."));
			String dbSchema = tmp.substring(tmp.indexOf(".")+1);
			if (dbSchema.indexOf(".") > -1){
				// pgsql
				String[] tmpSplit = tmp.split("\\.");
				dbSchema = tmpSplit[1];
				checkTable = tmpSplit[2];
			} else {
				dbSchema = null;
				checkTable = tmp.substring(tmp.indexOf(".") + 1);
			}


		//	buildFakeFKSet(dbmd, checkTable, dbCatalog);

			String checkTableFull = dbCatalog + ".";

			if (State.getInstance().dbMode == 2){ // postgresql
				checkTableFull+=dbSchema+".";
			}
			checkTableFull += checkTable;


			tableDependencies = result.getTableDeps().get(checkTableFull);
			// uncomment to see loop deps
//			    System.out.println(tmp + ", Deps: " + tableDependencies);
			if (tableDependencies == null) {
				tableDependencies = new TreeSet<String>(new CaseInsensitiveComparator());
				tableDepsWithPossibleCycles = new TreeMap<String, Boolean>(new CaseInsensitiveComparator());
				result.getTableDeps().put(checkTableFull, tableDependencies);
				result.getTableDepsWithPossibleCycles().put(checkTableFull, tableDepsWithPossibleCycles);


				ResultSet importedK = dbmd.getImportedKeys(dbCatalog, dbSchema, checkTable);
				HashSet<RelationItem> relList = new HashSet<RelationItem>();
//				State.getInstance().getFakeFKmatched().get(dbCatalog+"."+checkTable);
//				if (relList == null){
//					relList
//					State.getInstance().getFakeFKmatched().put(dbCatalog+"."+checkTable, relList);
//				}

				while (importedK.next()) {
					RelationItem relItem = new RelationItem();
					 String catalog = importedK.getString(JDBC_PKTABLE_CAT) == null ? dbCatalog : importedK.getString(JDBC_PKTABLE_CAT);  //STANIMIR
					 relItem.setCatalog(catalog);

					 relItem.setSchema(importedK.getString(JDBC_PKTABLE_SCHEM));
					relItem.setTableName(importedK.getString(JDBC_PKTABLE_NAME));
					relItem.setFkColumnName(importedK.getString(FKCOLUMN_NAME));

					relList.add(relItem);
				}

			//	for (HashSet<RelationItem> relFakeFKList: State.getInstance().getFakeFKmatched().values()){
					for (RelationItem relItem : relList) {
						if (singleSchema && (!relItem.getCatalog().equals(dbCatalog))) {
							continue;
						}
						String newDepCat = relItem.getCatalog();
						String newDepSchema = relItem.getSchema();
						String newDepTableName = relItem.getTableName();
						String newDep = newDepCat + "." + (newDepSchema != null ? newDepSchema+"." : "")+newDepTableName;

						String cat = newDep.substring(0, newDep.lastIndexOf("."));
						if (!singleSchema && !catalogs.contains(cat)) {

							if (greedy) {
								// New catalog seen. Add all tables
								ResultSet rs2 = dbmd.getTables(newDepCat, newDepSchema, "%", new String[] { JDBC_TABLE });

								// Get the table name
								while (rs2.next()) {

									 String catalog = rs2.getString(TABLE_CAT) == null ? cat : rs2.getString(TABLE_CAT);  //STANIMIR
									 String rs2Schema = rs2.getString(TABLE_SCHEM);
									 tables.add(catalog + "." + (rs2Schema != null ? rs2Schema+"." : "") + rs2.getString(JDBC_TABLE_NAME));
								}

								// New catalog seen. Add all views
								rs2 = dbmd.getTables(newDepCat, newDepSchema, "%", new String[] { "VIEW" });

								// Get the table name
								while (rs2.next()) {
									  String catalog = rs2.getString(TABLE_CAT) == null ? dbCatalog : rs2.getString(TABLE_CAT);  //STANIMIR
									  String rs2Schema = rs2.getString(TABLE_SCHEM);
									  String name = catalog + "." + (rs2Schema != null ? rs2Schema+"." : "") + rs2.getString(JDBC_TABLE_NAME);

									tables.add(name);
									viewSet.add(name);
								}

								catalogs.add(cat);
							}
						}

						// this checks for FK links which are marked as nullable.
						ResultSet fieldNames = dbmd.getColumns(dbCatalog, dbSchema, checkTable, relItem.getFkColumnName());
						if (fieldNames.next()) {
							if (!result.getTableDeps().get(checkTableFull).contains(newDep) && (!newDep.equalsIgnoreCase(checkTableFull.toUpperCase()))) {
								if (!greedy) {
									// do stupid tricks to get the proper case for mysql..
									ResultSet resultSet =
										dbmd.getTables(newDepCat, newDepSchema, newDepTableName,
												new String[] { JDBC_TABLE, "VIEW" });

									if (resultSet.next()) {
										   String catalog = resultSet.getString(TABLE_CAT) == null ? dbCatalog : resultSet.getString(TABLE_CAT);  //STANIMIR
										   String resultSetSchema = resultSet.getString(TABLE_SCHEM);

										   String match = catalog + "." + (resultSetSchema != null ? resultSetSchema+"." : "") + resultSet.getString(JDBC_TABLE_NAME);

										boolean matched = false;

										for (String s : tables) { // sucks!! : O(n)
											if (s.equalsIgnoreCase(match)) {
												matched = true;
												break;
											}
										}
										if (!matched) {
											tables.add(match);
										}
									}
									resultSet.close();
								}

								TreeMap<String, String> cyclicExclusionEntries = State.getInstance().cyclicTableExclusionListTables.get(checkTableFull);
								if ((fieldNames.getString("IS_NULLABLE").equalsIgnoreCase("YES")) ||
										((cyclicExclusionEntries != null) && ((cyclicExclusionEntries.containsKey(fieldNames.getString("COLUMN_NAME")))))) {
									tableDepsWithPossibleCycles.put(getTableNameInProperCase(dbmd, newDepCat, newDepSchema, newDepTableName), true); // for record keeping
									continue;
								}

								tableDepsWithPossibleCycles.put(getTableNameInProperCase(dbmd, newDepCat, newDepSchema, newDepTableName), false); // for record keeping
								tableDependencies.add(newDep); // for processing with no cycles

							}
					//	}
					}
				}
			}
			if ((tableDependencies.size() == 0) || parsedTables.containsAll(tableDependencies)) {
				// all deps are satisfied.
				// System.out.println("All deps matched. Adding " + checkTableFull + " to completed list");
				parsedTables.add(checkTableFull);
				boolean matched = false;
				for (String s : result.getCommitList()) { // sucks: O(n) but search space is small
					if (s.equalsIgnoreCase(checkTableFull)) {
						matched = true;
					}
				}
				if (!matched) {
					// System.out.println(getTableNameInProperCase(dbmd, checkTableFull));
					result.getCommitList().add(getTableNameInProperCase(dbmd, checkTableFull));
				}


				tables.remove(tblCount);
			}
			else {
				tblCount++;
			}

			if (tblCount > tables.size() - 1) { // we're not done yet
				tblCount = 0;
			}
		}

		if (singleSchema) {
			cache.put(dbCatalogs.iterator().next(), result);
		}
		return result;
	}


/*
	private static void buildFakeFKSet(final java.sql.DatabaseMetaData dbmd,
			String checkTable, String dbCatalog) throws SQLException {

		for (Entry<String, FakeFKPattern> entry: State.getInstance().getFakeFK().entrySet()){
			String tableName = entry.getKey();
			boolean fakeFKenabled = false;
			FakeFKPattern mapping = entry.getValue();
			ResultSet fieldNames ;

			String activeCatalog, activeTable;

			if (tableName.equals("*")){
				activeCatalog = dbCatalog;
				activeTable = checkTable;
				fieldNames = dbmd.getColumns(dbCatalog, null, checkTable, null);
				fakeFKenabled = mapping.isEnabled();

				FakeFKPattern exceptionMatch = State.getInstance().getFakeFK().get(activeCatalog+"."+activeTable);
				if (exceptionMatch != null){
					fakeFKenabled = exceptionMatch.isEnabled();
					activeCatalog = dbCatalog;
					activeTable = checkTable;
					fieldNames = dbmd.getColumns(activeCatalog, null, activeTable, null);
					mapping = exceptionMatch;

				}
			}

			else{
				fakeFKenabled = mapping.isEnabled();
				activeCatalog = getTableCatalog(tableName);
				activeTable = getTableName(tableName);
				fieldNames = dbmd.getColumns(activeCatalog, null, activeTable, null);
			}



			while (fakeFKenabled && fieldNames.next()) {
				String fname = fieldNames.getString("COLUMN_NAME");
				if (fname.matches(mapping.getPattern())){

					String targetTable = fname.replaceAll(mapping.getPattern(), mapping.getReplacePattern());
					if (tableExists(dbmd, activeCatalog, targetTable) && !targetTable.equalsIgnoreCase(activeTable)){
						RelationItem relItem = new RelationItem();
						relItem.setCatalog(activeCatalog);
						relItem.setTableName(activeTable);
						relItem.setFkColumnName(fname);
						relItem.setPkColumnName(fname);
						relItem.setFkCatalog(activeCatalog);
						relItem.setFkTableName(targetTable);
						relItem.setFkName(fname);
						relItem.setKeySeq(1);

						HashSet<RelationItem> relList = State.getInstance().getFakeFKmatched().get(activeCatalog+"."+targetTable);
						if (relList == null){
							relList = new HashSet<RelationItem>();
							State.getInstance().getFakeFKmatched().put(activeCatalog+"."+targetTable, relList);
						}
						System.out
								.println("Faking a Foreign key: " + relItem);
						relList.add(relItem);

					}
				}
			}
			fieldNames.close();
		}
	}
	*/



	/**
	 * @param dbmd
	 * @param checkTableFull
	 * @return
	 * @throws SQLException
	 */
	private static String getTableNameInProperCase(DatabaseMetaData dbmd,
			String checkTableFull) throws SQLException {
		return getTableNameInProperCase(dbmd, getTableCatalog(checkTableFull), getTableSchema(checkTableFull), getTableName(checkTableFull));
	}





	/** Add the tables that are in our cyclic dependency exlusion list.
	 * @param tables
	 * @param dbmd
	 * @throws SQLException */
	private static void addCyclicTables(LinkedList<String> tables, DatabaseMetaData dbmd) throws SQLException {
		TreeSet<String> tablesToAdd = new TreeSet<String>(new CaseInsensitiveComparator());
		Collection<TreeMap<String, String>> cycles = State.getInstance().getCyclicTableExclusionListTables().values();
		for (TreeMap<String, String> entry: cycles){
			for (String replacement: entry.values()){
				// New catalog seen. Add all tables

				String cat = SyncUtils.getTableCatalog(replacement);
				String tName = SyncUtils.getTableName(replacement);
				ResultSet rs2 = dbmd.getTables(cat, tName, tName, new String[] { JDBC_TABLE });

				// Get the table name
				if (rs2.next()) {
					  String catalog = rs2.getString(TABLE_CAT) == null ? cat : rs2.getString(TABLE_CAT);  //STANIMIR

					// keep in a treeset to avoid adding the same table twice
					  tablesToAdd.add(catalog + "." + rs2.getString(JDBC_TABLE_NAME));
				}


			}
		}
		tables.addAll(tablesToAdd);

	}



	/**
	 * Return commit order of given database
	 *
	 * @param connection
	 * @param singleSchema
	 * @param greedy
	 * @return a linked list in the right commit order
	 * @throws SQLException
	 */
	public static CommitResults getCommitOrder(Connection connection, boolean singleSchema, boolean greedy)
	throws SQLException {
		String dbCatalog = connection.getCatalog();

		if (State.getInstance().dbMode == 2){ // postgresql
			dbCatalog = State.getInstance().dbCatalog+"."+State.getInstance().dbSchema; //
		}
		DatabaseMetaData dbmd = connection.getMetaData();
		TreeSet<String> catalogs = new TreeSet<String>(new CaseInsensitiveComparator());
		catalogs.add(dbCatalog);
		return getCommitOrder(dbmd, catalogs, singleSchema, greedy);
	}



	/**
	 * Fetches all catalogs
	 *
	 * @param connection
	 * @return CommitResults
	 * @throws SQLException
	 */
	public static CommitResults getCompleteCommitOrder(Connection connection)
	throws SQLException {
		DatabaseMetaData dbmd = connection.getMetaData();
		ResultSet resultSet = dbmd.getCatalogs();

		TreeSet<String> catalogs = new TreeSet<String>(new CaseInsensitiveComparator());

		// Get the table name
		while (resultSet.next()) {
			String cat = resultSet.getString(TABLE_CAT);
			if (!cat.equalsIgnoreCase("mysql")) {
				catalogs.add(cat);
			}
		}

		resultSet.close();
		return getCommitOrder(dbmd, catalogs, false, true);
	}



	public static String[] getEnumValues(final Connection conn, final String tblName, final String fieldName, Boolean[] wasScrubbed)
	throws SQLException {
		return getEnumValues(conn, tblName, fieldName, wasScrubbed, false, null, null, null).getEnumText();
	}

	/**
	 * Given an enum, it returns all possible values of it. wasScrubbed will be true if enums didn't
	 * match completely
	 *
	 * @param conn
	 * @param tblName
	 * @param fieldName
	 * @param wasScrubbed
	 * @param fakeEnum
	 * @param keyCol
	 * @param valueCol
	 * @param otherCols
	 * @return An arraylist of enum values
	 * @throws SQLException
	 */
	public static EnumResult getEnumValues(final Connection conn, final String tblName, final String fieldName, Boolean[] wasScrubbed,
			boolean fakeEnum, String keyCol, String valueCol, TreeSet<String> otherCols)
	throws SQLException {
		EnumResult result = new EnumResult();
		Statement stat = null;
		ResultSet rs = null;
		TreeSet<String> enumset = new TreeSet<String>(new CaseInsensitiveComparator());
		try {
			String[] enumText = {};
			stat = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			if (!fakeEnum && State.getInstance().dbMode != 2){ // pgsql

				rs = stat.executeQuery(String.format("SHOW COLUMNS FROM %s LIKE '%s'", tblName, fieldName));

				rs.next();

				String enumTypes = rs.getString("type");

				enumTypes = enumTypes.substring(enumTypes.indexOf("(") + 1, enumTypes.length() - 1);

				enumText = enumTypes.replaceAll("'", "").split(",");
				wasScrubbed[0] = false;
				for (int i = 0; i < enumText.length; i++) {
					String res = cleanEnum(enumText[i], enumset, i, wasScrubbed);
					enumText[i] = res;

				}
			}
			else {
				LinkedHashSet<String> tmp = new LinkedHashSet<String>();
				if (fakeEnum){
					rs = stat.executeQuery(String.format("select * from %s", tblName));
				} else if (State.getInstance().dbMode == 2){
					wasScrubbed[0] = true;
					rs = stat.executeQuery(String.format("SELECT pg_enum.enumlabel, pg_enum.enumlabel AS enumlabel FROM pg_type JOIN pg_enum ON pg_enum.enumtypid = pg_type.oid"+
						" where pg_type.typname='%s'",  fieldName));
				}

				while (rs.next()) { // process results one row at a time
					String key = keyCol == null ? rs.getString(2).toUpperCase() : rs.getString(keyCol).toUpperCase();


					String val = valueCol == null ? rs.getString(1) : rs.getString(valueCol);
					String tmpKey = "";
					for (int i = 0; i < key.length(); i++) {
						String c = key.substring(i, i + 1).replace(' ', '_');
						if (c.matches("\\w")) {
							tmpKey += c.toUpperCase();
							if (!c.equals(c.toUpperCase())) {
								wasScrubbed[0] = true;
							}
						}
						else {
							wasScrubbed[0] = true;
						}
					}
					String enumAppend = ", ";
					if (otherCols != null) {
						for (String col : otherCols) {
							try {
								boolean valueWasNull = false;
								Object valueOfOther = rs.getObject(col);
								if (valueOfOther == null){
									valueOfOther = new String();
									valueWasNull = true;
								}
								result.getOtherColumns().put(SyncUtils.removeUnderscores(col), valueOfOther);

								if (!valueWasNull && valueOfOther instanceof String) {
									enumAppend += '"' + valueOfOther.toString() + '"';
								} else
									if (valueOfOther instanceof Long) {
										enumAppend += valueOfOther.toString() + "L";
									} else{
										if (valueWasNull){
											enumAppend += "null";
										} else {
											enumAppend += valueOfOther.toString();
										}
									}
								enumAppend += ", ";
							}
							catch (SQLException e) {
								HbnPojoGen.logE(String.format("ERROR: treatAsEnum other column %s was not found for table '%s', keyCol '%s' ", col, tblName, keyCol));
								System.exit(1);
							}
						}
					}
					String entry = tmpKey + "(\"" + val + "\"" + StringUtils.chomp(enumAppend, ", ") + ")";
					tmp.add(entry);
				}
				enumText = tmp.toArray(enumText);

			}

			result.setEnumText(enumText);

			return result;
		}
		finally {
			if (stat != null) {
				stat.close();
			}
			if (rs != null) {
				rs.close();
			}

		}
	}



	/**
	 * Scrub the enums for those characters which cannot be allowed in the java world (eg "+", etc)
	 *
	 * @param enumText
	 * @param enumset
	 * @param count
	 * @param wasScrubbed
	 * @return A scrubbed enum
	 */
	private static String cleanEnum(final String enumText, final TreeSet<String> enumset, final int count, Boolean[] wasScrubbed) {
		String txt = "(\"" + enumText + "\")";
		String tmp = "";


		for (int i = 0; i < enumText.length(); i++) {
			String c = enumText.substring(i, i + 1).replace(' ', '_');
			if (c.matches("\\w")) {
				tmp += c.toUpperCase();
				if (!c.equals(c.toUpperCase())) {
					wasScrubbed[0] = true;
				}
			}
			else {
				wasScrubbed[0] = true;
			}
		}

		if ((tmp.length() == 0) || tmp.substring(0, 1).matches("\\d")) {
			wasScrubbed[0] = true;
			tmp = "ENUM" + tmp;
		}

		if (enumset.contains(tmp)) {
			tmp = tmp + count;
			wasScrubbed[0] = true;
		}
		else {
			enumset.add(tmp);
		}
		if (!tmp.equalsIgnoreCase(enumText)){
			wasScrubbed[0] = true;
		}
		return tmp + txt;
	}



	/**
	 * Map the types from the DB world to the Java environment. We upscale some types since we do not have unsigned types in java :-(
	 *
	 * @param fieldObj
	 * @return Java type
	 */
	public static String mapSQLType(FieldObj fieldObj) {
		String result = "";
		switch (fieldObj.getFieldType()) {
		case java.sql.Types.BOOLEAN:
		case java.sql.Types.BIT:
			return "Boolean";
		case java.sql.Types.TINYINT:
			return "Byte";
		case java.sql.Types.SMALLINT:
			return "Integer";
		case java.sql.Types.INTEGER:
			if (!fieldObj.isFieldTypeUnsigned()) {
				result = "Integer";
			}
			else {
				result = "Long";
			}
			break;
		case java.sql.Types.BIGINT:
			result = "Long";
			break;
		case java.sql.Types.FLOAT:
		case java.sql.Types.REAL:
		case java.sql.Types.DOUBLE:
			result = "Double";
			break;
		case java.sql.Types.NUMERIC:
		case java.sql.Types.DECIMAL:
			result = "java.math.BigDecimal";
			break;
		case java.sql.Types.CHAR:
		case java.sql.Types.VARCHAR:
		case java.sql.Types.LONGVARCHAR:
		case java.sql.Types.NCHAR:
		case java.sql.Types.NVARCHAR:
		case java.sql.Types.LONGNVARCHAR:
			result = "String";
			break;
		case java.sql.Types.BINARY:
		case java.sql.Types.VARBINARY:
		case java.sql.Types.LONGVARBINARY:
		case java.sql.Types.BLOB:
			result = "Byte[]";
			break;
		case java.sql.Types.OTHER:
			if (fieldObj.getFieldColumnType().equalsIgnoreCase("UUID")){
				result = "java.util.UUID";
			} else {
				result = "Object";
			}
			break;
		case java.sql.Types.DATE:
			if (State.getInstance().isEnableJodaSupport() || State.getInstance().isEnableJDK8Support()){
				result = "LocalDate";
			} else {
				if (State.getInstance().isEnableJDK8Support()){
					result = "LocalDate";
				} else {
					result = "Date";

				}
			}
			break;
		case java.sql.Types.TIME:
		case java.sql.Types.TIMESTAMP:
			if (State.getInstance().isEnableJodaSupport()){
				result = "DateTime";
			} else {
				 if (State.getInstance().isEnableJDK8Support()){
					 result = "LocalDateTime";
				 } else {
					 result = "Date";
				 }
				 }
			break;
		case java.sql.Types.ROWID:
		case java.sql.Types.NCLOB:
		case java.sql.Types.SQLXML:
		case java.sql.Types.NULL:
		case java.sql.Types.DISTINCT:
		case java.sql.Types.STRUCT:
		case java.sql.Types.ARRAY:
		case java.sql.Types.CLOB:
		case java.sql.Types.REF:
		case java.sql.Types.JAVA_OBJECT:
		case java.sql.Types.DATALINK:
			result = "Object";
			break;
		default:
			result = "Object";
			break;
		}
		return result;
	}



	/**
	 * Convenience function
	 *
	 * @param s
	 * @return the same string with the first character set to uppercase
	 */
	public static String upfirstChar(String s) {
        if (s == null || s.length() == 0){
            return "";
        }
		return s.substring(0, 1).toUpperCase() + s.substring(1, s.length());
	}



	/**
	 * Convenience function
	 *
	 * @param s
	 * @return the same string with the first character set to lowercase
	 */
	public static String lowerfirstChar(String s) {
		return s.substring(0, 1).toLowerCase() + s.substring(1, s.length());
	}



	/**
	 * Copies src file to dst file. If the dst file does not exist, it is created.
	 *
	 * @param src
	 * @param dst
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void copyFile(InputStream src, String dst) throws FileNotFoundException, IOException {
		IOUtils.copy(src, new FileOutputStream(dst));
	}



	/**
	 * Copies a file, applying velocity transformations if necessary
	 *
	 * @param src
	 * @param dst
	 * @throws ResourceNotFoundException
	 * @throws ParseErrorException
	 * @throws Exception
	 *
    public static void copyVMFile(File src, String dst)
            throws ResourceNotFoundException, ParseErrorException, Exception {
        VelocityEngine ve = new VelocityEngine();
        Properties p = new Properties();
        p.setProperty("file.resource.loader.path", src.getParent());
        p.setProperty("runtime.interpolate.string.literals", "true");
        ve.init(p);

        Template generalTemplate = ve.getTemplate(src.getName());
        VelocityContext context = new VelocityContext();
        context.put(PROJECTNAME, State.getInstance().projectName);
        context.put(TOPLEVEL, State.getInstance().topLevel);
        context.put(LIB, State.getInstance().libPath);
        context.put("src", State.getInstance().getSrcFolder());
        context.put("target", "bin");
        context.put("test", State.getInstance().getTestFolder());

        context.put("applicationContextFilename", State.getInstance().getApplicationContextFilename());

        PrintWriter generalTemplateWriter = new PrintWriter(new BufferedWriter(new FileWriter(dst)));
        generalTemplate.merge(context, generalTemplateWriter);
        generalTemplateWriter.close();
    }

	 */

	private static String urlToName(URL url){
		String fname = url.getFile();
		return fname.substring(fname.indexOf("skeleton/")+9);
	}
	public static void copyDirectory(URL[] urls, String targetFolder) throws IOException{
		for (URL url: urls) {
			if (url.getPath().indexOf("skeleton/") == -1) {
				continue;
			}
			String fname=urlToName(url);
			if (fname.equals("") || fname.equals(".MySCMServerInfo")) {
				continue;
			}
			File dstDir;
			if (fname.indexOf("/") > -1){
				dstDir = new File(targetFolder+File.separator+urlToName(url));
			} else {
				dstDir = new File(targetFolder+File.separator);
			}

			if (fname.indexOf(PROJECTNAME) > -1) {
				dstDir = new File(targetFolder + File.separator + State.getInstance().projectName);
				if (!fname.endsWith("/")){
					fname = fname.substring(fname.indexOf(PROJECTNAME)+PROJECTNAME.length());
				}
			}

			if (urlToName(url).indexOf(TOPLEVEL) > -1) {
				dstDir = new File(targetFolder + File.separator + State.getInstance().topLevel.replaceAll("\\.", "/"));
				if (!fname.endsWith("/")){
					fname = fname.substring(fname.indexOf(TOPLEVEL)+TOPLEVEL.length());
				}
			}
			if (urlToName(url).indexOf(LIBRARIES) > -1) {
				if (State.getInstance().isMavenEnabled()){
					continue;
				}
				dstDir = new File(targetFolder + File.separator  + State.getInstance().libPath);
				if (!fname.endsWith("/")){
					fname = fname.substring(fname.indexOf(LIBRARIES)+LIBRARIES.length());
				}
			}
			if (urlToName(url).indexOf(SRC) > -1) {
				dstDir = new File(targetFolder + File.separator  + State.getInstance().getSrcFolder());
				if (!fname.endsWith("/")){
					fname = fname.substring(fname.indexOf(SRC)+SRC.length());
				}
			}

			if (urlToName(url).indexOf(RESOURCES) > -1) {
				dstDir = new File(targetFolder + File.separator  + State.getInstance().getResourceFolder());
				if (!fname.endsWith("/")){
					fname = fname.substring(fname.indexOf(RESOURCES)+RESOURCES.length());
				}
			}

			if (urlToName(url).indexOf(TESTRESOURCES) > -1) {
				dstDir = new File(targetFolder + File.separator  + State.getInstance().getTestResourceFolder());
				if (!fname.endsWith("/")){
					fname = fname.substring(fname.indexOf(TESTRESOURCES)+TESTRESOURCES.length());
				}
			}

			if (urlToName(url).indexOf(TEST) > -1) {
				dstDir = new File(targetFolder + File.separator  + State.getInstance().getTestFolder());
				if (!fname.endsWith("/")){
					fname = fname.substring(fname.indexOf(TEST)+TEST.length());
				}
			}

			if (urlToName(url).endsWith("/")) {

				if (!dstDir.exists()) {
					if (!dstDir.mkdirs()) {
						throw new RuntimeException("Cannot create Directory");
					}
				}
			} else {
				copyFile(url.openStream(), dstDir+File.separator+fname);
			}
		}
	}



	/**
	 * Dot to slash.
	 *
	 * @param objectPackage
	 * @return dot to slash
	 */
	public static String packageToDir(String objectPackage) {
		return objectPackage.replaceAll("\\.", "/");
	}



	/**
	 * Given an input string, remove underscores and convert to Java conventions
	 *
	 * @param input
	 * @return Java-convention name
	 */
	public static String removeUnderscores(String input) {
		StringBuffer result = new StringBuffer();
        if (input == null){
            return "";
        } else if (State.getInstance().disableUnderscoreConversion) {
			result.append(input);
		}
		else {

			String[] tmp = input.split("_");
			for (int i = 0; i < tmp.length; i++) {
				String fragment;
				if (i == 0) {
					fragment = tmp[i];
				}
				else {
					fragment = tmp[i].substring(0, 1).toUpperCase() + tmp[i].substring(1);
				}
				result.append(fragment);
			}
		}
		return result.toString();
	}



	/**
	 * Given a word, return the english plural equivalent
	 *
	 * @param input
	 * @return nice english
	 */
	public static String pluralize(String input) {
        String result = input;
        if (!State.getInstance().disableEnglishPlural) {
            String tmp = Noun.pluralOf(input.substring(0, 1).toLowerCase() + input.substring(1), State.getInstance().getCustomPluralizer());
            result = input.substring(0, 1) + tmp.substring(1);
        }

        return result;
    }



	/**
	 * Results of getCommitOrder
	 *
	 * @author wallacew
	 *
	 */
	public static final class CommitResults
	implements Serializable {

		/**
		 *
		 */
		private static final long serialVersionUID = -7845562419378909804L;
		/** Contains an ordered list as to how to commit records in a DB */
		private LinkedList<String> commitList = new LinkedList<String>();
		/** List of what each table requires as other table dependencies */
		private TreeMap<String, TreeSet<String>> tableDeps = new TreeMap<String, TreeSet<String>>(new CaseInsensitiveComparator());
		/** Like tableDeps, but holding the complete dependency list rather than just those FK marked as NotNull */
		private TreeMap<String, TreeMap<String, Boolean>> tableDepsWithPossibleCycles =
			new TreeMap<String, TreeMap<String, Boolean>>(new CaseInsensitiveComparator());
		/** the db cycle list */
		private TreeMap<String, LinkedList<String>> cycleList = new TreeMap<String, LinkedList<String>>(new CaseInsensitiveComparator());

		private Map<String, String> commentMap =  new HashMap<String, String>();


		/**
		 * Recursive call. Finds cycles in the DB
		 *
		 * @param start what to match
		 * @param test what we're seeing now
		 * @param seen what we've seen so far
		 * @param result linked list of result
		 */
		@SuppressWarnings("all")
		private void checkCycle(String start, String test, TreeSet<String> seen, LinkedList<String> result) {

			if (start.equalsIgnoreCase(test) && (test != null)) {
				result.add(test); // we've made a cycle
				return;
			}
			if (seen == null) {
				seen = new TreeSet<String>(new CaseInsensitiveComparator()); // init

			}
			if (test == null) { // initial invocation
				test = start;
			}
			if (seen.contains(test)) {
				return; // we may have cycles not in this round
			}
			seen.add(test);
			for (String check : this.tableDepsWithPossibleCycles.get(test).keySet()) {
				checkCycle(start, check, seen, result); // recursive call
				if (!result.isEmpty()) {
					result.add(test);
					return;
				}
			}
			return; // no cycles found
		}



		/**
		 * Build a list of all those entries that have a cycle in the db
		 */
		public void buildCycleList() {
			for (Entry<String, TreeMap<String, Boolean>> entry : this.tableDepsWithPossibleCycles.entrySet()) {
				LinkedList<String> result = new LinkedList<String>();
				checkCycle(entry.getKey(), null, null, result);
				if (!result.isEmpty()) {
					result.removeLast(); // will always be identical to the start
					this.cycleList.put(entry.getKey(), result);
				}
			}
		}



		/**
		 * Return the commit order list
		 *
		 * @return the commitOrder
		 */
		public final LinkedList<String> getCommitList() {
			return this.commitList;
		}



		/**
		 * Return the table dependencies
		 *
		 * @return the tableDeps
		 */
		public final TreeMap<String, TreeSet<String>> getTableDeps() {
			return this.tableDeps;
		}



		/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return this.commitList.toString();
		}



		/**
		 * Return a list of dependencies including ones with cycles in the DB
		 *
		 * @return the tableDepsWithPossibleCycles
		 */
		public final TreeMap<String, TreeMap<String, Boolean>> getTableDepsWithPossibleCycles() {
			return this.tableDepsWithPossibleCycles;
		}



		/**
		 * Get a list of all tables that loop around
		 *
		 * @return the cycleList
		 */
		public final TreeMap<String, LinkedList<String>> getCycleList() {
			return this.cycleList;
		}

	}



	/**
	 * Uses the package map to figure out the final destination path
	 *
	 * @param schema
	 * @param packageType
	 * @return string config
	 */
	public static String getConfigPackage(String schema, PackageTypeEnum packageType) {
		String result = "";
		String niceSchema = removeUnderscores(schema);
		PackageMap packageMap = State.getInstance().packageMaps.get(niceSchema);
		PackageMap defaultPackageMap = State.getInstance().packageMaps.get("DEFAULT");
		if (packageMap == null) {
			packageMap = defaultPackageMap;
			if (packageMap == null) {
				throw new IllegalArgumentException("Unable to find config map. Is <dbPackageMap> section defined?");
			}
		}

		switch (packageType) {
		case OBJECT:
			result = packageMap.getObjectPackage();
			if (result == null) {
				result = defaultPackageMap.getObjectPackage();
			}
			break;
		case OBJECTINTERFACE:
			result = packageMap.getObjectInterfacePackage();
			if (result == null) {
				result = defaultPackageMap.getObjectInterfacePackage();
			}
			break;
		case TABLE_REPO:
			result = packageMap.getObjectTableRepoPackage();
			if (result == null) {
				result = defaultPackageMap.getObjectTableRepoPackage();
			}
			break;
		case TABLE_REPO_FACTORY:
			result = packageMap.getRepositoryFactoryPackage();
			if (result == null) {
				result = defaultPackageMap.getRepositoryFactoryPackage();
			}
			break;
		case DAO:
			result = packageMap.getDaoPackage();
			if (result == null) {
				result = defaultPackageMap.getDaoPackage();
			}

			break;
		case DAOIMPL:
			result = packageMap.getDaoImplPackage();
			if (result == null) {
				result = defaultPackageMap.getDaoImplPackage();
			}

			break;
		case DATA:
			result = packageMap.getDataPackage();
			if (result == null) {
				result = defaultPackageMap.getDataPackage();
			}

			break;
		case ENUM:
			result = packageMap.getEnumPackage();
			if (result == null) {
				result = defaultPackageMap.getEnumPackage();
			}

			break;
		case ENUM_TARGET_BASE:
			result = packageMap.getEnumPackageTargetBase();
			if (result == null) {
				result = defaultPackageMap.getEnumPackageTargetBase();
			}

			break;
		case SUBTYPE_ENUM:
			result = packageMap.getEnumSubtypePackage();
			if (result == null) {
				result = defaultPackageMap.getEnumSubtypePackage();
			}

			break;
		case FACTORY:
			result = packageMap.getFactoryPackage();
			if (result == null) {
				result = defaultPackageMap.getFactoryPackage();
			}
			break;

		case UTIL:
			result = packageMap.getUtilPackage();
			if (result == null) {
				result = defaultPackageMap.getUtilPackage();
			}
			break;

		default:
			assert false;
		}

		result = result.replace("${DB}", niceSchema);
		// System.out.println("getConfigPackages given schema = "+ niceSchema + "type = "+packageType+". Returning "+ result);
		return result;

	}



	/**
	 * Returns The table name
	 *
	 * @param dottedInput
	 * @return the tablename
	 */
	public static String getTableName(String dottedInput) {
		return dottedInput.substring(dottedInput.lastIndexOf(".") + 1);
	}



	/**
	 * Return The table catalog
	 *
	 * @param dottedInput tablecatalog+"."+tablename
	 * @return The table catalog
	 */
	public static String getTableCatalog(String dottedInput) {
        if (dottedInput.indexOf(".") >= 0){
            return dottedInput.substring(0, dottedInput.indexOf("."));
        }
        return dottedInput;
	}


	/** Find a match even inside subclasses/superclass. Used in DFS
	 * @param match
	 * @param sourceClazz
	 * @return t/f
	 */
	public static boolean nestedMatchClass(Clazz match, Clazz sourceClazz) {
		boolean result = false;
		Clazz matching = match.isEmbeddable() ? match.getEmbeddedFrom() : match;
		Clazz search = sourceClazz;

		do {
			result = search.equals(matching);
			search = (!result && search.isSubclass()) ? search.getExtendsFrom().getClazz() : null;
		} while (!result && search != null);

		return result;
	}


	/** Find a property. Used in DFS
	 * @param sourceClazz
	 * @param targetClazz
	 * @return PropertyObj
	 */
	public static List<PropertyObj> matchProperties(Clazz sourceClazz, Clazz targetClazz) {
		LinkedList<PropertyObj> result = new LinkedList<PropertyObj>();
		for (PropertyObj property : targetClazz.getAllPropertiesIncludingComposite(false).values()) {
			Clazz match = null;
			// Check all possible states
			// if (property.isManyToOne()) {
			//   match = property.getManyToOneLink().getClazz();
			// }
			if (property.isOneToMany() && !property.isOneToNBackLinkDisabled()) {
				match = property.getOneToManyLink().getClazz();
			}
			if (property.isOneToOne() && !property.isOneToNBackLinkDisabled()) {
				match = property.getOneToOneLink().getClazz();
			}
			if (property.isManyToMany()) {
				match = property.getManyToManyLink().getDstProperty().getClazz();
			}
			// we found a hit.
			if ((match != null) && nestedMatchClass(match, sourceClazz)) {
				result.add(property);
			}
		}
		return result;
	}


	/** Find out the propertyObj to use for relating. Used in DFS
	 * @param usingProp
	 * @param sourceClazz
	 * @param targetClazz
	 * @return A PropertyObj to use
	 */
	public static PropertyObj getRelationProperty(String usingProp, Clazz sourceClazz, Clazz targetClazz){
		PropertyObj result = null;
		if (usingProp != null) {
			// user specified exactly the field to use
			result = targetClazz.getAllPropertiesIncludingComposite(true).get(usingProp);
		}
		else {
			// target property not specified. Search for it.
			List<PropertyObj> tmp = matchProperties(sourceClazz, targetClazz);
			result = tmp.isEmpty() ? null : tmp.get(0);
		}

		return result;
	}

	public static String convertCamelCaseToEnum(String s) {
		String result = s.substring(0, 1).toUpperCase();
		for (int i = 1; i < s.length(); i++) {
			String tmp = s.substring(i, i + 1);
			if (tmp.toUpperCase().equals(tmp)) {
				result += "_" + tmp.toUpperCase();
			}
			else {
				result += tmp;
			}
		}
		return result.toUpperCase();
	}



	/**
	 * @return the viewSet
	 */
	public static Set<String> getViewSet() {
		return viewSet;
	}



	/**
	 * @param viewSet the viewSet to set
	 */
	public static void setViewSet(Set<String> viewSet) {
		SyncUtils.viewSet = viewSet;
	}



	/**
	 * @param tmpTableName
	 * @return
	 */
	public static String getTableSchema(String tmpTableName) {
		String[] tmp = tmpTableName.split("\\.");
		if (tmp.length > 2){
			return tmp[1];
		}
		return null;
	}

}
