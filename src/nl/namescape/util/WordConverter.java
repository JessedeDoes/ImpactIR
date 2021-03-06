package nl.namescape.util;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.hwpf.HWPFDocumentCore;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.hwpf.converter.WordToHtmlUtils;
//import org.apache.poi.xwpf; // xwpf is voor word 2010 etc.
//import org.apache.poi.
import org.w3c.dom.Document;

import java.util.*;
public class WordConverter 
{

	public static void dinges(String docFile)
	{
		try
		{
			HWPFDocumentCore wordDocument = WordToHtmlUtils.loadDoc(new FileInputStream(docFile));

			WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(
					DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.newDocument());
			wordToHtmlConverter.processDocument(wordDocument);
			Document htmlDocument = wordToHtmlConverter.getDocument();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DOMSource domSource = new DOMSource(htmlDocument);
			StreamResult streamResult = new StreamResult(out);

			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer serializer = tf.newTransformer();
			serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			serializer.setOutputProperty(OutputKeys.METHOD, "html");
			serializer.transform(domSource, streamResult);
			out.close();

			String result = new String(out.toByteArray());
			System.out.println(result);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	public static Document Word2HtmlDocument(String docFile)
	{
		try
		{
			HWPFDocumentCore wordDocument = WordToHtmlUtils.loadDoc(new FileInputStream(docFile));

			WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(
					DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.newDocument());
			wordToHtmlConverter.processDocument(wordDocument);
			Document htmlDocument = wordToHtmlConverter.getDocument();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DOMSource domSource = new DOMSource(htmlDocument);
			DOMResult domResult = new DOMResult();

			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer serializer = tf.newTransformer();
			serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			serializer.setOutputProperty(OutputKeys.METHOD, "html");
			serializer.transform(domSource, domResult);
			out.close();
			//nl.openconvert.log.ConverterLog.defaultLog.println(domResult.getNode());
			return (Document) domResult.getNode();
			
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	

	
	
	public static void main(String[] args)
	{
		Document d = WordConverter.Word2HtmlDocument(args[0]);
		System.out.println(XML.documentToString(d));
	}
}
