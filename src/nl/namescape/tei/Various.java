package nl.namescape.tei;

import java.util.List;

import nl.namescape.util.XML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;




public class Various 
{
	// header / fileDesc / editionStmt / respStmt: [edition, respStmt]*
	public static void encodeTagger(Document d, String taggerName)
	{
		String[] path = { "teiHeader", "fileDesc", "editionStmt", "edition"};
		Element edition = createOrFindPath(d,d.getDocumentElement(), path);
		String[] path0 = { "teiHeader", "fileDesc", "editionStmt"};
		Element editionStmt = createOrFindPath(d,d.getDocumentElement(), path0);
		String s = edition.getTextContent();
		if (s == null || s.length() == 0)
			edition.setTextContent("linguistic processing: " + taggerName  + ".");
		else
			edition.setTextContent(s + " " + " linguistic processing: " + taggerName);
		String[] path1 = { "teiHeader", "fileDesc", "editionStmt", "respStmt"};
		Element respStmt = d.createElement("respStmt");
		editionStmt.appendChild(respStmt);
		Element e;
		respStmt.appendChild(e = d.createElement("resp"));
		e.setTextContent("linguistic annotation");
		respStmt.appendChild(e = d.createElement("name"));
		e.setTextContent(taggerName);
	}
	
	public static Element createOrFindPath(Document d, Element start, String[] elementNames)
	{
		Element p = start;
		for (String e: elementNames)
		{
			Element c = XML.getElementByTagname(p, e);
			if (c == null)
			{
				c = d.createElement(e);
				p.appendChild(c);
			}
			p = c;
		}
		return p;
	}
	
	public static Element getEnclosingBlock(Element w)
	{
		Node p = w.getParentNode();
		while (p != null && !TEITagClasses.tagSplitsWords(p.getNodeName()))
		{
			p = p.getParentNode();
		}
		return (Element) p;
	}

	public static void fixNamespaceStuff(Document d)
	{
		for (Element e: XML.getAllSubelements(d.getDocumentElement(), true))
		{
			String ns = e.getNamespaceURI();
			String z=null;
			if ((ns != null && ns.length()==0) || ((z = e.getAttribute("xml:xmlns")) != null) && z.equals("") )
			{
				System.err.println("empty namespace for "  + e.getNodeName());
				e.removeAttribute("xml:xmlns"); // HM werkt dit? nee dus
			}
		}
	}
	
	public static void numberParagraphs(Document d) // number block elements (parents of word or sentence)
	{
		int k=0;
		try
		{
			Element root = d.getDocumentElement();
			Element idno = XML.getElementByTagname(root,"idno");
			String id = idno.getTextContent().trim();
			id = "ns." + id;
			idno.setTextContent(id);
			Element previousBlock = null;
			List<Element> sentences = XML.getElementsByTagname(root,"s", false); // HM. 
			for (Element w: sentences)
			{
				Element b = getEnclosingBlock(w);
				if (b != previousBlock && b != null)
				{
					String bid  = id + "_block-" + k++;
					b.setAttribute("xml:id", bid);
				}
				previousBlock = b;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
