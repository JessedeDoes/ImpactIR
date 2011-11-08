package lemmatizer;
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
  public String classifyItem(Item i);
  public Distribution distributionForItem(Item i);
  public void train(Dataset d);
  public void train(Dataset d, int MAX_ITEMS_USED);
  public void save(String filename) throws java.io.IOException;
	public void setType(String classifierType);
}
