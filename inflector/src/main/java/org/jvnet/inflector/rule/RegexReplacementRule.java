package org.jvnet.inflector.rule;
import java.io.Serializable;
import java.util.regex.Matcher;

/**
 * <p>
 * A rule specified using a regular expression and a replacement string.
 * </p>
 * @author Tom White
 */
public class RegexReplacementRule extends AbstractRegexReplacementRule implements Serializable {
	
	/** */
    private static final long serialVersionUID = -2153299891328152681L;
    private final String replacement;
	
	/**
	 * Construct a rule using the given regular expression and replacement string.
	 * @param regex the regular expression used to match words
	 * @param replacement the string to use during replacement.
	 * The replacement string may contain references to subsequences captured matching.
	 * See {@link Matcher#appendReplacement}.
	 */
	public RegexReplacementRule(String regex, String replacement) {
		super(regex);
		this.replacement = replacement;
	}

	@Override
	public String replace(Matcher matcher) {
		return matcher.replaceFirst(replacement);
	}
	
}
