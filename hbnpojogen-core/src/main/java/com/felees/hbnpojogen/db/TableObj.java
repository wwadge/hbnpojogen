package com.felees.hbnpojogen.db;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import com.felees.hbnpojogen.CaseInsensitiveComparator;
import com.felees.hbnpojogen.SyncUtils;
import com.felees.hbnpojogen.obj.Clazz;

/** A representation of a table in the DB
 * @author wallacew
 *
 */
public class TableObj implements Serializable {
	
        /**
	 * 
	 */
	private static final long serialVersionUID = -2373720150716772104L;
		/** lowercase name for access via velocity */
	private String testHandle;
	/** the actual name retreived from the db */
	private String dbName;
	/** pretty table name */
	private String name; 
	/** Links to the fields in this table */
	private TreeMap<String, FieldObj> fields = new TreeMap<String, FieldObj>(new CaseInsensitiveComparator()); 
	/** Link to the exported keys */
	private TreeMap<String, LinkedList<KeyObj>> exportedKeys = new TreeMap<String, LinkedList<KeyObj>>(new CaseInsensitiveComparator());
	/** Link to the imported keys */
	private TreeMap<String, KeyObj> importedKeys = new TreeMap<String, KeyObj>(new CaseInsensitiveComparator());
    /** List of the primary keys */
    private TreeSet<String> primaryKeys = new TreeSet<String>(new CaseInsensitiveComparator());
    /** Map of sequences linked to keys */
    private Map<String, String> primaryKeySequences = new TreeMap<String, String>(new CaseInsensitiveComparator());

    /** List of the natural keys (assigned from config file) */
    private TreeMap<String, FieldObj> naturalKeys = new TreeMap<String, FieldObj>(new CaseInsensitiveComparator());
    /** database catalogue */
	private String dbCat; 
	private String dbSchema; 
	/** We had to fudge and scrub to make enums into our enums */
	private boolean containsScrubbedEnum;
	/** We have enum links */
	private boolean containsEnum;
	/** convenience linking back to the class object we have generated */
	private Clazz clazz;
	/** If true, this table is a view. */
	private boolean viewTable;
	
	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.dbCat+"."+this.dbName;
    }

	/** Returns the fields of this table
	 * @return The fields of this table
	 */
	public TreeMap<String, FieldObj> getFields() {
		return this.fields;
	}
	/** Sets the fields of this table
	 * @param fields
	 */
	public void setFields(TreeMap<String, FieldObj> fields) {
		this.fields = fields;
	}
	/** Returns the list of exported keys
	 * @return a list of exported keys
	 */
	public TreeMap<String, LinkedList<KeyObj>> getExportedKeys() {
		return this.exportedKeys;
	}
	/** Sets the list of exported keys
	 * @param exportedKeys
	 */
	public void setExportedKeys(TreeMap<String, LinkedList<KeyObj>> exportedKeys) {
		this.exportedKeys = exportedKeys;
	}
	/** Return the list of imported keys
	 * @return a list of imported keys
	 */
	public TreeMap<String, KeyObj> getImportedKeys() {
		return this.importedKeys;
	}
	/** Sets the list of imported keys
	 * @param importedKeys
	 */
	public void setImportedKeys(TreeMap<String, KeyObj> importedKeys) {
		this.importedKeys = importedKeys;
	}
	/** Returns the list of primary keys
	 * @return a list of primary keys
	 */ 
	public TreeSet<String> getPrimaryKeys() {
		return this.primaryKeys;
	}
	/** Sets the list of primary keys
	 * @param primaryKeys
	 */
	public void setPrimaryKeys(TreeSet<String> primaryKeys) {
		this.primaryKeys = primaryKeys;
	}
	
	/** Returns testHandle
	 * @return this.testHandle
	 */
	public String getTestHandle() {
		return this.testHandle;
	}
	/** Sets testhandle
	 * @param testHandle
	 */
	public void setTestHandle(String testHandle) {
		this.testHandle = SyncUtils.removeUnderscores(testHandle);
	}
	/** Return dbCatalog
	 * @return the catalog name
	 */
	public String getDbCat() {
		return this.dbCat;
	}
	/** Sets dbCatalog
	 * @param dbCat
	 */
	public void setDbCat(String dbCat) {
		this.dbCat = dbCat;
	}

	
	/** Return true if the enums could not be mapped cleanly onto Java (eg special chars)
	 * @return true/false
	 */
	public boolean isContainsScrubbedEnum() {
		return this.containsScrubbedEnum;
	}
	/** Sets containsScrubbedEnum option (enums that had their names changed in the java world)
	 * @param containsScrubbedEnum
	 */
	public void setContainsScrubbedEnum(boolean containsScrubbedEnum) {
		this.containsScrubbedEnum = containsScrubbedEnum;
	}
	/** Returns true if this table has an enum
	 * @return true/false
	 */
	public boolean isContainsEnum() {
		return this.containsEnum;
	}
	/** Return true if this table had an enum
	 * @param containsEnum
	 */
	public void setContainsEnum(boolean containsEnum) {
		this.containsEnum = containsEnum;
	}
	/** Returns link to the class object representing this table
	 * @return link to the class object representing this table
	 */
	public Clazz getClazz() {
		return this.clazz;
	}
	/** Sets the class object representing this table
	 * @param clazz
	 */
	public void setClazz(Clazz clazz) {
		this.clazz = clazz;
	}
	/** Returns the database name
	 * @return the dbName
	 */
	public final String getDbName() {
		return this.dbName;
	}
	/** Sets the database name
	 * @param dbName the dbName to set
	 */
	public final void setDbName(String dbName) {
		this.dbName = dbName;
	}
	/**  Returns the name of this table
	 * @return the name
	 */
	public final String getName() {
		return this.name;
	}
	/** Sets the name of this table
	 * @param name the name to set
	 */
	public final void setName(String name) {
		this.name = name;
	}
	
	/** Convenience function to return catalog.tablename
	 * @return catalog.tablename
	 */
	public final String getFullTableName(){
	    return this.dbCat+"."+(this.dbSchema == null ? "" : this.dbSchema + ".")+this.dbName;
	}
    /** Returns a list of natural keys 
     * @return the naturalKeys
     */
    public final TreeMap<String, FieldObj> getNaturalKeys() {
        return this.naturalKeys;
    }
    /** Sets the list of natural keys 
     * @param naturalKeys the naturalKeys to set
     */
    public final void setNaturalKeys(TreeMap<String, FieldObj> naturalKeys) {
        this.naturalKeys = naturalKeys;
    }

	/**
	 * @return the viewTable
	 */
	public boolean isViewTable() {
		return viewTable;
	}

	/**
	 * @param viewTable the viewTable to set
	 */
	public void setViewTable(boolean viewTable) {
		this.viewTable = viewTable;
	}

	public Map<String, String> getPrimaryKeySequences() {
		return primaryKeySequences;
	}

	public void setPrimaryKeySequences(Map<String, String> primaryKeySequences) {
		this.primaryKeySequences = primaryKeySequences;
	}

	public String getDbSchema() {
		return dbSchema;
	}

	public void setDbSchema(String dbSchema) {
		this.dbSchema = dbSchema;
	}
    
    
//    public boolean isInCyclicExclusionList(){
//    	return State
//    }
	
}
