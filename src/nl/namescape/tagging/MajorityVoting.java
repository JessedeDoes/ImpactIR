package nl.namescape.tagging;
import nl.namescape.nelexicon.ElementConcordancer;
import nl.namescape.sentence.JVKSentenceSplitter;
import nl.namescape.sentence.TEISentenceSplitter;
import nl.namescape.stats.NameFrequencyList;
import nl.namescape.tei.TEITagClasses;
import nl.namescape.tokenizer.TEITokenizer;
import nl.namescape.util.XML;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;

import org.w3c.dom.*;

import nl.namescape.evaluation.*;
import nl.namescape.filehandling.DirectoryHandling;
import nl.namescape.filehandling.SimpleInputOutputProcess;
public class MajorityVoting implements SimpleInputOutputProcess
{
	NameFrequencyList withType = new NameFrequencyList();
	NameFrequencyList withoutType = new NameFrequencyList();
	List<Element> allEntities = new ArrayList<Element>();

	int minFrequency = 10;
	double minProportion = 0.6;

	public void checkDifferentTaggingsOfSameName(Document d)
	{
		// allEntities = TEITagClasses.getNameElements(d);
		withoutType = new NameFrequencyList();
		withoutType.typeSensitive = false;
		// withType.processDocument(d);
		withoutType.processDocument(d);
		ElementConcordancer ec = new ElementConcordancer();
		
		for (String s: withoutType.keySet())
		{
			int f = withoutType.getFrequency(s);
			if (f >= minFrequency)
			{
				
				Counter<String> typeMap = withoutType.getTypes(s);
				List<String> sorted = typeMap.keyList();
				String topType = sorted.get(0);
				//System.err.println("looking at " + s + " top: " + topType);
				if (typeMap.get(topType) >= minProportion * f) // regard other types as nonsensical
				{
					for (Element e: withoutType.getInstances(s))
					{
						String typeWas = e.getAttribute("type");
						if (!typeWas.equals(topType))
						{
							System.err.println("VOTING: CHANGE TYPE OF " + s + " FROM "  + typeWas + " TO " + topType + " " + ec.getConcordance(e));
							e.setAttribute("type", topType);
						}
					}
				}
			}
		}
	}

	@Override
	public void handleFile(String inFilename, String outFilename) 
	{
		// TODO Auto-generated method stub
		Document d = null;

		try 
		{
			d = XML.parse(inFilename);
		} catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		try 
		{
			PrintStream pout = new PrintStream(new FileOutputStream(outFilename));
			checkDifferentTaggingsOfSameName(d);
			pout.print(XML.documentToString(d));
			pout.close();
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	@Override
	public void setProperties(Properties properties) 
	{
		// TODO Auto-generated method stub
	
	}
	
	public static void main(String[] args)
	{
		MajorityVoting mv  = new MajorityVoting();
		DirectoryHandling.tagAllFilesInDirectory(mv, args[0], args[1]);
	}
}
