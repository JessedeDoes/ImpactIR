package impact.ee.tagger;

public interface Context 
{
	public String getAttributeAt(String attributeName, int relativePosition);
	public void setAttributeAt(String attributeName, String attributeValue, int relativePosition);
}
