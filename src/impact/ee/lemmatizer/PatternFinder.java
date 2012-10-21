package impact.ee.lemmatizer;
public interface PatternFinder
{
  public Pattern findPattern(String a, String b);
  public Pattern findPattern(String a, String b, String PoS);
}
