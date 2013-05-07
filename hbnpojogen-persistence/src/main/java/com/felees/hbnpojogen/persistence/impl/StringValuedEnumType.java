/**
 * 
 */
package com.felees.hbnpojogen.persistence.impl;

/**
 * @author wallacew
 *
 */
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;


import org.hibernate.HibernateException;
import org.hibernate.usertype.EnhancedUserType;
import org.hibernate.usertype.ParameterizedType;

import static com.felees.hbnpojogen.persistence.impl.StringValuedEnumReflect.*;

/** For internal hibernate use.
 * @author unknown
 *
 * @param <T>
 */
@SuppressWarnings("unchecked")

//Please notice the calls to getNameFromValue *************************
public class StringValuedEnumType <T extends Enum & StringValuedEnum> 
        implements EnhancedUserType, ParameterizedType {

	
    /**
     * Enum class for this particular user type.
     */
    private Class<T> enumClass;
    private boolean pgEnum;
    /** ClassLoader to use. */
	private static ClassLoader classLoader;

	/** Sets the classloader to be used when loading classes reflectively. */
    public static void setClassLoader(ClassLoader classLoader) {
    	StringValuedEnumType.classLoader = classLoader;
    }
    /**
     * Value to use if null.
     */
    private String defaultValue;
    
    /** Creates a new instance of ActiveStateEnumType. */
    public StringValuedEnumType() {
    	StringValuedEnumType.classLoader = Thread.currentThread().getContextClassLoader();
	// do nothing
    }
    
    /** Sets param values. 
    * @param parameters params
    */
    public void setParameterValues(Properties parameters) {
        String enumClassName = parameters.getProperty("enum");
        String enumPostgres = parameters.getProperty("forPgSQL");
        try {
            this.enumClass = (Class<T>) Class.forName(enumClassName, true, StringValuedEnumType.classLoader).asSubclass(Enum.class)
                    .asSubclass(StringValuedEnum.class); //Validates the class but does not eliminate the cast
            this.pgEnum = "true".equalsIgnoreCase(enumPostgres);
        } catch (ClassNotFoundException cnfe) {
            throw new HibernateException("Enum class not found", cnfe);
        }

        setDefaultValue(parameters.getProperty("defaultValue"));
    }
	/** Gets the default value.
     * @return default value
     */
    public String getDefaultValue() {
        return this.defaultValue;
    }
 	/** Sets the default value.
     * @param defaultValue default value.
     */
      public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    /**
     * The class returned by <tt>nullSafeGet()</tt>.
     * @return Class
     */
    public Class returnedClass() {
        return this.enumClass;
    }


	/** Internal. 
	* @return sqlTypes int
	*/
    public int[] sqlTypes() {
        return new int[] { Types.CHAR };
    }
    
    /** Internal. 
	* @return mutable
	*/
    public boolean isMutable() {
        return false;
    }

    /**
     * Retrieve an instance of the mapped class from a JDBC resultset. Implementors
     * should handle possibility of null values.
     *
     * @param rs a JDBC result set
     * @param names the column names
     * @param owner the containing entity
     * @return Object
     * @throws HibernateException on error
     * @throws SQLException on error
     */
    @SuppressWarnings("all")
    public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
            throws HibernateException, SQLException {
    	
    	if (pgEnum){
	    	Object object = rs.getObject(names[0]);
	        if (rs.wasNull()) {
	        	 object = getDefaultValue();
	             if (object == null) { //no default value
	                 return null;
	             } 
	        }
	 
	        String name = getNameFromValue(enumClass, object);
	        Object res = rs.wasNull() ? null : Enum.valueOf(enumClass, name);

	        return res;
    	} 
    	
        String value = rs.getString( names[0] );
        if (value == null) {
            value = getDefaultValue();
            if (value == null) { //no default value
                return null;
            } 
        }
        String name = getNameFromValue(enumClass, value);
        Object res = rs.wasNull() ? null : Enum.valueOf(enumClass, name);
        
        return res;
    }
 
    /**
     * Write an instance of the mapped class to a prepared statement. Implementors
     * should handle possibility of null values. A multi-column type should be written
     * to parameters starting from <tt>index</tt>.
     *
     * @param st a JDBC prepared statement
     * @param value the object to write
     * @param index statement parameter index
     * @throws HibernateException on error
     * @throws SQLException on error
     */   
    @SuppressWarnings("all")
    public void nullSafeSet(PreparedStatement st, Object value, int index)
    throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, Types.CHAR);
        } else {
        	if (pgEnum){
                st.setObject( index, ((T) value).getValue(), 1111 ); // 1111 = java.sql.Types "OTHER"
        	} else {
        		st.setString( index, ((T) value).getValue() );
        	}
        }
    }
    
    /** Assemble.
    * @param cached internal
    * @param owner internal
    * @throws HibernateException internal
    * @return internal
    */
    @SuppressWarnings("all")
    public Object assemble(Serializable cached, Object owner)
            throws HibernateException {
        return cached;
    }
     /** Assemble.
    * @param value internal
    * @throws HibernateException internal
    * @return internal
    */
    @SuppressWarnings("all")
    public Serializable disassemble(Object value) throws HibernateException {
        return (Enum) value;
    }
    /** Deep copy.
    * @param value val
    * @throws HibernateException on error
    * @return obj
    */    
    @SuppressWarnings("all")
    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

   /** Equals.
    * @param x obj
    * @param y obj
    * @throws HibernateException internal
    * @return internal
    */
    public boolean equals(Object x, Object y) throws HibernateException {
        return x == y;
    }
   
   /** Hashcode.
    * @param x obj
    * @throws HibernateException internal
    * @return internal
    */
    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    /** Replace.
    * @param original original
    * @param target target
    * @param owner owner
    * @throws HibernateException internal
    * @return internal
    */
  	@SuppressWarnings("all")
    public Object replace(Object original, Object target, Object owner)
            throws HibernateException {
        return original;
    }

     /** ObjectToSqlString.
    * @param value value
    * @return internal
    */
    public String objectToSQLString(Object value) {
        return '\'' + ((T) value).getValue() + '\'';
    }

   /** toXMLString.
    * @param value value
    * @return internal
    */
    public String toXMLString(Object value) {
        return ((T) value).getValue();
    }

   /** fromXMLString.
    * @param xmlValue value
    * @return internal
    */
     public Object fromXMLString(String xmlValue) {
        String name = getNameFromValue(this.enumClass, xmlValue);
        return Enum.valueOf(this.enumClass, name);
    }
        
}