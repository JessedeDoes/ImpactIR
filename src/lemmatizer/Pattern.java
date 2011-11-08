package lemmatizer;

/**
 * 
 * @author jesse
 *<p>
 *Interface for (inflectional) patterns.
 *</p>
 * Currently only implementation is SImplePattern.
 * SFTS or some other finite state toolkit may be linked in later on
 */
public interface Pattern
{
  public String apply(String s);
  public String applyConverse(String s);
}
