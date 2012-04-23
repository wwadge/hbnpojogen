package org.jvnet.inflector.lang.it;

import static org.jvnet.inflector.rule.AbstractRegexReplacementRule.disjunction;
import static org.jvnet.inflector.rule.IrregularMappingRule.toMap;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jvnet.inflector.Pluralizer;
import org.jvnet.inflector.Rule;
import org.jvnet.inflector.RuleBasedPluralizer;
import org.jvnet.inflector.rule.CategoryInflectionRule;
import org.jvnet.inflector.rule.IrregularMappingRule;
import org.jvnet.inflector.rule.RegexReplacementRule;
import org.jvnet.inflector.rule.SuffixInflectionRule;

/**
 * <p>
 * A {@link Pluralizer} for Italian nouns.
 * </p>
 * <p>
 * Instances of this class are safe for multiple concurrent threads.
 * </p>
 * <p>
 * This code is based on the rules for plurals described in 
 * <a href="http://www.amazon.co.uk/gp/product/0563399430/202-0809787-0273425?v=glance&n=266239">BBC Italian Grammar</a>,
 * by Alwena Lamping.
 * </p>
 * <p>
 * This is not a full implementation since most exceptional categories (e.g. irregular nouns,
 * words ending -co which change to -ci, etc.) are very incomplete.
 * Also, pronouns are not supported.
 * </p>
 * @author Tom White
 */
public class NounPluralizer extends RuleBasedPluralizer implements Serializable {
	
	/** */
    private static final long serialVersionUID = 4260156287305105361L;

    private static final Map<String, String> IRREGULAR_NOUNS = toMap(new String[][]{
			{ "moglie", "mogli" },
			{ "uovo", "uova" },
			{ "lenzuolo", "lenzuola" },
			{ "paio", "paia" },
			{ "braccio", "braccia" },
			{ "dito", "dita" },
			{ "centinaio", "centinaia" },
			{ "uomo", "uomini" },
			{ "dio", "dei" },
			{ "collega", "colleghi" }, // assume male form 
			{ "atleta", "atleti" }, // assume male form
	});
	
	private static final String[] CATEGORY_UNINFLECTED_NOUNS = {
		"radio", "foto", "moto", // feminine word ending in -o
		"computer", "chef", "hostess", // loan words
	};
	
	private static final String[] CATEGORY_MA_MA_RULE = {
		// words ending -ma which are uninflected
		"cinema", "clima"
	};	

	private static final String[] CATEGORY_MA_ME_RULE = {
		// words ending -ma which change to -me (not sure this is a category)
		"vittima"
	};	

	private static final String[] CATEGORY_IO_II_RULE = {
		// words ending -io where the i is stressed
		"zio",
	};	

	private static final String[] CATEGORY_CO_CI_RULE = {
		// words ending -co preceded by a vowel
		"amico",
	};	
	
	private static final String[] CATEGORY_GO_GI_RULE = {
		// words ending -go preceded by a vowel
		"asparago",
	};	
	
	private static final String[] CATEGORY_CIA_CIE_RULE = {
		// words ending -cia preceded by a vowel or where the i is stressed
		"farmacia",
	};	
	
	private static final String[] CATEGORY_GIA_GIE_RULE = {
		// words ending -gia preceded by a vowel or where the i is stressed
		"valigia",
	};	
	
	private final List<Rule> rules = Arrays.asList(new Rule[] {
			
		// Blank word
		new RegexReplacementRule("^(\\s)$", "$1"),
		
		// Irregular nouns
		new IrregularMappingRule(IRREGULAR_NOUNS, "(?i)" + disjunction(IRREGULAR_NOUNS.keySet()) + "$"),
		
		// Nouns ending in -ista (referring to people)
		new SuffixInflectionRule("-ista", "-isti"), // assume male form
		
		// Irregular nouns that do not inflect in the plural
		new CategoryInflectionRule(CATEGORY_UNINFLECTED_NOUNS, "-", "-"),
		new SuffixInflectionRule("-[\u00e0|\u00e8|\u00ec|\u00f9]", "-", "-"),
		new SuffixInflectionRule("-ie?", "-", "-"),
		
		// Irregular masculine nouns ending in -ma
		new CategoryInflectionRule(CATEGORY_MA_MA_RULE, "-ma", "-ma"),		
		new CategoryInflectionRule(CATEGORY_MA_ME_RULE, "-ma", "-me"),		
		new SuffixInflectionRule("-ma", "-mi"),
					
		// Regular nouns ending in -o
		
		new CategoryInflectionRule(CATEGORY_IO_II_RULE, "-io", "-ii"),		
		new SuffixInflectionRule("-io", "-o", "-"),
		
		new CategoryInflectionRule(CATEGORY_CO_CI_RULE, "-co", "-ci"),		
		new SuffixInflectionRule("-co", "-chi"),
		
		new CategoryInflectionRule(CATEGORY_GO_GI_RULE, "-go", "-gi"),		
		new SuffixInflectionRule("-go", "-ghi"),
		
		new SuffixInflectionRule("-o", "-i"),
		
		// Regular nouns ending in -a
		
		new SuffixInflectionRule("-ca", "-che"),

		new SuffixInflectionRule("-ga", "-ghe"),

		new CategoryInflectionRule(CATEGORY_CIA_CIE_RULE, "-cia", "-cie"),		
		new SuffixInflectionRule("-cia", "-ce"),

		new CategoryInflectionRule(CATEGORY_GIA_GIE_RULE, "-gia", "-gie"),		
		new SuffixInflectionRule("-gia", "-ge"),

		new SuffixInflectionRule("-a", "-e"),
		
		// Regular nouns ending in -e

		new SuffixInflectionRule("-e", "-i"),
	
		// Don't inflect by default
		new SuffixInflectionRule("-", "-"),
	});

	/**
	 * Default constructor.
	 */
	public NounPluralizer() {
		setRules(rules);
		setLocale(Locale.ITALIAN);
	}

	
}
