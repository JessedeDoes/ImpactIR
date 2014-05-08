package nl.namescape.tei;
import nl.namescape.tokenizer.PunctuationTagger;
import nl.namescape.util.XML;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Alleen voor de NBV bijbels!!!!!
 * Het lijkt erop dat daar geen afkortingen in zitten,
 * dus kunnen punten er altijd af???
 * @author does
 *
 */

public class UntagNotesAndTitles 
{
	public static void UntagWordsInEditorialMatter(Document d)
	{
		Element e = d.getDocumentElement();
		//List<Element> all = XML.getAllSubelements(e, true);
		int editorial=0;
		int original=0;
		boolean excludeNotes = true;
		boolean excludeTitles = true;
		boolean removeTildes = true;
		
		if (excludeNotes)
		{
			List<Element> notes = XML.getElementsByTagname(e, "note", false);
			for (Element n: notes)
			{
				List<Element> words = nl.namescape.tei.TEITagClasses.getTokenElements(n);
				for (Element w: words)
					d.renameNode(w, "", "seg");
			}
		}
		if (excludeTitles)
		{
			List<Element> notes = XML.getElementsByTagname(e, "title", false);
			
			for (Element n: notes)
			{
				List<Element> words = nl.namescape.tei.TEITagClasses.getTokenElements(n);
				for (Element w: words)
					d.renameNode(w, "", "seg");
			}
		}
		if (removeTildes)
		{
			PunctuationTagger pt = new PunctuationTagger();
			List<Element> words =  XML.getElementsByTagname(e, "w", false);
			for (Element w: words)
			{
				String w1 = w.getTextContent();
				if (w1.contains("~"))
				{
					w.setAttribute("wOrig", w1);
					w1 = w1.replaceAll("~", "");
					w.setTextContent(w1);
				}
				if (w1.endsWith("."))
				{
					pt.tagPunctuation(w);
				}
			}
		}
	}
	
	public static void main(String [] args)
	{
		try 
		{
			Document d = XML.parse(args[0]);
			UntagNotesAndTitles.UntagWordsInEditorialMatter(d);
			System.out.println(XML.documentToString(d));
		} catch (ParserConfigurationException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
