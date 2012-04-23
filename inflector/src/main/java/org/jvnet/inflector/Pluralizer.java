package org.jvnet.inflector;

/**
 * <p>
 * <code>Pluralizer</code> converts singular word forms to their plural forms.
 * Methods that are passed <code>null</code> must throw
 * a {@link java.lang.NullPointerException}.
 * </p>
 * <p>
 * Implementations of this interface must be safe for use by multiple concurrent threads.
 * </p>
 * @author Tom White
 */
public interface Pluralizer {

	/**
	 * <p>
	 * Converts a word to its plural form.
	 * </p>
	 * <p>
	 * The return value is not defined if this method is passed a plural form.
	 * </p>
	 * @param word a singular form
	 * @return the plural form
	 */
	public String pluralize(String word);
	
	/**
	 * <p>
	 * Converts a word to its plural form for the given number of instances.
	 * Some languages (such as <a href="http://en.wikipedia.org/wiki/Dual_grammatical_number">Polish</a>)
	 * have different plural forms depending on the number
	 * of things being referred to.
	 * </p>
	 * <p>
	 * The return value is not defined if this method is passed a plural form.
	 * </p>
	 * @param word a singular form
	 * @param number the number of objects being referred to in the plural
	 * @return the plural form
	 */	
	public String pluralize(String word, int number);
	
}
