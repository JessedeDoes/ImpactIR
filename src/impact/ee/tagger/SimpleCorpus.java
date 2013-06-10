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


public class SimpleCorpus implements Corpus,  Iterable<impact.ee.tagger.Context>, Iterator<impact.ee.tagger.Context> 
{
	EnumerationWithContext<Map<String,String>> enumerationWithContext;
	boolean supportsReset = false;
	List<String> attributeList = new ArrayList<String>();
	Chunker chunker = null; // usually a sentence splitting criterion
	
	static class Chunker // default implementation
	{
		public boolean isChunkBoundary(Map<String,String> c)
		{
			String w = c.get("word");
			if (w != null && (w.equals(".") || w.equals("?") || w.equals("?")))
			{
				return true;
			}
			return false;
		}
	}
	
	public void setChunking()
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
				if (chunker != null && relativePosition != 0)
				{
					int increment = relativePosition > 0?1:-1;
					for (int i=increment; relativePosition>0?i<=relativePosition:i>=relativePosition; i+= increment)
					{
						if (chunker.isChunkBoundary(enumerationWithContext.get(relativePosition)))
							return enumerationWithContext.defaultT.get(featureName);
					}
				}
				Map<String,String> m = enumerationWithContext.get(relativePosition);
				return m.get(featureName);
			} catch (Exception e)
			{
				// System.err.println("failed to get " + featureName + " at " + relativePosition);
				return Feature.Unknown;
			}
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
			LineParser<String> lp = new LineParser<String>(b) { public String parseLine(String s) { return s;} } ;
			EnumerationWithContext<String> corpusje = 
					new EnumerationWithContext(String.class, lp, "DUMMY");
			
			while (corpusje.moveNext())
			{
				String foc = corpusje.get(0);
				String before =corpusje.get(-1);
				String beforebefore =corpusje.get(-2);
				String after = corpusje.get(1);
				String afterafter = corpusje.get(2);
				System.out.println(corpusje.lineNumber + "/" + corpusje.offset + ": " + beforebefore + "-" + foc + "-" + afterafter);
			}
		} catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
