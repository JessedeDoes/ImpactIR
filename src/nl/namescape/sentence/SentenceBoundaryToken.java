package nl.namescape.sentence;

import impact.ee.tagger.SentenceBoundary;

public class SentenceBoundaryToken extends TEIToken
{
	public SentenceBoundaryToken()
	{
		this.put("word", SentenceBoundary.SentenceBoundarySymbol);
	}
	
	@Override
	public String get(Object s)
	{
		return SentenceBoundary.SentenceBoundarySymbol;
	}
}
