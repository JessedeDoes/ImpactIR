package nl.namescape.nelexicon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.namescape.tei.TEITagClasses;
import nl.namescape.tokenizer.TEITokenizer;
import nl.namescape.util.XML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.util.UUID;

public class ElementConcordancer 
{
	Map<String, Document>
	tokenizedParagraphHash = new HashMap<String, Document>();
	int quotationLength = 50;
	
	public String getConcordance(Element n) 
	{
		String id;
		giveElementAnId(n);
		
		Element ancestor = (Element) n.getParentNode();
		
		
		while (true)
		{
			String text = ancestor.getTextContent().trim();
			String[] words = text.split("\\s+");
			nl.openconvert.log.ConverterLog.defaultLog.println("SIZE "  + words.length);
			if (words.length > this.quotationLength || 
					TEITagClasses.isSentenceSplittingElement(ancestor))
			{
			
				break;
			}
			try
			{
				ancestor = (Element) ancestor.getParentNode();
			} catch (Exception e)
			{
				break;
			}
		}
		
		giveElementAnId(ancestor);
		giveNamesIds(ancestor);
		
		Document tokenizedElement = tokenizedParagraphHash.get(ancestor.getAttribute("xml:id"));
		
		if (tokenizedElement == null)
		{
			tokenizedElement = 
			 new TEITokenizer().tokenizeString(XML.NodeToString(ancestor));
			tokenizedParagraphHash.put(ancestor.getAttribute("xml:id"), tokenizedElement); // OK dan hebben niet alle NE's hierin een ID!
		}
		
		// nl.openconvert.log.ConverterLog.defaultLog.println(XML.documentToString(tokenizedElement));
		
		List<Element> nameInContextx = XML.getElementsByTagnameAndAttribute(tokenizedElement.getDocumentElement(), 
				n.getTagName(), "xml:id", n.getAttribute("xml:id"), false);
		
		if (nameInContextx.size() == 0)
		{
			
			nl.openconvert.log.ConverterLog.defaultLog.println(n.getAttribute("xml:id") +  " NOT FOUND IN " + XML.documentToString(tokenizedElement));
			return "";
		}
		Element nameInContext = nameInContextx.get(0);
		
		List<Element> words = 
				TEITagClasses.getTokenElements(tokenizedElement.getDocumentElement());
		
		List<Element> wordsInEntity = 
				TEITagClasses.getTokenElements(nameInContext);
		
		Element firstWord = wordsInEntity.get(0);
		Element lastWord = wordsInEntity.get(wordsInEntity.size()-1);
		int startIndex = 1, endIndex=0;

		for (int i=0; i < words.size(); i++)
		{
			Element w = words.get(i);
			if (w==firstWord) startIndex=i;
			if (w==lastWord) endIndex=i;
		}
		
		int start = Math.max(0, startIndex-10);
		int end = Math.min(words.size(), endIndex+10);
		String concordance="";
		for (int i=start; i < end; i++)
		{
			if (i == startIndex)
				concordance += "<oVar>";
			
		
			concordance += words.get(i).getTextContent();
			if (i== endIndex)
				concordance += "</oVar>";
			if (i < end -1)
			{
				concordance += " ";
			}
		}
		nl.openconvert.log.ConverterLog.defaultLog.println("CONC: " + concordance);
		return concordance;
	}

	private void giveElementAnId(Element n) {
		String id = n.getAttribute("xml:id");
		
		if (id == null || id.length() == 0)
		{
			id = UUID.randomUUID().toString();
			n.setAttribute("xml:id", id);
		}
	}

	private void giveNamesIds(Element ancestor) 
	{
		List<Element> namez = TEITagClasses.getNameElements(ancestor);
		for (Element nm: namez)
		{
			String id;
			giveElementAnId(nm);
		}
	}
}
