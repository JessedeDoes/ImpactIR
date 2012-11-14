package nl.namescape.tagging;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import nl.namescape.util.XML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;



public class FrogTagXML 
{

	public static void main(String[] args)
	{
			//new NETagXML(false).injectFrogTags(args[0], args[1]);
	}

	public void injectFrogTags(String xmlFile, String neTaggedFile)
	{
		HashMap<String,String> id2tag = new HashMap<String,String>();
		HashMap<String,String> id2lemma = new HashMap<String,String>();
		try 
		{
			Document d = XML.parse(xmlFile);
			BufferedReader b = new BufferedReader(new InputStreamReader(new FileInputStream(neTaggedFile), "UTF-8"));
			String line;	
			while ((line = b.readLine()) != null)
			{	
				String[] parts = line.split("\\s+");
				if (parts.length > 4)
				{
					String neTag = parts[2]; 
					String lemma = parts[1];
					String id=parts[4];
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
					id2lemma.put(id,lemma);
				}
			}
			for (Element w: XML.getElementsByTagname(d.getDocumentElement(), "w", false))
			{
				String z = id2tag.get(w.getAttribute("id"));
				if (z != null)
				{
					//System.err.println(z);
					w.setAttribute("function", z);
				}
				String l = id2lemma.get(w.getAttribute("id"));
				if (l != null)
				{
					//System.err.println(z);
					w.setAttribute("lemma", l);
				}
			}
			//tagRanges(d);
			//addNyms(d);
			//numberParagraphs(d);
			System.out.println(XML.documentToString(d));
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
