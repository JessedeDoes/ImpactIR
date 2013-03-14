package nl.namescape.nelexicon;

import nl.namescape.tei.Metadata;

import org.w3c.dom.Document;

public class NEDocument
{
	public Integer primaryKey;
	public String title;
	public String author;
	public String publicationYear;
	public String documentID;
	public String isbn;
	public String url;
	public String fullDocumentText;
	
	Document DOMDocument = null;
	
	public NEDocument(Document d)
	{
		this.DOMDocument = d;
		getMetadata(d);
	}
	
	public void getMetadata(Document d) 
	{
		// TODO Auto-generated method stub
		Metadata m = new Metadata(d);
		this.title = m.getValue("title");
		this.author = m.getValue("author");
		this.publicationYear = m.getValue("pubyear");
		this.isbn = m.getValue("isbn");
		this.documentID = d.getDocumentElement().getAttribute("xml:id");
	}
}