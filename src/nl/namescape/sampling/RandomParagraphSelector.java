package nl.namescape.sampling;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import nl.namescape.tei.Paragraph;
import nl.namescape.util.XML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;



public class RandomParagraphSelector 
{
	Map<String,Paragraph> paragraphMap = new HashMap<String,Paragraph>();
	Set<String> selection = new HashSet<String>();
	int numTokens = 1000000;
	
	
	public String getDocumentId(Document d)
	{
		Element e = d.getDocumentElement();
		String idno = XML.getElementContent(e, "idno");
		return idno;
	}
	public void selectParagraphs(List<String> fileNames)
	{
		for (String fn: fileNames)
		{
			try 
			{
				System.err.println("parsing: " + fn);
				Document d = XML.parse(fn);
				List<Element> paragraphs = 
						XML.getElementsByTagname(d.getDocumentElement(), "p", false);
				for (Element e: paragraphs)
				{
					if (Paragraph.isDecentParagraph(e))
					{
						Paragraph p = new Paragraph(e);
						paragraphMap.put(p.id,p);
					}
				}
			} catch (Exception e) 
			{
				e.printStackTrace();
			} 
		}
		List<String> allIds = new ArrayList<String>();
		allIds.addAll(paragraphMap.keySet());
		Collections.shuffle(allIds);
		int nTokens = 0;
		
		for (String s: allIds)
		{
			Paragraph p = paragraphMap.get(s);
			nTokens += p.numTokens;
			if (nTokens > numTokens)
				break;
			selection.add(p.id);
		}
		
		for (String fn: fileNames)
		{
			try 
			{
				Document d = XML.parse(fn);
				List<Element> paragraphs = 
						XML.getElementsByTagname(d.getDocumentElement(), "p", false);
				int nSelectedTokensInDocument = 0;
				String selectedPart="";
				for (Element e: paragraphs)
				{
					Paragraph p = new Paragraph(e);
					if (selection.contains(p.id))
					{
						//System.out.println(ParseUtils.)
						nSelectedTokensInDocument+= p.numTokens;
						selectedPart += XML.NodeToString(e);
					}
				}
				if (nSelectedTokensInDocument > 0)
				System.out.println("<doc id='" +
				getDocumentId(d) + 
						"' nSelectedTokens='" 
				+ nSelectedTokensInDocument + "'>\n" 
				+ "<file>" 
				+ fn 
				+ "</file>\n"  
				+ selectedPart + "\n</doc>");
			} catch (Exception e) 
			{
				e.printStackTrace();
			} 
		}
		System.err.println("Selected: " + nTokens + " tokens");
	}
	
	
	public List<String> readList(String listFileName)
	{
		try 
		{
			BufferedReader reader = new BufferedReader(new FileReader(listFileName));
			List<String> filenames = new ArrayList<String>();
			String filename;
			while ((filename = reader.readLine()) != null)
			{
				filenames.add(filename);
			}
			return filenames;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		RandomParagraphSelector rps = new RandomParagraphSelector();
		rps.selectParagraphs(rps.readList(args[0]));
	}
}
