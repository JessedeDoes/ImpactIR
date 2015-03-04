package nl.namescape;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import nl.namescape.sentence.JVKSentenceSplitter;
import nl.namescape.sentence.TEISentenceSplitter;
import nl.namescape.tei.TEITagClasses;
import nl.namescape.tokenizer.TEITokenizer;
import nl.namescape.util.XML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.util.Set;

public class BIOOutput implements nl.openconvert.filehandling.SimpleInputOutputProcess
{
	boolean tagParts = true;
	private Properties properties; 

	public void printBIO(Document d, PrintStream out)
	{
		List<Element> sentences = nl.namescape.tei.TEITagClasses.getSentenceElements(d);
		for (Element s: sentences)
		{
			List<Element> names = 
					XML.getElementsByTagname(s, TEITagClasses.nameTagNames, false);
			for (Element n:names)
			{
				List<Element> tokens = 	nl.namescape.tei.TEITagClasses.getTokenElements(n);
				String type = n.getAttribute("type");
				if (tokens.size() > 5)
				{
					System.err.println("Suspect: Long entity: " + n.getTextContent());
				}
				int nWordsSeen=0;
				int nWords = nl.namescape.tei.TEITagClasses.getWordElements(n).size();
				for (int k=0; k < tokens.size(); k++)
				{
					Element t = tokens.get(k);
					if (t.getTagName().contains("w"))
					{
						if (nWordsSeen==0)
							t.setAttribute("neLabel", "B-" + type);
						else
							t.setAttribute("neLabel", "I-" + type);
						nWordsSeen++;
					}
					else if (nWordsSeen > 0 && nWordsSeen < nWords) // internal pc
						t.setAttribute("neLabel", "I-" + type);
				}
			}

			if (tagParts)
			{
				List<Element> nameParts = 
						XML.getElementsByTagname(s, TEITagClasses.namePartTags, false);
				
				for (Element n:nameParts)
				{
					List<Element> tokens = 	nl.namescape.tei.TEITagClasses.getWordElements(n);
					String subtype = n.getAttribute("type");
					int nWordsSeen=0;
					int nWords = nl.namescape.tei.TEITagClasses.getWordElements(n).size();
					
					for (int k=0; k < tokens.size(); k++)
					{
						Element t = tokens.get(k);
						if (t.getTagName().contains("w"))
						{
							if (nWordsSeen==0)
								t.setAttribute("nePartLabel", "B-" + subtype);
							else
								t.setAttribute("nePartLabel", "I-" + subtype);
							nWordsSeen++;
						}
						else if (nWordsSeen > 0 && nWordsSeen < nWords) // internal pc
							t.setAttribute("nePartLabel", "I-" + subtype);
					}
				}
			}			
			List<Element> tokens = 	nl.namescape.tei.TEITagClasses.getTokenElements(s);
			for (Element t: tokens)
			{

				String label = t.getAttribute("neLabel");
				if (label == null || label.equals(""))
					label = "O"; 
				
				if (tagParts)
				{
					String partLabel = t.getAttribute("nePartLabel");
					if (partLabel == null || partLabel.equals(""))
						partLabel = "O"; 
					out.println(t.getTextContent() + "\t" + label + "\t" + partLabel);
				} else
					out.println(t.getTextContent() + "\t" + label);
			}
			if (tokens.size() > 0) // no empty sentences in output
				out.print("\n");
		}
	}


	@Override
	public void handleFile(String in, String out) 
	{
		boolean tokenize = false;
		Document d = null;
		if (tokenize)
		{
			TEITokenizer tok = new TEITokenizer();
			d = tok.getTokenizedDocument(in, true);
			new TEISentenceSplitter(new JVKSentenceSplitter()).splitSentences(d);
		} else
		{
			try 
			{
				d = XML.parse(in);
			} catch (Exception e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		try 
		{
			PrintStream pout = new PrintStream(new FileOutputStream(out));
			printBIO(d, pout);
		} catch (FileNotFoundException e) 
		{
			
			e.printStackTrace();
		}
	}

	@Override
	public void setProperties(Properties properties) 
	{
		// TODO Auto-generated method stub
		this.properties = properties;
	}
	public static void main(String[] args)
	{
		System.err.println("hi there!");
		
		try
		{
			nl.openconvert.filehandling.DirectoryHandling.tagAllFilesInDirectory(new BIOOutput(), args[0], 
				args[1]);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}


	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
}
