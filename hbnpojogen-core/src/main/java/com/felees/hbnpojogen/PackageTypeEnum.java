/**
 * Just an enum.
 */
package com.felees.hbnpojogen;

import java.io.Serializable;

/**
 * @author wallacew
 *
 */
public enum PackageTypeEnum implements Serializable {
	/** Constant. */
	OBJECT,
	/** Constant. */
	OBJECTINTERFACE,
	/** Constant. */
	TABLE_REPO,
	/** Constant. */
	TABLE_REPO_FACTORY,

	/** Constant. */
	DAO,
    /** Constant. */
    ENUM,
    /** Constant. */
    SUBTYPE_ENUM,
	/** Constant. */
	FACTORY,
	/** Constant. */
	DATA,
	UTIL,
	/** Constant. */
	DAOIMPL;
}
