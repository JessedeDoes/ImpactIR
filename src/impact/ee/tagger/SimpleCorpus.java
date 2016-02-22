package impact.ee.tagger;

import impact.ee.classifier.Feature;
import impact.ee.util.TabSeparatedFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import nl.namescape.sentence.SentenceBoundaryToken;


public class SimpleCorpus implements Corpus,  Iterable<impact.ee.tagger.Context>, Iterator<impact.ee.tagger.Context> 
{
	EnumerationWithContext<Map<String,String>> enumerationWithContext;
	boolean supportsReset = false;
	List<String> attributeList = new ArrayList<String>();
	Chunker chunker = new Chunker(); // usually a sentence splitting criterion
	static Class chunkBoundaryClass = SentenceBoundary.class;
	
	static class Chunker // default implementation
	{
		public boolean isChunkBoundary(Map<String,String> c)
		{
			return c != null && c instanceof SentenceBoundary;
		}
	}
	
	public void setChunking(boolean b)
	{
		if (b) 
			setChunker();
		else
			this.chunker = null;
	}
	
	private  void setChunker()
	{
		chunker = new Chunker();
	}
	
	class SimpleLineParser extends LineParser<Map<String,String>>
	{
		TabSeparatedFile tabjes = null;
		
		String[] fieldNames = {"word", "tag"};
		
		public SimpleLineParser(BufferedReader b, String[] fieldNames) 
		{
			super(b);
			this.fieldNames = fieldNames;
			// TODO Auto-generated constructor stub
		}
		
		public SimpleLineParser(BufferedReader b) 
		{
			super(b);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public  Map<String,String> parseLine(String l) 
		{ 
			Map<String,String> m = new HashMap<String,String>();
			try
			{
				String[] f = l.split("\t");
				if (l.matches("^\\s*$") ||
						f.length ==0 || 
						f[0].equals(SentenceBoundary.SentenceBoundarySymbol))
				{
					//nl.openconvert.log.ConverterLog.defaultLog.println("__EOS__!");
					return new SentenceBoundary();
				}
				for (int j=0; j < f.length && j < fieldNames.length; j++)
					m.put(fieldNames[j], f[j]);
			} catch (Exception e)
			{
				return null;
			}
			return m;
		}
	}
	
	public class Context implements impact.ee.tagger.Context
	{
		@Override
		public String getAttributeAt(String featureName, int relativePosition) 
		{
			try
			{
			
				Map<String,String> m = enumerationWithContext.get(relativePosition);
				
				m = dealWithBoundaryConditions(relativePosition, m);
				
				String v =  m.get(featureName);
				
				if (featureName.equals("word") && v == null)
				{
					nl.openconvert.log.ConverterLog.defaultLog.println("NO WORD in map at position: " + relativePosition +  " map= " + m);
					// nl.openconvert.log.ConverterLog.defaultLog.println(m.getClass().getName());
					// ok dit gaat dus fout bij het doorgeven van alle ingevulde features in apply...
					//System.exit(1);
				}
				return v;
			} catch (Exception e)
			{
				//nl.openconvert.log.ConverterLog.defaultLog.println("failed to get " + featureName + " at " + relativePosition);
				return Feature.Unknown;
			}
		}

		/** If there is a chunk boundary
		 * (strictly) between 0 and relativePosition, return the dummy context 
		 * Also: when we 'just' run out of context (fallback object)
		 * we want to return the chunk boundary? Hm??
		 * So Dummy Dummy SB <real word>
		 * and <real word> SB Dummy Dummy?
		 * But why not just the dummies???
		 */
		
		protected Map<String, String> dealWithBoundaryConditions( int relativePosition, Map<String, String> m)
		{
			//nl.openconvert.log.ConverterLog.defaultLog.println(m + " at " + relativePosition);
			if (chunker != null)
			{
				if (relativePosition != 0)
				{
					int increment = relativePosition > 0 ? 1: -1;
					for (int i=increment; 
							relativePosition>0?
										i<relativePosition
										:i>relativePosition; 
							i+= increment)
					{
						if (chunker.isChunkBoundary(enumerationWithContext.get(i)))
						{
							//nl.openconvert.log.ConverterLog.defaultLog.println("Ha!"  + relativePosition + " i = " + i);
							return enumerationWithContext.defaultT;
						}
					}
				}
				// if m is a dummy, but one closer to focus is not, return a sentence boundary symbol
				if (m != null && enumerationWithContext.defaultT.equals(m))
				{
					if (relativePosition >= 1  && !
							enumerationWithContext.get(relativePosition -1).equals(enumerationWithContext.defaultT) )
					{
						//nl.openconvert.log.ConverterLog.defaultLog.println("set SB at " + relativePosition + " / " +  enumerationWithContext.get(relativePosition -1));
						m = new SentenceBoundaryToken();
					} else if (relativePosition <= -1  && !
							enumerationWithContext.get(relativePosition +1).equals(enumerationWithContext.defaultT) )
					{
						//nl.openconvert.log.ConverterLog.defaultLog.println(enumerationWithContext.defaultT);
						//nl.openconvert.log.ConverterLog.defaultLog.println("set SB at " + relativePosition +   " / " +  enumerationWithContext.get(relativePosition +1));
						m = new SentenceBoundaryToken();
					} else
					{
						//nl.openconvert.log.ConverterLog.defaultLog.println("Keep dummy  at "+ relativePosition);
					}
				} 
			}
			return m;
		}

		@Override
		public void setAttributeAt(String attributeName, String attributeValue,
				int relativePosition) 
		{
			Map<String,String> m = enumerationWithContext.get(relativePosition);
			if (m != null)
			{
				m.put(attributeName, attributeValue);
			}
		}
		
		public Set<String> getAttributes()
		{
			Map<String,String> m = enumerationWithContext.get(0);
			if (m != null)
				return m.keySet();
			return new HashSet<String>();
		}
	}
	
	private Context context = new Context();
	
	public SimpleCorpus(EnumerationWithContext<Map<String,String>> enumerationWithContext)
	{
		this.enumerationWithContext = enumerationWithContext;
	}
	
	public SimpleCorpus(String fileName, String[] fieldNames)
	{
		supportsReset = true;
		for (String s: fieldNames) attributeList.add(s);
		try
		{
			BufferedReader b = new BufferedReader(new FileReader(new File(fileName)));
			LineParser<Map<String,String>> lp = new SimpleLineParser(b, fieldNames);
			this.enumerationWithContext = 
					new EnumerationWithContext(Map.class, lp, new DummyMap());
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	boolean hasLookahead = false;
	impact.ee.tagger.Context lookahead  = null;
	
	@Override
	public Iterable<impact.ee.tagger.Context> enumerate() 
	{
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public Iterator<impact.ee.tagger.Context> iterator() 
	{
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public boolean hasNext() 
	{
		// TODO Auto-generated method stub
		if (hasLookahead)
			return true;
		if (this.enumerationWithContext.moveNext())
		{
			hasLookahead = true;
			return true;
		}
		return false;
	}

	@Override
	public impact.ee.tagger.Context next() 
	{
		// TODO Auto-generated method stub
		if (hasLookahead)
		{
			hasLookahead = false;
		} else
		{
			if (!this.enumerationWithContext.moveNext())
			{
				hasLookahead = false;
				return null;
			}
		}
		hasLookahead = false;
		return this.context;
	}

	@Override
	public void remove() 
	{
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args)
	{
		try 
		{
			BufferedReader b = new BufferedReader(new FileReader(new File(args[0])));
			String[] atts = {"word"};
	
			
		
			SimpleCorpus simpleCorpus = new SimpleCorpus(args[0], atts);
		for   (impact.ee.tagger.Context c: simpleCorpus.enumerate())
			{
			
				String focWord = c.getAttributeAt("word", 0);
				String before = c.getAttributeAt("word", -1);
				String beforebefore =  c.getAttributeAt("word", -2);
				String after =  c.getAttributeAt("word", 1 );
				String afterafter = c.getAttributeAt("word", 2);
				System.out.println(beforebefore + ", "  + before + "-<" + focWord + ">-" + after + "," + afterafter);
			}
		} catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
