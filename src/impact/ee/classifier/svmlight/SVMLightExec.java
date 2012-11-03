package impact.ee.classifier.svmlight;

import impact.ee.classifier.svmlight.SVMLightClassifier.Problem;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import jnisvmlight.SVMLightModel;


/**
 * This runs a version of svm_learn which has been hacked to
 * do All vs One multiclass training.<br>
 * Binary models are stored in a specified directory with filename = class name, which is of course
 * a problem if class name is not a valid file name.
 * <p>
 * The external command is:
 * <pre>
 *  svm_multilearn -z m &lt;training data>  &lt;directory where binary models are stored>
 *  </pre>
 *  To Do
 */

public class SVMLightExec 
{
	static String learner = "svm_multilearn";
	static boolean verbose = true;
	static double parameterC = 1; // 0.0714;
	
	public static void trainClassifiers(File trainingFile, File modelDirectory)
	{
		try 
		{
			ProcessBuilder pb  = new ProcessBuilder(learner, "-z", "m", "-c",  (parameterC + ""),
					trainingFile.getCanonicalPath(), modelDirectory.getCanonicalPath());

			Map<String, String> env = pb.environment();
			//env.put("PATH", programDir + "/");

			//pb.directory(new File(programDir));
			pb.redirectErrorStream(true);
			final File tf = trainingFile;

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
							if (verbose)
								System.err.println("[Stdout] " + line);
							// als regel "examples read " bevat, kan
							// je de trainingFile weggooien...
							if (line.contains("examples read"))
							{
								System.err.println("deleting combined training file...");
								tf.delete();
							}
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
		String tempDir = impact.ee.util.IO.getTempDir();
		File trainingFile = File.createTempFile("combined.", ".train");
		File modelDirectory = File.createTempFile("model.", ".dir");
		modelDirectory.delete();
		modelDirectory.mkdir();
		p.print(trainingFile.getCanonicalPath());
		SVMLightExec.trainClassifiers(trainingFile, modelDirectory);
		//File zz = new File("/tmp/model.5496025864372792763.dir/");
		File[] modelFiles = modelDirectory.listFiles();
		return modelFiles;
	}
}
