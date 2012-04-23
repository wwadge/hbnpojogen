package org.jvnet.inflector.rule;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * <p>
 * A rule for specifying an irregular inflection using a combination of a map of
 * singular to plural forms and a regular expression replacement. Subclasses
 * cn implement {@link #replace} to perform the actual replacement, which by default 
 * uses the map to substitute a plural form for the corresponding singular form found in group 0
 * of the regular expression match.
 * </p>
 * @author Tom White
 */
public class IrregularMappingRule extends AbstractRegexReplacementRule implements Serializable{

	/** */
    private static final long serialVersionUID = 9069582525363046599L;
    protected final Map<String, String> mappings;
	
	/**
	 * <p>
	 * Construct a rule using the given regular expression and irregular forms map.
	 * </p>
	 * @param wordMappings the map of singular to plural forms
	 * @param regex the regular expression used to match words. Match information 
	 * is available to subclasses in the {@link #replace} method. 
	 */
	public IrregularMappingRule(Map<String, String> wordMappings, String regex) {
		super(regex);
		this.mappings = wordMappings;
	}
	
	@Override
	public String replace(Matcher m) {
		return mappings.get(m.group(0).toLowerCase());
	}	
	
	/**
	 * <p>
	 * Turn the array of String array mapping pairs into a map.
	 * </p>
	 * @param wordMappings
	 * @return a map of singular to plural forms
	 */
	public static Map<String, String> toMap(String[][] wordMappings) {
		Map<String, String> mappings = new HashMap<String, String>();
		for (int i = 0; i < wordMappings.length; i++) {
			String singular = wordMappings[i][0];
			String plural = wordMappings[i][1];
			mappings.put(singular, plural);
		}
		return mappings;
	}
	
}
