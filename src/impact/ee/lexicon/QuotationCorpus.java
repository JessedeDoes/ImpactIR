package impact.ee.lexicon;

import impact.ee.tagger.Context;
import impact.ee.tagger.Corpus;
import impact.ee.util.SimpleTokenizer;
import impact.ee.util.Database.MapFetcher;
import impact.ee.util.SimpleTokenizer.Token;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

//import lexicon.LexiconDatabase.QuotationsAsCorpus.Attestation;

public class QuotationCorpus implements impact.ee.tagger.Corpus, Iterable<Context>, Iterator<Context> 
{
	/**
	 * 
	 */
	private final LexiconDatabase lexiconDatabase;

	public enum inTraining {inTrainingSet, inTestSet};
	public enum inSample { inSampleSet, selectAll };
	
	String query = "select l.*, w.*, t.start_pos, t.end_pos, t.quote " +
			" from wordforms w, lemmata l, simple_analyzed_wordforms a, token_attestations t, documents d, attestationPortions p" +
			" where l.lemma_id=a.lemma_id and w.wordform_id = a.wordform_id " +
			" and a.analyzed_wordform_id=t.analyzed_wordform_id  " +
			" and t.document_id = d.document_id " + 
			" and t.attestation_id = p.attestation_id " + 
			" SAMPLE and p.inTrainingSet=TRAINING " + 
			" and d.year_from > 1500 and d.year_to < 1750 ";
	
	boolean lookahead;
	Attestation lastFetched=null;
	MapFetcher mf;
	
	class Attestation implements Context
	{
		
		String quotation;
		String wordform;
		String lemmaPoS;
		String lemma;
		int start_pos;
		int end_pos;
		int focus_position=-1;
	
		List<SimpleTokenizer.Token> tokenizedQuotation;
		
		public Attestation(Map<String,String> m)
		{
			quotation = m.get("quote");
			lemma = m.get("modern_lemma");
			lemmaPoS = m.get("lemma_part_of_speech");
			wordform = m.get("wordform");
			start_pos = Integer.parseInt(m.get("start_pos"));
			end_pos = Integer.parseInt(m.get("end_pos"));
			tokenizedQuotation = new SimpleTokenizer().tokenizeText(quotation);
			
			// find focus position
			for (int i=0; i < tokenizedQuotation.size(); i++)
			{
				SimpleTokenizer.Token t = tokenizedQuotation.get(i);
				if (t.end_pos > start_pos && t.start_pos < end_pos) // overlap
				{
					focus_position = i;
					break;
				}
			}
		}
		
		@Override
		public String getAttributeAt(String attributeName,
				int relativePosition) 
		{
			// TODO Auto-generated method stub
			
			if (relativePosition != 0 && attributeName.equals("word"))
			{
				int x = focus_position + relativePosition;
				if (x >= 0 && x < tokenizedQuotation.size())
					return tokenizedQuotation.get(x).content;
			}
			
			if (relativePosition == 0)
			{
				if (attributeName.equals("quote"))
					return quotation;
				if (attributeName.equals("word"))
					return this.wordform;
				if (attributeName.equals("lemma"))
					return this.lemma;
				if (attributeName.equals("tag"))
					return this.lemmaPoS;
			}
			return null;
		}

		@Override
		public void setAttributeAt(String attributeName,
				String attributeValue, int relativePosition) 
		{
			// TODO Auto-generated method stub
			// NOT IMPLEMENTED....
			if (relativePosition == 0)
			{
				if (attributeName.equals("tag"))
				{
					this.lemmaPoS = attributeValue;
				}
			}
		}
	}
	
	public QuotationCorpus(LexiconDatabase lexiconDatabase, inTraining t, inSample s)
	{
		this.lexiconDatabase = lexiconDatabase;
		if (!this.lexiconDatabase.tableExists("simple_analyzed_wordforms"))
		{
			System.err.println("NO SIMPLE ANALYZED WORDFORMS!");
			//System.exit(1);
			query = query.replaceAll("simple_analyzed_wordforms", "analyzed_wordforms");
		}
		if (this.lexiconDatabase.tableExists("attestationPortions"))
		{
			if (s == inSample.inSampleSet)
				query=query.replaceAll("SAMPLE", " and inSampleSet=1 ");
			else
				query=query.replaceAll("SAMPLE", "");
			if (t == inTraining.inTrainingSet)
				query=query.replaceAll("TRAINING", "1");
			else
				query=query.replaceAll("TRAINING", "0");
		}
		mf = new MapFetcher(this.lexiconDatabase.connection, query);
	}
	
	@Override
	public Iterable<Context> enumerate() 
	{
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public Iterator<Context> iterator() 
	{
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public boolean hasNext() 
	{	
		if (lookahead)
			return true;
		Map<String,String> m= mf.fetchMap();
		if (m != null)
		{
			lookahead = true;
			lastFetched = new Attestation(m);
			return true;
		}
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Context next() 
	{
		if (lookahead)
		{
			lookahead = false;
			return lastFetched;
		} else if (hasNext())
		{
			return lastFetched;
		}
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove() 
	{
		// TODO Auto-generated method stub
		
	}	
	
	public static void main(String[] args) throws Exception
	{
		//System.err.println(createSimpleWordformTableSQL);
		System.err.println("load database...");
	
		LexiconDatabase l = new LexiconDatabase("svowim02", "EE3_5");
		// l.createRandomSelections();
		// System.exit(0);
		
		Corpus corpus = l.getQuotationCorpus(inTraining.inTestSet, inSample.inSampleSet);
		
		int N=0;
		for (Context c: corpus.enumerate())
		{
			System.err.println(N + " " +  c.getAttributeAt("word",-1)  + "_" + c.getAttributeAt("word", 0) +  "_" + 
					c.getAttributeAt("word",1) + " LEMMA:" + c.getAttributeAt("lemma" , 0) + " " + c.getAttributeAt("quote", 0));
			N++;
		}
		System.err.println("N= " + N);
		System.exit(0);
	}
}