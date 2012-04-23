package org.jvnet.inflector.rule;

import java.io.Serializable;

/**
 * <p>
 * A rule for specifying an inflection using suffixes that only applies to a 
 * subset of words with those suffixes (a category).
 * </p>
 * @author Tom White
 */
public class CategoryInflectionRule extends SuffixInflectionRule implements Serializable {
	
	/** */
    private static final long serialVersionUID = 8202369008967739022L;
    private final String regex;
	
	/**
	 * <p>
	 * Construct a rule for <code>words</code> with suffix <code>singularSuffix</code> which
	 * becomes <code>pluralSuffix</code> in the plural.
	 * </p>
	 * @param words the set of words that define this category
	 * @param singularSuffix the singular suffix, starting with a "-" character
	 * @param pluralSuffix the plural suffix, starting with a "-" character
	 */
	public CategoryInflectionRule(String[] words, String singularSuffix, String pluralSuffix) {
		super(singularSuffix, pluralSuffix);
		this.regex = "(?i)" + AbstractRegexReplacementRule.disjunction(words);
	}

	@Override
	public boolean applies(String word) {
		return word.matches(regex);
	}
	
}
