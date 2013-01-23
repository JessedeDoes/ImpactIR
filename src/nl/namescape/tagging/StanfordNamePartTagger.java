package nl.namescape.tagging;

import java.io.File;

import org.w3c.dom.Element;



public class StanfordNamePartTagger extends NERServletClient 
{
	public StanfordNamePartTagger()
	{
		super();
		classifier = "karina";
		requestParameters.put("classifier", classifier);
	}
	
	@Override
	public void tagWordElement(Element w, String line) 
	{
		try
		{
			String[] parts = line.split("\\s+");
			w.setAttribute("neLabel", parts[1]);
			w.setAttribute("nePartLabel", parts[2]);
		} catch (Exception e)
		{
			System.err.println(line);
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		NERServletClient stan = new StanfordNamePartTagger();
		//FrogClient frog = new FrogClient();
		if (args.length == 0)
		{
			for (int i=0; i < 100; i++)
			{
				String s = stan.tagString("Weet je ,  Piet is gek . Maar Jan is niet zoveel normaler ."  + " " + i);
			}
		} else
		{
			DocumentTagger dt = new DocumentTagger(stan);
			File f = new File(args[0]);
			if (f.isDirectory())
				dt.tagXMLFilesInDirectory(args[0], args[1]);
			else
				dt.tagXMLFile(args[0], args[1]);
		}
	}
}
