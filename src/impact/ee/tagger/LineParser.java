package impact.ee.tagger;

import java.io.BufferedReader;
import java.util.Enumeration;

class LineParser<T> implements Enumeration<T>
{
	BufferedReader b = null;
	String s = null;
	boolean empty=false;
	boolean lookahead = false;
	
	public LineParser(BufferedReader b)
	{
		this.b = b;
	}
	
	public T parseLine(String l) { return null; }

	@Override
	public boolean hasMoreElements() 
	{
		// TODO Auto-generated method stub
		if (empty)
			return false;
		if (lookahead)
			return true;
		try
		{
			s = b.readLine();
			if (s != null)
			{
				lookahead = true;
				return true;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public T nextElement() 
	{
		// TODO Auto-generated method stub
		if (empty)
			return null;
		if (hasMoreElements())
		{
			lookahead = false;
			return parseLine(s);
		}
		return null;
	}
}