package com.github.wwadge.hbnpojogen;


import java.util.Map;
import java.util.TreeMap;


/**
 * @author wallacew
 * @version $Revision$
 */
public class EnumResult {
    private String[] enumText = {};
    private Map<String, Object> otherColumns = new TreeMap<String, Object>();


    /**
     * Gets
     *
     * @return
     */
    public String[] getEnumText() {
        return this.enumText;
    }


    /**
     * Sets
     *
     * @param enumText
     */
    public void setEnumText(String[] enumText) {
        this.enumText = enumText;
    }


    /**
     * Gets
     *
     * @return
     */
    public Map<String, Object> getOtherColumns() {
        return this.otherColumns;
    }


    /**
     * Sets
     *
     * @param otherColumns
     */
    public void setOtherColumns(Map<String, Object> otherColumns) {
        this.otherColumns = otherColumns;
    }

}
