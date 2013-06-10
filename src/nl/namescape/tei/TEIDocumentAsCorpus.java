package nl.namescape.tei;
import java.util.List;

import org.w3c.dom.*;

import impact.ee.tagger.Context;
import impact.ee.tagger.Corpus;

@Deprecated
public class TEIDocumentAsCorpus implements Corpus
{
	Document document;
	List<Element> tokenElements;
	int position=0;
	
	private String getAttribute(String name, int relativePosition)
	{
		int p = this.position + relativePosition;
		if (p < 0 || p > tokenElements.size())
		{
			
		}
		return null;
	}
	public TEIDocumentAsCorpus(Document d)
	{
		this.document = d;
		tokenElements = TEITagClasses.getTokenElements(d);
		position = 0;
	}
	@Override
	public Iterable<Context> enumerate() 
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
