package nl.namescape.tokenizer;

import nl.namescape.util.Proxy;
import nl.namescape.util.XML;

import org.w3c.dom.Document;

public class Pretokenizer
{
	public static void main(String[] args)
	{
		Proxy.setProxy();
		new TEITokenizer().preTokenizeFile(args[0]);
	}
}
