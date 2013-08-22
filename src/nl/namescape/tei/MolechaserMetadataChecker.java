package nl.namescape.tei;
import nl.namescape.filehandling.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import nl.namescape.util.Proxy;
import nl.namescape.util.XML;
import org.w3c.dom.*;
public class MolechaserMetadataChecker implements DoSomethingWithFile
{

	ConcurrentHashMap<String,Set<String>> 
		idmap = new ConcurrentHashMap<String,Set<String>>();
	ConcurrentMap<String,String> multiProperties = new ConcurrentHashMap<String,String>();
	int N = 0;
	boolean checkIdno = true;
	@Override
	public void handleFile(String fileName) 
	{
		// TODO Auto-generated method stub
		//System.err.println(N + ": "+ fileName);
		try
		{
			Document d = XML.parse(fileName,false);
			Metadata m = new Metadata(d);
			checkMetadata(m, fileName);
		} catch (Exception e)
		{
			e.printStackTrace();
			//System.exit(1);
		}
	}
	
	public boolean isYear(String y)
	{
		return y.matches("^[0-9][0-9][0-9][0-9]$");
	}
	
	public  void checkMetadata(Metadata m, String fileName)
	{
		
		String witnessYear_from = m.getValue("witnessYear_from").trim();
		String witnessYear_to = m.getValue("witnessYear_to").trim();
		String idno = m.getValue("idno").trim();
		
		
		if (!isYear(witnessYear_from))
		{
			System.err.println("invalid year in " + fileName + " : " + witnessYear_from);
		}
		if (!isYear(witnessYear_to))
		{
			System.err.println("invalid year in " + fileName + " : " + witnessYear_to);
		}
		
		if (checkIdno)
		{
			Set<String> filesWithThisId = idmap.get(idno);

			if (filesWithThisId == null)
			{
				filesWithThisId = new HashSet<String>();
				idmap.put(idno, filesWithThisId);
			} else
			{
				filesWithThisId.add(fileName);
				System.err.println("Error: duplicate idno "  + idno + " " + filesWithThisId);
			}
			filesWithThisId.add(fileName);
		}
		
		Set<String> languages = m.metadata.get("languageVariant");
		String l;
		if (languages == null || languages.size() != 1)
		{
			System.err.println("Wrong number of language variants: " + languages);
		} else
		{
			l = languages.iterator().next();
			if (!l.equals("NN") && !l.equals("BN"))
			{
				System.err.println("Wrong language variant: " + l);
			}
		}
		
		for (String name: m.metadata.keySet())
		{
			Set<String> values = m.metadata.get(name);
			if (values.size() > 1 && !multiProperties.containsKey(name))
			{
				System.err.println("Multiply defined property " + name +  " in " + fileName +  " :  " + values);
				multiProperties.put(name,name);
			}
		}
		if (N % 1000 == 0)
		{
			print(m,fileName);
		}
		N++;
	}
	
	public synchronized void print(Metadata m, String fileName)
	{
		
		String authorLevel1 = m.getValue("authorLevel1").trim();
		String authorLevel2 = m.getValue("authorLevel2").trim();
		String titleLevel1 = m.getValue("titleLevel1").trim();
		String titleLevel2 = m.getValue("titleLevel2").trim();
		String witnessYear_from = m.getValue("witnessYear_from").trim();
		String witnessYear_to = m.getValue("witnessYear_to").trim();
		String languageVariant = m.getValue("languageVariant").trim();
		String corpusProvenance =  m.getValue("corpusProvenance").trim();
		String author=authorLevel1.equals("")?authorLevel2:authorLevel1;
		String title=authorLevel1.equals("")?titleLevel2:titleLevel1;
		
		System.out.println(N + "\t" + corpusProvenance+ "\t" +  fileName + "\t" + languageVariant + "\t" 
				+ author + "\t" + title + "\t" + 
				witnessYear_from + "\t"+ witnessYear_to + "\t" + 
				authorLevel1 + "\t" +  authorLevel2 + "\t" 
				+ titleLevel1 + "\t" + titleLevel2);
		System.out.flush();
	}
	
	public static void main(String[] args)
	{
		Proxy.setProxy();
		DoSomethingWithFile d = new MolechaserMetadataChecker();
		MultiThreadedFileHandler m = new MultiThreadedFileHandler(d,4);
		DirectoryHandling.traverseDirectory(d, args[0]);
		m.shutdown();
	}
}
