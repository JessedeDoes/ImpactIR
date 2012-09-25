package tagger;

public class DummyContext implements Context 
{

	@Override
	public String getAttributeAt(String attributeName, int relativePosition) 
	{
		// TODO Auto-generated method stub
		return "DUMMY";
	}

	@Override
	public void setAttributeAt(String attributeName, String attributeValue,
			int relativePosition) {
		// TODO Auto-generated method stub
		
	}

}
