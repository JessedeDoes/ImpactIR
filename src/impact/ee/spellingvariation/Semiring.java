package impact.ee.spellingvariation;

/**
 * Not used. Might be used to switch conveniently between log and 'ordinary' weight accumulation
 * 
 * @author jesse
 *
 * 
 */

public abstract class Semiring
{
  public abstract double multiply(double a, double b);
  public abstract double add(double a, double b);

  public static class Log extends Semiring
  {
    public double multiply(double a, double b)
    {
      return a + b;
    }

    public double add(double a, double b)
    {
      return -1 * Math.log(Math.exp(-1 *a) + Math.exp(-1 * b));
    }
  }

  public static class Reals extends Semiring
  {
    public double multiply(double a, double b)
    {
      return a * b;
    }

    public double add(double a, double b)
    {
      return a+b;
    }
  }
  
  public static class Max extends Semiring
  {
  	public double multiply(double a, double b)
  	{
  		return a + b;
  	}
  	public double add(double a, double b)
  	{
  		return Math.max(a, b);
  	}
  }
}
