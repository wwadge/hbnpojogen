package com.felees.hbnpojogen.obj;

import java.io.Serializable;

/** Defines the type of the property
 * @author wallacew
 *
 */
public enum PropertyMeta implements Serializable {
    /** Property Type */
    NORMAL_FIELD,
    /** Property Type */
    ENUM_FIELD, 
    /** Property Type */
    MANY_TO_ONE_FIELD, 
    /** Property Type */
    PRIMARY_FIELD,
    /** Property Type */
    PRIMARY_FOREIGN_KEY, 
    /** Property Type */
    COMPOSITE_MANY_TO_ONE, 
    /** Property Type */
    ONE_TO_ONE_FIELD;
}
