package nl.namescape.filehandling;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadedFileHandler implements DoSomethingWithFile, SimpleInputOutputProcess
{
	ExecutorService pool;
	DoSomethingWithFile baseHandler1;
	SimpleInputOutputProcess baseHandler2;
	
	public class UnaryTask implements Runnable
	{
		String fileName = null;
		
		public UnaryTask(String fileName)
		{
			this.fileName = fileName;
		}
		@Override
		public void run() 
		{
			if (fileName != null && baseHandler1 != null)
				baseHandler1.handleFile(fileName);
		}
	}
	
	
	public class BinaryTask implements Runnable
	{
		String inFile = null;
		String outFile = null;
		public  BinaryTask(String inFile, String outFile)
		{
			this.inFile = inFile;
			this.outFile = outFile;
		}
		@Override
		public void run() 
		{
			if (inFile != null && baseHandler2 != null)
				baseHandler2.handleFile(inFile,outFile);
		}
	}
	
	public MultiThreadedFileHandler(DoSomethingWithFile h, int nThreads)
	{
		this.baseHandler1 = h;
		this.pool =  Executors.newFixedThreadPool(nThreads);
	}

	@Override
	public void handleFile(String fileName) 
	{
		// TODO Auto-generated method stub
		UnaryTask t = new UnaryTask(fileName);
		pool.execute(t);
	}

	@Override
	public void handleFile(String in, String out) 
	{
		// TODO Auto-generated method stub
		BinaryTask t = new BinaryTask(in,out);
		pool.execute(t);
	}
	
	public static void main(String args[])
	{
		
	}
}
