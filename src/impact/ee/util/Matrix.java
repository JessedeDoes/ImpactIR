package impact.ee.util;
public class Matrix
{
  public static double[][] newMatrix(int m, int n)
  {
    double [][] R = new double[m][n];
    for (int i=0; i < m; i++)
    {
      for (int j=0; j < n; j++) R[i][j] = 0;
    }
    return R;
  }
/* 
  static void print(C ** x, int m, int n)
  {
    for (int i=0; i < m; i++)
    {
      for (int j=0; j < n; j++)
      {
        fprintf(stderr," %4.4f ", x[i][j]);
      }
      fprintf(stderr,"\n");
    }
  }

  static void free(C** x, int m)
  {
    for (int i=0; i < m; i++)
    {
      delete x[i];
    }
    delete x;
  }
*/
};

