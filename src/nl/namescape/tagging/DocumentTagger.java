package nl.namescape.tagging;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.SAXParser;


import nl.namescape.filehandling.SimpleInputOutputProcess;
import nl.namescape.sentence.JVKSentenceSplitter;
import nl.namescape.sentence.TEISentenceSplitter;
import nl.namescape.tei.TEITagClasses;
import nl.namescape.tokenizer.PunctuationTagger;
import nl.namescape.tokenizer.TEITokenizer;
import nl.namescape.util.Options;
import nl.namescape.util.Progress;
import nl.namescape.util.XML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/*

 */
public class DocumentTagger implements SimpleInputOutputProcess
{
	public boolean tokenize = true;
	public boolean splitSentences = false;
	SentenceTagger sentenceTagger = null;
	protected Properties properties;
	
	
	public DocumentTagger()
	{
		
	}
	
	public DocumentTagger(SentenceTagger st)
	{
		sentenceTagger = st;
	}
	
	private String join(String separator, List<String> words)
	{
		String s = "";
		for (int i=0; i < words.size(); i++)
		{
			s += words.get(i) + ((i<words.size()-1)?separator:"");
		}
		return s;
	}
	
	private String tagSentence(List<String> words)
	{
		String sentence="";
	
		for (int i=0; i < words.size(); i++)
		{
			sentence += words.get(i) + ((i<words.size()-1)?" ":"");
		}
		// System.err.println(sentence);
		return sentenceTagger.tagString(sentence);
		
	}
	
	private String tagSentence(String sentence)
	{
		String[] words = sentence.split("\\s+");
		ArrayList<String> wordlist = new ArrayList<String>();
		for (String w: words)
			wordlist.add(w);
		return tagSentence(wordlist);
	}

	public void addTaggingToTokenizedDocument(Document d)
	{
		Element root = d.getDocumentElement();
		List<Element> sentences = XML.getElementsByTagname(root, "s", false);
		int nWords = 0;
		int nSentences = 0;
		long startTime = System.currentTimeMillis();
		boolean defer = false;
		
		String[] tokenElementNames = {"w", "pc"};
		List<String> allSentences = new ArrayList<String>();
		double totalSentences = sentences.size();
		
		String threadId = "thread" + Thread.currentThread().getId();
		
		Progress.setMessage(threadId,"start tagging...");
		Progress.setPercentage(threadId, 0);
		
		for (Element s: sentences)
		{
			List<String> tokens= new ArrayList<String>();
			List<Element> tokenElements = 
					XML.getElementsByTagname(s, tokenElementNames, false);
			// OK onderstaand kan mis gaan bij lege tokens...
			for (Element t: tokenElements)
			{
				String str = t.getTextContent();
				if (str.length()  == 0 || str.equals(" ") || str.matches("^\\s+$"))
					t.getParentNode().removeChild(t);
				else
				{
					tokens.add(sentenceTagger.tokenToString(t));
				}
			}
			// in case we removed some: need to update list...
			tokenElements = 
					XML.getElementsByTagname(s, tokenElementNames, false);
			nSentences++;
			
			Progress.setPercentage(threadId, nSentences / totalSentences);
			Progress.setMessage(threadId, "tagging, at " + nSentences + " of " 
			+ totalSentences +  " sentences ");
			nWords += tokens.size();
			if (nSentences % 50 == 0)
			{
				//reconnect();
				double elapsed = (System.currentTimeMillis() - startTime) / 1000.0;
				System.err.println("seconds: "  + elapsed + " sentences: " + nSentences + " words: "  + nWords + " .....  ");
			}
			if (tokens.size() > 0)
			{
				if (defer) 
					allSentences.add(join(" ", tokens));
				if (defer) continue;
				String sentenceTagged = sentenceTagger.tagString(join(" ", tokens));
				if (sentenceTagged != null)
				{
					String[] wordTaggings = sentenceTagged.split("\\s*\n\\s*");
					for (int i=0; i < tokens.size(); i++)
					{
						sentenceTagger.tagWordElement(tokenElements.get(i), wordTaggings[i]);
					}
				/*
				List<TaggedoWord> taggedWords = tagWords(tokens);

				if (taggedWords.size() < tokens.size())
				{ 
					System.err.println("TAGGING ERROR IN SENTENCE #" 
							+ s.getTextContent().replaceAll("\\s+", " ") + "#");
					System.err.println("tagged response size: "  + taggedWords.size() + " < " + tokens.size());
					System.err.println("response: " + flat(taggedWords));
					s.setAttribute("failed", "miserably");
					reconnect();
					continue;
				}
				for (int i=0; i < tokens.size(); i++)
				{
					try
					{
						tokenElements.get(i).setAttribute("function", taggedWords.get(i).tag);
						tokenElements.get(i).setAttribute("lemma", taggedWords.get(i).lemma);
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				*/
				}
			}
		}
		Progress.setPercentage(threadId,1);
		Progress.setMessage(threadId,"tagging done, total " + nWords + " words");
		if (defer)
		{
			String all = join("\n<utt>\n", allSentences);
			//System.out.println(tagString(all));
		}
		
		long endTime = System.currentTimeMillis();
		long interval = endTime - startTime;
		double secs = interval / 1000.0;
		double wps = nWords / secs;
		System.err.println("tokens " + nWords);
		System.err.println("seconds " + secs);
		System.err.println("tokens per second " + wps);
		sentenceTagger.postProcessDocument(d);
	}
	
    public void tagTokenizedXMLFile(String xmlFile)
    {
    	try 
		{
			Document d = XML.parse(xmlFile);
			addTaggingToTokenizedDocument(d);
			System.out.println(XML.documentToString(d));
		} catch (Exception e)
		{
			e.printStackTrace();
		}
    }
    
	public Document tagUntokenizedXMLFile(String inFile)
	{
		try 
		{
			TEITokenizer tok = new TEITokenizer();
			Document d0 = tok.getTokenizedDocument(inFile, 
					true);
			TEISentenceSplitter splitter = 
					new TEISentenceSplitter(new JVKSentenceSplitter());
			splitter.splitSentences(d0);
			addTaggingToTokenizedDocument(d0);
			return d0;
		} catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public void tagXMLFile(String inFile, String outFile)
	{
		try 
		{
			Document d0;
			if (this.tokenize)
			{
				TEITokenizer tok = new TEITokenizer();
				d0 = tok.getTokenizedDocument(inFile, true);
				TEISentenceSplitter splitter = new TEISentenceSplitter(new JVKSentenceSplitter());
				splitter.splitSentences(d0);
			}  else
			{
				d0 = nl.namescape.util.XML.parse(inFile);
				if (this.splitSentences)
				{
					TEISentenceSplitter splitter = new TEISentenceSplitter(new JVKSentenceSplitter());
					(new PunctuationTagger()).tagPunctuation(d0);
					splitter.splitSentences(d0);
				}
				
			}
			addTaggingToTokenizedDocument(d0);

			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(outFile), "UTF8");
			out.write(XML.documentToString(d0));
			out.close();
		} catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void tagListOfUntokenizedDocuments(String listFile,  String outFolderName)
	{
		try 
		{
			BufferedReader r = new BufferedReader(new 
					InputStreamReader(new FileInputStream(listFile), "UTF-8"));
			String s;
			while ((s = r.readLine()) != null)
			{	
				String b = (new File(s)).getName();
				tagXMLFile(s, outFolderName + "/" + b);
			}
			//sortByFrequency();
		} catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void tagXMLFilesInDirectory(String folderName, String outFolderName)
	{
		File f = new File(folderName);
		boolean saveToZip = false;
		ZipOutputStream zipOutputStream = null;
		
		if (f.isDirectory())
		{
			if (outFolderName.endsWith(".zip"))
			{
				try 
				{
					zipOutputStream = 
							new ZipOutputStream(new FileOutputStream(outFolderName));
				} catch (FileNotFoundException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				saveToZip = true;
			}
			
			File[] entries = f.listFiles();
			for (File x: entries)
			{
				String base = x.getName();
				System.err.println(base);
				if (x.isFile())
				{
					if (!x.getName().endsWith(".xml"))
						continue;
					try 
					{
						if (saveToZip)
						{
							String entryName = x.getCanonicalPath();
							if (entryName.startsWith(f.getCanonicalPath()))
								entryName = entryName.substring(f.getCanonicalPath().length()+1);
							
							Document d = tagUntokenizedXMLFile(x.getCanonicalPath());
							String s = XML.documentToString(d);
							
							ZipEntry newEntry = new ZipEntry(entryName);
							newEntry.setComment("Frogged version of "+ x.getCanonicalPath());
							zipOutputStream.putNextEntry(newEntry);
							zipOutputStream.write(s.getBytes("UTF-8"));
							zipOutputStream.closeEntry();
							zipOutputStream.flush();
						} else
						{
							File outFile = new File( outFolderName + "/" + base);
							if (!outFile.exists())
							{
								tagXMLFile(x.getCanonicalPath(), outFolderName + "/" + base);
							}
						}
					} catch (Exception e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else
				{
					try
					{
						tagXMLFilesInDirectory(x.getCanonicalPath(), outFolderName);
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		if (saveToZip)
		{
			try {
				zipOutputStream.close();
			} catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args)
	{
		nl.namescape.util.Options options = new nl.namescape.util.Options(args);
		args = options.commandLine.getArgs();

		try
		{
			String taggerClassName = options.getOption("tagger");
			SentenceTagger st = (SentenceTagger)  
					(Class.forName(taggerClassName).newInstance());
			DocumentTagger dt = new DocumentTagger(st);
			dt.tokenize = options.getOptionBoolean("tokenize", true);

			File f = new File(args[0]);
			if (f.isDirectory())
				dt.tagXMLFilesInDirectory(args[0], args[1]);
			else
				dt.tagXMLFile(args[0], args[1]);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}

	@Override
	public void handleFile(String inFilename, String outFilename) 
	{
		this.tagXMLFile(inFilename, outFilename);
	}

	@Override
	public void setProperties(Properties properties) 
	{
		// TODO Auto-generated method stub
		this.properties = properties;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
}
