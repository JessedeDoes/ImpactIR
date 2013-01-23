package nl.namescape.tagging;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;

public class StanfordAPIClient implements SentenceTagger 
{
	boolean useTags = false; // should be true...
	List<AbstractSequenceClassifier> listOfTaggers = new ArrayList<AbstractSequenceClassifier>();
	
	@Override
	public String tagString(String in) 
	{
		// TODO Auto-generated method stub
		return StanfordSentenceTagging.tagSentence(in, listOfTaggers);
	}

	@Override
	public void tagWordElement(Element w, String line) 
	{
		try
		{
			String[] parts = line.split("\\s+");
			w.setAttribute("neLabel", parts[1]);
			if (parts.length > 2)
			{
				w.setAttribute("nePartLabel", parts[2]);
			}
		} catch (Exception e)
		{
			System.err.println(line);
			e.printStackTrace();
		}
	}

	@Override
	public void postProcessDocument(Document d) 
	{
		(new nl.namescape.tei.TEINameTagging()).realizeNameTaggingInTEI(d);
	}

	@Override
	public String tokenToString(Element t) 
	{
		// TODO looka at useTags thing
		String w = t.getTextContent();
		if (useTags)
		{
			String tag = t.getAttribute("neLabel");
			if (tag == null || tag=="")
				tag="O";
			return w + "\t" + tag;
		}
		return w;
	}
}
