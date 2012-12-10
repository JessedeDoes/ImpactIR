package nl.namescape.stats;
import nl.namescape.tei.TEITagClasses;

import org.w3c.dom.*;
import java.util.*;

/**
 * Try to use collocation information
 * For each sentence... [BLA]
 * @author does
 * should we make this n-pass (? rather NOT)
 * First a typefrequency pass; next a bigram pass, 
 * Next step: add all the rest. (?)
 */

public class MultiwordExtractor 
{
	WordList tf = new WordList();
	int minimumFrequency = 2;
	
	public void extract(Document d)
	{
		List<Element> sentences = TEITagClasses.getSentenceElements(d);
		 
	}
}
