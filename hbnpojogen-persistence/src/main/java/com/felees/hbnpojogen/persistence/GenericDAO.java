
package com.felees.hbnpojogen.persistence;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;


/**
 * Generic DAO class.
 * @author lincolns, wallacew
 * @param <T> 
 * @param <PK> 
 */
public interface GenericDAO<T, PK extends Serializable> {

	/** 
	 * Persist the newInstance object into database.
	 * @param newInstance to save
	 * @return The identifier
	 **/
    PK save(T newInstance);

    /**
     * Save or update.
     * @param transientObject to save
     */
	void saveOrUpdate(T transientObject);
    
    /** 
     * Retrieve a persisted object with a given id from the database.
     * @param id to load
     * @return An object of type T
     */
    T load(PK id);
    
    /** 
     * Retrieve a persisted object with a given id from the database.
     * @param id to get
     * @return An object of type T
     */
    T get(PK id);
    /** 
     * Save changes made to a persistent object.  
     * @param transientObject object to update
     **/
	void update(T transientObject);

    /** 
     * Remove the given object from persistent storage in the database.
     * @param persistentObject object to delete.
     **/
	void delete(T persistentObject);
    
    /** 
     * Remove the given object from persistent storage in the database. 
     * @param s Query to execute 
     * @return A query object
     **/
	Query getQuery(String s);
	
	/** Deletes an object of a given Id. Will load the object internally so 
	 * consider using delete (T obj) directly.
	 * @param id Id of record
	 */
	void delete(PK id);
	
	
	/** Delete object from disk.
	 * @param persistentObject to delete
	 * @param session to use
	 * 
	 */
	void delete(T persistentObject, Session session);

	/** Deletes an object of a given Id. Will load the object internally so consider using delete (T obj) directly.
	 * @param id to delete 
	 * @param session to use
	 */
	void delete(PK id, Session session);

	/**
	 * Loads the given Object.
	 * @param id to load
	 * @param session to use
	 * @return  an object of type T
	 */
	T load(PK id, Session session);

	/**
	 * Loads the given Object.
	 * @param id Id to load
	 * @param session to use
	 * @return An object of type T
	 */
	T get(PK id, Session session);

	/** Save object to disk using given session.
	 * @param o to save
	 * @param session to use
	 * @return the id of the saved object
	 * 
	 */
	PK save(T o, Session session);

	/** Save or update given object.
	 * @param o item to save.
	 * @param session to use
	 * 
	 */
	void saveOrUpdate(T o, Session session); 

	/** Update given object.
	 * @param o item to update 
	 * @param session to use
	 * 
	 */
	void update(T o, Session session); 
	
	/** Refreshes the object of type T.
	 * @param persistentObject to refresh
	 */
	void refresh(T persistentObject);
	
	/**
	 * Get a query handle.
	 * @param s Query to use
	 * @param session to use
	 * @return Query object
	 */
	Query getQuery(String s, Session session);

	/** FindByExample.
	 * @param exampleInstance to use
	 * @param excludeProperty to exclude
	 * @return A list of objects
	 */
	List<T> findByExample(T exampleInstance, String... excludeProperty);
	
	/** Returns a list of objects.
	 * @return list of objects
	 */
	List<T> findAll();

    /** Flushes the cache of the currently-used session.
     * 
     */
    void flush();
    
    /** Object to evict from cache.
     * @param obj Object to evict
     */
    void evict(Object obj);
	
	/** Hibernate wrapper.
	 * @param criterion to filter.
	 * @return list of objects
	 */
	List<T> findByCriteria(Criterion... criterion);
		
	/** Return the currently set class.
     * @return the currently set class.
     */
	Class<T> getPersistentClass(); 
}
