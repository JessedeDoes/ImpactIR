package nl.namescape.tagging;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface SentenceTagger 
{
	public String tagString(String in);
	public void tagWordElement(Element e, String line);
	public void postProcessDocument(Document d);
	public String tokenToString(Element t);
}