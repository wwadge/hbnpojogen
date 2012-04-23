package com.felees.hbnpojogen.obj;

import java.io.Serializable;


/** Helper class to handle join tables
 * @author wallacew
 *
 */
public class JoinTable implements Comparable<JoinTable>, Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -342066601575899425L;
	/** Join link should bounce off this class */ 
	private Clazz linkClass;
	/** The source leg of this link */
	private PropertyObj srcProperty;
	/** The destination leg of this link */
	private PropertyObj dstProperty;
	/** when bouncing off a map, this is the src property on the map side. */
	private PropertyObj srcPropertyBounce;
	/** when bouncing off a map, this is the property on the map side. */
	private PropertyObj dstPropertyBounce;
	

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "linkClass = "+this.linkClass.toString()+", srcProperty = "
			+this.srcProperty.toString()+", dstProperty = "+this.dstProperty.toString();
	}
	/** Constructor
	 * @param srcProperty
	 * @param dstProperty
	 * @param linkClass
	 */
	public JoinTable(final PropertyObj srcProperty, final PropertyObj dstProperty, Clazz linkClass) {
		this.srcProperty=srcProperty;
		this.dstProperty=dstProperty;
		this.linkClass=linkClass;
	}
	/** Returns the linked class
	 * @return the linkClass
	 */
	public final Clazz getLinkClass() {
		return this.linkClass;
	}
	/** Sets the linked class
	 * @param linkClass the linkClass to set
	 */
	public final void setLinkClass(Clazz linkClass) {
		this.linkClass = linkClass;
	}
	/** Return the source property object on the source join
	 * @return the srcProperty
	 */
	public final PropertyObj getSrcProperty() {
		return this.srcProperty;
	}
	/** Sets the source property object on the source join
	 * @param srcProperty the srcProperty to set
	 */
	public final void setSrcProperty(PropertyObj srcProperty) {
		this.srcProperty = srcProperty;
	}
	/** Gets the destination property object on the destination of the join
	 * @return the destPropery
	 */
	public final PropertyObj getDstProperty() {
		return this.dstProperty;
	}
	/** Sets the destination property object on the destination of the join
	 * @param destPropery the destPropery to set
	 */
	public final void setDstProperty(PropertyObj destPropery) {
		this.dstProperty = destPropery;
	}
	@Override
	public int compareTo(JoinTable o) {
		
		return o.getLinkClass().getClassName().compareTo(this.getLinkClass().getClassName());
	}
	/**
	 * @return the srcPropertyBounce
	 */
	public PropertyObj getSrcPropertyBounce() {
		return this.srcPropertyBounce;
	}
	/**
	 * @param srcPropertyBounce the srcPropertyBounce to set
	 */
	public void setSrcPropertyBounce(PropertyObj srcPropertyBounce) {
		this.srcPropertyBounce = srcPropertyBounce;
	}
	/**
	 * @return the dstPropertyBounce
	 */
	public PropertyObj getDstPropertyBounce() {
		return this.dstPropertyBounce;
	}
	/**
	 * @param dstPropertyBounce the dstPropertyBounce to set
	 */
	public void setDstPropertyBounce(PropertyObj dstPropertyBounce) {
		this.dstPropertyBounce = dstPropertyBounce;
	}
}
