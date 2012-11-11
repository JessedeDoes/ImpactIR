package nl.namescape.tei;
/*
 * Deze klasse maakt de Illinois NE tagger xml-inpakbaar
 * Iets dergelijks voor Stanford doen...
 */
import LbjTagger.*;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.io.*;


import nl.namescape.BIOOutput;
import nl.namescape.tagging.LBJAPIClient;
import nl.namescape.tokenizer.TEITokenizer;
import nl.namescape.util.Proxy;
import nl.namescape.util.StopWatch;
import nl.namescape.util.XML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import lbj.*;
import IO.OutFile;
import LBJ2.parse.*;
import org.w3c.dom.ranges.*;


/**
 * Contains the stuff needed to realize BIO-style tagging as XML tags.
 * In een eerder stadium worden resultaten van de NE tagging tijdelijk gerealiseerd met BIO-attributen bij de &lt;w>-tags:<br>
 * From
 * <pre>
 * &lt;w neLabel="B-person" nePartLabel="B-forename">Kees&lt;/w> 
 * &lt;w neLabel="I-person" nePartLabel="B-surname">de&lt;/w> 
 * &lt;w neLabel="I-person" nePartLabel="I-surname">Boer&lt;/w>
 * </pre>
 * <br>
 * To
 * <pre>
 * </pre>
 * Main function is realizeNameTaggingInTEI<br>
 * @author does
 *
 */
public class TEINameTagging implements nl.namescape.filehandling.SimpleInputOutputProcess
{
	public static final String namescapeURI = "http://www.namescape.nl/";
	private static boolean tagWithNameAndType = true;
	private TEITokenizer teiTokenizer = new TEITokenizer();
	private static String namescapeNamespace = "ns";
	public static String defaultNameTag =  namescapeNamespace + ":" + "ne"; // namespace + ":" + "ne";
	public  static String namePartTag = "ns:nePart";
	private NymListBuilder nymListBuilder = new NymListBuilder();
	private static boolean addNormalizedForm = true;

	/**
	 * Read an xml file and a tab separated bio file, merge the results in the XML<br>
	 * Now obsolete.
	 * @param xmlFile
	 * @param neTaggedFile
	 */

	@Deprecated
	public void injectNETags(String xmlFile, String neTaggedFile)
	{
		HashMap<String,String> id2tag = new HashMap<String,String>();
		try 
		{
			Document d = XML.parse(xmlFile);
			BufferedReader b = new BufferedReader(new InputStreamReader(new FileInputStream(neTaggedFile), "UTF-8"));
			String line;	
			while ((line = b.readLine()) != null)
			{	
				String[] parts = line.split("\\s+");
				if (parts.length > 3)
				{
					String neTag = parts[1]; 
					String id=parts[3];
					if (id.contains(":"))
					{

						if (!id.startsWith("HAS"))
							id="neverMind";
						else
						{
							String[] idparts = id.split(":");
							id=idparts[1];
						}
					}
					id2tag.put(id,neTag);
				}
			}
			for (Element w: XML.getElementsByTagname(d.getDocumentElement(), "w", false))
			{
				String z = id2tag.get(w.getAttribute("id"));
				if (z != null)
				{
					//System.err.println(z);
					w.setAttribute("neLabel", z);
				}
			}
			realizeNameTaggingInTEI(d);
			System.out.println(XML.documentToString(d));
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static  void addNamescapeNamespace(Document d)
	{
		d.getDocumentElement().setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ns", namescapeURI);
	}

	public void realizeNameTaggingInTEI(Document d) 
	{
		Element root = d.getDocumentElement();

		// we need to add a namespace declaration.....

		root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ns", namescapeURI);

		addEntityTags(d);
		nymListBuilder.addNormalizedFormsAndNyms(d);

		for (Element w: XML.getElementsByTagname(d.getDocumentElement(), nl.namescape.tei.TEITagClasses.tokenTagNames, false))
		{
			w.removeAttribute("neLabel");
			w.removeAttribute("nePartLabel");
		}
		// tei.Various.fixNamespaceStuff(d);
	}	

	/**
	 * Zet tags om de named entities<br>
	 * 
	 * @param d
	 */

	public static void addEntityTags(Document d)
	{
		List<Element> wordElements = XML.getElementsByTagname(d.getDocumentElement(), "w", false);

		tagEntities(d, wordElements, false);
		List<Element> persons = XML.getElementsByTagnameAndAttribute(d.getDocumentElement(),defaultNameTag, "type", "person", false);
		for (Element p: persons)
		{
			wordElements = XML.getElementsByTagname(p, "w", false);
			tagEntities(d, wordElements, true);
			tagStructureAttribute(p);
		}
	}

	public static void tagEntities(Document d, List<Element> wordElements, boolean tagParts) 
	{
		String entityType = "";
		Element entityStart = null;
		Element entityEnd = null;

		for (int i=0; i < wordElements.size(); i++)
		{
			Element w = wordElements.get(i);
			String label = w.getAttribute(tagParts?"nePartLabel":"neLabel");
			if (label.startsWith("B-") || label.startsWith("U-"))
			{
				flushEntity(d, entityStart, entityEnd, entityType, tagParts);
				entityStart = null;

				entityType = label.substring(2);
				entityStart = w;
				entityEnd = w;
			}
			if (label.startsWith("I-") || label.startsWith("L-") )
			{
				// entityType = label.substring(2);
				entityEnd = w; 
			}
			if (label.equals("O") || label.startsWith("L-") || label.startsWith("U-"))
			{
				flushEntity(d, entityStart, entityEnd, entityType, tagParts);
				entityStart = null;
			}
		}
		if (entityStart != null)
			flushEntity(d, entityStart, entityEnd, entityType, tagParts);
	}

	private static String makeType(String t)
	{
		String l = t.toLowerCase();
		if (l.contains("per"))
			return "person";
		if (l.contains("org"))
			return "organisation";
		if (l.contains("geo") || l.contains("loc"))
			return "location";
		if (l.contains("misc") || l.contains("oth") || l.contains("unk"))
			return "misc";
		return t;
	}

	private static Element makeNameElement(Document d, String neType, boolean part)
	{
		String name ="";
		String attribute = null;
		if (tagWithNameAndType)
		{
			Element e = d.createElement(part?namePartTag:defaultNameTag);
			e.setAttribute("type", makeType(neType));
			return e;
		}
		if (neType.equalsIgnoreCase("misc"))
		{
			name = defaultNameTag;
			attribute = "misc";
		}
		else if (neType.equalsIgnoreCase("org"))
			name = "orgName";
		else if (neType.equalsIgnoreCase("loc"))
			name = "placeName";
		else if (neType.equalsIgnoreCase("per") || neType.equalsIgnoreCase("pers"))
			name = "persName";

		Element e = d.createElement(name);
		if (attribute != null)
			e.setAttribute("type", attribute);
		return e;
	}
	/**
	 * Zet een tag om de named entity.
	 * @param d
	 * @param entityStart
	 * @param entityEnd
	 * @param type
	 * @param tagParts 
	 */
	private static void flushEntity(Document d, Element entityStart, Element entityEnd, String type, boolean tagParts)
	{
		if (entityStart == null)
			return;
		DocumentRange dr = (DocumentRange) d;
		Range range = dr.createRange();
		range.setStartBefore(entityStart);
		range.setEndAfter(entityEnd);
		Element e =  makeNameElement(d, type, tagParts);
		try
		{
			range.surroundContents(e);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}

		// System.err.println(e.getParentNode() + " " + e.getTextContent());
		range.detach();
	}

	public static void tagStructureAttribute(Element e) 
	{
		if (e.getAttribute("type").equals("person"))
		{
			List<Element> parts = XML.getElementsByTagname(e, "ns:nePart", false);
			List<String> subtypes = new ArrayList<String>();
			//System.err.println(parts.size() + " in " + XML.NodeToString(e));
			if (parts != null)
			{
				for (Element p: parts)
					subtypes.add(p.getAttribute("type"));
			}
			e.setAttribute("structure", nl.namescape.util.Util.join(subtypes, "_"));
		}
	}



	@Override
	public void handleFile(String in, String out) 
	{
		
		try
		{
			Document d = XML.parse(in);
			this.realizeNameTaggingInTEI(d);
			PrintStream pout = new PrintStream(new FileOutputStream(out));
			pout.print(XML.documentToString(d));
			pout.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		nl.namescape.filehandling.DirectoryHandling.tagAllFilesInDirectory(new TEINameTagging(), args[0], 
				args[1]);

	}
}
