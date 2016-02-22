package nl.namescape.tagging;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;



public class ImpactLexiconClient implements SentenceTagger 
{
	public String server = "svowim01.inl.loc";
	public int port = 8090;
	public URL url = null; 
	public String classifier =  "dutch";
	public String servletLocation = "/Lexicon/LexiconServlet";
	Map<String,String> requestParameters = new HashMap<String,String>();	

	public ImpactLexiconClient()
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
		requestParameters.put("action", "lemmatize");
		requestParameters.put("alternatives", ""+1);
	}

	@Override
	public String tagString(String in) 
	{
		//nl.openconvert.log.ConverterLog.defaultLog.println("Tagging: "  + in);
		requestParameters.put("input", in);
		return nl.namescape.util.HTTP.postRequest(url, requestParameters);
	}

	@Override
	public void tagWordElement(Element w, String line) 
	{
		try
		{
			String[] parts = line.split("\\s+");
			w.setAttribute("function", parts[1]);
			w.setAttribute("lemma", parts[2]);
		} catch (Exception e)
		{
			nl.openconvert.log.ConverterLog.defaultLog.println(line);
			e.printStackTrace();
		}
	}

	@Override
	public void postProcessDocument(Document d) 
	{
		// TODO Auto-generated method stub
		(new nl.namescape.tei.TEINameTagging()).realizeNameTaggingInTEI(d);
	}
	
	@Override
	public String tokenToString(Element t) 
	{
		// TODO Auto-generated method stub
		return t.getTextContent();
	}

	public static void main(String[] args)
	{
		ImpactLexiconClient stan = new ImpactLexiconClient();
		//FrogClient frog = new FrogClient();
		if (args.length == 0)
		{
			for (int i=0; i < 1; i++)
			{
				String s = stan.tagString("Weet je ,  Piet is gek . Maar Jan is niet zoveel normaler ."  + " " + i);
				System.out.println(s);
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
