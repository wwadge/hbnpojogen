package org.jvnet.inflector;
import java.io.Serializable;
import java.util.Locale;

/**
 * <p>
 * A <code>Noun</code> represents the grammatical part of speech that refers to
 * a person, place, thing, event, substance, quality or idea.
 * </p>
 * <p>
 * This class contains a number of static convenience methods ({@link #pluralOf(String)} for forming plurals.
 * </p>
 * @author Tom White
 */
public class Noun implements Serializable{
	
	/**
	 * <p>
	 * Creates a new {@link Pluralizer} instance for the default locale.
	 * </p>
	 * @return a pluralizer instance for the default locale
	 */
	public static Pluralizer pluralizer() {
		return pluralizer(Locale.getDefault());
	}
	
	/**
	 * <p>
	 * Creates a new {@link Pluralizer} instance for the specified locale.
	 * </p>
	 * @param locale the locale specifying the language of the pluralizer
	 * @return a pluralizer instance for the specified locale, or <code>null</code> if there is none for this locale
	 */
	public static Pluralizer pluralizer(Locale locale) {
		String className = "org.jvnet.inflector.lang." + locale.getLanguage() + ".NounPluralizer";
		try {
			Class<?> c = Class.forName(className);
			return (Pluralizer) c.newInstance();
		} catch (ClassNotFoundException e) {
			return null;
		} catch (InstantiationException e) {
			throw new RuntimeException("Problem instantiating " + className, e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Problem instantiating " + className, e);
		}
	}

	/**
	 * <p>
	 * Converts a noun to its plural form using the {@link Pluralizer} for the default locale.
	 * </p>
	 * <p>
	 * The return value is not defined if this method is passed a plural form.
	 * </p>
	 * @param word a singular form
	 * @return the plural form
	 */	
	public static String pluralOf(String word) {
		return pluralOf(word, pluralizer());
	}
	
	/**
	 * <p>
	 * Converts a noun to its plural form for the given number of instances
	 * using the {@link Pluralizer} for the default locale.
	 * </p>
	 * <p>
	 * The return value is not defined if this method is passed a plural form.
	 * </p>
	 * @param word a singular form
	 * @param number the number of objects being referred to in the plural
	 * @return the plural form
	 */	
	public static String pluralOf(String word, int number) {
		return pluralOf(word, number, pluralizer());
	}
	
	/**
	 * <p>
	 * Converts a noun to its plural form using the {@link Pluralizer} for the given locale.
	 * </p>
	 * <p>
	 * The return value is not defined if this method is passed a plural form.
	 * </p>
	 * @param word a singular form
	 * @param locale the locale specifying the language of the pluralizer
	 * @return the plural form
	 */		
	public static String pluralOf(String word, Locale locale) {
		return pluralOf(word, pluralizer(locale));
	}
	
	/**
	 * <p>
	 * Converts a noun to its plural form for the given number of instances
	 * using the {@link Pluralizer} for the given locale.
	 * </p>
	 * <p>
	 * The return value is not defined if this method is passed a plural form.
	 * </p>
	 * @param word a singular form
	 * @param number the number of objects being referred to in the plural
	 * @param locale the locale specifying the language of the pluralizer
	 * @return the plural form
	 */		
	public static String pluralOf(String word, int number, Locale locale) {
		return pluralOf(word, number, pluralizer(locale));
	}
	
	/**
	 * <p>
	 * Converts a noun to its plural form using the given {@link Pluralizer}.
	 * </p>
	 * <p>
	 * The return value is not defined if this method is passed a plural form.
	 * </p>
	 * @param word a singular form
	 * @param pluralizer a noun pluralizer
	 * @return the plural form
	 */		
	public static String pluralOf(String word, Pluralizer pluralizer) {
		return pluralizer == null ? null : pluralizer.pluralize(word);
	}	

	/**
	 * <p>
	 * Converts a noun to its plural form for the given number of instances
	 * using the given {@link Pluralizer}.
	 * </p>
	 * <p>
	 * The return value is not defined if this method is passed a plural form.
	 * </p>
	 * @param word a singular form
	 * @param number the number of objects being referred to in the plural
	 * @param pluralizer a noun pluralizer
	 * @return the plural form
	 */		
	public static String pluralOf(String word, int number, Pluralizer pluralizer) {
		return pluralizer == null ? null : pluralizer.pluralize(word, number);
	}

}
