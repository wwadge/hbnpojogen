/**
 *
 */
package com.github.wwadge.hbnpojogen;

import java.io.Serializable;
import java.util.TreeMap;

/**
 * @author wallacew
 */
public class GeneratedValueSchemas implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3970247201480225514L;
    /**
     * Key = table, value = Fields
     */
    private TreeMap<String, GeneratedValueFields> tables = new TreeMap<String, GeneratedValueFields>(new CaseInsensitiveComparator());

    /**
     * @return map
     */
    public TreeMap<String, GeneratedValueFields> getTables() {
        return tables;
    }

    /**
     * @param tables
     */
    public void setTables(TreeMap<String, GeneratedValueFields> tables) {
        this.tables = tables;
    }
}
