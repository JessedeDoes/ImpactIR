package classifier.svmlight;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import jnisvmlight.SVMLightModel;

import classifier.svmlight.SVMLightClassifier.Problem;

/*
 *  svm_multilearn -z m training.smaller.svmproblem  Models/
 */

public class SVMLightExec 
{
	static String learner = "svm_multilearn";
	
	public static void trainClassifiers(File trainingFile, File modelDirectory)
	{
		try 
		{
			ProcessBuilder pb  = new ProcessBuilder(learner, "-z", "m", 
					trainingFile.getCanonicalPath(), modelDirectory.getCanonicalPath());

			Map<String, String> env = pb.environment();
			//env.put("PATH", programDir + "/");

			//pb.directory(new File(programDir));
			pb.redirectErrorStream(true);


			final Process process = pb.start();
			new Thread(new Runnable() 
			{
				@Override
				public void run() 
				{
					try 
					{
						String line;
						final InputStream stdout = process.getInputStream();
						BufferedReader brCleanUp = new BufferedReader(
								new InputStreamReader(stdout));
						while ((line = brCleanUp.readLine()) != null) 
						{
							System.err.println("[Stdout] " + line);
						}
						brCleanUp.close();
					} catch (IOException e)
					{
						e.printStackTrace(System.err);
					}
				}
			}).start();
			process.waitFor();
		} catch (Exception e) 
		{
			e.printStackTrace();
			// TODO: handle exception
		}
	}

	public static File[] runTrainingProgram(Problem p) throws IOException 
	{
		String tempDir = util.IO.getTempDir();
		File trainingFile = File.createTempFile("combined.", ".train");
		File modelDirectory = File.createTempFile("model.", ".dir");
		modelDirectory.delete();
		modelDirectory.mkdir();
		p.print(trainingFile.getCanonicalPath());
		SVMLightExec.trainClassifiers(trainingFile, modelDirectory);
		File[] modelFiles = modelDirectory.listFiles();
	
		return modelFiles;
	}
}
