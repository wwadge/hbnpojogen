package org.jvnet.inflector;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * An implementation of {@link Pluralizer} that is implemented using an ordered list of {@link Rule}s.
 * It is possible to specify a fallback {@link Pluralizer} that is invoked if none of the rules match.
 * This makes it easy to override some rules of another {@link Pluralizer}.
 * </p>
 * <p>
 * This class also preserves leading and trailing whitespace, so individual rules don't need to
 * explicitly handle it.
 * Case is also preserved; that is, the output of all uppercase input is automatically uppercased, and
 * the output of titlecase input is automatically titlecased.
 * This means rules can act in a case-insensitive manner.
 * </p>
 * <p>
 * Instances of this class are safe for multiple concurrent threads.
 * </p>
 * @author Tom White
 */
public class RuleBasedPluralizer implements Pluralizer, Serializable{
	
	/** */
    private static final long serialVersionUID = 7466676432551660465L;

    static class IdentityPluralizer implements Pluralizer, Serializable {
		/** */
        private static final long serialVersionUID = -794888679795568207L;
        public String pluralize(String word) {
			return word;
		}
		public String pluralize(String word, int number) {
			return word;
		}
	}

	private static final Pluralizer IDENTITY_PLURALIZER = new IdentityPluralizer();
	
	private List<Rule> rules;
	private Locale locale;
	private Pluralizer fallbackPluralizer;
	
	/**
	 * <p>
	 * Constructs a pluralizer with an empty list of rules.
	 * Use the setters to configure.
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	public RuleBasedPluralizer() {
		this(Collections.EMPTY_LIST, Locale.getDefault());
	}
	
	/**
	 * <p>
	 * Constructs a pluralizer that uses a list of rules then an identity {@link Pluralizer}
	 * if none of the rules match.
	 * This is useful to build your own {@link Pluralizer} from scratch.
	 * </p>
	 * @param rules the rules to apply in order
	 * @param locale the locale specifying the language of the pluralizer
	 */
	public RuleBasedPluralizer(List<Rule> rules, Locale locale) {
		this(rules, locale, IDENTITY_PLURALIZER);
	}
	
	/**
	 * <p>
	 * Constructs a pluralizer that uses first a list of rules then a fallback {@link Pluralizer}.
	 * This is useful to override the behaviour of an existing {@link Pluralizer}.
	 * </p>
	 * @param rules the rules to apply in order
	 * @param locale the locale specifying the language of the pluralizer
	 * @param fallbackPluralizer the pluralizer to use if no rules match
	 */
	public RuleBasedPluralizer(List<Rule> rules, Locale locale, Pluralizer fallbackPluralizer) {
		this.rules = rules;
		this.locale = locale;
		this.fallbackPluralizer = fallbackPluralizer;
	}	
	
	public Pluralizer getFallbackPluralizer() {
		return fallbackPluralizer;
	}

	public void setFallbackPluralizer(Pluralizer fallbackPluralizer) {
		this.fallbackPluralizer = fallbackPluralizer;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public List<Rule> getRules() {
		return rules;
	}

	public void setRules(List<Rule> rules) {
		this.rules = rules;
	}

	/**
	 * <p>
	 * Converts a noun or pronoun to its plural form.
	 * </p>
	 * <p>
	 * This method is equivalent to calling <code>pluralize(word, 2)</code>.
	 * </p>
	 * <p>
	 * The return value is not defined if this method is passed a plural form.
	 * </p>
	 * @param word a singular noun
	 * @return the plural form of the noun
	 */	
	public String pluralize(String word) {
		return pluralize(word, 2);
	}

	/**
	 * <p>
	 * Converts a noun or pronoun to its plural form for the given number of instances.
	 * If <code>number</code> is 1, <code>word</code> is returned unchanged.
	 * </p>
	 * <p>
	 * The return value is not defined if this method is passed a plural form.
	 * </p>
	 * @param word a singular noun
	 * @param number the number of objects being referred to in the plural
	 * @return the plural form of the noun
	 */	
	public String pluralize(String word, int number) {
		if (number == 1) {
			return word;
		}
		
		Pattern pattern = Pattern.compile("\\A(\\s*)(.+?)(\\s*)\\Z");
		Matcher matcher = pattern.matcher(word);
		if (matcher.matches()) {
			String pre = matcher.group(1);
			String trimmedWord = matcher.group(2);
			String post = matcher.group(3);
			String plural = pluralizeInternal(trimmedWord);
			if (plural == null) {
				return fallbackPluralizer.pluralize(word, number);
			}
			return pre + postProcess(trimmedWord, plural) + post;
		}
		return word;		

	}
	
	/**
	 * <p>
	 * Goes through the rules in turn until a match is found at which point the rule is applied
	 * and the result returned.
	 * If no rule matches, returns <code>null</code>.
	 * </p>
	 * @param word a singular noun
	 * @return the plural form of the noun, or <code>null</code> if no rule matches
	 */
	protected String pluralizeInternal(String word) {
		for (Rule rule : rules) {
			if (rule.applies(word)) {
				return rule.apply(word);
			}
		}
		return null;
	}	
	
	/**
	 * <p>
	 * Apply processing to <code>pluralizedWord</code>. This implementation ensures
	 * the case of the plural is consistent with the case of the input word.
	 * </p>
	 * <p>
	 * If <code>trimmedWord</code> is all uppercase, then <code>pluralizedWord</code>
	 * is uppercased.
	 * If <code>trimmedWord</code> is titlecase, then <code>pluralizedWord</code>
	 * is titlecased.
	 * </p>
	 * @param trimmedWord the input word, with leading and trailing whitespace removed
	 * @param pluralizedWord the pluralized word
	 * @return the <code>pluralizedWord</code> after processing
	 */
	protected String postProcess(String trimmedWord, String pluralizedWord) {
		if (trimmedWord.matches("^\\p{Lu}+$")) {
			return pluralizedWord.toUpperCase(locale);
		} else if (trimmedWord.matches("^\\p{Lu}.*")) {
			return pluralizedWord.substring(0, 1).toUpperCase(locale) + pluralizedWord.substring(1);
		}
		return pluralizedWord;
	}

}
