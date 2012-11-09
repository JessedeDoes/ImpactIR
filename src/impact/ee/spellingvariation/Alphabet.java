package impact.ee.spellingvariation;



/**
 * This is a bit silly. 
 * Serves to make it easy to write convenient toString() methods for readable
 *  representations of states, multigrams, etc
 * @author jesse
 *
 */
interface CodeToStringPairMapping
{
	public String getLHS(int i);
	public String getRHS(int i);
}

/**
 * The alphabet class maps strings to sequences of integers
 * in order to limit the character set to characters actually seen in the data.
 * <p>
 * This helps to implement more compact storage for state models - which is a TODO item btw.
 */

public class Alphabet implements java.io.Serializable
{
	private static final long serialVersionUID = -3721789212383351329L;
	public static final int 空 = 0;
  public static final char initialBoundary = '^';
  public static final char finalBoundary = '$';
  public static final String initialBoundaryString = "^";
  public static final String  finalBoundaryString = "$";
  
  /**
   * Codestring is basically an array of integer codes for characters
   * wrapped in a class because we want the toString function
   */
  
  public class CodedString
  {
    public int size;
    public  int[] data;
    Alphabet alphabet;
    CodedString() {};
    
    int get(int i)
    {
      return data[i];
    }
    
    public String toString()
    {
      return this.alphabet.decode(this);
    }
  };

  public static String removeBoundaryMarkers(String s)
  {
  	if (s.startsWith(initialBoundaryString))
  		s = s.substring(1);
  	if (s.endsWith(finalBoundaryString))
  		s = s.substring(0,s.length()-1);
  	return s;
  }
  
  private int[] codes = new int[1<<16];
  public int size;
  public String alphabet;

  public Alphabet(String alphabet)
  {
    codes = new int[1<<16];
    this.alphabet = alphabet;
    codes[0xffff] = 0;
    for (int i=0; i < alphabet.length(); i++)
    {
      codes[alphabet.charAt(i)] = i+1;
    }
    size = alphabet.length() + 1;
  }

  public CodedString encode(String s)
  {
    int codes[] = new int[s.length() + 1];
    for (int i=0; i < s.length(); i++)
    {
      codes[i] = this.codes[s.charAt(i)];
    }
    CodedString cs = new CodedString();
    cs.alphabet = this;
    cs.size = s.length();
    cs.data = codes;
    return cs;
  }

  String decode(CodedString cs)
  {
    char s[] = new char[cs.size];
    for (int i=0; i < cs.size; i++)
    {
      s[i] = alphabet.charAt(cs.data[i]-1);
    } 
    return new String(s);
  }

  char decode(int c)
  {
	if (c == 0)
	  return 0;
	try
	{
      return alphabet.charAt(c-1);
	} catch (Exception e)
	{
	  e.printStackTrace();
	  System.err.println("Past niet: " + c);
	  return 0;	
	}
  }

  public int encode(char c)
  {
    return codes[c];
  }

  public void addSymbolsFrom(String s)
  {
    for (int i=0; i < s.length(); i++)
    {
      char c = s.charAt(i);
      if (codes[c] != 0) {} else
      {
        codes[c] = size;
        alphabet += c;
        size++;
      }
    }
  }  

  boolean stringInAlphabet(String s)
  {
    if (s == null)
    {
      //fprintf(stderr,"NULL in string_ok\n");
      return false;
    }
    for (int i=0; i < s.length(); i++)
    {
      char c = s.charAt(i);
      try
      {
        if (codes[c] != 0) {} else
        {
          return false;
        }
      } catch(Exception e) // array bounds
      {
        return false;
      }
    }
    return true;
  }
/*
  void addFromFile(FILE *f)
{
  wint_t c=0;
  while ((c = fgetwc(f)) != WEOF)
  {
    wchar_t wc = c;
    if (codes[wc]) {} else
    {
      fwprintf(stdout, L"new char %c (%d)\n",wc, size);
      codes[wc] = size;
      this -> alphabet = (wchar_t *) realloc(this -> alphabet, sizeof(wchar_t) * (size+2));
      alphabet[size] = wc;
      alphabet[size+1] = 0;
      size++;
    }
  }
*/
}

/*
int main(int argc, char *argv[])
{
  FILE *f = fopen(argv[1],"r");
  if (f)
  {
    Alphabet a(L"");
    a.add_from_file(f); 
    fwprintf(stdout,L"%ls %d\n",a.alphabet,wcslen(a.alphabet));
  }
}
*/
