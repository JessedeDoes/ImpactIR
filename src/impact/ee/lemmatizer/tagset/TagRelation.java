package impact.ee.lemmatizer.tagset;
import java.util.*; 

public interface TagRelation 
{
	public boolean compatible(Tag t1, Tag t2);
	public boolean compatible(String t1, String t2);
	public Set<Tag> findCompatibleTags(Tag t);
}
