package org.jvnet.inflector;

/**
 * <p>
 * A <code>Rule</code> represents how a word changes from one form to another. 
 * </p>
 * <p>
 * Implementations of this interface must be safe for use by multiple concurrent threads in order to
 * satisfy the contract of {@link Pluralizer}.
 * </p>
 * @author Tom White
 */
public interface Rule {
	/**
	 * <p>
	 * Tests to see if this rule applies for the given word.
	 * </p>
	 * @param word the word that is being tested
	 * @return <code>true</code> if this rule should be applied, <code>false</code> otherwise
	 */
	public boolean applies(String word);
	
	/**
	 * <p>
	 * Applies this rule to the word, and transforming it into a new form.
	 * </p>
	 * @param word the word to apply this rule to
	 * @return the transformed word
	 */
	public String apply(String word);
}
