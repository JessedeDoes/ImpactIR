package nl.namescape.util;

import java.util.ArrayList;
import java.util.List;

import nl.namescape.util.WordTableConverter.Cell;
import nl.namescape.util.WordTableConverter.Row;
import nl.namescape.util.WordTableConverter.Table;
import nl.openconvert.filehandling.DirectoryHandling;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class HTMLTableConverter extends WordTableConverter 
{

	@Override
	public void handleFile(String fileName)
	{
		// TODO Auto-generated method stub
		nl.openconvert.log.ConverterLog.defaultLog.println(fileName);
	
		try
		{
			Document d = TagSoupParser.parse2DOM(fileName);
			allTables.addAll(extractTables(d));
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		HTMLTableConverter wtc = new HTMLTableConverter();
		DirectoryHandling.traverseDirectory(wtc, args[0]);
		wtc.dumpAll();
	}
}
