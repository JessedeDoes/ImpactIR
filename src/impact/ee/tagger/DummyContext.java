package impact.ee.tagger;

public class DummyContext implements Context 
{

	@Override
	public String getAttributeAt(String attributeName, int relativePosition) 
	{
		return "DUMMY";
	}

	@Override
	public void setAttributeAt(String attributeName, String attributeValue,
			int relativePosition) 
	{
		System.err.println("Trying to set attribute on dummy context...");
	}
}
