package nl.namescape.tei;

import java.io.File;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;

import nl.namescape.filehandling.DirectoryHandling;
import nl.namescape.filehandling.DoSomethingWithFile;
import nl.namescape.util.XML;

import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;


import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.validate.rng.CompactSchemaReader;
import com.thaiopensource.xml.sax.CountingErrorHandler;
import com.thaiopensource.xml.sax.ErrorHandlerImpl;


/**
 * 
 * @author does
 *
 */
public class Validator implements DoSomethingWithFile
{
	//http://blog.krecan.net/2011/05/02/using-jing-for-relax-ng-validation/
	//we are using Relax NG compact format
	private static String namescapeSchema="/mnt/Projecten/Taalbank/Namescape/Codering/namescape.rng";
	
	private com.thaiopensource.validate.Validator thaiValidator = null;
	CountingErrorHandler errorCounter = null;
	int errorCount = 0;
	
	public Validator()
	{
		setSchema(namescapeSchema);
	}

	public void setSchema(String fileName)
	{
		try
		{
			SchemaReader schemaReader = com.thaiopensource.validate.rng.SAXSchemaReader.getInstance();

			//schema can be reused, it's thread safe
			Schema schema = schemaReader.createSchema(ValidationDriver.fileInputSource(
					new File(fileName)), PropertyMap.EMPTY);

			//can use different error handler here (try DraconianErrorHandler http://www.thaiopensource.com/relaxng/api/jing/com/thaiopensource/xml/sax/DraconianErrorHandler.html)
			errorCounter = new CountingErrorHandler(); // new ErrorHandlerImpl()
			PropertyMapBuilder  builder = new PropertyMapBuilder();
			builder.put(ValidateProperty.ERROR_HANDLER, errorCounter);
			//NOTE: Validator is NOT thread safe
			thaiValidator = schema.createValidator(builder.toPropertyMap());
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public int getErrorCount()
	{
		return errorCount;
	}
	
	public boolean validate(Document d)
	{
		Source source = new DOMSource(d.getDocumentElement()); // dit werkt dus niet vanwege namespace ellende...
		thaiValidator.reset();
		boolean valid = false;
		try 
		{
			TransformerFactory.newInstance().newTransformer().transform(source, 
					new SAXResult(thaiValidator.getContentHandler()));
			if ((errorCount = errorCounter.getErrorCount()) > 0)
			{
				// System.err.println("Errors: " + errorCounter.getErrorCount());
				valid = false;
			} else
			{
				valid = true;
			}
			errorCounter.reset();
		} catch (TransformerConfigurationException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return valid;
	}
	
	public void validate(String fileName)
	{
		try
		{
			Document d = XML.parse(fileName);
			validate(d);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	public static void main(String[] args)
	{
		Validator v =  new Validator();
		DirectoryHandling.traverseDirectory(v, args[0]);
		
	}

	@Override
	public void handleFile(String fileName) 
	{
		// TODO Auto-generated method stubfil
		validate(fileName);
		if (getErrorCount() > 0)
		{
			System.out.println(fileName + "\t" + getErrorCount());
		}
	}
}
