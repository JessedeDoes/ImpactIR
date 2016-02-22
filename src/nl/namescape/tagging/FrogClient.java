package nl.namescape.tagging;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.namescape.sentence.JVKSentenceSplitter;
import nl.namescape.sentence.TEISentenceSplitter;
import nl.namescape.tokenizer.TEITokenizer;
import nl.namescape.util.Proxy;
import nl.namescape.util.XML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;



/*
 * Use the frog server 
 * Gaat uit van een frog -S <port> --skip=cmnp op server
 * Geef reeds getokenizeerde zin op 1 regel aan frog
 * Output heeft de vorm
 */
public class FrogClient extends DocumentTagger implements SentenceTagger
{

	class TaggedWord
	{
		String id;
		String token;
		String tag;
		String lemma;
		String analysis;
		double confidence;
	}

	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	public int port = 8000;
	public String server= "pcob40";

	public FrogClient()
	{
		connect();
		this.sentenceTagger = this;
	}
	
	@Override
	public void tagWordElement(Element e, String line) 
	{
		// TODO Auto-generated method stub
		String[] parts = line.split("\t");
		try
		{
		  
		 
		   e.setAttribute("lemma", parts[2]);
		   e.setAttribute("function", parts[4]);
		   e.setAttribute("ana", parts[3]);
		   //String tag = parts[4];
		} catch (Exception e1)
		{
			
		}
	}

	// TODO check and fix problems with incompatible tokenization
	
	public String tagString(String sentence)
	{
		sentence = sentence.replaceAll("\u00A0", " ");
		sentence = sentence.trim();
		
		//sentence = sentence.replace("^\\p{Zs}+", "");
		out.println(sentence);
		String taggedSentence = "";
		//out.println("");
		String line;
		List<String> answer = new ArrayList<String>();
		try 
		{
			while ((line = in.readLine()) != null)
			{
				/// nl.openconvert.log.ConverterLog.defaultLog.println("received" + line);
				answer.add(line);
				taggedSentence += line + "\n";
				if (line.equals("READY"))
				{
					break;
				}
			}
		} catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int x = sentence.charAt(0);
		
		String[] inputTokens = sentence.split("\\p{Zs}+");
		// nl.openconvert.log.ConverterLog.defaultLog.println(String.format("%x",x) + ":" + sentence);
		String fixed = checkAlignment(inputTokens,answer);
			if (fixed != null) taggedSentence = fixed;
		return taggedSentence;
	}

	private String  checkAlignment(String[] inputTokens, List<String> answer) 
	{
		// TODO Auto-generated method stub
		List<TaggedWord> tw = getTaggedWords(answer);
		boolean fixIt=true;
		
		boolean allOk = true;
		String fixedResult=null;
		for (int i=0; i < inputTokens.length; i++)
		{
			if (tw.size() <= i || !tw.get(i).token.equals(inputTokens[i]))
			{
				nl.openconvert.log.ConverterLog.defaultLog.println(tw.get(i).token + "?" + inputTokens[i]);
				// nl.openconvert.log.ConverterLog.defaultLog.println("problem in sentence: at " + i + "\n" + util.Util.join(answer, "\n") + "\n\n");
				allOk=false;
				break;
			}
		}
		if (allOk)
			return null;
		boolean canBeFixed=true;
		if (!allOk)
		{
			
			List<TaggedWord> r[] = new ArrayList[inputTokens.length];
			for (int i=0; i < r.length; i++)
				r[i] = new ArrayList<TaggedWord>();
			nl.openconvert.log.ConverterLog.defaultLog.println("trying to fix....");
			int k = 0; // position in inputTokens array
			int p = 0; // position in current token.
			
			for (int i=0; i < tw.size(); i++)
			{ 
				TaggedWord t = tw.get(i);
				String lookFor = t.token;
				if (k >= inputTokens.length)
				{
					canBeFixed=false;
					nl.openconvert.log.ConverterLog.defaultLog.println("failure [tw te groot]  while fixing!!!!");
					break;
				}
				if (inputTokens[k].equals(lookFor))
				{
					r[k].add(t);
					k++;
					p=0;
				} else
				{
					int p0 = inputTokens[k].indexOf(lookFor, p);
					if (p0 >= 0)
					{
						// match answer i to inputToken k
						p = p0 + lookFor.length();
						r[k].add(t);
						if (p >= inputTokens[k].length())
						{
							k++;
							p=0;
						}
					} else
					{
						nl.openconvert.log.ConverterLog.defaultLog.println("failure while fixing!!!! " + i +  " " + lookFor + " " + k);
						canBeFixed=false;
						
						//r.add(null);
						k++;
						p=0;
						break;
						// dit hoort niet te gebeuren!
					}
				}
			}
			if (canBeFixed)
			{
				nl.openconvert.log.ConverterLog.defaultLog.println("sentence could be fixed");
				fixedResult="";
				for (int i=0; i < inputTokens.length; i++)
				{
					List<TaggedWord> s = r[i];
					
					String tok="";
					String tag="";
					String lemma = "";
					String ana = "";
					for (int j=0; j < s.size(); j++)
					{
						TaggedWord w = s.get(j);
						tok += w.token;
						tag += ((tag=="")?"":"+") + w.tag;
						lemma += ((lemma=="")?"":"+") + w.lemma;
						ana += ((ana=="")?"":"+") + w.analysis;
					}
					fixedResult += (i+1) + "\t" + tok + "\t" + lemma + "\t" + ana  + "\t" + tag + "\n";
					// nl.openconvert.log.ConverterLog.defaultLog.println(i + " " + tok + " " + tag);
				}
				// nl.openconvert.log.ConverterLog.defaultLog.println("FIXED:" + fixedResult);
				return fixedResult;
			}
		}
		return null;
	}
	public void connect()
	{
		//Create socket connection
		try
		{
			socket = new Socket(server, port);
			out = new PrintWriter(socket.getOutputStream(), 
					true);
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
		} catch (UnknownHostException e) 
		{
			nl.openconvert.log.ConverterLog.defaultLog.println("Unknown host: " + server);
			System.exit(1);
		} catch  (IOException e) 
		{
			nl.openconvert.log.ConverterLog.defaultLog.println("No I/O: " + e);
			//System.exit(1);
		}
	}

	public void abortConnection()
	{
		try
		{
			socket.close();
			socket = null;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void reconnect()
	{
		abortConnection();
		connect();
	}

	
	public List<TaggedWord> getTaggedWords(List<String> lines)
	{
		List<TaggedWord> taggedWords = new ArrayList<TaggedWord>();
		
		for (int i=0; i < lines.size(); i++)
		{
			String[] parts = lines.get(i).split("\t");
			try
			{
			   TaggedWord tw = new TaggedWord();
			   tw.id = parts[0];
			   tw.token = parts[1];
			   tw.lemma = parts[2];
			   tw.analysis = parts[3];
			   tw.tag = parts[4];
			   taggedWords.add(tw);
			   //String tag = parts[4];
			} catch (Exception e)
			{
				// dont break...
				// e.printStackTrace();
				// break;
			}
		}
		return taggedWords;
	}

	
	public String flat(List<TaggedWord> list)
	{
		String s ="";
		for (TaggedWord t: list)
		{
			s += t.id + ":" + t.token + "/" + t.tag + " ";
		}
		return s;
	}
	
	public static void main(String[] args)
	{
		Proxy.setProxy();
		DocumentTagger f = new DocumentTagger(new FrogClient());
	
		// f.connect();
		
		if (new File(args[0]).isDirectory())
		{
			f.tagXMLFilesInDirectory(args[0], args[1]);
			return;
		}
		boolean tokenize = true;
		if (tokenize)
		{
			if (args.length == 1)
			{
				TEITokenizer tok = new TEITokenizer();
				Document d0 = tok.getTokenizedDocument(args[0], 
						true);
				TEISentenceSplitter splitter = 
						new TEISentenceSplitter(new JVKSentenceSplitter());
				splitter.splitSentences(d0);
				f.addTaggingToTokenizedDocument(d0);
				System.out.println(XML.documentToString(d0));
			} else
			{
				f.tagListOfUntokenizedDocuments(args[0], args[1]);
			}
		} else f.tagTokenizedXMLFile(args[0]);
		// System.out.println(f.tagSentence("Dit is een eenvoudige zin ."));
	}
	@Override
	public void postProcessDocument(Document d) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String tokenToString(Element t) 
	{
		// TODO Auto-generated method stub
		return t.getTextContent();
	}

}
