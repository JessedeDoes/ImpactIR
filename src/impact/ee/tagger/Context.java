package impact.ee.tagger;

import java.util.List;
import java.util.Set;
public interface Context 
{
	public String getAttributeAt(String attributeName, int relativePosition);
	public void setAttributeAt(String attributeName, String attributeValue, int relativePosition);
	public Set<String> getAttributes();
}
