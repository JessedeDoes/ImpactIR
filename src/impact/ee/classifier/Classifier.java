package impact.ee.classifier;


/**
 * 
 * @author jesse
 * 
 * <p>
 * Interface for classifiers.
 * Currently supports weka classifiers, libsvm (java version), svmlight via JNI_SVM-light-6.01
 * </p>
 */
public interface Classifier
{
	public String classifyInstance(Instance i);
	public Distribution distributionForInstance(Instance i);
	public void train(Dataset d);
	public void train(Dataset d, int MAX_ITEMS_USED);
	public void save(String filename) throws java.io.IOException;
	public void  load(String filename);
	public void setType(String classifierType);
}
