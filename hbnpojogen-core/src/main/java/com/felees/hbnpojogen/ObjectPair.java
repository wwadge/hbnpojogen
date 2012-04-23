/**
 * A simple key-value pair holder for when TreeMap<S, T> is not ideal (eg 
 * LinkedList is required).
 */
package com.felees.hbnpojogen;

import java.io.Serializable;

/** A Simple key-value pair holder of any type.
 * @author wallacew
 * @param <S> Type to hold as a "key"
 * @param <T> Type to hold as a "value"
 *
 */
public class ObjectPair<S, T> implements Serializable, Comparable<S> {
	
	/**
	 * Serialization requirement.
	 */
	private static final long serialVersionUID = -4576578297536905312L;
	/** Key part of this object. */
	S key;
	/** Value part of this object. */
	T value;
	
	
	/** Default Constructor.
	 * @param key Key part.
	 * @param val Value part
	 */
	public ObjectPair(S key, T val) {
		this.key = key;
		this.value = val;
	}
	/**
	 * @return the key
	 */
	public final S getKey() {
		return key;
	}
	/**
	 * @param key the key to set
	 */
	public final void setKey(S key) {
		this.key = key;
	}
	/**
	 * @return the value
	 */
	public final T getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public final void setValue(T value) {
		this.value = value;
	}
	
	@Override
	public int compareTo(S o) {
		return this.compareTo(o);
	}
	
	public String toString() {
		return "<"+this.getKey().toString() + ", "+ this.getValue().toString()+">";
	}
	
	

}
