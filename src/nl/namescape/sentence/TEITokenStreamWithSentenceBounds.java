package nl.namescape.sentence;

import java.util.List;

import nl.namescape.tei.TEITagClasses;
import nl.namescape.util.XML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TEITokenStreamWithSentenceBounds extends TEITokenStream 
{
	public TEITokenStreamWithSentenceBounds(Document d)
	{

		this.document = d;
		sentenceSplittingElements = TEITagClasses.getSentenceSplittingElements(d);

		List<Element> S = TEITagClasses.getSentenceElements(d);
		for (Element s: S) // this means words NOT in a sentence are not tagged ....
		{
			List<Element> l = TEITagClasses.getTokenElements(s);

			for (Element e: l)
			{
				TEIToken t;
				if (e.getNodeName().contains("w"))
				{
					t = new TEIToken(e);
				} else
				{
					t = new TEIPunctuation(e);
				}
				element2TokenMap.put(e,t);
				tokens.add(t); // zinsgrenzen .......
			}
			tokens.add(new SentenceBoundaryToken());
		}

		// mark all last elements of <p>-like as sentence final
		// BUT: need to decide on a good set of sentence splitting elements
		// dit hoeft hier eigenlijk helemaal niet meer....
		for (Element p: sentenceSplittingElements)
		{
			List<Element> tokzInP = XML.getElementsByTagname(p, 
					TEITagClasses.tokenTagNames, false);
			if (tokzInP.size() > 0)
			{
				Element te = tokzInP.get(tokzInP.size()-1);
				Token t = element2TokenMap.get(te);
				if (t != null)
				{
					element2TokenMap.get(te).setIsEOS(true);
				}
			}
		}	
		currentPosition=0;
	}
}
