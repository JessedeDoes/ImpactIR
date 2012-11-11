package nl.namescape.util;

import java.util.*;

import nl.namescape.filehandling.DirectoryHandling;
import nl.namescape.filehandling.DoSomethingWithFile;

import org.w3c.dom.*;
/**
 * extracts all top-level tables from all word documents in a directory.
 * Prints tab-separated output.
 * @author does
 *
 */
public class WordTableConverter implements DoSomethingWithFile
{ 
	List<Table> allTables = new ArrayList<Table>();
	static class Row
	{
		List<Cell> cells = new ArrayList<Cell>();
	}
	
	static class Cell
	{
		String text;
	}
	
	static class Table
	{
		List<Row> rows = new ArrayList<Row>();
	}
	
	public List<Table> extractTables(Document d)
	{
		Element r = d.getDocumentElement();
		List<Table> tables = new ArrayList<Table>();
		List<Element> tableElements = XML.getElementsByTagname(r,"table", false);
		for (Element t: tableElements)
		{
			Table table = new Table();
			tables.add(table);
			for (Element re : XML.getElementsByTagname(t,"tr", false))
			{
				Row row = new Row();
				table.rows.add(row);
				for (Element ce : XML.getElementsByTagname(re,"td", false))
				{
					Cell cell = new Cell();
					cell.text = ce.getTextContent().trim().replaceAll("\\s+", " ");
					// System.err.println("<"  + cell.text + ">");
					row.cells.add(cell);
				}
			}
		}
		return tables;
	}

	public void dumpAll()
	{
		for (Table t: allTables)
		{
			for (Row r: t.rows)
			{
				List<String> cells = new ArrayList<String>();
				for (Cell c: r.cells)
				{
					cells.add(c.text);
				}
				System.out.println(Util.join(cells, "\t"));
			}
		}
	}
	@Override
	public void handleFile(String fileName)
	{
		// TODO Auto-generated method stub
		System.err.println(fileName);
		if (!fileName.endsWith(".doc"))
			return;
		try
		{
			Document d = WordConverter.Word2HtmlDocument(fileName);
			allTables.addAll(extractTables(d));
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		WordTableConverter wtc = new WordTableConverter();
		DirectoryHandling.traverseDirectory(wtc, args[0]);
		wtc.dumpAll();
	}
}
