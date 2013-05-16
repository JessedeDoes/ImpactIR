package impact.ee.tagger;

import java.util.HashMap;

public interface Tagger 
{
	public HashMap<String,String> apply(Context c);
	public Corpus tag(Corpus inputCorpus);
	public void setProperties(java.util.Properties properties);
}
