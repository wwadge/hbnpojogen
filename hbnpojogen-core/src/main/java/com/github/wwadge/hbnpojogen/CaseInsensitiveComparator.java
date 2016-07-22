package com.github.wwadge.hbnpojogen;

import java.io.Serializable;
import java.util.Comparator;

/**
 * To make treemap case insensitive
 *
 * @author wallacew
 */
public class CaseInsensitiveComparator implements Comparator<String>, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -1963338405421574150L;

    @Override
    public int compare(String o1, String o2) {
        return o1.toUpperCase().compareTo(o2.toUpperCase());
    }

}
