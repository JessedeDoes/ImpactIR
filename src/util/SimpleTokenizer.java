package util;

import java.util.regex.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;


public class SimpleTokenizer
{
       static Pattern nonWordPattern = Pattern.compile("\\W+");
       static Pattern AtMostPunctuationPattern = Pattern.compile("^(\\p{P}|\\s)*$");
    
	static Pattern prePunctuationPattern = Pattern.compile("(^|\\s)\\p{P}+");
	static Pattern postPunctuationPattern = Pattern.compile("\\p{P}+($|\\s)");

	static Pattern leadingBlanks = Pattern.compile("^\\s+");
	static Pattern trailingBlanks = Pattern.compile("\\s+$");
	
	public String prePunctuation="";
	public String postPunctuation="";
	public String trimmedToken="";

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
	}
	
	public static boolean isPunctuationOrWhite(String t)
	{
		Matcher m = AtMostPunctuationPattern.matcher(t);
		return m.matches();
		//return t.equals("-") || t.equals(".") || t.equals(",") || t.equals("?") || t.equals("!");
	}
	
}
