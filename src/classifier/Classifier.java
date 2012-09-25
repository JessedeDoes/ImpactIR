package classifier;


/**
 * 
 * @author jesse
 * <p>
 * Interface for classifiers.
 * Currently only weka classifiers in use, others will follow
 * </p>
 */
public interface Classifier
{
	public String classifyInstance(Instance i);
	public Distribution distributionForInstance(Instance i);
	public void train(Dataset d);
	public void train(Dataset d, int MAX_ITEMS_USED);
	public void save(String filename) throws java.io.IOException;
	public void load(String filename);
	public void setType(String classifieirType);
}
