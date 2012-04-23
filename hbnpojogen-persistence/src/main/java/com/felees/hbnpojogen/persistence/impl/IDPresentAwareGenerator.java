package com.felees.hbnpojogen.persistence.impl;


import java.io.Serializable;
import java.lang.reflect.Method;

import org.hibernate.engine.SessionImplementor;
import org.hibernate.id.IdentityGenerator;



/**
 * <br>
 * A <tt>custom generator</tt> that behaves as follows: If id is null (or zero to workaround a hibernate bug), behave as
 * if generator was set to AUTO (i.e. get the autoincrement value from the database. Otherwise use the provided ID.
 * 
 * Mapping parameters supported: none
 * 
 * @author wallacew
 */
public class IDPresentAwareGenerator extends IdentityGenerator {

    @Override
    public Serializable generate(SessionImplementor session, Object obj) {
        Serializable result = null;

        // if user has set the id manually, don't change it.
        try {
            Class<?> clazz = obj.getClass();
            Method idMethod = clazz.getMethod("getId");
            result = (Serializable) idMethod.invoke(obj);
        }
        catch (Exception e) {
            // do nothing
        }

        // id not set (or set to 0 due to hibernate bug workaround), proceed
        // as if generator was set to AUTO
        if (result == null || (result.toString().equals("0"))) {

            return super.generate(session, obj);
        }

        return result;
    }
}
