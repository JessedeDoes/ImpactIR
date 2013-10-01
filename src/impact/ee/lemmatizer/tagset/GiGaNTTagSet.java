package impact.ee.lemmatizer.tagset;
import java.util.*;
public class GiGaNTTagSet extends TagSet
{
	String[] ip = { "NOU-C", "NOU-P", "VRB", "AA" };
	Set<String> inflectingPoS = new HashSet<String>(Arrays.asList(ip));
	
	@Override
	public boolean isInflectingPoS(String tag) 
	{
		// TODO Auto-generated method stub
		return  inflectingPoS.contains(this.getPoS(tag));
	}

	@Override
	public Tag parseTag(String tag) 
	{
		// TODO Auto-generated method stub
		return Tag.parseParoleStyleTag(tag);
	}

}
