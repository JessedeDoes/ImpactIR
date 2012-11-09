package impact.ee.tagger.features;



import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// TODO: put in a regexp for ordinals...
// fraction num/num  and perhaps even 30-5/8


/**
 * <i>This file copied from Stanford NER! TODO: adapt for Dutch!</i>
 * <br>
 * 
 * Provides static methods which
 * map any String to another String indicative of its "word shape" -- e.g.,
 * whether capitalized, numeric, etc.  Different implementations may
 * implement quite different, normally language specific ideas of what
 * word shapes are useful.
 *
 * @author Christopher Manning, Dan Klein
 */
public class WordShapeClassifier 
{

  public static final int NOWORDSHAPE = -1;
  public static final int WORDSHAPEDAN1 = 0;
  public static final int WORDSHAPECHRIS1 = 1;
  public static final int WORDSHAPEDAN2 = 2;
  public static final int WORDSHAPEDAN2USELC = 3;
  public static final int WORDSHAPEDAN2BIO = 4;
  public static final int WORDSHAPEDAN2BIOUSELC = 5;
  public static final int WORDSHAPEJENNY1 = 6;
  public static final int WORDSHAPEJENNY1USELC = 7;
  public static final int WORDSHAPECHRIS2 = 8;
  public static final int WORDSHAPECHRIS2USELC = 9;
  public static final int WORDSHAPECHRIS3 = 10;
  public static final int WORDSHAPECHRIS3USELC = 11;
  public static final int WORDSHAPECHRIS4 = 12;


  // This class cannot be instantiated
  private WordShapeClassifier() 
  {
  }


  public static int lookupShaper(String name) {
    if (name == null) {
      return NOWORDSHAPE;
    } else if (name.equalsIgnoreCase("dan1")) {
      return WORDSHAPEDAN1;
    } else if (name.equalsIgnoreCase("chris1")) {
      return WORDSHAPECHRIS1;
    } else if (name.equalsIgnoreCase("dan2")) {
      return WORDSHAPEDAN2;
    } else if (name.equalsIgnoreCase("dan2useLC")) {
      return WORDSHAPEDAN2USELC;
    } else if (name.equalsIgnoreCase("dan2bio")) {
      return WORDSHAPEDAN2BIO;
    } else if (name.equalsIgnoreCase("dan2bioUseLC")) {
      return WORDSHAPEDAN2BIOUSELC;
    } else if (name.equalsIgnoreCase("jenny1")) {
      return WORDSHAPEJENNY1;
    } else if (name.equalsIgnoreCase("jenny1useLC")) {
      return WORDSHAPEJENNY1USELC;
    } else if (name.equalsIgnoreCase("chris2")) {
      return WORDSHAPECHRIS2;
    } else if (name.equalsIgnoreCase("chris2useLC")) {
      return WORDSHAPECHRIS2USELC;
    } else if (name.equalsIgnoreCase("chris3")) {
      return WORDSHAPECHRIS3;
    } else if (name.equalsIgnoreCase("chris3useLC")) {
      return WORDSHAPECHRIS3USELC;
    } else if (name.equalsIgnoreCase("chris4")) {
      return WORDSHAPECHRIS4;
    } else {
      return NOWORDSHAPE;
    }
  }

  /**
   * Returns true if the specified word shaper uses
   * known lower case words.
   */
  public static boolean usesLC(int shape) {
    return (shape == WORDSHAPEDAN2USELC || 
            shape == WORDSHAPEDAN2BIOUSELC ||
            shape == WORDSHAPEJENNY1USELC ||
            shape == WORDSHAPECHRIS2USELC ||
            shape == WORDSHAPECHRIS3USELC);  
  }
  

  /**
   * Specify the string and the int identifying which word shaper to
   * use and this returns the result of using that wordshaper on the word.
   */
  public static String wordShape(String inStr, int wordShaper) {
    return wordShape(inStr, wordShaper, false);
  }

  /**
   * Specify the string and the int identifying which word shaper to
   * use and this returns the result of using that wordshaper on the word.
   */
  public static String wordShape(String inStr, int wordShaper, Set knownLCWords) {
    return wordShape(inStr, wordShaper, false, knownLCWords);
  }

  public static String wordShape(String inStr, int wordShaper, boolean markKnownLC) {
    switch (wordShaper) {
      case NOWORDSHAPE:
        return inStr;
      case WORDSHAPEDAN1:
        return wordShapeDan1(inStr);
      case WORDSHAPECHRIS1:
        return wordShapeChris1(inStr);
      case WORDSHAPEDAN2:
        return wordShapeDan2(inStr, markKnownLC);
      case WORDSHAPEDAN2USELC:
        return wordShapeDan2(inStr, true);
      case WORDSHAPEDAN2BIO:
        return wordShapeDan2Bio(inStr, markKnownLC);
      case WORDSHAPEDAN2BIOUSELC:
        return wordShapeDan2Bio(inStr, true);
      case WORDSHAPEJENNY1:
        return wordShapeJenny1(inStr, markKnownLC);
      case WORDSHAPEJENNY1USELC:
        return wordShapeJenny1(inStr, true);
      case WORDSHAPECHRIS2:
        return wordShapeChris2(inStr, markKnownLC, false);
      case WORDSHAPECHRIS2USELC:
        return wordShapeChris2(inStr, true, false);
      case WORDSHAPECHRIS3:
        return wordShapeChris2(inStr, markKnownLC, true);
      case WORDSHAPECHRIS3USELC:
        return wordShapeChris2(inStr, true, true);
      case WORDSHAPECHRIS4:
        return wordShapeChris4(inStr, markKnownLC, false);
      default:
        throw new IllegalStateException("Bad WordShapeClassifier");
    }
  }

  public static String wordShape(String inStr, int wordShaper, boolean markKnownLC, Set knownLCWords) {

    setKnownLowerCaseWords(knownLCWords);
    return wordShape(inStr, wordShaper, markKnownLC);
  }

  /**
   * A fairly basic 5-way classifier, that notes digits, and upper
   * and lower case, mixed, and non-alphanumeric.
   */
  public static String wordShapeDan1(String s) {
    boolean digit = true;
    boolean upper = true;
    boolean lower = true;
    boolean mixed = true;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (!Character.isDigit(c)) {
        digit = false;
      }
      if (!Character.isLowerCase(c)) {
        lower = false;
      }
      if (!Character.isUpperCase(c)) {
        upper = false;
      }
      if ((i == 0 && !Character.isUpperCase(c)) || (i >= 1 && !Character.isLowerCase(c))) {
        mixed = false;
      }
    }
    if (digit) {
      return "ALL-DIGITS";
    }
    if (upper) {
      return "ALL-UPPER";
    }
    if (lower) {
      return "ALL-LOWER";
    }
    if (mixed) {
      return "MIXED-CASE";
    }
    return "OTHER";
  }


  /**
   * A fine-grained word shape classifier, that equivalence classes.
   * lower and upper case and digits, and collapses sequences of the
   * same type, but keeps all punctuation, etc. <p>
   * <i>Note:</i> We treat '_' as a lowercase letter, sort of like many
   * programming languages.  We do this because we use '_' joining of
   * tokens in some applications like RTE.
   *
   * @param s           The String whose shape is to be returned
   * @param markKnownLC Whether to mark words whose lower case form is
   *                    found in the previously initialized list of known
   *                    lower case words
   * @return The word shape
   * @see #addKnownLowerCaseWords(Collection)
   */
  public static String wordShapeDan2(String s, boolean markKnownLC) {
    StringBuilder sb = new StringBuilder("WT-");
    char lastM = '~';
    boolean nonLetters = false;
    int len = s.length();
    for (int i = 0; i < len; i++) {
      char c = s.charAt(i);
      char m = c;
      if (Character.isDigit(c)) {
        m = 'd';
      } else if (Character.isLowerCase(c) || c == '_') {
        m = 'x';
      } else if (Character.isUpperCase(c)) {
        m = 'X';
      }
      if (m != 'x' && m != 'X') {
        nonLetters = true;
      }
      if (m != lastM) {
        sb.append(m);
      }
      lastM = m;
    }
    if (len <= 3) {
      sb.append(":").append(len);
    }
    if (markKnownLC) {
      if (!nonLetters && knownLCWords.contains(s.toLowerCase())) {
        sb.append("k");
      }
    }
    // System.err.println("wordShapeDan2: " + s + " became " + sb);
    return sb.toString();
  }

  public static String wordShapeJenny1(String s, boolean markKnownLC) {
    StringBuilder sb = new StringBuilder("WT-");
    char lastM = '~';
    boolean nonLetters = false;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      char m = c;

      if (Character.isDigit(c)) {
        m = 'd';
      } else if (Character.isLowerCase(c)) {
        m = 'x';
      } else if (Character.isUpperCase(c)) {
        m = 'X';
      }

      for (int j = 0; j < greek.length; j++) {
        if (s.startsWith(greek[j],i)) {
          m = 'g';
          i = i + greek[j].length() - 1;
          //System.out.println(s + "  ::  " + s.substring(i+1));
          break;
        }
      }

      if (m != 'x' && m != 'X') {
        nonLetters = true;
      }
      if (m != lastM) {
        sb.append(m);
      }
      lastM = m;


    }
    if (s.length() <= 3) {
      sb.append(":").append(s.length());
    }
    if (markKnownLC) {
      if (!nonLetters && knownLCWords.contains(s.toLowerCase())) {
        sb.append("k");
      }
    }
    //System.out.println(s+" became "+sb);
    return sb.toString();
  }


  /**
   * This one picks up on Dan2 ideas, but seeks to make less distinctions
   * mid sequence by sorting for long words, but to maintain extra
   * distinctions for short words.
   *
   * @param omitIfInBoundary If true, character classes present in the
   *                         first or last two letters of the word are not also registered
   *                         as classes that appear in the middle of the word.
   */
  public static String wordShapeChris2(String s, boolean markKnownLC, boolean omitIfInBoundary) {
    StringBuilder sb = new StringBuilder();
    StringBuilder endSB = new StringBuilder();
    Set boundSet = new HashSet();
    Set seenSet = new TreeSet();  // TreeSet guarantees stable ordering
    boolean nonLetters = false;

    for (int i = 0, len = s.length(); i < len; i++) {
      char c = s.charAt(i);
      char m = c;
      int iIncr = 0;
      if (Character.isDigit(c)) {
        m = 'd';
      } else if (Character.isLowerCase(c)) {
        m = 'x';
      } else if (Character.isUpperCase(c) || Character.isTitleCase(c)) {
        m = 'X';
      }
      for (int j = 0; j < greek.length; j++) {
        if (s.startsWith(greek[j], i)) {
          m = 'g';
          iIncr = greek[j].length() - 1;
          //System.out.println(s + "  ::  " + s.substring(i+1));
          break;
        }
      }
      if (m != 'x' && m != 'X') {
        nonLetters = true;
      }

      if (i < 2) {
        sb.append(m);
        boundSet.add(new Character(m));
      } else if (i < len - 2) {
        seenSet.add(new Character(m));
      } else {
        boundSet.add(new Character(m));
        endSB.append(m);
      }
      // System.out.println("Position " + i + " --> " + m);
      i += iIncr;
    }
    // put in the stored ones sorted and add end ones
    for (Iterator it = seenSet.iterator(); it.hasNext();) {
      Character chr = (Character) it.next();
      if (!omitIfInBoundary || !boundSet.contains(chr)) {
        char ch = chr.charValue();
        sb.append(ch);
      }
    }
    sb.append(endSB);

    if (markKnownLC) {
      if (!nonLetters && knownLCWords.contains(s.toLowerCase())) {
        sb.append("k");
      }
    }
    // System.out.println(s + " became " + sb);
    return sb.toString();
  }


  /**
   * This one picks up on Dan2 ideas, but seeks to make less distinctions
   * mid sequence by sorting for long words, but to maintain extra
   * distinctions for short words, by always recording the class of the
   * first and last two characters of the word.
   * Compared to chris2 on which it is based,
   * it uses more Unicode classes, and so collapses things like
   * punctuation more,
   * and might work better with real unicode.
   *
   * @param omitIfInBoundary If true, character classes present in the
   *              first or last two letters of the word are not also registered
   *              as classes that appear in the middle of the word.
   */
  public static String wordShapeChris4(String s, boolean markKnownLC, boolean omitIfInBoundary) {
    StringBuilder sb = new StringBuilder();
    StringBuilder endSB = new StringBuilder();
    Set boundSet = new HashSet();
    Set seenSet = new TreeSet();  // TreeSet guarantees stable ordering
    boolean nonLetters = false;
    for (int i = 0, len = s.length(); i < len; i++) {
      char c = s.charAt(i);
      char m;
      int iIncr = 0;
      if (Character.isDigit(c)) {
        m = 'd';
      } else if (Character.isLowerCase(c)) {
        m = 'x';
      } else if (Character.isUpperCase(c) || Character.isTitleCase(c)) {
        m = 'X';
      } else if (Character.isWhitespace(c) || Character.isSpaceChar(c)) {
        m = 's';
      } else {
        int type = Character.getType(c);
        if (type == Character.CURRENCY_SYMBOL) {
          m = '$';
        } else if (type == Character.MATH_SYMBOL) {
          m = '+';
        } else if (type == Character.OTHER_SYMBOL || c == '|') {
          m = '|';
        } else if (type == Character.START_PUNCTUATION) {
          m = '(';
        } else if (type == Character.END_PUNCTUATION) {
          m = ')';
        } else if (type == Character.INITIAL_QUOTE_PUNCTUATION) {
          m = '`';
        } else if (type == Character.FINAL_QUOTE_PUNCTUATION || c == '\'') {
          m = '\'';
        } else if (type == Character.CONNECTOR_PUNCTUATION) {
          m = '_';
        } else if (type == Character.DASH_PUNCTUATION) {
          m = '-';
        } else {
          if (c >= ' ' && c <= '~') {
            m = c;
          } else {
            m = 'q';
          }
        }
      }
      for (int j = 0; j < greek.length; j++) {
        if (s.startsWith(greek[j], i)) {
          m = 'g';
          iIncr = greek[j].length() - 1;
          //System.out.println(s + "  ::  " + s.substring(i+1));
          break;
        }
      }
      if (m != 'x' && m != 'X') {
        nonLetters = true;
      }

      if (i < 2) {
        sb.append(m);
        boundSet.add(new Character(m));
      } else if (i < len - 2) {
        seenSet.add(new Character(m));
      } else {
        boundSet.add(new Character(m));
        endSB.append(m);
      }
      // System.out.println("Position " + i + " --> " + m);
      i += iIncr;
    }
    // put in the stored ones sorted and add end ones
    for (Iterator it = seenSet.iterator(); it.hasNext();) {
      Character chr = (Character) it.next();
      if (!omitIfInBoundary || !boundSet.contains(chr)) {
        char ch = chr.charValue();
        sb.append(ch);
      }
    }
    sb.append(endSB);

    if (markKnownLC) {
      if (!nonLetters && knownLCWords.contains(s.toLowerCase())) {
        sb.append("k");
      }
    }
    // System.out.println(s + " became " + sb);
    return sb.toString();
  }


  private static Set knownLCWords = new HashSet();

  public static Set getKnownLowerCaseWords() {
    return knownLCWords;
  }

  public static void setKnownLowerCaseWords(Set words) {
    knownLCWords = words;
  }

  public static void addKnownLowerCaseWords(Collection words) {
    knownLCWords.addAll(words);
  }


  /**
   * Returns a fine-grained word shape classifier, that equivalence classes
   * lower and upper case and digits, and collapses sequences of the
   * same type, but keeps all punctuation.  This adds an extra recognizer
   * for a greek letter embedded in the String, which is usefu for bio.
   */
  public static String wordShapeDan2Bio(String s, boolean useKnownLC) {
    String sh = wordShapeDan2(s, useKnownLC);
    if (containsGreekLetter(s)) {
      sh = sh + "-GREEK";
    }
    return sh;
  }


  /** List of greek letters for bio.  We omit eta, mu, nu, xi, phi, chi, psi.
   *  Maybe should omit rho too, but it is used in bio "Rho kinase inhibitor".
   */
  private static final String[] greek = {"alpha", "beta", "gamma", "delta", "epsilon", "zeta", "theta", "iota", "kappa", "lambda", "omicron", "rho", "sigma", "tau", "upsilon", "omega"};
  private static final Pattern biogreek = Pattern.compile("alpha|beta|gamma|delta|epsilon|zeta|theta|iota|kappa|lambda|omicron|rho|sigma|tau|upsilon|omega", Pattern.CASE_INSENSITIVE);


  /**
   * Somewhat ad-hoc list of only greek letters that bio people use, partly
   * to avoid false positives on short ones.
   */
  private static boolean containsGreekLetter(String s) {
    Matcher m = biogreek.matcher(s);
    return m.find();
  }



  public static String wordShapeChris1(String s) {
    int length = s.length();
    if (length == 0) {
      return "SYMBOLS";
    }

    boolean cardinal = false;
    boolean number = true;
    boolean seenDigit = false;
    boolean seenNonDigit = false;

    for (int i = 0; i < length; i++) {
      char ch = s.charAt(i);
      boolean digit = Character.isDigit(ch);
      if (digit) {
        seenDigit = true;
      } else {
        seenNonDigit = true;
      }
      // allow commas, decimals, and negative numbers
      digit = digit || ch == '.' || ch == ',' || (i == 0 && (ch == '-' || ch == '+'));
      if (!digit) {
        number = false;
      }
    }

    if (!seenDigit) {
      number = false;
    }
    if (seenDigit && !seenNonDigit) {
      cardinal = true;
    }

    if (cardinal) {
      if (length < 4) {
        return "CARDINAL13";
      } else if (length == 4) {
        return "CARDINAL4";
      } else {
        return "CARDINAL5PLUS";
      }
    } else if (number) {
      return "NUMBER";
    }

    boolean seenLower = false;
    boolean seenUpper = false;
    boolean allCaps = true;
    boolean allLower = true;
    boolean initCap = false;
    boolean dash = false;
    boolean period = false;

    for (int i = 0; i < length; i++) {
      char ch = s.charAt(i);
      boolean up = Character.isUpperCase(ch);
      boolean let = Character.isLetter(ch);
      boolean tit = Character.isTitleCase(ch);
      if (ch == '-') {
        dash = true;
      } else if (ch == '.') {
        period = true;
      }

      if (tit) {
        seenUpper = true;
        allLower = false;
        seenLower = true;
        allCaps = false;
      } else if (up) {
        seenUpper = true;
        allLower = false;
      } else if (let) {
        seenLower = true;
        allCaps = false;
      }
      if (i == 0 && (up || tit)) {
        initCap = true;
      }
    }

    if (length == 2 && initCap && period) {
      return "ACRONYM1";
    } else if (seenUpper && allCaps && !seenDigit && period) {
      return "ACRONYM";
    } else if (seenDigit && dash && !seenUpper && !seenLower) {
      return "DIGIT-DASH";
    } else if (initCap && seenLower && seenDigit && dash) {
      return "CAPITALIZED-DIGIT-DASH";
    } else if (initCap && seenLower && seenDigit) {
      return "CAPITALIZED-DIGIT";
    } else if (initCap && seenLower & dash) {
      return "CAPITALIZED-DASH";
    } else if (initCap && seenLower) {
      return "CAPITALIZED";
    } else if (seenUpper && allCaps && seenDigit && dash) {
      return "ALLCAPS-DIGIT-DASH";
    } else if (seenUpper && allCaps && seenDigit) {
      return "ALLCAPS-DIGIT";
    } else if (seenUpper && allCaps && dash) {
      return "ALLCAPS";
    } else if (seenUpper && allCaps) {
      return "ALLCAPS";
    } else if (seenLower && allLower && seenDigit && dash) {
      return "LOWERCASE-DIGIT-DASH";
    } else if (seenLower && allLower && seenDigit) {
      return "LOWERCASE-DIGIT";
    } else if (seenLower && allLower && dash) {
      return "LOWERCASE-DASH";
    } else if (seenLower && allLower) {
      return "LOWERCASE";
    } else if (seenLower && seenDigit) {
      return "MIXEDCASE-DIGIT";
    } else if (seenLower) {
      return "MIXEDCASE";
    } else if (seenDigit) {
      return "SYMBOL-DIGIT";
    } else {
      return "SYMBOL";
    }
  }


  /**
   * Usage: <code>java edu.stanford.nlp.process.WordShapeClassifier
   * [-wordShape name] string+ </code><br>
   * where <code>name</code> is an argument to <code>lookupShaper</code>.
   * Known names have patterns along the lines of: dan[12](bio)?(UseLC)?,
   * jenny1(useLC)?, chris[1234](useLC)?.
   */
  public static void main(String[] args) {
    int i = 0;
    int classifierToUse = WORDSHAPECHRIS1;
    if (args.length == 0) {
      System.out.println("edu.stanford.nlp.process.WordShapeClassifier" + " [-wordShape name] string+");
    } else if (args[0].charAt(0) == '-') {
      if (args[0].equals("-wordShape") && args.length >= 2) {
        classifierToUse = lookupShaper(args[1]);
        i += 2;
      } else {
        System.err.println("Unknown flag: " + args[0]);
        i++;
      }
    }

    for (; i < args.length; i++) {
      System.out.print(args[i] + ": ");
      System.out.println(wordShape(args[i], classifierToUse));
    }
  }

}

