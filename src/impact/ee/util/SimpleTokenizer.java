package impact.ee.util;

import java.util.regex.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

//import util.SimpleTokenizer.Token.TokenType;


public class SimpleTokenizer
{
    static Pattern nonWordPattern = Pattern.compile("\\W+");
    static Pattern AtMostPunctuationPattern = Pattern.compile("^(\\p{P}|\\s)*$");
    
	static Pattern prePunctuationPattern = Pattern.compile("(^|\\s)\\p{P}+");
	static Pattern postPunctuationPattern = Pattern.compile("\\p{P}+($|\\s)");
	static Pattern letterPattern = Pattern.compile(".*\\p{L}.*");
	static Pattern leadingBlanks = Pattern.compile("^\\s+");
	static Pattern trailingBlanks = Pattern.compile("\\s+$");
	static Pattern wordOrTagPattern = Pattern.compile("[^\\s<>]+|<[^<>]*>");
	
	public String prePunctuation="";
	public String postPunctuation="";
	public String trimmedToken="";

	public enum  TokenType { word, punctuation};
	
	public static class Token
	{
		public int start_pos, end_pos;	
		public TokenType type;
		public String content;
	}
	
	public List<Token> tokenizeText(String q)
	{
		List<Token> tokens = new ArrayList<Token>();
		if (q == null)
			return tokens;
		
		Matcher m1 = wordOrTagPattern.matcher(q);
		
		while (m1.find())
		{
			int s = m1.start();
			int e = m1.end();
			String w = q.substring(s,e);
			if (w.contains("<")) // skip tags
				continue;
			tokenize(w);
			
			if (prePunctuation.length() > 0)
			{
				Token t = new Token();
				t.start_pos = s;
				t.end_pos = s + prePunctuation.length();
				t.type = TokenType.punctuation;
				t.content = prePunctuation;
				tokens.add(t);
			}
			
			if (trimmedToken.length() > 0)
			{
				Token t = new Token();
				t.start_pos = s + prePunctuation.length();
				t.end_pos = t.start_pos + trimmedToken.length();
				t.type = TokenType.word;
				t.content = trimmedToken;
				tokens.add(t);
			}
			
			if (prePunctuation.length() > 0)
			{
				Token t = new Token();
				t.start_pos = s + prePunctuation.length() + trimmedToken.length();
				t.end_pos = t.start_pos + postPunctuation.length();
				t.type = TokenType.punctuation;
				t.content = postPunctuation;
				tokens.add(t);
			}
			// System.err.println(w + ": " + s + " -- " + e);
		}
		return tokens;
	}
	
	public void tokenize(String t)
	{
		Matcher m1 = prePunctuationPattern.matcher(t);
		Matcher m2 = postPunctuationPattern.matcher(t);
		
		int s=0; int e = t.length();

		if (m1.find())
	 		s = m1.end();
		if (m2.find())
			e = m2.start();	

		if (e < s) e=s;
		trimmedToken = t.substring(s,e);
		prePunctuation = t.substring(0,s);
		postPunctuation = t.substring(e,t.length());
	}

	public static String trim(String s)
	{
		Matcher x = prePunctuationPattern.matcher(s);
		s = x.replaceAll("");
		Matcher y = postPunctuationPattern.matcher(s);
		return y.replaceAll("");
	}
	
	public static String trimWhiteSpace(String s)
	{
		Matcher x = leadingBlanks.matcher(s);
		s = x.replaceAll("");
		Matcher y = trailingBlanks.matcher(s);
		return y.replaceAll("");
	}
	
	public static void main(String[] args)
	{
		new SimpleTokenizer().tokenizeText("de aap is gek.");
	}
	
	public static boolean isPunctuationOrWhite(String t)
	{
		Matcher m = letterPattern.matcher(t);
		return !(m.matches());
		//return t.equals("-") || t.equals(".") || t.equals(",") || t.equals("?") || t.equals("!");
	}
	
}
