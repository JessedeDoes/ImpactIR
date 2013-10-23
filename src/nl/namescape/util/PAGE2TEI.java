package nl.namescape.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Properties;

import nl.namescape.filehandling.DirectoryHandling;
import nl.namescape.filehandling.SimpleInputOutputProcess;

import org.w3c.dom.Document;

import impact.ee.util.Resource;

public class PAGE2TEI implements SimpleInputOutputProcess
{
	XSLTTransformer transformer;
	TagSoupParser parser = new TagSoupParser();
	
	public PAGE2TEI()
	{
		try
		{
			transformer = new XSLTTransformer((new Resource()).openStream("xsl/page2tei.xsl"));
		} catch (Exception e)
		{
			
		}
	}
	
	private void removeOtherNamespaces(Document d)
	{
		
	}
	
	public Document convertDocument(Document htmlDocument)
	{
		 return transformer.transformDocument(htmlDocument);
	}
	
	@Override
	public void handleFile(String docFile, String outFilename) 
	{
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		
		
		
		try 
		{
			Document htmlDocument = XML.parse(docFile);
			//System.err.println(XML.documentToString(htmlDocument));
			Document teiDocument  = transformer.transformDocument(htmlDocument);
			PrintStream pout = new PrintStream(new FileOutputStream(outFilename));
			pout.print(XML.documentToString(teiDocument));
			pout.close();
		} catch (Exception e) 
		{
			e.printStackTrace
			();
			
		}	
	}

	@Override
	public void setProperties(Properties properties) {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args)
	{
		PAGE2TEI x = new PAGE2TEI();
		DirectoryHandling.tagAllFilesInDirectory(x, args[0], args[1]);
		//x.dinges("/mnt/Projecten/Taalbank/Werkfolder_Redactie/Jesse/Projecten/Papiamento/Mosaiko 5 HV Kap 1 vershon 2012 10 28.doc");
	}
}
