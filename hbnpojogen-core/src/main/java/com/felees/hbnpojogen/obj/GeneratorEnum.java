package com.felees.hbnpojogen.obj;

import java.io.Serializable;

/** Generator Enum.
 * @author wallacew
 *
 */
public enum GeneratorEnum implements Serializable {
	/** Constant */
	AUTO,
	/** Constant */
	UUID,
	/** Constant */
	GUID,
	/** Constant */
	NONE,
	/** Constant. */
    CUSTOM,
    /** Constant. */
    PKS,
    SEQUENCE,
    /** Constant. */
    IDAWARE;
}
