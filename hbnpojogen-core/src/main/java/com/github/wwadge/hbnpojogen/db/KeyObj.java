package com.github.wwadge.hbnpojogen.db;

import com.github.wwadge.hbnpojogen.CaseInsensitiveComparator;

import java.io.Serializable;
import java.util.TreeMap;

/**
 * Imported/Exported keys representation
 *
 * @author wallacew
 */
public class KeyObj implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -5994093176012581240L;
    /**
     * pk table name
     */
    private String pkTableName;
    private String pkTableSchema;
    /**
     * pk table catalog
     */
    private String pkTableCatalog;
    /**
     * for exported keys.
     */
    private String field;

    /**
     * Key = pkColName, Value = fkColName
     */
    private TreeMap<String, String> keyLinks = new TreeMap<String, String>(
            new CaseInsensitiveComparator());

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.pkTableCatalog + "." + this.pkTableSchema + "." + this.pkTableName + ", keyLinks="
                + this.keyLinks.toString();
    }

    /**
     * Constructor
     *
     * @param fkColName
     * @param pkTableName
     * @param pkTableCatalog
     * @param pkColName
     */
    public KeyObj(String fkColName, String pkTableName, String pkTableCatalog, String pkTableSchema,
                  String pkColName) {

        this.pkTableName = pkTableName;
        this.pkTableSchema = pkTableSchema;
        this.pkTableCatalog = pkTableCatalog;
        this.keyLinks.put(fkColName, pkColName);
    }

    /**
     * Constructor for exported keys.
     *
     * @param pkTableName
     * @param field
     */
    public KeyObj(String pkTableName, String field) {

        this.pkTableName = pkTableName;
        this.field = field;
    }

    /**
     * Convenience function
     *
     * @return true if the key is a composite key
     */
    public boolean isCompositeKey() {
        return this.keyLinks.size() > 1;
    }

    /**
     * Convenience function
     *
     * @return pkTablecatalog + "." + pkTableName
     */
    public String getPKFullTableName() {
        return this.pkTableCatalog + "." + (this.pkTableSchema == null ? "" : this.pkTableSchema + ".") + this.pkTableName;
    }

    /**
     * Return PK Table name
     *
     * @return pk table name
     */
    public String getPkTableName() {
        return this.pkTableName;
    }

    /**
     * Set the  PK Table name
     *
     * @param name
     */
    public void setPkTableName(String name) {
        this.pkTableName = name;
    }

    /**
     * Returns the db catalog
     *
     * @return The db catalog
     */
    public String getPkTableCatalog() {
        return this.pkTableCatalog;
    }

    /**
     * Sets the db catalog
     *
     * @param pkTableCatalog
     */
    public void setPkTableCatalog(String pkTableCatalog) {
        this.pkTableCatalog = pkTableCatalog;
    }

    /**
     * Return the keyLinks
     *
     * @return the keyLinks
     */
    public final TreeMap<String, String> getKeyLinks() {
        return this.keyLinks;
    }

    /**
     * Sets the keyLinks
     *
     * @param keyLinks the keyLinks to set
     */
    public final void setKeyLinks(TreeMap<String, String> keyLinks) {
        this.keyLinks = keyLinks;
    }

    /**
     * @return the field
     */
    public final String getField() {
        return this.field;
    }

    /**
     * @param field the field to set
     */
    public final void setField(String field) {
        this.field = field;
    }

}
