package impact.ee.classifier.libsvm;
//import libsvm.*;
import de.bwaldvogel.liblinear.*;

import impact.ee.classifier.Classifier;
import impact.ee.classifier.Dataset;
import impact.ee.classifier.Distribution;
import impact.ee.classifier.Instance;
import impact.ee.classifier.Distribution.Outcome;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


/*
 * Wrapper for libsvm
 * Can we also do this for svmlight which may be better (glavnoe, faster)??
 * 
 * http://www.mpi-inf.mpg.de/~mtb/
 * 
 * 
 * options:
-s svm_type : set type of SVM (default 0)
	0 -- C-SVC
	1 -- nu-SVC
	2 -- one-class SVM
	3 -- epsilon-SVR
	4 -- nu-SVR
-t kernel_type : set type of kernel function (default 2)
	0 -- linear: u'*v
	1 -- polynomial: (gamma*u'*v + coef0)^degree
	2 -- radial basis function: exp(-gamma*|u-v|^2)
	3 -- sigmoid: tanh(gamma*u'*v + coef0)
-d degree : set degree in kernel function (default 3)
-g gamma : set gamma in kernel function (default 1/num_features)
-r coef0 : set coef0 in kernel function (default 0)
-c cost : set the parameter C of C-SVC, epsilon-SVR, and nu-SVR (default 1)
-n nu : set the parameter nu of nu-SVC, one-class SVM, and nu-SVR (default 0.5)
-p epsilon : set the epsilon in loss function of epsilon-SVR (default 0.1)
-m cachesize : set cache memory size in MB (default 100)
-e epsilon : set tolerance of termination criterion (default 0.001)
-h shrinking: whether to use the shrinking heuristics, 0 or 1 (default 1)
-b probability_estimates: whether to train a SVC or SVR model for probability estimates, 0 or 1 (default 0)
-wi weight: set the parameter C of class i to weight*C, for C-SVC (default 1)

 */

public class LibLinearClassifier implements Classifier 
{
	//libsvm.svm svmObject = new svm();
	Parameter parameter = new Parameter(SolverType.MCSVM_CS, 1, 1e-3);
	Model model = null;
	Map<String,Integer> featureMap = new HashMap<String,Integer>();
	Map<String,Double> labelMap = new HashMap<String,Double>();
	Map<Double,String> inverseLabelMap = new HashMap<Double,String>();

	@Override

	public String classifyInstance(Instance i)
	{
		//int m = getNumberOfKnownFeatures(i);
		// nl.openconvert.log.ConverterLog.defaultLog.println(m + " in " + i);
		//FeatureNode[] x = new FeatureNode[m];
		List<FeatureNode> xList = new ArrayList<FeatureNode>();
		Double L = handleItem(i, xList, false);
		FeatureNode[] x = new FeatureNode[xList.size()];
		x = xList.toArray(x);
		double v = Linear.predict(model,x); // !! apparently not always possible
		String answer = this.inverseLabelMap.get(v);
		return answer;
	}

	@Override
	public Distribution distributionForInstance(Instance i)
	{
		//int m = getNumberOfKnownFeatures(i);
		List<FeatureNode> xList = new ArrayList<FeatureNode>();
		Double L = handleItem(i, xList, false);
		FeatureNode[] x = new FeatureNode[xList.size()];
		x = xList.toArray(x);

		//int svm_type=Linear.(model);
		int nr_class = model.getNrClass() ; // svm.svm_get_nr_class(model);
		double[] prob_estimates= new double[nr_class];
		double v = Linear.predictProbability(model,x,prob_estimates); // !! apparently not always possible

		Distribution d = new Distribution();
		for (int j=0; j < nr_class; j++)
		{
			String label = this.inverseLabelMap.get(j);
			d.addOutcome(label, prob_estimates[j]);
		}
		d.sort();
		return d;
	}


	@Override

	public void train(Dataset d) 
	{
		setDefaultParameters();
		Problem problem = makeProblem(d,parameter);
		/*
		String error_msg = (problem,parameter);

		if(error_msg != null)
		{
			nl.openconvert.log.ConverterLog.defaultLog.print("ERROR: "+error_msg+"\n");
			System.exit(1);
		}
		*/
		
		this.model = Linear.train(problem,parameter);
	}

	public void saveModel(String fileName)
	{
		try 
		{
			model.save(new File(fileName));
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	public void setDefaultParameters()
	{
		/*
		parameter.svm_type = svm_parameter.C_SVC;
		
		//parameter.degree = 3;
		//parameter;	// 1/num_features
		parameter.
		parameter.coef0 = 0;
		parameter.nu = 0.5;
		parameter.cache_size = 500;
		parameter.C = 1;
		parameter.eps = 1e-3;
		parameter.p = 0.1;
		parameter.shrinking = 0; // 0 might be faster (?)
		parameter.probability = 0;
		parameter.nr_weight = 0;
		parameter.weight_label = new int[0];
		parameter.weight = new double[0];
		//cross_validation = 0;
		 * */
		 
	}



	public Problem makeProblem(Dataset d, Parameter param)
	{
		// TODO Auto-generated method stub
		Problem problem = new Problem();
		Vector<Double> vy = new Vector<Double>();
		Vector<FeatureNode[]> vx = new Vector<FeatureNode[]>();

		for (Instance i: d.instances)
		{	
			//FeatureNode[] x = new FeatureNode[i.values.size()]; // neen dit klopt niet meer et de stochastic values...
			// double L = handleItem(i, x, true);

			List<FeatureNode> xList = new ArrayList<FeatureNode>();
			Double L = handleItem(i, xList, true);
			FeatureNode[] x = new FeatureNode[xList.size()];
			x = xList.toArray(x);

			vy.addElement(L);
			vx.addElement(x);
		}


		int max_index = featureMap.size() - 1;

		problem = new Problem();
		problem.l = vy.size();

		problem.x = new FeatureNode[problem.l][];
		for (int i = 0; i < problem.l; i++)
			problem.x[i] = vx.elementAt(i);

		problem.y = new double[problem.l];
		for (int i = 0; i < problem.l; i++)
			problem.y[i] = vy.elementAt(i);
		
		problem.n = max_index+1;
		//problem

		/*
		if (param.gamma == 0 && max_index > 0)
			param.gamma = 1.0 / max_index;
		*/
		

		return problem;
	}

	/*
	private int xgetNumberOfKnownFeatures(Instance i)
	{
		int m = i.values.size();
		int k = 0;
		for (int j=0; j < m; j++) 
		{
			String s = i.values.get(j);
			String key = j + ":" + s;
			Integer index = featureMap.get(key);
			if (index != null)
			{
				k++;
			}
		}
		return k;
	}
	 */

	/*
	 * from item to array of FeatureNode
	 * let op: nodes moeten worden gesorteerd op index
	 */

	private Double handleItem(Instance instance, List<FeatureNode> x, boolean training) 
	{
		int m = instance.values.size();
		Double labelN = labelMap.get(instance.classLabel);

		if (training && labelN == null)
		{
			double N = labelMap.size();
			labelMap.put(instance.classLabel,N);
			inverseLabelMap.put(N,instance.classLabel);
			labelN = N;
		}

		int k=0;

		for (int j=0; j < m; j++) 
		{
			String s = instance.values.get(j);
			String key = j + ":" + s;
			Integer index = featureMap.get(key);
			if (index == null && training) // FOUT: tijdens predictie alleen bekende features meenemen...
			{
				int N = featureMap.size();
				featureMap.put(key, N);
				index = N;
			}
			if (index != null)
			{
				FeatureNode n = new FeatureNode(index+1,1);
				x.add(n);
				k++;
			}
		}

		//k = 0;
		m = instance.stochasticValues.size();
		for (int j=0; j < m; j++)
		{
			Distribution d = instance.stochasticValues.get(j);
			if (d != null)
			{
				for (Outcome o: d.outcomes)
				{
					String key = j + "/" + o.label;
					Integer index = featureMap.get(key);
					if (index == null && training) // FOUT: tijdens predictie alleen bekende features meenemen...
					{
						int N = featureMap.size();
						featureMap.put(key, N);
						index = N;
					}
					if (index != null)
					{
						FeatureNode n = new FeatureNode(index+1, o.p); // he dit klopt niet
						x.add(n);
						k++;
					}
				}
			}
		}
		Comparator<FeatureNode> nodeComparator = new Comparator<FeatureNode>() 
		{
			@Override public int compare(FeatureNode arg0, FeatureNode arg1) { return arg0.getIndex() - arg1.getIndex(); }
		};

		Collections.sort(x, nodeComparator);

		int indexBefore=0;
		
		for (Feature n : x) 
		{
			if (n.getIndex() <= indexBefore) 
			{
				nl.openconvert.log.ConverterLog.defaultLog.println(n.getIndex() + "  <= " + indexBefore);
				throw new IllegalArgumentException("feature nodes must be sorted by index in ascending order");
			}
			indexBefore = n.getIndex();
		}

		return labelN;
	}

	@Override
	public void train(Dataset d, int MAX_ITEMS_USED) 
	{
		d.reduceItems(MAX_ITEMS_USED);
		train(d);
	}

	@Override
	public void save(String filename) throws IOException 
	{
		// TODO Auto-generated method stub
		saveModel(filename);
	}

	public void load(String filename)
	{
		try 
		{
			model = Model.load(new File(filename));
		} catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void setType(String classifierType) 
	{

	}
}
