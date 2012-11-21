package nl.namescape.tei;
import java.util.ArrayList;
import java.util.List;

import nl.namescape.util.XML;

import org.w3c.dom.*;




import java.util.*;


public class TEITagClasses 
{

	// Deze elementen zijn zeker sentence splitters
	// figDesc mag er nog wel bij
	
	// pas op file en doc een hackje voor de niet-tei versie van de trainingdata
	static String[] sentenceSplittingElementNames = 
		{"p", "ab", "head", "lg", "closer", "note", 
			"byline", "dateline", "file", "doc",
			"opener", "salute",  "signed", "trailer"};
	static Set<String> splitTags = new HashSet<String>();
	public static String[] inlineTagNames = 
	{"hi", "milestone", "pb", "i", "name", "persName", "orgName", "placeName"};
	static Set<String> inlineTags = new HashSet<String>();
	public static String[] milestone = {"pb", "cb", "milestone"};
	public static String[] tokenTagNames = {"w", "pc"};
	public static String[] nameTagNames = {"ns:ne", "name", "ne"};
	public static String[] namePartTags = {"ns:nePart", "nePart"};
	
	static Set<String> nameTags = new HashSet<String>();
	
	static
	{
		for (String s: sentenceSplittingElementNames)
			splitTags.add(s);
		for (String s: inlineTagNames)
			inlineTags.add(s);
		for (String s: nameTagNames)
					nameTags.add(s);
	};
	
	public static boolean noSentenceBreakIn(Element e)
	{
		return nameTags.contains(e.getNodeName()) 
				|| nameTags.contains(e.getLocalName());
	}
	
	public static List<Element> getTokenElements(Document d)
	{
		Set<String> elNames = new HashSet<String>();
		for (String x: TEITagClasses.tokenTagNames) 
			elNames.add(x);
		Element doc = d.getDocumentElement();
		List<Element> l = 
				XML.getElementsByTagname(doc, elNames, false);
		return l;
	}
	
	public static List<Element> getNameElements(Document d)
	{
		Set<String> elNames = new HashSet<String>();
		for (String x: TEITagClasses.nameTagNames) 
			elNames.add(x);
		Element doc = d.getDocumentElement();
		List<Element> l = 
				XML.getElementsByTagname(doc, elNames, false);
		return l;
	}
	public static List<Element> getTokenElements(Element e)
	{
		Set<String> elNames = new HashSet<String>();
		for (String x: TEITagClasses.tokenTagNames) 
			elNames.add(x);
		
		List<Element> l = 
				XML.getElementsByTagname(e, elNames, false);
		return l;
	}
	public static List<Element> getWordElements(Element e)
	{
		return XML.getElementsByTagname(e, "w", false);
	}
	public static List<Element> getSentenceElements(Document d)
	{
		return XML.getElementsByTagname(d.getDocumentElement(), "s", false);
	}

	/**
	 * Een tijdelijke fix voor de oude gefrogde bestanden.
	 * Vergeet niet weer weg te halen...
	 * @param d
	 * @return
	 */
	public static boolean fixIds(Document d)
	{
		List<Element> tokenz = getTokenElements(d);
		int k=1;
		boolean eek = false;
		for (Element e: tokenz)
		{
			
			if (e.getAttribute("xml:id") == null || e.getAttribute("xml:id").equals(""))
			{
				eek = true;
				String s = e.getAttribute("id");
				if (s == null || s.equals(""))
					s = "wx." + k++;
				else
					e.removeAttribute("id");
				e.setAttribute("xml:id", "w." + s);

			} else
			{

			}
			if (e.getLocalName().contains("pc"))
			{
				e.removeAttribute("function");
			} else
			{
				if (e.getAttribute("type") == null || e.getAttribute("type").equals(""))
				{
					String s = e.getAttribute("function");
					if (s == null)
						s="UNK";
					else
						e.removeAttribute("function");
					e.setAttribute("type", s);
				}
			}
		}
		return eek;
	}
	
	/**
	  Zinsplitsende tags: natuurlijk het lijstje hierboven gegeven.
	  maar dat is vast niet alles.
	  Iedere w moet uiteindelijk bevat zijn in een sentence splitter
	  neem dus voor alle w die niet in een van bovenstaande zitten
	  een parent die ook geen inline tag is
	*/
	public static Set<Element> getSentenceSplittingElements(Document d)
	{
		List<Element> ssl = new ArrayList<Element>();
		Set<Element> sss  = new HashSet<Element>();
		Element root = d.getDocumentElement();
		List<Element> tokens = XML.getElementsByTagname(root, tokenTagNames, false);
		Element previousParent = null;
		
		for (Element t: tokens)
		{
			Element parent = (Element) t.getParentNode();
			if (previousParent == parent)
				continue;
			boolean foundGoodAncestor = false;
			
			for (Node n = parent; n != null; n = n.getParentNode())
			{
				if (n.getNodeType() != Node.ELEMENT_NODE)
					break;
				
				Element e  = (Element) n;
				if (splitTags.contains(e.getNodeName()))
				{
					foundGoodAncestor = true; 
					sss.add(e);
					ssl.add(e); // Er kan een volgordeprobleem optreden! 
					break;
				} else if (sss.contains(e))
				{
					foundGoodAncestor = true;
					break;
				}
			}
			
			if (!foundGoodAncestor)
			{
				for (Node n = parent; n != null; n = n.getParentNode())
				{
					if (n.getNodeType() != Node.ELEMENT_NODE)
						break;
					Element e  = (Element) n;
					if (!inlineTags.contains(e.getNodeName()))
					{
						sss.add(e);
						ssl.add(e);
						/*
						System.err.println("Adding instance of " + 
						 	e.getNodeName() + " to sentence splitters!" + 
								ParseUtils.NodeToString(e)); */
						foundGoodAncestor = true;
						break;
					}
				}
			}
			
			if (!foundGoodAncestor)
			{
				System.err.println("No good ancestor found for token: "  + XML.NodeToString(t));
			}
			previousParent = parent;
		}
		return sss;
	}

	public static void removeTokenization(Document d)
	{
		List<Element> sentences = getSentenceElements(d);
		List<Element> tokens = getTokenElements(d);
		for (Element t: tokens)
		{
			XML.removeInterveningNode(t);
		}
		for (Element s: sentences)
		{
			XML.removeInterveningNode(s);
		}
	}
	
	public static boolean tagSplitsWords(String tagName)
	{
		return nameTags.contains(tagName) || !(inlineTags.contains(tagName));
	}
}
