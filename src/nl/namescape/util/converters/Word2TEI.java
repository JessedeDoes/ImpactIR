package nl.namescape.util.converters;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Properties;

import impact.ee.util.Resource;
import nl.namescape.sentence.JVKSentenceSplitter;
import nl.namescape.sentence.TEISentenceSplitter;
import nl.namescape.tokenizer.TEITokenizer;
import nl.namescape.util.WordConverter;
import nl.namescape.util.XML;
import nl.namescape.util.XSLTTransformer;
import nl.openconvert.filehandling.DirectoryHandling;
import nl.openconvert.filehandling.MultiThreadedFileHandler;
import nl.openconvert.filehandling.SimpleInputOutputProcess;

import org.w3c.dom.*;

public class Word2TEI implements SimpleInputOutputProcess
{
	WordConverter converter = new WordConverter();
	XSLTTransformer transformer;
	public Word2TEI()
	{
		try
		{
			transformer = new XSLTTransformer((new Resource()).openStream("xsl/html2tei.xsl"));
		} catch (Exception e)
		{
			
		}
	}
	
	public void dinges(String docFile)
	{
		Document htmlDocument = WordConverter.Word2HtmlDocument(docFile);
		System.err.println(XML.documentToString(htmlDocument));
		Document teiDocument  = transformer.transformDocument(htmlDocument);
		System.out.println(XML.documentToString(teiDocument));
	}
	


	@Override
	public void handleFile(String docFile, String outFilename) 
	{
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		Document htmlDocument = WordConverter.Word2HtmlDocument(docFile);
		//System.err.println(XML.documentToString(htmlDocument));
		Document teiDocument  = transformer.transformDocument(htmlDocument);
		
		
		try 
		{
			PrintStream pout = new PrintStream(new FileOutputStream(outFilename));
			pout.print(XML.documentToString(teiDocument));
			pout.close();
		} catch (FileNotFoundException e) 
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
		Word2TEI x = new Word2TEI();
		
		//DirectoryHandling.tagAllFilesInDirectory(x, args[0], args[1]);
		//MultiThreadedFileHandler m = new MultiThreadedFileHandler(x,nThreads);
		System.err.println("Start tagging from " + args[2] + " to " + args[3]);
		
		DirectoryHandling.traverseDirectory(x, new File(args[0]), new File(args[1]), null);
		//x.dinges("/mnt/Projecten/Taalbank/Werkfolder_Redactie/Jesse/Projecten/Papiamento/Mosaiko 5 HV Kap 1 vershon 2012 10 28.doc");
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
}
