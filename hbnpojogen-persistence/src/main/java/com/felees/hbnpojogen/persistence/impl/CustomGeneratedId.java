package com.felees.hbnpojogen.persistence.impl;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Properties;

import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.id.AbstractUUIDGenerator;
import org.hibernate.id.Configurable;
import org.hibernate.type.Type;

/**
 * <b>uuid</b><br>
 * <br>
 * A <tt>custom UUID generator</tt> that returns a string from java.util.UUID,
 * This string will consist of only hex digits. Optionally,
 * the string may be generated with separators between each
 * component of the UUID.
 *
 * Mapping parameters supported: none
 *
 * @author wallacew
 */

public class CustomGeneratedId extends AbstractUUIDGenerator implements Configurable {

	// private String sep = "";
	/** Generate function. 
	* @param session Session handle
	* @param obj obj type
	* @return Serializable object
	*/
	@SuppressWarnings("unused")
	public Serializable generate(SessionImplementor session, Object obj) {
		Serializable result = null;

		// if user has set the id manually, don't change it.		
		try {
			Class<?> clazz = obj.getClass();
			Method idMethod = clazz.getMethod("getId");
			result = (Serializable) idMethod.invoke(obj);
		} catch (Exception e) {
			// do nothing
		}
		
		if (result == null) {
			result = java.util.UUID.randomUUID().toString();
		}
		
		return result; 
	}

	/** Currently unused. 
	* @param type hibernate config 
	* @param params hibernate config 
	* @param d dialect to use
	*/
	@SuppressWarnings("unused")
	public void configure(Type type, Properties params, Dialect d) {
		// sep = PropertiesHelper.getString("separator", params, "");
	}
}
