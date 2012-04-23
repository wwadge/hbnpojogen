package com.felees.hbnpojogen;

import java.io.Serializable;

import com.felees.hbnpojogen.db.TableObj;

/** just a hack to get commit order right when writing unit tests
 * @author wallacew
 *
 */
public class VelocityTable implements Serializable, Comparable<VelocityTable>{
    /**
	 * 
	 */
	private static final long serialVersionUID = 5678910761714392893L;
	/** For commit order */
	private String key;
	/** For commit order */
	private TableObj value;
	
	/** Getter key
	 * @return this.key
	 */
	public String getKey() {
		return this.key;
	}
	/** Setter for key
	 * @param key
	 */
	public void setKey(String key) {
		this.key = key;
	}
	/** Return table object
	 * @return this.value
	 */
	public TableObj getValue() {
		return this.value;
	}
	/** Setter table object
	 * @param value
	 */
	public void setValue(TableObj value) {
		this.value = value;
	}
	
	@Override
	public int compareTo(VelocityTable o) {
		return this.key.compareTo(o.getKey());
	}
}
