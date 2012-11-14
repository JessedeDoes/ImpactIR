package impact.ee.tagger;

import java.lang.reflect.Array;
import java.util.Enumeration;
import java.util.Iterator;

/*
 * Werkt nog niet goed als bestand heel kort is!
 */
public class EnumerationWithContext<T> implements Iterator<T>// T is the token-with-attributes type
{
	int windowSize= 41;
	int focus = 20;
	int capacity = 10000;
	boolean first = true;
	int offset= -1 * (windowSize - focus); // where the current window starts in the buffer
	int overdue;
	T[] buffer;
	int lineNumber  = 0;  
	T defaultT = null;
	Class elementClass;
	
	Enumeration<T> inputStream;
	
	public EnumerationWithContext(Class<T> c, Enumeration<T> inputStream, T defaultT)
	{
		buffer = (T[]) Array.newInstance(c,capacity);
		this.inputStream = inputStream;
		this.defaultT = defaultT;
		this.elementClass = c;
		offset  = -1 * (windowSize - focus);
	}
	
	void readInitialWindow() // read in the first window and ensure right context is present
	{
		//System.err.println("element class:" + elementClass);
		//System.err.println("default:" + defaultT);
		
		
		for (int i=0; i < focus; i++)
			buffer[i] = defaultT;
		for (int i=focus; i < windowSize; i++)
		{
			if (inputStream.hasMoreElements()) 
				buffer[i] = inputStream.nextElement();
			else
			{
				if (overdue >= windowSize - focus -1)
				{
					break;
				} else // why no dummy in buffer?
				{
					overdue++;
				}
			}
		}
		offset=0;
	}
			   
	void shift(T x)
	{
		if (this.windowSize + offset + 1 < buffer.length)
		{
			buffer[offset+windowSize] = x;
			offset ++;
		} else
		{
			for (int i = 1; i <= windowSize - 1; i++)
			{
				buffer[i-1] = buffer[offset+i];
			}

			offset = 0;
			buffer[windowSize-1] =  x;
		}
	}
	
	T get(int i)
	{
		if (offset+focus+i < 0)
			return defaultT;
		return buffer[offset+focus+i];
	}
	
	void set(int i, T t)
	{
		if (offset+focus+i > 0)
			buffer[offset+focus+i] = t;
	}
	
	boolean moveNext()
	{
		if (first)
		{
			this.readInitialWindow();
			first = false;
			return true;
		}
		if (inputStream.hasMoreElements())
		{
			T next = inputStream.nextElement();
			shift(next);
			lineNumber++;
			return true;
		} else
		{
			if (overdue >= windowSize - focus -1)
			{
				// EOF...
				return false;
			} else
			{
				overdue++;
				shift(defaultT);
				lineNumber++;
				return true;
			}
		}

	}

	@Override
	public boolean hasNext() 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public T next() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}
}