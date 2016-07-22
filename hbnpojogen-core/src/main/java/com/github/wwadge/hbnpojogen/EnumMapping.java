package com.github.wwadge.hbnpojogen;

import java.io.Serializable;
import java.util.TreeSet;


/**
 * @author wallacew
 * @version $Revision: 3$
 */
public class EnumMapping implements Serializable {
    /** */
    private static final long serialVersionUID = -159650200637534991L;
    /**
     * Key label column.
     */
    private String keyColumnLabel;
    /**
     * Value label column.
     */
    private String valueColumnLabel;

    private String dsttableFieldname;
    /**
     * Value label column.
     */
    private TreeSet<String> otherColumnLabels;


    /**
     * Gets
     *
     * @return
     */
    public TreeSet<String> getOtherColumnLabels() {
        return this.otherColumnLabels;
    }


    /**
     * Sets
     *
     * @param otherColumnLabels
     */
    public void setOtherColumnLabels(TreeSet<String> otherColumnLabels) {
        this.otherColumnLabels = otherColumnLabels;
    }


    /**
     * Gets
     *
     * @return
     */
    public String getKeyColumnLabel() {
        return this.keyColumnLabel;
    }


    /**
     * Sets
     *
     * @param keyColumnLabel
     */
    public void setKeyColumnLabel(String keyColumnLabel) {
        this.keyColumnLabel = keyColumnLabel;
    }


    /**
     * Gets
     *
     * @return
     */
    public String getValueColumnLabel() {
        return this.valueColumnLabel;
    }


    /**
     * Sets
     *
     * @param valueColumnLabel
     */
    public void setValueColumnLabel(String valueColumnLabel) {
        this.valueColumnLabel = valueColumnLabel;
    }


    /**
     * Gets
     *
     * @return
     */
    public String getDsttableFieldname() {
        return this.dsttableFieldname;
    }


    /**
     * Sets
     *
     * @param dsttableFieldname
     */
    public void setDsttableFieldname(String dsttableFieldname) {
        this.dsttableFieldname = dsttableFieldname;
    }
}
