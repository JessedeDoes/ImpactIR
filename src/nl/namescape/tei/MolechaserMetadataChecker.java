package nl.namescape.tei;
import nl.namescape.filehandling.*;
import nl.namescape.util.XML;
import org.w3c.dom.*;
public class MolechaserMetadataChecker implements DoSomethingWithFile
{

	@Override
	public void handleFile(String fileName) 
	{
		// TODO Auto-generated method stub
		try
		{
			Document d = XML.parse(fileName);
			Metadata m = new Metadata(d);
			print(m, fileName);
		} catch (Exception e)
		{
			
		}
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
		
		System.out.println(corpusProvenance+ "\t" +  fileName + "\t" + languageVariant + "\t" 
				+ author + "\t" + title + "\t" + 
				witnessYear_from + "\t"+ witnessYear_to + "\t" + 
				authorLevel1 + "\t" +  authorLevel2 + "\t" 
				+ titleLevel1 + "\t" + titleLevel2);
	}
	
	public static void main(String[] args)
	{
		DoSomethingWithFile d = new MolechaserMetadataChecker();
		MultiThreadedFileHandler m = new MultiThreadedFileHandler(d,4);
		DirectoryHandling.traverseDirectory(m, args[0]);
		m.shutdown();
	}
}
