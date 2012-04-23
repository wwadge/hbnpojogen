package com.felees.hbnpojogen;

import java.io.Serializable;

public class FakeFKPattern implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1686541777692763787L;
	private String pattern;
	private String replacePattern;
	private boolean enabled;
	
	public FakeFKPattern(String pattern, String replacePattern, boolean enabled){
		this.pattern = pattern;
		this.replacePattern = replacePattern;
		this.enabled = enabled;
	}
	
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	public String getReplacePattern() {
		return replacePattern;
	}
	public void setReplacePattern(String replacePattern) {
		this.replacePattern = replacePattern;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
