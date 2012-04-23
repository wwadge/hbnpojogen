package com.felees.hbnpojogen;

import java.io.Serializable;
import java.util.TreeMap;

import com.felees.hbnpojogen.obj.GeneratorEnum;

/**
 * @author wallacew
 * 
 */
public class GeneratedValueFields implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5436626887890431955L;
	/**
	 * field
	 */
	private TreeMap<String, GeneratorEnum> fields = new TreeMap<String, GeneratorEnum>(
			new CaseInsensitiveComparator());

	/**
	 * Key = field, Value = Generator
	 * 
	 * @return map
	 */

	public TreeMap<String, GeneratorEnum> getFields() {
		return fields;
	}

	/**
	 * @param fields
	 */
	public void setFields(TreeMap<String, GeneratorEnum> fields) {
		this.fields = fields;
	}

}
