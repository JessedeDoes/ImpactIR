package nl.namescape.sentence;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Properties;

import nl.namescape.filehandling.DirectoryHandling;
import nl.namescape.tokenizer.TEITokenizer;
import nl.namescape.util.Proxy;
import nl.namescape.util.XML;

import org.w3c.dom.Document;




public class TEISentenceSplitter implements nl.namescape.filehandling.SimpleInputOutputProcess
{
	SentenceSplitter splitter=null;
	private Properties properties;
	
	public TEISentenceSplitter(SentenceSplitter splitter)
	{
		this.splitter = splitter;
	}
	
	public void splitSentences(Document d)
	{
		TEITokenStream t = new TEITokenStream(d);
		splitter.split(t);
		t.tagSentences();
	}
	
	

	@Override
	public void handleFile(String in, String out) 
	{
		// TODO Auto-generated method stub
		boolean tokenize = false;
		Document d = null;
		
		TEITokenizer tok = new TEITokenizer();
		d = tok.getTokenizedDocument(in, true);
		this.splitSentences(d);
		
		try 
		{
			PrintStream pout = new PrintStream(new FileOutputStream(out));
			pout.print(XML.documentToString(d));
			pout.close();
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
		Proxy.setProxy();
		TEISentenceSplitter s = new TEISentenceSplitter(new JVKSentenceSplitter());
		DirectoryHandling.tagAllFilesInDirectory(s, args[0], args[1]);
	}
}
