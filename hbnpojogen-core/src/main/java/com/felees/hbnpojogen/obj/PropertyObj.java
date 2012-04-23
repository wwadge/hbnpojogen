package com.felees.hbnpojogen.obj;


import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.felees.hbnpojogen.CascadeState;
import com.felees.hbnpojogen.State;
import com.felees.hbnpojogen.SyncUtils;
import com.felees.hbnpojogen.db.FieldObj;
import com.felees.hbnpojogen.db.KeyObj;
import com.felees.hbnpojogen.db.TableObj;



/**
 * Represents a property in a class object
 *
 * @author wallacew
 *
 */
public class PropertyObj
implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 3722382654182875234L;
	/** Link to the db field */
	private FieldObj fieldObj;
	/** Link to the class this property belongs to */
	private Clazz clazz;
	/** Like javaName property but without the first letter capitalised */
	private String propertyName;
	/** Java property name */
	private String javaName;
	/** Java-specific mapping of the db field type (Integer, String, etc. May also be a name of another class) */
	private String javaType;
	/** If type is string, this indicates the max length allowed by the field */
	private Integer length;
	/**
	 * If true, the underlying field is marked as autoinc or this property needs a
	 *
	 * @generatedvalue tag
	 */
	private boolean generatedValue;
	/** This is marked as a primary key */
	private boolean idField;
	/** This property was set as a natural key in the config file */
	private boolean naturalKey;
	/** This field is an enumeration */
	private boolean enumType;
	/** If true, the field is marked as one-to-one */
	private boolean oneToOne;
	/** If this is an autoinc (aka generated) field, define generator in use */
	private GeneratorEnum generatorType = GeneratorEnum.NONE;
	/** If true, the field is marked as many-to-many */
	private boolean manyToMany;
	/** If field is marked as many-to-many, this is a pointer to the same entry in the joinTable entry in the class */
	private JoinTable manyToManyLink;
	/** if true, this is the inverse side of a many-to-many link */
	private boolean manyToManyInverseSide;
	/** This field is a many-to-one field */
	private boolean manyToOne;
	/** The FK link is composite */
	private boolean compositeManyToOne;
	/** This field is a one-to-many field */
	private boolean oneToMany;
	/** If field is oneToOne, this points to the PropertyObj we're pointing too from the OneToMany side */
	private PropertyObj oneToOneLink;
	/** For one-to-one, link to the actual KeyObj this is coming from */
	private KeyObj OneToOneKey;
	/** if true, this is the inverse side of a one-to-one link */
	private boolean oneTooneInverseSide;
	/** If field is manyToOne, this points to the PropertyObj we're pointing too from the OneToMany side */
	private PropertyObj manyToOneLink;
	/** If field is {one,many}To{one,many}, this points to the PropertyObj on the other side that is acting as an inverse.
	 * This is different from manyToOneLink for example since that always points to the
	 * ID field of the target table. */
	private PropertyObj inverseLink;
	/** For many-to-one, link to the actual KeyObj this is coming from */
	private KeyObj manyToOneKey;
	/** If field is oneToMany, this points to one or more PropertyObj on the other side */
	private PropertyObj OneToManyLink;
	/** If field is linking to a composite PK class */
	private boolean composite;
	/** If field is composite - link to composite class implementing this composite key */
	private Clazz compositeLink;
	/** Annotations to add on top of each property where this is referenced */
	private TreeSet<String> propertyLevelAnnotations = new TreeSet<String>();
	/** Annotations to add on top of each method where this is referenced */
	private TreeSet<String> methodLevelAnnotationsOnSetters = new TreeSet<String>();
	/** Annotations to add on top of each method where this is referenced */
	private TreeSet<String> methodLevelAnnotationsOnGetters = new TreeSet<String>();
	/** Annotations to add on top of each method where this is referenced */
	private TreeSet<String> methodLevelSettersPrecondition = new TreeSet<String>();
	/** Annotations to add on top of each method where this is referenced */
	private TreeSet<String> methodLevelGettersPrecondition = new TreeSet<String>();
	/** Java snippets to add on top of each method where this is referenced */
	private TreeSet<String> methodLevelSettersPostcondition = new TreeSet<String>();
	/** Java snippets to add on top of each method where this is referenced */
	private TreeSet<String> methodLevelGettersPostcondition = new TreeSet<String>();

	/**
	 * If true, the javaname/propertyname have been changed because we had multiple oneToMany on this field
	 */
	private boolean clashResolved;

	/**
	 * Denotes the property as a FK causing a cyclic dependency.
	 */
	private boolean cyclicDependencyProperty;

	/**
	 * Clazz to be used as a replacement to resolve cyclic dependencies.
	 */
	private Clazz cyclicDependencyReplacementClazz;
	/** This property link cannot be cascaded automatically. */
	private boolean requiresManualSave;
	/** Flagged from config to mark that this backlink is not requested. */
	private boolean oneToNBackLinkDisabled;
	/** If true, property has a @Valid annotation. */
	private boolean validatorAnnotated;

	/**
	 * Gets
	 *
	 * @return clzz
	 */
	public Clazz getCyclicDependencyReplacementClazz() {
		return cyclicDependencyReplacementClazz;
	}


	public String getCascadeTypeManyToOne(){
	    Set<String> cascade = getXToXCascadeType(State.getInstance().getManyToOneCascadeEnabled(), this.manyToOneLink);
	    return getCascadeTypeXtoX(cascade);
	}

	public String getCascadeTypeOneToOne(){
        Set<String> cascade = getXToXCascadeType(State.getInstance().getOneToOneCascadeEnabled(), this.oneToOneLink);
        return getCascadeTypeXtoX(cascade);
    }

    public String getCascadeTypeOneToMany(){
        Set<String> cascade = getXToXCascadeType(State.getInstance().getOneToManyCascadeEnabled(), this.OneToManyLink);
        return getCascadeTypeXtoX(cascade);
    }

    /**
     * 
     *
     * @param cascade
     * @return
     */
    private String getCascadeTypeXtoX(Set<String> cascade) {
        String result = "{";
	    for (String cascadeType: cascade){
	        result += "org.hibernate.annotations.CascadeType."+cascadeType+", ";
	    }
	    
	    return result.substring(0, result.length()-2)+"}";
    }

	/**
	 * Sets
	 *
	 * @param cyclicDependencyReplacementClazz
	 */
	public void setCyclicDependencyReplacementClazz(Clazz cyclicDependencyReplacementClazz) {
		this.cyclicDependencyReplacementClazz = cyclicDependencyReplacementClazz;
	}



	/**
	 * Gets
	 *
	 * @return t/f
	 */
	public boolean isCyclicDependencyProperty() {
		return cyclicDependencyProperty;
	}



	/**
	 * Sets
	 *
	 * @param cyclicDependencyProperty
	 */
	public void setCyclicDependencyProperty(boolean cyclicDependencyProperty) {
		this.cyclicDependencyProperty = cyclicDependencyProperty;
	}



	/**
	 * Returns the full java type eg com.foo.bar.ATable rather than just ATable
	 *
	 * @return the full type, expanded if it's a manytoone, onetoMany
	 */
	public String getFullJavaType() {
		String result = this.javaType;
		if (this.manyToOne) {
			result = this.manyToOneLink.getClazz().getFullClassName();
		}
		else {
			if (this.oneToMany) {
				result = this.OneToManyLink.getClazz().getFullClassName();
			}
		}
		return result;

	}



	/**
	 * Return true if this property is an array (for toString function)
	 *
	 * @return True if this property is an array (for toString function)
	 */
	public boolean isArrayType() {
		return (this.javaType != null) && (this.javaType.indexOf("[]") > -1);
	}



	/**
	 * Return True if the field is of type "Boolean" (to use isXXXX instead of getXXXX)
	 *
	 * @return True if the field is of type "Boolean" (to use isXXXX instead of getXXXX)
	 */
	public final boolean isBooleanField() {
		return this.javaType.equals("Boolean");
	}



	/**
	 * Convenience function for velocity (null checking in velocity sucks)
	 *
	 * @return true if we have a length field
	 */
	public final boolean hasLength() {
		return this.length != null;
	}



	/**
	 * Returns true if this is a primary foreign key
	 *
	 * @return true if this is a primary foreign key
	 */
	public final boolean isPFK() {
		return this.fieldObj.isPrimaryKey() && this.fieldObj.isForeignKey();
	}



	/**
	 * Returns 0 if it's a normal field, 1 if field is an enum, 2 if it's a many-to-one field, 3 if it's a many-to-one field but the FK is composite
	 *
	 * @param inEmbedMode
	 * @return PropertyMeta enum
	 */
	public final PropertyMeta getPropertyMeta(boolean inEmbedMode) {
		TreeSet<String> oneToOneInnerLink = State.getInstance().getOneToOneTables().get(this.fieldObj.getTableObj().getFullTableName());
		if (this.fieldObj.isForeignKey() && (oneToOneInnerLink != null) && (oneToOneInnerLink.contains(this.fieldObj.getName()))) {
			return PropertyMeta.ONE_TO_ONE_FIELD;
		}
		if (this.fieldObj.isPrimaryKey() && (!inEmbedMode)) {
			if (this.fieldObj.isForeignKey()) {
				return PropertyMeta.PRIMARY_FOREIGN_KEY;
			}

			return PropertyMeta.PRIMARY_FIELD;
		}


		if (this.fieldObj.getEnumName() != null) {
			return PropertyMeta.ENUM_FIELD;
		}

		// is it a many-to-one?
				TableObj tobj = this.clazz.getTableObj();
		String fieldName = this.fieldObj.getName();

		for (KeyObj keyObj : tobj.getImportedKeys().values()) {
			if (keyObj.getKeyLinks().containsKey(fieldName) && (State.getInstance().tables.get(keyObj.getPKFullTableName()) != null)) {
				if (keyObj.isCompositeKey()) {
					return PropertyMeta.COMPOSITE_MANY_TO_ONE;
				}
				return PropertyMeta.MANY_TO_ONE_FIELD;
			}
		}

		return PropertyMeta.NORMAL_FIELD;


	}




	/**
	 * Convenience function. Return true if the manyToOne link is eventually pointing back to an instance of the same class
	 *
	 * @param alsoCheckNullable
	 * @param link
	 *
	 * @return true if the manyToOne link is eventually pointing back to an instance of the same class
	 */
	public final boolean isCyclicReference(boolean alsoCheckNullable, PropertyObj link) {
		boolean result = false;

		if (this.isManyToOne()) {
			// get cycle list for this table, if available
			LinkedList<String> cycleList = State.getInstance().commitResult.getCycleList().get(this.getClazz().getTableObj().getFullTableName());
			// get the table object of the target
			TableObj targetTable = link.getClazz().getTableObj();

			TableObj cycleTable = null;
			if (cycleList != null) {
				// we had an entry. Fetch the last table
				cycleTable = State.getInstance().tables.get(cycleList.getLast());
			}
			// we have a self-loop or an entry in the cycle list pertaining to this record
			result = this.getClazz().equals(link.getClazz()) || targetTable.equals(cycleTable);

			if (alsoCheckNullable && (!this.getFieldObj().isNullable())) {
				result = false;
			}
		}

		return result;
	}



	/**
	 * Convenience function. Return true if the manyToOne link is eventually pointing back to an instance of the same class
	 *
	 * @return true if the manyToOne link is eventually pointing back to an instance of the same class
	 */
	public final boolean isCyclicReference() {
		boolean result = false;

		if (this.isManyToOne()) {
			result = isCyclicReference(true, this.getManyToOneLink());
		}
		else {
			if (this.isOneToOne()) {
				result = isCyclicReference(true, this.getOneToOneLink());
			}
		}
		return result;
	}



	/**
	 * Convenience function. Return true if the field has the autoinc field switched on
	 *
	 * @return true if the field has the autoinc field switched on
	 */
	public final boolean isAutoInc() {
		return this.fieldObj.isAutoInc() && (!this.manyToMany);
	}



	/**
	 * If true, the database field is marked as having a default value.
	 *
	 * @return true/false
	 */
	public final boolean isDefaultValue() {
		return this.fieldObj.isDefaultValue();
	}



	/**
	 * Convenience function. Return true if the field can accept null values
	 *
	 * @return true if the field can accept null values
	 */
	public final boolean isNullable() {
		return this.fieldObj.isNullable();
	}



	/**
	 * Return
	 *
	 * @return the fieldObj
	 */
	public final FieldObj getFieldObj() {
		return this.fieldObj;
	}



	/**
	 * Sets the link to the field object from the table
	 *
	 * @param fieldObj the fieldObj to set
	 */
	public final void setFieldObj(FieldObj fieldObj) {
		this.fieldObj = fieldObj;
	}



	/**
	 * Return the type of this property in the java world
	 *
	 * @return the javaType
	 */
	public final String getJavaType() {
		return this.javaType;
	}



	/**
	 * Sets the type of this property in the java world
	 *
	 * @param javaType the javaType to set
	 */
	public final void setJavaType(String javaType) {
		this.javaType = SyncUtils.removeUnderscores(javaType);
	}



	/**
	 * Returns true if this property is an ID field
	 *
	 * @return the idField
	 */
	public final boolean isIdField() {
		return this.idField;
	}



	/**
	 * Set to true if this property is an ID field
	 *
	 * @param idField the idField to set
	 */
	public final void setIdField(boolean idField) {
		this.idField = idField;
	}



	/**
	 * Return the java name of this propert
	 *
	 * @return the javaName
	 */
	public final String getJavaName() {
		return this.javaName;
	}



	/**
	 * Sets the name of this property
	 *
	 * @param javaName the javaName to set
	 */
	public final void setJavaName(String javaName) {
		this.javaName = SyncUtils.removeUnderscores(javaName);
	}



	/**
	 * Returns the propertyName
	 *
	 * @return the propertyName
	 */
	public final String getPropertyName() {
		return this.propertyName;
	}



	/**
	 * Sets the propertyName
	 *
	 * @param propertyName the propertyName to set
	 */
	public final void setPropertyName(String propertyName) {
		this.propertyName = SyncUtils.removeUnderscores(propertyName);
	}



	/**
	 * Link back to the class object
	 *
	 * @return the clazz
	 */
	public final Clazz getClazz() {
		return this.clazz;
	}



	/**
	 * Sets the link to the class holding this property
	 *
	 * @param clazz the clazz to set
	 */
	public final void setClazz(Clazz clazz) {
		this.clazz = clazz;
	}



	/**
	 * Return true if this property is marked as an enum
	 *
	 * @return the enumType
	 */
	public final boolean isEnumType() {
		return this.enumType;
	}



	/**
	 * Set to true if if this property is marked as an enum
	 *
	 * @param enumType true/false
	 */
	public final void setEnumType(boolean enumType) {
		this.enumType = enumType;
	}



	/**
	 * Return true if this property is marked as being "many-to-one"
	 *
	 * @return the manyToOne
	 */
	public final boolean isManyToOne() {
		return this.manyToOne;
	}



	/**
	 * Set to true if this property is marked as being "many-to-one"
	 *
	 * @param manyToOne the manyToOne to set
	 */
	public final void setManyToOne(boolean manyToOne) {
		this.manyToOne = manyToOne;
	}



	/**
	 * Returns true if this property is marked as "one-to-many"
	 *
	 * @return the oneToMany
	 */
	public final boolean isOneToMany() {
		return this.oneToMany;
	}



	/**
	 * Set to true if this property is marked as "one-to-many"
	 *
	 * @param oneToMany the oneToMany to set
	 */
	public final void setOneToMany(boolean oneToMany) {
		this.oneToMany = oneToMany;
	}



	/**
	 * Return the property link on the other side of the many-to-one
	 *
	 * @return the manyToOneLink
	 */
	public final PropertyObj getManyToOneLink() {
		return this.manyToOneLink;
	}



	/**
	 * Sets the property link on the other side of the many-to-one
	 *
	 * @param manyToOneLink the manyToOneLink to set
	 */
	public final void setManyToOneLink(PropertyObj manyToOneLink) {
		this.manyToOneLink = manyToOneLink;
	}



	/**
	 * Returns the property link on the oneToMany
	 *
	 * @return the oneToManyLink
	 */
	public final PropertyObj getOneToManyLink() {
		return this.OneToManyLink;
	}



	/**
	 * Sets the property link on the oneToMany
	 *
	 * @param oneToManyLink the oneToManyLink to set
	 */
	public final void setOneToManyLink(PropertyObj oneToManyLink) {
		this.OneToManyLink = oneToManyLink;
	}



	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Name: " + this.javaName + ", ");
		sb.append("Type: " + this.javaType + ", ");
		sb.append("Field dbname: " + this.fieldObj.getName() + ", ");
		sb.append("Class obj name: ");
		if (this.clazz == null) {
			sb.append(" null\n\n ");
		}
		else {
			sb.append(this.clazz.getClassName() + "\n\n");
		}
		return sb.toString();
	}



	/**
	 * Convenience function. Returns true if the many to one link is flagged as being a natural key
	 *
	 * @return true if many to one link is marked as a natural key in the config file
	 */
	public boolean isNaturalManyToOneLink() {
		// this is only used for the case where we do not have a composite foreign key since in that
		// case we always use the "referencedColumnName" tag
		return State.getInstance().tables.get(this.getManyToOneKey().getPKFullTableName()).getNaturalKeys().containsKey(
				this.getManyToOneKey().getKeyLinks().values().iterator().next());
	}



	/**
	 * Link to the table imported key
	 *
	 * @return the manyToOneKey
	 */
	public final KeyObj getManyToOneKey() {
		return this.manyToOneKey;
	}



	/**
	 * Set the link to the table imported key
	 *
	 * @param manyToOneKey the manyToOneKey to set
	 */
	public final void setManyToOneKey(KeyObj manyToOneKey) {
		this.manyToOneKey = manyToOneKey;
	}



	/**
	 * @return the length
	 */
	public final Integer getLength() {
		return this.length;
	}



	/**
	 * Sets the length of this property
	 *
	 * @param length the length to set
	 */
	public final void setLength(Integer length) {
		this.length = length;
	}



	/**
	 * Return true if property is part of a composite key
	 *
	 * @return the composite
	 */
	public final boolean isComposite() {
		return this.composite;
	}



	/**
	 * Sets true if property is part of a composite key
	 *
	 * @param composite the composite to set
	 */
	public final void setComposite(boolean composite) {
		this.composite = composite;
	}



	/**
	 * Get any custom property annotation
	 *
	 * @return the propertyLevelAnnotations
	 */
	public final TreeSet<String> getPropertyLevelAnnotations() {
		return this.propertyLevelAnnotations;
	}



	/**
	 * Sets any custom property annotations
	 *
	 * @param propertyLevelAnnotations the propertyLevelAnnotations to set
	 */
	public final void setPropertyLevelAnnotations(TreeSet<String> propertyLevelAnnotations) {
		this.propertyLevelAnnotations = propertyLevelAnnotations;
	}



	/**
	 * Return the link to the composite class
	 *
	 * @return the compositeLink
	 */
	public final Clazz getCompositeLink() {
		return this.compositeLink;
	}



	/**
	 * Sets the link to the composite class
	 *
	 * @param compositeLink the compositeLink to set
	 */
	public final void setCompositeLink(Clazz compositeLink) {
		this.compositeLink = compositeLink;
	}



	/**
	 * Return custom method annotations on setters (from config file)
	 *
	 * @return the methodLevelAnnotationsOnSetters
	 */
	public final TreeSet<String> getMethodLevelAnnotationsOnSetters() {
		return this.methodLevelAnnotationsOnSetters;
	}



	/**
	 * Sets custom method annotations on setters (from config file)
	 *
	 * @param methodLevelAnnotationsOnSetters the methodLevelAnnotationsOnSetters to set
	 */
	public final void setMethodLevelAnnotationsOnSetters(TreeSet<String> methodLevelAnnotationsOnSetters) {
		this.methodLevelAnnotationsOnSetters = methodLevelAnnotationsOnSetters;
	}



	/**
	 * Return custom method annotations on getters
	 *
	 * @return the methodLevelAnnotationsOnGetters
	 */
	public final TreeSet<String> getMethodLevelAnnotationsOnGetters() {
		return this.methodLevelAnnotationsOnGetters;
	}



	/**
	 * Sets custom method annotations on getters
	 *
	 * @param methodLevelAnnotationsOnGetters the methodLevelAnnotationsOnGetters to set
	 */
	public final void setMethodLevelAnnotationsOnGetters(TreeSet<String> methodLevelAnnotationsOnGetters) {
		this.methodLevelAnnotationsOnGetters = methodLevelAnnotationsOnGetters;
	}



	/**
	 * Return true if this property is a many-to-many property
	 *
	 * @return the manyToMany
	 */
	public final boolean isManyToMany() {
		return this.manyToMany;
	}



	/**
	 * Set to true if this property is a many-to-many property
	 *
	 * @param manyToMany the manyToMany to set
	 */
	public final void setManyToMany(boolean manyToMany) {
		this.manyToMany = manyToMany;
	}



	/**
	 * Get the join table responsible for the many-to-many link
	 *
	 * @return the manyToManyLink
	 */
	public final JoinTable getManyToManyLink() {
		return this.manyToManyLink;
	}



	/**
	 * Sets the join table responsible for the many-to-many link
	 *
	 * @param manyToManyLink the manyToManyLink to set
	 */
	public final void setManyToManyLink(JoinTable manyToManyLink) {
		this.manyToManyLink = manyToManyLink;
	}



	/**
	 * Return true if this is the "inverse" side of a many-to-many relation
	 *
	 * @return the manyToManyInverseSide
	 */
	public final boolean isManyToManyInverseSide() {
		return this.manyToManyInverseSide;
	}



	/**
	 * Set to true if this is the "inverse" side of a many-to-many relation
	 *
	 * @param manyToManyInverseSide the manyToManyInverseSide to set
	 */
	public final void setManyToManyInverseSide(boolean manyToManyInverseSide) {
		this.manyToManyInverseSide = manyToManyInverseSide;
	}



	/**
	 * Return true if this property had a name clash
	 *
	 * @return the clashResolved
	 */
	public final boolean isClashResolved() {
		return this.clashResolved;
	}



	/**
	 * Set to true if this property had a name clash
	 *
	 * @param clashResolved the clashResolved to set
	 */
	public final void setClashResolved(boolean clashResolved) {
		this.clashResolved = clashResolved;
	}



	/**
	 * Return the human friendly equivalent of this property
	 *
	 * @return Javaname
	 */
	public String getNiceJavaName() {
		return niceEnglish(this.javaName);
	}



	/**
	 * Sets the human friendly equivalent of this property
	 *
	 * @return property name
	 */
	public String getNicePropertyName() {
		return niceEnglish(this.propertyName);
	}



	/**
	 * Convenience function to return a human-friendly name
	 *
	 * @param input
	 * @return nice english
	 */
	private String niceEnglish(String input) {
		String result = input;

		if (this.isManyToMany() || this.isOneToMany()) {
			result = SyncUtils.pluralize(input);
		}
		return result;
	}



	/**
	 * Return the javaname with the first character set to lowercase
	 *
	 * @return the javaname with the first character set to lowercase
	 */
	public String getLowerCaseFriendlyName() {
		return this.javaName.substring(0, 1).toLowerCase() + this.javaName.substring(1);
	}



	/**
	 * @return the naturalKey
	 */
	public final boolean isNaturalKey() {
		return this.naturalKey;
	}



	/**
	 * Set to true if this key is marked as being a "natural key"
	 *
	 * @param naturalKey the naturalKey to set
	 */
	public final void setNaturalKey(boolean naturalKey) {
		this.naturalKey = naturalKey;
	}



	/**
	 * @return list
	 */
	public final List<String> getValidatorAnnotations() {
		LinkedList<String> result = new LinkedList<String>();

		if (State.getInstance().isEnableJodaSupport()){
		    if (this.javaType.equalsIgnoreCase("DateTime")){
		        result.add("@Type(type=\"org.joda.time.contrib.hibernate.PersistentDateTime\")");
		    }
		}
		if (State.getInstance().isEnableHibernateValidator()) {

			if (!this.oneToMany &&  !this.manyToOne &&  !this.manyToMany && !this.isPFK() && !this.isOneToOne()){
				if (!this.isNullable()) {
					result.add("@Mandatory");
				}

				if (this.hasLength()) {
					result.add("@Length(max="+this.length+")");
				}
			} else if (!this.isPFK()) {
				validatorAnnotated = true;
				result.add("@Valid");
			}
		}

		return result;


	}
	/**
	 * Get the annotation for this property. Convenience function to make template nicer
	 *
	 * @return Column annotation
	 */
	public final String getColumnAnnotation() {
		LinkedList<String> annotation = new LinkedList<String>();
		StringBuffer sb = new StringBuffer();
		if (!this.manyToOne && !this.manyToMany && (!this.isPFK() || this.getClazz().isEmbeddable()) && !this.isOneToOne()) {
			if ((!this.clazz.isCompositePrimaryKey() && this.idField) || (this.fieldObj.getName().indexOf("_") > 0)) {
				annotation.add(String.format("name = \"%s\"", this.fieldObj.getName()));
			}
			if (this.clazz.isSubclass() && this.idField) {
				annotation.add("insertable = false, updatable = false");
			}

			if (!this.isNullable() && !State.getInstance().isEnableHibernateValidator()) {
				annotation.add("nullable = false");
			}

			if (this.fieldObj.isAliased()){
				annotation.add("name = \""+this.fieldObj.getName()+"\"");
			}
			if (this.hasLength()) {
				annotation.add("length = " + this.length);
			}

			for (String tmp : annotation) {
				sb.append(tmp);
				sb.append(", ");
			}
			if (sb.length() > 0) {
				sb.deleteCharAt(sb.lastIndexOf(","));
				sb.insert(0, "@Column( ");
				sb.append(" )");
			}
		}
		return sb.toString().trim();
	}



	/**
	 * Return true if this is a compositeManyToOne
	 *
	 * @return the compositeManyToOne
	 */
	public final boolean isCompositeManyToOne() {
		return this.compositeManyToOne;
	}



	/**
	 * Set to true if this is a compositeManyToOne
	 *
	 * @param compositeManyToOne the compositeManyToOne to set
	 */
	public final void setCompositeManyToOne(boolean compositeManyToOne) {
		this.compositeManyToOne = compositeManyToOne;
	}



	/**
	 * @return the oneToOne
	 */
	public final boolean isOneToOne() {
		return this.oneToOne;
	}



	/**
	 * @param oneToOne the oneToOne to set
	 */
	public final void setOneToOne(boolean oneToOne) {
		this.oneToOne = oneToOne;
	}



	/**
	 * @return the oneToOneLink
	 */
	public final PropertyObj getOneToOneLink() {
		return this.oneToOneLink;
	}



	/**
	 * @param oneToOneLink the oneToOneLink to set
	 */
	public final void setOneToOneLink(PropertyObj oneToOneLink) {
		this.oneToOneLink = oneToOneLink;
	}



	/**
	 * @return the oneToOneKey
	 */
	public final KeyObj getOneToOneKey() {
		return this.OneToOneKey;
	}



	/**
	 * @param oneToOneKey the oneToOneKey to set
	 */
	public final void setOneToOneKey(KeyObj oneToOneKey) {
		this.OneToOneKey = oneToOneKey;
	}



	/**
	 * @return the oneTooneInverseSide
	 */
	public final boolean isOneTooneInverseSide() {
		return this.oneTooneInverseSide;
	}



	/**
	 * @param oneTooneInverseSide the oneTooneInverseSide to set
	 */
	public final void setOneTooneInverseSide(boolean oneTooneInverseSide) {
		this.oneTooneInverseSide = oneTooneInverseSide;
	}



	/**
	 * @return enum
	 */
	public GeneratorEnum getGeneratorType() {
		return generatorType;
	}



	/**
	 * @param generatorType
	 */
	public void setGeneratorType(GeneratorEnum generatorType) {
		this.generatorType = generatorType;
	}



	/**
	 * @return getter
	 */
	public boolean isGeneratedValueAuto() {
		return GeneratorEnum.AUTO.equals(this.generatorType);
	}



	/**
	 * @return getter
	 */
	public boolean isGeneratedValueGUID() {
		return GeneratorEnum.GUID.equals(this.generatorType);
	}



	/**
	 * @return getter
	 */
	public boolean isGeneratedValueUUID() {
		return GeneratorEnum.UUID.equals(this.generatorType);
	}


    /**
     * @return getter
     */
    public boolean isGeneratedValueIdAware() {
        return GeneratorEnum.IDAWARE.equals(this.generatorType);
    }


	/**
	 * @return genvalue custom
	 */
	public boolean isGeneratedValueCustom() {
		return GeneratorEnum.CUSTOM.equals(this.generatorType);
	}

	/**
	 * @return genvalue custom
	 */
	public boolean isGeneratedValuePKS() {
		return GeneratorEnum.PKS.equals(this.generatorType);
	}


	/**
	 * @return genvalue
	 */
	public boolean isGeneratedValue() {
		return generatedValue;
	}



	/**
	 * @param generatedValue
	 */
	public void setGeneratedValue(boolean generatedValue) {
		this.generatedValue = generatedValue;
	}



	public boolean isRequiresManualSave() {
		return requiresManualSave;
	}



	public void setRequiresManualSave(boolean requiresManualSave) {
		this.requiresManualSave = requiresManualSave;
	}



	/**
	 * @return the oneToNBackLinkDisabled
	 */
	public final boolean isOneToNBackLinkDisabled() {
		return oneToNBackLinkDisabled;
	}



	/**
	 * @param oneToNBackLinkDisabled the oneToNBackLinkDisabled to set
	 */
	public final void setOneToNBackLinkDisabled(boolean oneToNBackLinkDisabled) {
		this.oneToNBackLinkDisabled = oneToNBackLinkDisabled;
	}

	/** If the config file specifies that cascading is allowed from this many-to-one
	 * field, return true.
	 * @return true/false
	 */
	public boolean isManyToOneCascadeEnabledByConfig() {
		return isXToXCascadeEnabledByConfig(State.getInstance().getManyToOneCascadeEnabled(), this.manyToOneLink);
	}

	/** If the config file specifies that cascading is allowed from this one-to-many
	 * field, return true.
	 * @return true/false
	 */
	public boolean isOneToManyCascadeEnabledByConfig() {
		return isXToXCascadeEnabledByConfig(State.getInstance().getOneToManyCascadeEnabled(), this.OneToManyLink);
	}

	/** If the config file specifies that cascading is allowed from this one-to-one
	 * field, return true.
	 * @return true/false
	 */
	public boolean isOneToOneCascadeEnabledByConfig() {
		return isXToXCascadeEnabledByConfig(State.getInstance().getOneToOneCascadeEnabled(), this.oneToOneLink);
	}
	/** If the config file specifies that cascading is allowed from this many-to-many
	 * field, return true.
	 * @return true/false
	 */
	public boolean isManyToManyCascadeEnabledByConfig() {
		return isXToXCascadeEnabledByConfig(State.getInstance().getManyToManyCascadeEnabled(), this.manyToManyLink.getDstProperty());
	}

	/** Checks to see if this property has a match in the cascade list to return if enabled or disabled.
	 * @param state
	 * @param propertyObj 
	 * @return t/f
	 */
	private boolean isXToXCascadeEnabledByConfig(TreeMap<String, CascadeState> state, PropertyObj target) {
		if (target == null){
			return false;
		}
        String classPackage = this.getClazz().getClassPackage();
        String className = this.getClazz().getClassName();

        String targetClassPackage = target.getClazz().getClassPackage();
        String targetClassName = target.getClazz().getClassName();
        String targetPropertyName = target.getPropertyName();

		
        CascadeState match = matchCascade(state,  
		"*.*.*",
		"*."+className+".*",
		"*."+className+"."+propertyName,
		classPackage+".*."+propertyName,
		classPackage+".*.*",
		classPackage+"."+className+".*", 
		"*.*."+propertyName,
		classPackage+"."+className+"."+propertyName);

		if (match == null) {
		      match = matchCascade(state, "to:*.*.*",
		        "to:*."+targetClassName+".*",
		        "to:*."+targetClassName+"."+targetPropertyName,
		        "to:"+targetClassPackage+".*."+targetPropertyName,
		        "to:"+targetClassPackage+".*.*",
		        "to:"+targetClassPackage+"."+targetClassName+".*",
		        "to:*.*."+targetPropertyName,
		        "to:"+targetClassPackage+"."+targetClassName+"."+targetPropertyName);
		}
		// get the default
		boolean enableCascading = state.get("*").isCascadeEnabled();

		if (match != null) {
			// we're using: default... EXCEPT
			enableCascading = match.isCascadeEnabled();
		}


		return enableCascading;
	}
	
	   /** Checks to see if this property has a match in the cascade list to return if enabled or disabled.
     * @param state
     * @param propertyObj 
     * @return t/f
     */
    private Set<String> getXToXCascadeType(TreeMap<String, CascadeState> state, PropertyObj target) {

        String classPackage = this.getClazz().getClassPackage();
        String className = this.getClazz().getClassName();

        String targetClassPackage = target.getClazz().getClassPackage();
        String targetClassName = target.getClazz().getClassName();
        String targetPropertyName = target.getPropertyName();

        
        CascadeState match = matchCascade(state, "*.*.*", "*."+className+".*", 
        "*."+className+"."+this.propertyName,
        classPackage+".*."+this.propertyName,
        classPackage+".*.*",
        classPackage+"."+className+".*",
        "*.*."+this.propertyName,
        classPackage+"."+className+"."+this.propertyName);

        if (match == null) {
              match = 
                  matchCascade(state, 
                  "to:*.*.*",
                "to:*."+targetClassName+".*",
                "to:*."+targetClassName+"."+targetPropertyName,
                "to:"+targetClassPackage+".*."+targetPropertyName,
                "to:"+targetClassPackage+".*.*",
                "to:"+targetClassPackage+"."+targetClassName+".*",
                "to:*.*."+targetPropertyName,
                "to:"+targetClassPackage+"."+targetClassName+"."+targetPropertyName);
        }
        // get the default
        Set<String> cascadeType = state.get("*").getCascadeType();

        if (match != null) {
            // we're using: default... EXCEPT
            cascadeType  = match.getCascadeType();
        }


        return cascadeType;
    }

    private CascadeState matchCascade(TreeMap<String, CascadeState> state, String... matchString ){
        CascadeState match = null;
        for (String item: matchString){
            CascadeState cascade = state.get(item);
            if (cascade != null) {
                match = cascade;
                break;
            }
        }
        return match;
    }
    
	public boolean isTestValueOverride(){
        TreeMap<String, String> defTest = State.getInstance().getDefaultTestValues().get(this.clazz.getTableObj().getFullTableName().toUpperCase());
        if (defTest != null) {
            return defTest.get(this.propertyName.toUpperCase()) != null;
        }
        return false;

    }

	/** If the config file specifies that cascading is allowed from this many-to-one
	 * field, return true.
	 * @return true/false
	 */
	public boolean isManyToOneLazyEnabledByConfig() {
		return isXToXLazyEnabledByConfig(State.getInstance().getManyToOneLazyEnabled());
	}
	
	
	/** If the config file specifies that cascading is allowed from this one-to-many
	 * field, return true.
	 * @return true/false
	 */
	public boolean isOneToManyLazyEnabledByConfig() {
		return isXToXLazyEnabledByConfig(State.getInstance().getOneToManyLazyEnabled());
	}

	/** If the config file specifies that cascading is allowed from this one-to-one
	 * field, return true.
	 * @return true/false
	 */
	public boolean isOneToOneLazyEnabledByConfig() {
		return isXToXLazyEnabledByConfig(State.getInstance().getOneToOneLazyEnabled());
	}
	/** If the config file specifies that cascading is allowed from this many-to-many
	 * field, return true.
	 * @return true/false
	 */
	public boolean isManyToManyLazyEnabledByConfig() {
		return isXToXLazyEnabledByConfig(State.getInstance().getManyToManyLazyEnabled());
	}

	/** Checks to see if this property has a match in the Lazy list to return if enabled or disabled.
	 * @param state
	 * @return t/f
	 */
	private boolean isXToXLazyEnabledByConfig(TreeMap<String, Boolean> state) {

		String classPackage = this.getClazz().getClassPackage();
		String className = this.getClazz().getClassName();

		boolean match = state.containsKey("*.*.*") ||
		state.containsKey("*."+className+".*") ||
		state.containsKey("*."+className+"."+propertyName) ||
		state.containsKey(classPackage+".*."+propertyName) ||
		state.containsKey(classPackage+".*.*") ||
		state.containsKey(classPackage+"."+className+".*") ||
		state.containsKey("*.*."+propertyName) ||
		state.containsKey(classPackage+"."+className+"."+propertyName);

		// get the default
		boolean enableLazy = state.get("*");

		if (match) {
			// we're using: default... EXCEPT
			enableLazy = !enableLazy;
		}


		return enableLazy;
	}



	/**
	 * @return the methodLevelSettersPrecondition
	 */
	public final TreeSet<String> getMethodLevelSettersPrecondition() {
		return methodLevelSettersPrecondition;
	}



	/**
	 * @param methodLevelSettersPrecondition the methodLevelSettersPrecondition to set
	 */
	public final void setMethodLevelSettersPrecondition(
			TreeSet<String> methodLevelSettersPrecondition) {
		this.methodLevelSettersPrecondition = methodLevelSettersPrecondition;
	}



	/**
	 * @return the methodLevelGettersPrecondition
	 */
	public final TreeSet<String> getMethodLevelGettersPrecondition() {
		return methodLevelGettersPrecondition;
	}



	/**
	 * @param methodLevelGettersPrecondition the methodLevelGettersPrecondition to set
	 */
	public final void setMethodLevelGettersPrecondition(
			TreeSet<String> methodLevelGettersPrecondition) {
		this.methodLevelGettersPrecondition = methodLevelGettersPrecondition;
	}



	/**
	 * @return the methodLevelSettersPostcondition
	 */
	public final TreeSet<String> getMethodLevelSettersPostcondition() {
		return methodLevelSettersPostcondition;
	}



	/**
	 * @param methodLevelSettersPostcondition the methodLevelSettersPostcondition to set
	 */
	public final void setMethodLevelSettersPostcondition(
			TreeSet<String> methodLevelSettersPostcondition) {
		this.methodLevelSettersPostcondition = methodLevelSettersPostcondition;
	}



	/**
	 * @return the methodLevelGettersPostcondition
	 */
	public final TreeSet<String> getMethodLevelGettersPostcondition() {
		return methodLevelGettersPostcondition;
	}



	/**
	 * @param methodLevelGettersPostcondition the methodLevelGettersPostcondition to set
	 */
	public final void setMethodLevelGettersPostcondition(
			TreeSet<String> methodLevelGettersPostcondition) {
		this.methodLevelGettersPostcondition = methodLevelGettersPostcondition;
	}



	/**
	 * @return the validatorAnnotated
	 */
	public final boolean isValidatorAnnotated() {
		return this.validatorAnnotated;
	}



	/**
	 * @param validatorAnnotated the validatorAnnotated to set
	 */
	public final void setValidatorAnnotated(boolean validatorAnnotated) {
		this.validatorAnnotated = validatorAnnotated;
	}



	/**
	 * @return the inverseLink
	 */
	public PropertyObj getInverseLink() {
		return this.inverseLink;
	}



	/**
	 * @param inverseLink the inverseLink to set
	 */
	public void setInverseLink(PropertyObj inverseLink) {
		this.inverseLink = inverseLink;
	}

	/**
	 * @return t/f
	 */
	public boolean isInverseLinkDisabled(){
		return this.inverseLink == null || this.inverseLink.isOneToNBackLinkDisabled();
	}
	
	public boolean isExcludedFromEquality() {
	    String fname = this.getFieldObj().getName();
	    Set<String> excludes = State.getInstance().getEqualityExcludes();
	    return excludes.contains("*.*."+fname) ||
	    excludes.contains("*."+this.clazz.getTableObj().getDbName()+"."+fname) ||
	    excludes.contains(this.clazz.getTableObj().getDbCat()+".*."+fname);
	    
	}
	
	public boolean isTransientField() {
	    String fname = this.getFieldObj().getName();
	    Set<String> transientField = State.getInstance().getTransientFields();
	    return transientField.contains("*.*."+fname) ||
	    transientField.contains("*."+this.clazz.getTableObj().getDbName()+"."+fname) ||
	    transientField.contains(this.clazz.getTableObj().getDbCat()+".*."+fname);
	    
	}
	/** Returns true if the config file says that this field came from a db field
	 * that has a unique index on it. 
	 * 
	 *
	 * @return
	 */
	public boolean isUnique(){
	    String table = this.fieldObj.getTableObj().getFullTableName();
	    TreeSet<String> fields = State.getInstance().getUniqueKeys().get(table);
	    return fields != null && fields.contains(this.fieldObj.getName());
	    
	}
}
