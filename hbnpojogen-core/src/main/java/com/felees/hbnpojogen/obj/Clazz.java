package com.felees.hbnpojogen.obj;


import java.io.Serializable;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import com.felees.hbnpojogen.CaseInsensitiveComparator;
import com.felees.hbnpojogen.HbnPojoGen;
import com.felees.hbnpojogen.ObjectPair;
import com.felees.hbnpojogen.PackageTypeEnum;
import com.felees.hbnpojogen.State;
import com.felees.hbnpojogen.SyncUtils;
import com.felees.hbnpojogen.VelocityHelper;
import com.felees.hbnpojogen.db.TableObj;



/**
 * Representation of a class
 *
 * @author wallacew
 *
 */
public class Clazz
implements Serializable, Comparable<Clazz> {

	/**
	 *
	 */
	private static final long serialVersionUID = 3912526350175342614L;
	/**
	 * If true, one of the properties (=fields) has a default value set in the db so we need to
	 * markup our class definition.
	 */
	private boolean dynamicUpdatesInserts;
	/** The table equivalent of the catalog */
	private String classPackage;
	/** If config specified suffix, this holds it.*/
	private String suffix = "";
	/** target class file */
	private String className;
	/** this class extends another */
	private boolean subclass;
	/** this class has subclasses */
	private boolean superclass;
	/** Class Type -- chrisp */
	private String classType = "";
	/** link to the table object we've used to arrive at this class */
	private TableObj tableObj;
	/** Links to properties in this class */
	private TreeMap<String, PropertyObj> properties = new TreeMap<String, PropertyObj>(new CaseInsensitiveComparator());
	/** Links to properties in this class */
	private TreeMap<String, PropertyObj> hiddenCurrencyProperties = new TreeMap<String, PropertyObj>(new CaseInsensitiveComparator());
	/** imports needed by this class */
	private TreeSet<String> imports = new TreeSet<String>(new CaseInsensitiveComparator());
	/** custom interfaces as specified by config */
	private TreeSet<String> customInterfaces = new TreeSet<String>(new CaseInsensitiveComparator());
	/** custom interfaces as specified by config */
	private TreeSet<String> customExtends = new TreeSet<String>(new CaseInsensitiveComparator());
	/** backed by a view. */
	private boolean tableIsAView;




	/** link to embeddable class */
	private Clazz embeddableClass = null;
	/** link to embeddable class */
	private Clazz embeddedFrom = null;
	/** this class contains a composite key */
	private boolean compositePrimaryKey;
	/**
	 * if true, this class is annotated with
	 *
	 * @embeddable
	 */
	private boolean embeddable;
	/** bit 1 = AUTO, bit 2= UID, bit 3 = GUID, 4 = Custom, 5 = PKS */
	private BitSet generatorEnumSupport = new BitSet(10);
	/** if class extends another, this is where we're linking to */
	private PropertyObj extendsFrom;
	/**
	 * This is the property on this *subclass* that is being used to link to the "extendsFrom".
	 */
	private PropertyObj extendingProperty;
	/** From config file - just anything to be pasted on top of the class as annotation */
	private String classAnnotation;
	/** From config file - just anything to be pasted at bottom of class */
	private String classCustomCode;
	/** From config file - just anything to be pasted at bottom of fields list */
	private String classCustomCodeFields;

	/** Flag indicates if config file wants this class to be considered as a link table. */
	private boolean joinTable;
	/**
	 * If true, the table is a join table that only contains fields to link two sides with each
	 * other
	 */
	private boolean hiddenJoinTable;
	/** List of mappings to bounce off a link table */
	private TreeSet<JoinTable> joinMappings = new TreeSet<JoinTable>();
	/**
	 * For the case of classes that have a many-to-one but cascading was switched off from the
	 * config file, this will contain stuff like a.getB().getC(). This is only used externally for
	 * the data fixtures project. key = ref to class we'll get in the end, val = a.getB().getC()
	 */
	private LinkedList<ObjectPair<Clazz, String>> uncascadedOps = new LinkedList<ObjectPair<Clazz, String>>();
	/**
	 * If true, this class is named like another class in another schema so we'd need to expand the
	 * classname to use the full classname in certain cases
	 */
	private boolean nameAmbiguityPossible;

	/**
	 * Denotes the table as part of the cyclic table exclusion list - chrisp
	 */
	private boolean cyclicExclusionTable = false;
	/**
	 * Denotes the table as part of the cyclic table exclusion replacement list - chrisp
	 */
	private boolean cyclicExclusionReplacementTable = false;

	/** A list of enums that is used in the getXXXsubclassType(); */
	private List<String> subclassEnum = new LinkedList<String>();
	/** The className as computed internally prior to adding optional suffixes
	 * defined by the config.
	 */
	private String classNameNoSuffix;



	/**
	 * Gets a list of enums that is used in the getXXXsubclassType()
	 *
	 * @return enum list
	 */
	public List<String> getSubclassEnum() {
		return this.subclassEnum;
	}



	/**
	 * Sets
	 *
	 * @param subclassEnum
	 */
	public void setSubclassEnum(List<String> subclassEnum) {
		this.subclassEnum = subclassEnum;
	}



	/**
	 * Gets
	 *
	 * @return t/f
	 */
	public boolean isCyclicExclusionReplacementTable() {
		return cyclicExclusionReplacementTable;
	}



	/**
	 * Sets
	 *
	 * @param cyclicExclusionReplacementTable
	 */
	public void setCyclicExclusionReplacementTable(boolean cyclicExclusionReplacementTable) {
		this.cyclicExclusionReplacementTable = cyclicExclusionReplacementTable;
	}



	/**
	 * Returns a list of all properties for use in a constructor
	 *
	 * @return A list of all properties for use in a constructor
	 */
	public TreeMap<String, PropertyObj> getPropertiesForConstructor() {
		TreeMap<String, PropertyObj> result = new TreeMap<String, PropertyObj>(new CaseInsensitiveComparator());

		for (Entry<String, PropertyObj> prop : this.properties.entrySet()) {
			if (!prop.getValue().isNullable() && (!prop.getValue().isOneToMany()) && (!prop.getValue().isManyToMany() && !prop.getValue().isOneToOne())) {
				result.put(prop.getKey(), prop.getValue());
			}
		}
		return result;
	}



	/**
	 * Convenience - returns all normal + all extended properties in one TreeMap for cleaner
	 * velocity files
	 *
	 * @return all properties
	 */
	public TreeMap<String, PropertyObj> getAllProperties() {
		TreeMap<String, PropertyObj> result = new TreeMap<String, PropertyObj>(new CaseInsensitiveComparator());
		result.putAll(this.properties);

		PropertyObj extendsClass = this.extendsFrom;
		while (extendsClass != null) {
			result.putAll(extendsClass.getClazz().getProperties());
			if (extendsClass.getClazz().getExtendsFrom() != extendsClass) {
				extendsClass = extendsClass.getClazz().getExtendsFrom();
			}
			else {
				extendsClass = null;
			}
		}
		return result;
	}


	/**
	 * Like getAllProperties except that the treemap returned may have proeprtyName as the key as
	 * opposed to field_name.
	 *
	 * @param propertyNameAsKey
	 * @return a treemap of properties.
	 */
	public TreeMap<String, PropertyObj> getAllProperties(boolean propertyNameAsKey) {
		if (!propertyNameAsKey) {
			return getAllProperties();
		}

		TreeMap<String, PropertyObj> result = new TreeMap<String, PropertyObj>(new CaseInsensitiveComparator());
		for (PropertyObj prop : this.properties.values()) {
			result.put(prop.getPropertyName(), prop);
		}

		PropertyObj extendsClass = this.extendsFrom;
		while (extendsClass != null) {
			for (PropertyObj prop : extendsClass.getClazz().getProperties().values()) {
				result.put(prop.getPropertyName(), prop);
			}

			if (extendsClass.getClazz().getExtendsFrom() != extendsClass) {
				extendsClass = extendsClass.getClazz().getExtendsFrom();
			}
			else {
				extendsClass = null;
			}
		}

		return result;

	}



	/**
	 * Keep moving up in the object hierarchy to reach the topmost class. This is needed because
	 * proxies always point to the top level.
	 *
	 * @return Clazz
	 */
	public Clazz getProxyTopLevelClass() {
		Clazz result = this;

		while (result.isSubclass()) {
			result = result.getExtendsFrom().getClazz();
		}

		return result;
	}


	public TreeMap<String, PropertyObj> getAllPropertiesIncludingOfFirstNonAbstractClass() {
		TreeMap<String, PropertyObj> result = new TreeMap<String, PropertyObj>(new CaseInsensitiveComparator());
		result.putAll(getAllProperties());
		if (this.isAbstractClass()) {
			Clazz clazz = VelocityHelper.findNonAbstractSubclass(this, State.getInstance().getClasses());
			if (clazz != null) {
				result.putAll(clazz.getAllProperties());
			}
		}

		return result;
	}

	public TreeMap<String, PropertyObj> getAllPropertiesIncludingAllNonAbstractClass() {
		TreeMap<String, PropertyObj> result = new TreeMap<String, PropertyObj>(new CaseInsensitiveComparator());
		result.putAll(getAllProperties());
		if (this.isAbstractClass()) {
			List<Clazz> classes = VelocityHelper.findAllNonAbstractSubclass(this, State.getInstance().getClasses());
			if (classes != null) {
				for (Clazz clazz : classes) {
					result.putAll(clazz.getAllProperties());
				}
			}
		}

		return result;

	}

	/**
	 * Convenience - returns all normal + all extended properties in one TreeMap for cleaner
	 * velocity files
	 *
	 * @return all properties
	 */
	 public TreeMap<String, PropertyObj> getAllPropertiesNoBackLinks() {
		 TreeMap<String, PropertyObj> result = new TreeMap<String, PropertyObj>();
		 for (Entry<String, PropertyObj> entry : getAllProperties().entrySet()) {
			 if (!entry.getValue().isOneToNBackLinkDisabled()) {
				 result.put(entry.getKey(), entry.getValue());
			 }
		 }
		 return result;
	 }



	 /**
	  * Convenience - returns all normal + all extended properties in one TreeMap for cleaner
	  * velocity files
	  *
	  * @return returns all normal + all extended properties in one TreeMap for cleaner velocity
	  *         files
	  */
	 public TreeMap<String, PropertyObj> getAllPropertiesWithoutPFK() {
		 TreeMap<String, PropertyObj> result = null;
		 if (!this.isEmbeddable()) {
			 result = new TreeMap<String, PropertyObj>(new CaseInsensitiveComparator());
			 result.putAll(getPropertiesWithoutPFK());

			 PropertyObj extendsClass = this.extendsFrom;
			 while (extendsClass != null) {
				 result.putAll(extendsClass.getClazz().getPropertiesWithoutPFK(extendsClass.getClazz().getProperties()));
				 if (extendsClass.getClazz().getExtendsFrom() != extendsClass) {
					 extendsClass = extendsClass.getClazz().getExtendsFrom();
				 }
				 else {
					 extendsClass = null;
				 }
			 }
		 }
		 else {
			 result = getAllProperties();
		 }
		 return result;
	 }


	 public TreeMap<String, PropertyObj> getAllPropertiesWithoutPFKNoBackLinks() {
		 return filterAwayBackLinks(getAllPropertiesWithoutPFK());
	 }


	 /**
	  * Returns a list of all properties without primary foreign keys
	  *
	  * @return PropertiesWithoutPFK
	  */
	 public TreeMap<String, PropertyObj> getPropertiesWithoutPFK() {
		 return getPropertiesWithoutPFK(this.properties);
	 }

	 public TreeMap<String, PropertyObj> getPropertiesNoBackLinks() {
		 return filterAwayBackLinks(getProperties());
	 }



	 /**
	  * Returns a list of all properties without primary foreign keys
	  *
	  * @return PropertiesWithoutPFK
	  */
	 public TreeMap<String, PropertyObj> getPropertiesWithoutPFKNoBackLinks() {
		 return filterAwayBackLinks(getPropertiesWithoutPFK());
	 }

	 /**
	  * Returns a list of all properties without primary foreign keys
	  *
	  * @param properties
	  * @return PropertiesWithoutPFK
	  */
	 @SuppressWarnings("hiding")
	 public TreeMap<String, PropertyObj> getPropertiesWithoutPFK(TreeMap<String, PropertyObj> properties) {
		 TreeMap<String, PropertyObj> result = new TreeMap<String, PropertyObj>(new CaseInsensitiveComparator());

		 for (Entry<String, PropertyObj> prop : properties.entrySet()) {
			 if (prop.getValue().isOneToMany() || !prop.getValue().isPFK() || prop.getValue().isManyToMany() || (prop.getValue().isComposite())) {
				 result.put(prop.getKey(), prop.getValue());
			 }
		 }
		 return result;
	 }


	 public TreeMap<String, PropertyObj> filterAwayBackLinks(TreeMap<String, PropertyObj> properties) {
		 TreeMap<String, PropertyObj> result = new TreeMap<String, PropertyObj>(new CaseInsensitiveComparator());

		 for (Entry<String, PropertyObj> prop : properties.entrySet()) {
			 if (!(prop.getValue().isOneToNBackLinkDisabled())) {
				 result.put(prop.getKey(), prop.getValue());
			 }
		 }
		 return result;
	 }




	 /**
	  * Returns true if one of the fields in this class is marked as autoincrement
	  *
	  * @return true if one of the fields in this class is marked as autoincrement
	  */
	 public boolean hasAutoInc() {
		 boolean result = false;
		 for (PropertyObj propery : this.properties.values()) {
			 if (propery.isAutoInc()) {
				 result = true;
				 break;
			 }
		 }
		 return result;

	 }

	 /**
	  * Returns true if one of the fields in this class is marked as autoincrement
	  *
	  * @return true if one of the fields in this class is marked as autoincrement
	  */
	 public boolean hasMoneyWithCurrencyType() {
		 boolean result = false;
		 for (PropertyObj propery : this.properties.values()) {
			 if (propery.isMoneyType()) {
				 result = true;
				 break;
			 }
		 }
		 return result;

	 }


	 /**
	  * Returns a name that is suitable for a property name eg fooClass for the class named FooClass
	  *
	  * @return a name that is suitable for a property name
	  */
	 public String getClassPropertyName() {
		 return this.className.substring(0, 1).toLowerCase() + this.className.substring(1);
	 }



	 /**
	  * Returns the plural name of this class
	  *
	  * @return The plural name of this class
	  */
	 public String getNiceClassName() {
		 return SyncUtils.pluralize(this.className);
	 }



	 /**
	  * Returns The plural name of this class' property name
	  *
	  * @return The plural name of this class' property name
	  */
	 public String getNiceClassPropertyName() {
		 return SyncUtils.pluralize(getClassPropertyName());
	 }



	 /**
	  * Count the number of many to one properties in normal and extended properties
	  *
	  * @return the number of many to one properties in normal and extended properties
	  */
	 public int countManyToOneProperties() {
		 int result = 0;
		 for (PropertyObj property : getAllProperties().values()) {
			 if (property.isManyToOne()) {
				 result++;
			 }
		 }

		 return result;

	 }


	 /**
	  * Count the number of many to one properties in normal and extended properties
	  *
	  * @return the number of many to one properties in normal and extended properties
	  */
	 public int countOneToOneProperties() {
		 int result = 0;
		 for (PropertyObj property : getAllProperties().values()) {
			 if (property.isOneToOne()) {
				 result++;
			 }
		 }

		 return result;

	 }


	 /**
	  * Count the number of many to one properties in normal and extended properties excluding
	  * inverse.
	  *
	  * @return the number of many to one properties in normal and extended properties
	  */
	 public int countOneToOnePropertiesWithoutInverseSide() {
		 int result = 0;
		 for (PropertyObj property : getAllProperties().values()) {
			 if (property.isOneToOne() && !property.isOneTooneInverseSide()) {
				 result++;
			 }
		 }

		 return result;

	 }

	 /**
	  *
	  *
	  * @return t/f
	  */
	 public int countManyToOneAndCompositePropertiesAndOneToOne() {
		 int result = 0;
		 for (PropertyObj property : getAllProperties().values()) {
			 if (property.isComposite() || property.isManyToOne() || (property.isOneToOne() && !property.isOneTooneInverseSide())) {
				 result++;
			 }
		 }

		 return result;

	 }



	 /**
	  * Count the number of many to one properties in normal and extended properties
	  *
	  * @return the number of many to one properties
	  */
	 public int countManyToOneAndCompositeProperties() {
		 int result = 0;
		 for (PropertyObj property : getAllProperties().values()) {
			 if (property.isManyToOne() || property.isComposite()) {
				 result++;
			 }
		 }

		 return result;

	 }

	 /**
	  * Counts the fields that are NOT marked as many-to-many etc.
	  *
	  * @return no of fields
	  */
	 public int countNormalFields() {
		 int result = 0;
		 for (PropertyObj property : this.properties.values()) {
			 if ((!property.isManyToMany()) && (!property.isManyToOne()) && (!property.isOneToOne()) && (!property.isOneToMany()) && (!property.isIdField())) {
				 result++;
			 }
		 }
		 return result;
	 }

	 /**
	  * If a many-to-one field is present in one of this classes' fields or extended fields, return
	  * true
	  *
	  * @return true if we have a one-to-many
	  */
	 public boolean containsAManyToOneProperty() {
		 return countManyToOneProperties() > 0;
	 }


	 /**
	  * If a many-to-one field is present in one of this classes' fields or extended fields, return
	  * true
	  *
	  * @return true if we have a one-to-many
	  */
	 public boolean containsAOneToOneProperty() {
		 return countOneToOneProperties() > 0;
	 }



	 /**
	  * If a one-to-one field is present in one of this classes' fields or extended fields, return
	  * true
	  *
	  * @return true if we have a one-to-many
	  */
	 public boolean containsAOneToOnePropertyWithoutInverseSide() {
		 return countOneToOnePropertiesWithoutInverseSide() > 0;
	 }



	 /**
	  * Returns the properties linked to this class
	  *
	  * @return the properties linked to this class
	  */
	 public TreeMap<String, PropertyObj> getProperties() {
		 return this.properties;
	 }

	 public TreeMap<String, PropertyObj> getProperties(boolean propertyNameAsKey) {
		 if (!propertyNameAsKey) {
			 return getProperties();
		 }

		 TreeMap<String, PropertyObj> result = new TreeMap<String, PropertyObj>(new CaseInsensitiveComparator());
		 for (PropertyObj prop : this.properties.values()) {
			 result.put(prop.getPropertyName(), prop);
		 }

		 return result;
	 }





	 /**
	  * Sets the properties linked to this class
	  *
	  * @param properties
	  */
	 public void setProperties(TreeMap<String, PropertyObj> properties) {
		 this.properties = properties;
	 }



	 /**
	  * Returns true if class is a subclass
	  *
	  * @return true if class is a subclass
	  */
	 public boolean isSubclass() {
		 return this.subclass;
	 }



	 /**
	  * Flags this class as being a subclass of another
	  *
	  * @param subclass
	  */
	 public void setSubclass(boolean subclass) {
		 this.subclass = subclass;
	 }



	 /**
	  * Returns a list of imports
	  *
	  * @return a treeset of imports required for this class
	  */
	 public TreeSet<String> getImports() {
		 return this.imports;
	 }



	 /**
	  * Sets the imports related to this class
	  *
	  * @param imports
	  */
	 public void setImports(TreeSet<String> imports) {
		 this.imports = imports;
	 }



	 /**
	  * Returns true if this class is a superclass of some other class
	  *
	  * @return true/false
	  */
	 public boolean isSuperclass() {
		 return this.superclass;
	 }



	 /**
	  * Flags this class as being a superclass of another
	  *
	  * @param superclass
	  */
	 public void setSuperclass(boolean superclass) {
		 this.superclass = superclass;
	 }



	 /**
	  * Returns the classname of this class
	  *
	  * @return the classname of this class
	  */
	 public String getClassName() {
		 return this.className;
	 }



	 /**
	  * Sets the classname of this class
	  *
	  * @param className
	  */
	 public void setClassName(String className) {
		 //        String name = ;
		 //        this.suffix = suffixMap(this.classPackage, name);
		 //        this.classNameNoSuffix = name;
		 this.className = SyncUtils.removeUnderscores(className);
	 }



	 /**
	  * Returns true if this class has a suffix configured by the user
	  *
	  * @return suffix/no suffix
	  */
	 public boolean hasClassSuffix(){
		 return this.suffix != "";
	 }





	 /**
	  * Returns the classType of this class
	  *
	  * @return the classType of this class
	  */
	 public String getClassType() {
		 return this.classType;
	 }



	 /**
	  * Sets the classType of this class
	  *
	  * @param classType
	  */
	 public void setClassType(String classType) {
		 this.classType = classType;
	 }



	 /**
	  * Returns true if class is Abstract.
	  *
	  * @return t/f
	  */
	 public boolean isAbstractClass() {
		 if (this.classType.compareToIgnoreCase("abstract") == 0) {
			 return true;
		 }
		 return false;
	 }



	 /**
	  * Returns the link back to the table object
	  *
	  * @return the table reference
	  */
	 public TableObj getTableObj() {
		 return this.tableObj;
	 }



	 /**
	  * Sets the table object link
	  *
	  * @param tableObj
	  */
	 public void setTableObj(TableObj tableObj) {
		 this.tableObj = tableObj;
	 }



	 /**
	  * If we have an embeddable class, this is the link to it
	  *
	  * @return the embedded class reference
	  */
	 public Clazz getEmbeddableClass() {
		 return this.embeddableClass;
	 }


	 /**
	  * If we have an embeddable class as an id, return true
	  *
	  * @return t/f
	  */
	 public boolean hasEmbeddableClass() {
		 return this.embeddableClass != null;
	 }


	 /**
	  * Sets the link to the embeddable class
	  *
	  * @param embeddableClass
	  */
	 public void setEmbeddableClass(Clazz embeddableClass) {
		 this.embeddableClass = embeddableClass;
	 }



	 /**
	  * Sets if we have a composite primary key
	  *
	  * @param compositePrimaryKey
	  */
	 public void setCompositePrimaryKey(boolean compositePrimaryKey) {
		 this.compositePrimaryKey = compositePrimaryKey;
	 }



	 /**
	  * Returns true if class has a composite primary key, false otherwise
	  *
	  * @return true if class has a composite primary key
	  */
	 public boolean isCompositePrimaryKey() {
		 return this.compositePrimaryKey;
	 }



	 /**
	  * Returns true if class is flagged as embeddable, false otherwise
	  *
	  * @return true if class is flagged as embeddable
	  */
	 public boolean isEmbeddable() {
		 return this.embeddable;
	 }



	 /**
	  * Marks this class as being embeddable (or not)
	  *
	  * @param embeddable
	  */
	 public void setEmbeddable(boolean embeddable) {
		 this.embeddable = embeddable;
	 }



	 /**
	  * Link for class
	  *
	  * @param extendsFrom the extendsFrom to set
	  */
	 public final void setExtendsFrom(PropertyObj extendsFrom) {
		 this.extendsFrom = extendsFrom;
	 }



	 /**
	  * Return the extendsFrom
	  *
	  * @return the extendsFrom
	  */
	 public final PropertyObj getExtendsFrom() {
		 return this.extendsFrom;
	 }



	 /**
	  * Return true if this class has a composite key
	  *
	  * @return true/false
	  */
	 public boolean hasCompositeKey() {
		 return this.tableObj.getPrimaryKeys().size() > 1;
	 }



	 /**
	  * Return true if the given property is part of a composite key
	  *
	  * @param property
	  * @return true if property is part of a composite key
	  */
	 public boolean isCompositeKey(PropertyObj property) {
		 return this.tableObj.getPrimaryKeys().contains(property.getFieldObj().getName());
	 }



	 /*
	  * (non-Javadoc)
	  *
	  * @see java.lang.Object#toString()
	  */
	 @Override
	 public String toString() {
		 return this.className + " implementing table " + (this.isEmbeddable() ? " none (this is a composite PK class) " : this.tableObj.getDbName()) + "\n";
	 }



	 /**
	  * Return true if this class is representing a join table
	  *
	  * @return true/false
	  */
	 public final boolean isJoinTable() {
		 return this.joinTable;
	 }



	 /**
	  * Flags this class as being a join table
	  *
	  * @param joinTable the joinTable to set
	  */
	 public final void setJoinTable(boolean joinTable) {
		 this.joinTable = joinTable;
	 }



	 /**
	  * Returns a list of join table mappings
	  *
	  * @return the joinMappings
	  */
	 public final TreeSet<JoinTable> getJoinMappings() {
		 return this.joinMappings;
	 }



	 /**
	  * Sets the list of join table mappings
	  *
	  * @param joinMappings the joinMappings to set
	  */
	 public final void setJoinMappings(TreeSet<JoinTable> joinMappings) {
		 this.joinMappings = joinMappings;
	 }



	 /**
	  * Returns true if this class is only visible in the hibernate annotations world
	  *
	  * @return the hiddenJoinTable
	  */
	 public final boolean isHiddenJoinTable() {
		 return this.hiddenJoinTable;
	 }



	 /**
	  * Flags this class as being a hidden join table reference
	  *
	  * @param hiddenJoinTable the hiddenJoinTable to set
	  */
	 public final void setHiddenJoinTable(boolean hiddenJoinTable) {
		 this.hiddenJoinTable = hiddenJoinTable;
	 }



	 /**
	  * Returns
	  *
	  * @return the classPackage
	  */
	 public final String getClassPackage() {
		 return this.classPackage;
	 }



	 /**
	  * Sets the class package name
	  *
	  * @param classPackage the classCatalog to set
	  */
	 public final void setClassPackage(String classPackage) {
		 this.classPackage = SyncUtils.removeUnderscores(classPackage);
	 }



	 /**
	  * The com.foo.bar. (including the final dot, excluding the className)
	  *
	  * @return the classPrefix
	  */
	 public final String getClassPrefix() {
		 return String.format("%s.", SyncUtils.getConfigPackage(this.getTableObj().getDbCat(), PackageTypeEnum.OBJECT));
	 }



	 /**
	  * Return the complete class name
	  *
	  * @return The complete class name
	  */
	 public final String getFullClassName() {
		 return getClassPrefix() + this.className;
	 }



	 /**
	  * The full classname of the DAO
	  *
	  * @return the classDAO
	  */
	 public final String getClassDAO() {
		 return String.format("%s.%sDao", SyncUtils.getConfigPackage(this.getTableObj().getDbCat(), PackageTypeEnum.DAO), this.className);
		 // return String.format("%s.%s.model.dao.%s.%sDao", HbnPojoGen.topLevel,
		 // HbnPojoGen.projectName, this.classPackage, this.className);
	 }



	 /**
	  * The full classname of the DAO
	  *
	  * @return the classDAO
	  */
	 public final String getClassDAOPrefix() {
		 return String.format("%s.%s.", SyncUtils.getConfigPackage(this.getTableObj().getDbCat(), PackageTypeEnum.DAO), this.classPackage);
		 // return String.format("%s.%s.model.dao.%s.", HbnPojoGen.topLevel, HbnPojoGen.projectName,
		 // this.classPackage);
	 }



	 /**
	  * Return true if class might clash with another class having the same name (in a different
	  * package)
	  *
	  * @return the nameAmbiguityPossible
	  */
	 public final boolean isNameAmbiguityPossible() {
		 return this.nameAmbiguityPossible;
	 }



	 /**
	  * Sets if class might clash with another class having the same name (in a different package)
	  *
	  * @param nameAmbiguityPossible the nameAmbiguityPossible to set
	  */
	 public final void setNameAmbiguityPossible(boolean nameAmbiguityPossible) {
		 this.nameAmbiguityPossible = nameAmbiguityPossible;
	 }



	 /**
	  * Expand class name, if potential clash
	  *
	  * @return a string with the dotted expanded class
	  */
	 public final String getMaybeExpandClassName() {
		 String result = "";
		 if (this.nameAmbiguityPossible) {
			 result = this.getClassPrefix() + this.className;
		 }
		 else {
			 result = this.className;
		 }
		 return result;
	 }



	 /**
	  * Expand class name, if potential clash
	  *
	  * @return a string with the dotted expanded class
	  */
	 public final String getMaybeExpandClassWithPackage() {
		 String result = "";
		 if (this.nameAmbiguityPossible) {
			 result = SyncUtils.upfirstChar(this.getClassPackage()) + this.className;
		 }
		 else {
			 result = this.className;
		 }
		 return result;
	 }



	 /**
	  * Expand class name, if potential clash
	  *
	  * @return a string with the dotted expanded class
	  */
	 public final String getMaybeExpandDAO() {
		 String result = "";
		 if (this.nameAmbiguityPossible) {
			 result = SyncUtils.getConfigPackage(this.getTableObj().getDbCat(), PackageTypeEnum.DAO) + "." + this.className + "Dao";
		 }
		 else {
			 result = this.className + "Dao";
		 }

		 return result;
	 }



	 /**
	  * @return path
	  */
	 public final String getDAOBean() {
		 String result = "";
		 if (this.nameAmbiguityPossible) {
			 result = this.getClassDAOPrefix() + SyncUtils.lowerfirstChar(this.className) + "DaoImpl";
		 }
		 else {
			 result = SyncUtils.lowerfirstChar(this.className) + "DaoImpl";
		 }

		 return result;
	 }



	 /**
	  * "Hibernate"+this.classPackage+"DaoFactory" (convenience function)
	  *
	  * @return "Hibernate"+this.classPackage+"DaoFactory"
	  */
	 public final String getHibernateDAO() {
		 return "Hibernate" + SyncUtils.upfirstChar(this.classPackage) + "DaoFactory";
	 }



	 /**
	  * Returns this.classPackage+"DataPoolFactory" (convenience function)
	  *
	  * @return this.classPackage+"DataPoolFactory"
	  */
	 public final String getDataPoolFactory() {
		 return SyncUtils.upfirstChar(this.classPackage) + "DataPoolFactory";
	 }



	 /**
	  * Returns the full classname path of this class datapoolfactory (convenience function)
	  *
	  * @return this.classPackage+"DataPoolFactory"
	  */
	 public final String getFullDataPoolFactory() {
		 return String.format("%s.%s", SyncUtils.getConfigPackage(this.getTableObj().getDbCat(), PackageTypeEnum.FACTORY), SyncUtils.upfirstChar(this.classPackage) + "DataPoolFactory");

		 // return
		 // HbnPojoGen.topLevel+"."+HbnPojoGen.projectName+".factories."+this.classPackage+"."+SyncUtils.upfirstChar(this.classPackage)+"DataPoolFactory";
	 }

	 /**
	  * Return data pool factory bean
	  *
	  * @return data pool factory bean
	  */
	 public final String getDataPoolFactoryBean() {
		 return SyncUtils.lowerfirstChar(this.classPackage) + "DataPoolFactory";
	 }


	 /**
	  * Returns the full classname path of this class hibernatedaofactory (convenience function)
	  *
	  * @return a classpath
	  */
	 public final String getFullHibernateDAOFactory() {
		 return String.format("%s.%s", SyncUtils.getConfigPackage(this.getTableObj().getDbCat(), PackageTypeEnum.FACTORY), "Hibernate" + SyncUtils.upfirstChar(this.classPackage) + "DaoFactory");
		 // return
		 // HbnPojoGen.topLevel+"."+HbnPojoGen.projectName+".factories."+this.classPackage+".Hibernate"+SyncUtils.upfirstChar(this.classPackage)+"DaoFactory";
	 }



	 /**
	  * @return class annottion
	  */
	 public String getClassAnnotation() {
		 return classAnnotation;
	 }



	 /**
	  * @param classAnnotation
	  */
	 public void setClassAnnotation(String classAnnotation) {
		 this.classAnnotation = classAnnotation;
	 }



	 /**
	  * @return true if generated enum = auto
	  */
	 public boolean isGeneratedValueAuto() {
		 return this.generatorEnumSupport.get(GeneratorEnum.AUTO.ordinal());
	 }



	 /**
	  * @return true if generated enum = guid
	  */
	 public boolean isGeneratedValueGUID() {
		 return this.generatorEnumSupport.get(GeneratorEnum.GUID.ordinal());
	 }



	 /**
	  * @return true if generated enum = uuid
	  */
	 public boolean isGeneratedValueUUID() {
		 return this.generatorEnumSupport.get(GeneratorEnum.UUID.ordinal());
	 }



	 /**
	  * @return true if generated enum = uuid
	  */
	 public boolean isGeneratedValueUUIDWithoutDashes() {
		 return this.generatorEnumSupport.get(GeneratorEnum.UUIDWithoutDashes.ordinal());
	 }



	 /**
	  * @return true if generated enum = idaware
	  */
	 public boolean isGeneratedValueIDAware() {
		 return this.generatorEnumSupport.get(GeneratorEnum.IDAWARE.ordinal());
	 }


	 /**
	  * @return true if generated enum = cusom
	  */
	 public boolean isGeneratedValueCustom() {
		 return this.generatorEnumSupport.get(GeneratorEnum.CUSTOM.ordinal());
	 }



	 /**
	  * @return true if generated enum = cusom
	  */
	 public boolean isGeneratedValuePKS() {
		 return this.generatorEnumSupport.get(GeneratorEnum.PKS.ordinal());
	 }


	 /**
	  * @param enabled
	  */
	 public void setGeneratedValueAuto(boolean enabled) {
		 this.generatorEnumSupport.set(GeneratorEnum.AUTO.ordinal(), enabled);
	 }



	 /**
	  * @param enabled
	  */
	 public void setGeneratedValueGUID(boolean enabled) {
		 this.generatorEnumSupport.set(GeneratorEnum.GUID.ordinal(), enabled);
	 }



	 /**
	  * @param enabled
	  */
	 public void setGeneratedValueCustom(boolean enabled) {
		 this.generatorEnumSupport.set(GeneratorEnum.CUSTOM.ordinal(), enabled);
	 }



	 /**
	  * @param enabled
	  */
	 public void setGeneratedValuePKS(boolean enabled) {
		 this.generatorEnumSupport.set(GeneratorEnum.PKS.ordinal(), enabled);
	 }


	 /**
	  * @param enabled
	  */
	 public void setGeneratedValueIDAware(boolean enabled) {
		 this.generatorEnumSupport.set(GeneratorEnum.IDAWARE.ordinal(), enabled);
	 }

	 /**
	  * @param enabled
	  */
	 public void setGeneratedValueUUID(boolean enabled) {
		 this.generatorEnumSupport.set(GeneratorEnum.UUID.ordinal(), enabled);
	 }

	 /**
	  * @param enabled
	  */
	 public void setGeneratedValueUUIDWithoutDashes(boolean enabled) {
		 this.generatorEnumSupport.set(GeneratorEnum.UUIDWithoutDashes.ordinal(), enabled);
	 }



	 /**
	  * Currently broken by hibernate.
	  *
	  * @return don't use
	  */
	 public boolean isDynamicUpdatesInserts() {
		 return dynamicUpdatesInserts;
	 }



	 /**
	  * Currently broken by hibernate.
	  *
	  * @param dynamicUpdatesInserts
	  */
	 public void setDynamicUpdatesInserts(boolean dynamicUpdatesInserts) {
		 this.dynamicUpdatesInserts = dynamicUpdatesInserts;
	 }



	 /**
	  * @return the extendingProperty
	  */
	 public final PropertyObj getExtendingProperty() {
		 return extendingProperty;
	 }



	 /**
	  * @param extendingProperty the extendingProperty to set
	  */
	 public final void setExtendingProperty(PropertyObj extendingProperty) {
		 this.extendingProperty = extendingProperty;
	 }



	 /**
	  * Gets
	  *
	  * @return t/f
	  */
	 public boolean isCyclicExclusionTable() {
		 return cyclicExclusionTable;
	 }



	 /**
	  * Sets
	  *
	  * @param cyclicExclusionTable
	  */
	 public void setCyclicExclusionTable(boolean cyclicExclusionTable) {
		 this.cyclicExclusionTable = cyclicExclusionTable;
	 }



	 /**
	  * @return interfacepackagename
	  */
	 public String getInterfacePackageName() {
		 return String.format("%s.%s", SyncUtils.getConfigPackage(this.getTableObj().getDbCat(), PackageTypeEnum.OBJECTINTERFACE), "I" + this.className);
	 }

	 /**
	  * @return interfacepackagename
	  */
	 public String getInterfaceClassName() {
		 return "I" + this.className;
	 }



	 /**
	  * Returns true if any of the class parents are part of the cyclic exclusion list.
	  *
	  * @param clazz
	  * @return t/f
	  */
	 private boolean isParentCyclicExclusionTable(Clazz clazz) {
		 if (clazz.isSubclass()) {
			 Clazz parent = clazz.extendsFrom.getClazz();
			 if (parent.isCyclicExclusionTable()) {
				 return true;
			 }
			 return this.isParentCyclicExclusionTable(parent);
		 }
		 return false;

	 }



	 /**
	  * Returns true if any of the class parents are part of the cyclic exclusion list.
	  *
	  * @return t/f
	  */
	 public boolean isParentCyclicExclusionTable() {
		 return this.isParentCyclicExclusionTable(this);
	 }



	 /**
	  * Returns the spring bean name assigned to the data layer handling this class
	  *
	  * @return bean name
	  */
	 public String getDataLayerImplBeanName() {
		 return "dataLayer" + SyncUtils.upfirstChar(this.classPackage) + "Impl";
	 }

	 /**
	  * Returns the spring bean name assigned to the data layer handling this class
	  *
	  * @return bean name
	  */
	 public String getMockRepoBeanName() {
		 return "mock" + SyncUtils.upfirstChar(this.className);
	 }


	 /**
	  * Returns the package name of the services data layer Implementation
	  *
	  * @return string
	  */
	 public String getDataLayerImplFullClassName() {
		 return getDataLayerInterfaceFullClassName() + "Impl";
	 }


	 public String getBootstrapHelper() {
		 String returnValue = "12L";
		 for (PropertyObj property : getAllProperties().values()) {
			 if (property.isIdField()) {
				 int fieldtype = property.getFieldObj().getFieldType();

				 switch (fieldtype) {
				 case java.sql.Types.BOOLEAN:
				 case java.sql.Types.BIT:
					 returnValue = "new Boolean(true)";
					 break;

				 case java.sql.Types.TINYINT:
					 returnValue = "new Byte((byte)1)";
					 break;
				 case java.sql.Types.SMALLINT:
					 returnValue = "1";
					 break;

				 case java.sql.Types.INTEGER:
					 if (property.getFieldObj().isFieldTypeUnsigned()) {
						 returnValue = "1L";
					 }
					 else {
						 returnValue = "1";
					 }
					 break;

				 case java.sql.Types.BIGINT:
					 returnValue = "1L";
					 break;
					 // Removed from the lower group since mediumIn returns a decimal
				 case java.sql.Types.DECIMAL:
				 case java.sql.Types.DOUBLE:
				 case java.sql.Types.FLOAT:
				 case java.sql.Types.REAL:
					 returnValue = "new Double(0.0)";
					 break;

				 case java.sql.Types.NUMERIC:
					 returnValue = "new Float(0.0)";
					 break;

				 case java.sql.Types.CHAR:
					 returnValue = "'A'";
					 break;

				 case java.sql.Types.VARCHAR:
				 case java.sql.Types.LONGVARCHAR:
				 case java.sql.Types.NCHAR:
				 case java.sql.Types.NVARCHAR:
				 case java.sql.Types.LONGNVARCHAR:
					 returnValue = "new String(\"\")";
					 break;

				 case java.sql.Types.DATE:
				 case java.sql.Types.TIME:
				 case java.sql.Types.TIMESTAMP:
					 returnValue = "new Date()";
					 break;

				 case java.sql.Types.BINARY:
				 case java.sql.Types.VARBINARY:
				 case java.sql.Types.LONGVARBINARY:
				 case java.sql.Types.ROWID:
				 case java.sql.Types.NCLOB:
				 case java.sql.Types.SQLXML:
				 case java.sql.Types.NULL:
				 case java.sql.Types.OTHER:
				 case java.sql.Types.JAVA_OBJECT:
				 case java.sql.Types.DISTINCT:
				 case java.sql.Types.STRUCT:
				 case java.sql.Types.ARRAY:
				 case java.sql.Types.BLOB:
				 case java.sql.Types.CLOB:
				 case java.sql.Types.REF:
				 case java.sql.Types.DATALINK:
					 returnValue = "new Object()";
					 break;

				 default:
					 returnValue = "new Object()";
					 break;
				 }
				 break; // we found what we wanted
			 }
		 }
		 return returnValue;
	 }

	 /**
	  * Returns the package name of the services data layer interface
	  *
	  * @return string
	  */
	 public String getDataLayerInterfaceFullClassName() {
		 String config = SyncUtils.getConfigPackage(this.classPackage, PackageTypeEnum.DATA);
		 String result = config + ".DataLayer" + SyncUtils.upfirstChar(this.classPackage);
		 return result;
	 }


	 /**
	  * Returns something like: DataLayerWmsImpl.
	  *
	  * @return string of layer.
	  */
	 public String getDataLayerImplClassName() {
		 return "DataLayer" + SyncUtils.upfirstChar(this.classPackage) + "Impl";
	 }

	 /**
	  * Returns something like: DataLayerWms.
	  *
	  * @return string of layer.
	  */
	 public String getDataLayerInterfaceClassName() {
		 return "DataLayer" + SyncUtils.upfirstChar(this.classPackage);
	 }

	 /**
	  * Returns something like: DataLayerWms.
	  *
	  * @return string of layer.
	  */
	 public String getDataLayerInterfaceClassNameProperty() {
		 return SyncUtils.lowerfirstChar(this.getDataLayerInterfaceClassName());
	 }

	 @Override
	 public boolean equals(Object aThat) {
		 if (this == aThat) {
			 return true;
		 }
		 if ((aThat == null) || (!(aThat instanceof Clazz))) {
			 return false;
		 }
		 final Clazz that = (Clazz) aThat;
		 return this.getFullClassName().equals(that.getFullClassName());
	 }

	 @Override
	 public int hashCode() {
		 return this.getFullClassName().hashCode();
	 }



	 @Override
	 public int compareTo(Clazz o) {
		 return this.getFullClassName().compareTo(o.getFullClassName());
	 }



	 /**
	  * @return the uncascadedOps
	  */
	  public final LinkedList<ObjectPair<Clazz, String>> getUncascadedOps() {
		  return uncascadedOps;
	  }



	  /**
	   * @param uncascadedOps the uncascadedOps to set
	   */
	  public final void setUncascadedOps(LinkedList<ObjectPair<Clazz, String>> uncascadedOps) {
		  this.uncascadedOps = uncascadedOps;
	  }


	  public String getTypeOfId() {
		  if (getAllPropertiesIncludingOfFirstNonAbstractClass().get("id") == null) {
			  if (!this.isEmbeddable()){
				  HbnPojoGen.logE("ERROR: Attempting to get ID of a class that hasn't got one??? Class = " + this.getFullClassName());
				  if (State.getInstance().linkTables.get(this.getTableObj().getFullTableName()) != null) {
					  HbnPojoGen.logE("(you defined this class as a link table in the config file, but no primary key was defined on the table (" + this.getTableObj().getFullTableName() + ")");
				  }
			  }
			  return "Serializable";
		  }
		  return getAllPropertiesIncludingOfFirstNonAbstractClass().get("id").getJavaType();
	  }



	  /**
	   * @return the classCustomCode
	   */
	  public final String getClassCustomCode() {
		  return classCustomCode;
	  }



	  /**
	   * @param classCustomCode the classCustomCode to set
	   */
	  public final void setClassCustomCode(String classCustomCode) {
		  this.classCustomCode = classCustomCode;
	  }



	  /**
	   * @return the embeddedFrom
	   */
	  public final Clazz getEmbeddedFrom() {
		  return embeddedFrom;
	  }



	  /**
	   * @param embeddedFrom the embeddedFrom to set
	   */
	  public final void setEmbeddedFrom(Clazz embeddedFrom) {
		  this.embeddedFrom = embeddedFrom;
	  }

	  public final String getPackageNameAbbrvPlusClassName() {
		  return this.getClassPackage() + "." + this.getClassName();
	  }




	  /**
	   * Gets
	   *
	   * @return
	   */
	  public TreeSet<String> getCustomInterfaces() {
		  return this.customInterfaces;
	  }



	  /**
	   * Sets
	   *
	   * @param customInterfaces
	   */
	  public void setCustomInterfaces(TreeSet<String> customInterfaces) {
		  this.customInterfaces = customInterfaces;
	  }


	  public boolean isInNoOutputList() {
		  return State.getInstance().getNoOutPutForSchemaList().contains(this.classPackage);
	  }




	  /**
	   * Gets
	   *
	   * @return
	   */
	  public boolean isImmutable() {
		  Map<String, Boolean> immutableTables = State.getInstance().getImmutableTables();

		  String schema = (this.getTableObj().getDbSchema() == null ? "" : this.getTableObj().getDbSchema() + ".");

		  Boolean staticTest = immutableTables.get(this.getTableObj().getDbCat()+schema+".*");
		  if (staticTest == null){
			  staticTest = immutableTables.get("*."+schema+this.getTableObj().getDbName());
			  if (staticTest == null){
				  staticTest = immutableTables.get("*.*."+schema+this.getTableObj().getDbName());
				  if (staticTest == null){

					  staticTest = immutableTables.get("*.*.*");
					  if (staticTest == null){
						  staticTest = immutableTables.get(this.getTableObj().getDbCat()+"."+schema+this.getTableObj().getDbName());
					  }
				  }
			  }
		  }

		  return (staticTest != null) || this.tableIsAView;

	  }

	  /**
	   * Gets
	   *
	   * @return
	   */
	  public boolean isImmutableAndNotStaticTest() {
		  Map<String, Boolean> immutableTables = State.getInstance().getImmutableTables();

		  String schema = (this.getTableObj().getDbSchema() == null ? "" : this.getTableObj().getDbSchema() + ".");

		  Boolean staticTest = immutableTables.get(this.getTableObj().getDbCat()+schema+".*");
		  if (staticTest == null){
			  staticTest = immutableTables.get("*."+schema+this.getTableObj().getDbName());
			  if (staticTest == null){
				  staticTest = immutableTables.get("*.*."+schema+this.getTableObj().getDbName());
				  if (staticTest == null){

					  staticTest = immutableTables.get("*.*.*");
					  if (staticTest == null){
						  staticTest = immutableTables.get(this.getTableObj().getDbCat()+"."+schema+this.getTableObj().getDbName());
					  }
				  }
			  }
		  }


		  return (isImmutable()) && (staticTest == false);

	  }




	  /**
	   * Gets
	   *
	   * @return
	   */
	  public String getSuffix() {
		  return this.suffix;
	  }




	  /**
	   * Sets
	   *
	   * @param suffix
	   */
	  public void setSuffix(String suffix) {
		  this.suffix = suffix;
	  }




	  /**
	   * Gets
	   *
	   * @return
	   */
	  public String getClassNameNoSuffix() {
		  return this.classNameNoSuffix;
	  }




	  /**
	   * Sets
	   *
	   * @param classNameNoSuffix
	   */
	  public void setClassNameNoSuffix(String classNameNoSuffix) {
		  this.classNameNoSuffix = classNameNoSuffix;
	  }


	  public TreeSet<String> getCustomExtends() {
		  return this.customExtends;
	  }



	  public void setCustomExtends(TreeSet<String> customExtends) {
		  this.customExtends = customExtends;
	  }

	  public TreeMap<String, PropertyObj> getAllPropertiesIncludingComposite(boolean propertyNameAsKey) {
		  TreeMap<String, PropertyObj> result = getAllProperties(propertyNameAsKey);
		  if (this.hasEmbeddableClass()){
			  result.remove("id");
			  result.putAll(this.getEmbeddableClass().getAllProperties(propertyNameAsKey));
		  }
		  return result;
	  }



	  /**
	   * @return the classCustomCodeFields
	   */
	  public String getClassCustomCodeFields() {
		  return classCustomCodeFields;
	  }



	  /**
	   * @param classCustomCodeFields the classCustomCodeFields to set
	   */
	  public void setClassCustomCodeFields(String classCustomCodeFields) {
		  this.classCustomCodeFields = classCustomCodeFields;
	  }



	  /**
	   * @return the tableIsAView
	   */
	  public boolean isTableIsAView() {
		  return tableIsAView;
	  }



	  /**
	   * @param tableIsAView the tableIsAView to set
	   */
	  public void setTableIsAView(boolean tableIsAView) {
		  this.tableIsAView = tableIsAView;
	  }



	  /** for spring data
	   * @return
	   */
	  public String getRepositoryFullClassName() {
		  return SyncUtils.getConfigPackage(this.getTableObj().getDbCat(), PackageTypeEnum.TABLE_REPO)+"."+this.className+"Repository";
	  }
	  /** for spring data
	   * @return
	   */
	  public String getRepositoryClassName() {
		  return this.className+"Repository";
	  }
	  /** for spring data
	   * @return
	   */
	  public String getRepositoryClassNamePropertyName() {
		  return this.getClassPropertyName()+"Repository";
	  }



	/**
	 * @return the hiddenCurrencyProperties
	 */
	public TreeMap<String, PropertyObj> getHiddenCurrencyProperties() {
		return hiddenCurrencyProperties;
	}



	/**
	 * @param hiddenCurrencyProperties the hiddenCurrencyProperties to set
	 */
	public void setHiddenCurrencyProperties(
			TreeMap<String, PropertyObj> hiddenCurrencyProperties) {
		this.hiddenCurrencyProperties = hiddenCurrencyProperties;
	}

}
