package org.jvnet.inflector.rule;
import java.io.Serializable;

import org.jvnet.inflector.Rule;

/**
 * <p>
 * A rule for specifying an inflection using suffixes. For example, the English nouns
 * which have the suffix -y, generally change the suffix to -ies in the plural. Such a rule would
 * be expressed as <code>new SuffixInflectionRule("-y", "-ies")</code>.
 * </p>
 * @author Tom White
 */
public class SuffixInflectionRule implements Rule, Serializable{
	
	/** */
    private static final long serialVersionUID = -6412698059095031731L;
    private final String regex;
	private final String singularSuffix;
	private final String pluralSuffix;
	
	/**
	 * <p>
	 * Construct a rule for words with suffix <code>singularSuffix</code> which
	 * becomes <code>pluralSuffix</code> in the plural.
	 * </p>
	 * @param singularSuffix the singular suffix, starting with a "-" character
	 * @param pluralSuffix the plural suffix, starting with a "-" character
	 */
	public SuffixInflectionRule(String singularSuffix, String pluralSuffix) {
		this(singularSuffix, singularSuffix, pluralSuffix);
	}
	
	/**
	 * <p>
	 * Construct a rule for words with suffix <code>suffix</code>, where 
     * <code>singularSuffix</code>
	 * becomes <code>pluralSuffix</code> in the plural.
	 * @param suffix the suffix, starting with a "-" character,  which the end of the word must match.
	 * Note that regular expression patterns may be used.
	 * @param singularSuffix the singular suffix, starting with a "-" character.
	 * Note that it must be true that <code>suffix</code> ends with <code>singularSuffix</code>.
	 * @param pluralSuffix the plural suffix, starting with a "-" character
	 * </p>
	 */	
	public SuffixInflectionRule(String suffix, String singularSuffix, String pluralSuffix) {
		// TODO: check suffix ends with singularSuffix?
		this.regex = "(?i).*" + suffix.substring(1) + "$";
		this.singularSuffix = singularSuffix;
		this.pluralSuffix = pluralSuffix;
	}	

	public boolean applies(String word) {
		return word.matches(regex);
	}
	
	public String apply(String word) {
		int i = word.toUpperCase().lastIndexOf(singularSuffix.substring(1).toUpperCase());
		//TODO: check i
		// TODO: make case insensitive
		return word.substring(0, i) + pluralSuffix.substring(1);
	}

}
