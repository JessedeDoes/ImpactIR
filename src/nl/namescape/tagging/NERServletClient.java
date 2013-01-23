package nl.namescape.tagging;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;



public class NERServletClient implements SentenceTagger 
{
	public String server = "svowim01.inl.loc";
	public int port = 8090;
	public URL url = null; 
	public String classifier =  "conll2002";
	public String servletLocation = "/NER/NERServlet";
	Map<String,String> requestParameters = new HashMap<String,String>();	
	public boolean useTags = false;
	
	public NERServletClient()
	{
		try
		{
			url = new URL("http://" + server + ":" + port  + servletLocation);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		requestParameters.put("outputFormat", "WordPerLine");
		requestParameters.put("preserveSpacing", "false");
		requestParameters.put("classifier", classifier);
	}

	@Override
	public String tagString(String in) 
	{
		//System.err.println("Tagging: "  + in);
		requestParameters.put("input", in);
		return nl.namescape.util.HTTP.postRequest(url, requestParameters);
	}

	@Override
	public void tagWordElement(Element w, String line) 
	{
		try
		{
			String[] parts = line.split("\\s+");
			w.setAttribute("neLabel", parts[1]);
		} catch (Exception e)
		{
			System.err.println(line);
			e.printStackTrace();
		}
	}

	@Override
	public void postProcessDocument(Document d) 
	{
		// TODO Auto-generated method stub
		(new nl.namescape.tei.TEINameTagging()).realizeNameTaggingInTEI(d);
	}
	
	public static void main(String[] args)
	{
		NERServletClient stan = new NERServletClient();
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

	@Override
	public String tokenToString(Element t) 
	{
		// TODO Auto-generated method stub
		String w = t.getTextContent();
		if (useTags)
		{
			String tag = t.getAttribute("neLabel");
			if (tag == null || tag=="")
				tag="O";
			return w + "\t" + tag;
		}
		return w;
	}
}
