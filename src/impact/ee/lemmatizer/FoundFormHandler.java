package impact.ee.lemmatizer;

public interface FoundFormHandler
{
  public void foundForm(String lemma, String tag, String lemmaPoS, Rule r, double p, int rank);
}