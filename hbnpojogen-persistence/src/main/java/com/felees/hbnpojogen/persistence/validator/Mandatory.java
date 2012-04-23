package com.felees.hbnpojogen.persistence.validator;


import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;



/**
 * Designates a property that is required to have a value. It cannot be null or
 * empty. The default message is "validator.required" which is assumed to exist
 * in the expected resource bundle ValidatorMessages.properties.
 * 
 * 
 * 
 * @author alexiaa
 * @version $Revision: 1$
 */
@Documented
@Target( { METHOD, FIELD })
@Retention(RUNTIME)
@Inherited
@Deprecated
public @interface Mandatory {
    /** Default message. */
	String message() default "Property marked as mandatory and cannot be left blank.";
}
