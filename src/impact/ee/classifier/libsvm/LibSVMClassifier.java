package impact.ee.classifier.libsvm;
import libsvm.*;

import impact.ee.classifier.Classifier;
import impact.ee.classifier.Dataset;
import impact.ee.classifier.Distribution;
import impact.ee.classifier.FeatureSet;
import impact.ee.classifier.Instance;
import impact.ee.classifier.Distribution.Outcome;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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

public class LibSVMClassifier implements Classifier, Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	transient libsvm.svm svmObject = new svm();
	transient libsvm.svm_parameter parameter = new svm_parameter();
	libsvm.svm_model model = null; // deze mag wel serializable zijn
	Map<String,Integer> featureMap = new HashMap<String,Integer>();
	Map<String,Double> labelMap = new HashMap<String,Double>();
	Map<Double,String> inverseLabelMap = new HashMap<Double,String>();

	@Override

	public String classifyInstance(Instance i)
	{
		// int m = getNumberOfKnownFeatures(i);
		// System.err.println(m + " in " + i);
		// svm_node[] x = new svm_node[m];
		List<svm_node> xList = new ArrayList<svm_node>();
		Double L = handleItem(i, xList, false);
		svm_node[] x = new svm_node[xList.size()];
		x = xList.toArray(x);
		double v = svm.svm_predict(model,x); // !! apparently not always possible
		String answer = this.inverseLabelMap.get(v);
		return answer;
	}

	@Override
	public Distribution distributionForInstance(Instance i)
	{
		//int m = getNumberOfKnownFeatures(i);
		List<svm_node> xList = new ArrayList<svm_node>();
		Double L = handleItem(i, xList, false);
		svm_node[] x = new svm_node[xList.size()];
		x = xList.toArray(x);

		int svm_type=svm.svm_get_svm_type(model);
		int nr_class=svm.svm_get_nr_class(model);
		double[] prob_estimates= new double[nr_class];
		double v = svm.svm_predict_probability(model,x,prob_estimates); // !! apparently not always possible

		Distribution d = new Distribution();
		for (int j=0; j < nr_class; j++)
		{
			double J = j;
			String label = this.inverseLabelMap.get(J);
			d.addOutcome(label, prob_estimates[j]);
		}
		d.sort();
		return d;
	}


	@Override

	public void train(Dataset d) 
	{
		setDefaultParameters();
		svm_problem problem = makeProblem(d,parameter);
		String error_msg = svm.svm_check_parameter(problem,parameter);

		if(error_msg != null)
		{
			System.err.print("ERROR: "+error_msg+"\n");
			System.exit(1);
		}
		
		this.model = svm.svm_train(problem,parameter);
	}

	public void saveModel(String fileName)
	{
		try 
		{
			svm.svm_save_model(fileName,model);
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	public void setDefaultParameters()
	{
		parameter.svm_type = svm_parameter.C_SVC;
		parameter.kernel_type = svm_parameter.LINEAR; // was RBF; dat werkt niet (omdat je dan ook andere parameters anders moet doen?)
		parameter.degree = 3;
		parameter.gamma = 0;	// 1/num_features
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
	}



	public svm_problem makeProblem(Dataset d, svm_parameter param)
	{
		// TODO Auto-generated method stub
		svm_problem problem = new svm_problem();
		Vector<Double> vy = new Vector<Double>();
		Vector<svm_node[]> vx = new Vector<svm_node[]>();

		for (Instance i: d.instances)
		{	
			//svm_node[] x = new svm_node[i.values.size()]; // neen dit klopt niet meer et de stochastic values...
			// double L = handleItem(i, x, true);

			List<svm_node> xList = new ArrayList<svm_node>();
			Double L = handleItem(i, xList, true);
			svm_node[] x = new svm_node[xList.size()];
			x = xList.toArray(x);

			vy.addElement(L);
			vx.addElement(x);
		}


		int max_index = featureMap.size() - 1;

		problem = new svm_problem();
		problem.l = vy.size();

		problem.x = new svm_node[problem.l][];
		for (int i = 0; i < problem.l; i++)
			problem.x[i] = vx.elementAt(i);

		problem.y = new double[problem.l];
		for (int i = 0; i < problem.l; i++)
			problem.y[i] = vy.elementAt(i);

		if (param.gamma == 0 && max_index > 0)
			param.gamma = 1.0 / max_index;

		if(param.kernel_type == svm_parameter.PRECOMPUTED)
			for(int i=0;i<problem.l;i++)
			{
				if (problem.x[i][0].index != 0)
				{
					System.err.print("Wrong kernel matrix: first column must be 0:sample_serial_number\n");
					System.exit(1);
				}
				if ((int)problem.x[i][0].value <= 0 || (int)problem.x[i][0].value > max_index)
				{
					System.err.print("Wrong input format: sample_serial_number out of range\n");
					System.exit(1);
				}
			}

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
	 * from item to array of svm_node
	 * let op: nodes moeten worden gesorteerd op index
	 */

	private Double handleItem(Instance instance, List<svm_node> x, boolean training) 
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
				svm_node n = new svm_node();
				x.add(n);

				n.index = index+1;
				n.value = 1; // TODO do something about real-valued features
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
						svm_node n = new svm_node();
						x.add(n);
						n.index = index+1;
						n.value = 1; // TODO werkt o.p nou helemaal niet? (pfft dit klopte dus niet!)
						k++;
					}
				}
			}
		}
		Comparator<svm_node> nodeComparator = new Comparator<svm_node>() 
		{
			@Override public int compare(svm_node arg0, svm_node arg1) { return arg0.index - arg1.index; }
		};

		Collections.sort(x, nodeComparator);
		return labelN;
	}

	@Override
	public void train(Dataset d, int MAX_ITEMS_USED) 
	{
		d.reduceItems(MAX_ITEMS_USED);
		train(d);
	}

	@Override
	public void save(String fileName) throws IOException 
	{
		// TODO Auto-generated method stub
		try
		{
			FileOutputStream fileOut =
					new FileOutputStream(fileName);
			ObjectOutputStream out =
					new ObjectOutputStream(fileOut);
			out.writeObject(this);
			out.close();
			fileOut.close();
		}catch(IOException i)
		{
			i.printStackTrace();
		}
	}

	public static LibSVMClassifier loadFromFile(String fileName)
	{
		try
		{
			FileInputStream fileIn =
					new FileInputStream(fileName);
			ObjectInputStream in =
					new ObjectInputStream(fileIn);
			LibSVMClassifier fs = (LibSVMClassifier) in.readObject();
			in.close();
			fileIn.close();
			return fs;
		}catch(Exception i)
		{
			i.printStackTrace();
			return null;
		}
	}
	public void load(String fileName)
	{
		try 
		{
			LibSVMClassifier x = loadFromFile(fileName);
			this.model = x.model;
			this.featureMap = x.featureMap;
			this.inverseLabelMap = x.inverseLabelMap;
			this.labelMap = x.labelMap;
		} catch (Exception e) 
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
