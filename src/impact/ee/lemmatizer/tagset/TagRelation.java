package impact.ee.lemmatizer.tagset;
import java.util.*; 

public interface TagRelation 
{
	public boolean compatible(Tag t1, Tag t2);
	public boolean corpusTagCompatibleWithLexiconTag(String t1, String t2, boolean allowConversions);
}
