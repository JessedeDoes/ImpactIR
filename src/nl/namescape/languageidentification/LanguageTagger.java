package nl.namescape.languageidentification;
import org.w3c.dom.*;

import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

import java.util.*;

public class LanguageTagger 
{
	static
	{
		try 
		{
			DetectorFactory.loadProfileFromJar();
		} catch (LangDetectException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void tagLanguages(Document d)
	{
		
	}
}
