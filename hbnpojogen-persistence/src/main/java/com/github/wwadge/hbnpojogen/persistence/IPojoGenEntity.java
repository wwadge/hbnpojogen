package com.github.wwadge.hbnpojogen.persistence;


import java.io.Serializable;


/**
 * All generated model classes implement this interface.
 *
 * @author wallacew
 * @version $Revision: 3$
 */
public interface IPojoGenEntity {

    /**
     * Return the type of this class. Useful for when dealing with proxies.
     *
     * @return Defining class.
     */
    Class<?> getClassType();

    /**
     * Return the id.
     *
     * @return the id.
     */
    Serializable getId();


}
