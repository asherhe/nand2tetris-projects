import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The JackTokenizer reomves all whitespaces and comments from a Jack source
 * file, and then breaks it into Jack tokens, as specified by the Jack grammar
 */
public class JackTokenizer {
  /**
   * A classification of a token into the five lexical elements as specified in
   * the Jack grammar
   */
  public static enum TokenType {
    KEYWORD, SYMBOL, IDENTIFIER, INT_CONST, STRING_CONST
  };

  /**
   * Some constants related to the Jack syntax
   */
  private static class JackSyntax {
    /**
     * The pattern for a one-line comment in Jack
     * 
     * Regex Breakdown: (?m): ^ and $ denote the start and end of a line,
     * respectively // : A literal "//" .* : Any non-newline character $ : Denotes
     * the end of the line
     */
    public static String commentPattern = "(?m)//.*$";

    /**
     * The pattern or a multiline comment in Jack
     * 
     * Regex Breakdown: (?s): The expression . (any character) will also include
     * whitespace /* : Matches the literal "/*" (A "\" is put in front of the "*"
     * because it is a metacharacter) .*? : Any character (including newlines), but
     * as less as possible
     */
    public static String multilineCommentPattern = "(?s)/\\*.*?\\*/";

    /**
     * The pattern to match a token, with only spaces to separate
     * identifiers/keywords
     * 
     * Regex Breakdown: First option - String: \" : Matches a double quote .*?:
     * Reluctantly matches characters (because we don't want to also eat up the
     * closing quote) \" : Matches a double quote Second option - Keyword,
     * identifier, or number: \w+: Any word character (letters, digits, or
     * underscore), appearing at least once Third option - Symbol: [^ ] : Not a
     * space character [\W&&[^ ]]: A non-word character that is also not a space
     * character
     */
    public static Pattern tokenPattern = Pattern.compile("\".*?\"|\\w+|[^\\s]");

    /**
     * A list of all the Jack-language keywords
     */
    public static List<String> keywords = Arrays
        .asList(new String[] { "class", "constructor", "function", "method", "field", "static", "var", "int", "char",
            "boolean", "void", "true", "false", "null", "this", "let", "do", "if", "else", "while", "return" });
  }

  /**
   * The Jack program, in a matcher for getting the next token easily
   */
  private Matcher jackProgram;

  /**
   * Holds the current token; all other properties will be deduced from this
   */
  private String token;

  /**
   * Creates a JackTokenizer and sets it up for use.
   * 
   * @param file The path to the Jack source file
   */
  public JackTokenizer(Path file) {
    // Get file contents
    String fContents = "";
    try {
      fContents = Files.readString(file);
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Get rid of comments
    fContents = fContents.replaceAll(JackSyntax.commentPattern, "");

    // Get rid of multiline comments
    fContents = fContents.replaceAll(JackSyntax.multilineCommentPattern, "");

    // Condense all whitespace into spaces
    fContents = fContents.replaceAll("\\s+", " ");

    // Strip leading spaces
    fContents = fContents.replaceAll("(?m)^\\s|\\s$", "");

    // Create Matcher from input
    jackProgram = JackSyntax.tokenPattern.matcher(fContents);
  }

  /**
   * Determines whether we have run through all the tokens
   */
  public boolean hasMoreTokens() {
    return !jackProgram.hitEnd();
  }

  /**
   * Advances to the next token. Should only be called if hasMoreTokens() is true
   */
  public void advance() {
    jackProgram.find();
    token = jackProgram.group();
  }

  /**
   * Finds the token type of the current token
   */
  public TokenType tokenType() {
    if (token.matches("\\d+")) { // Is a number
      return TokenType.INT_CONST;
    }

    if (token.matches("\\W")) { // Not a word character
      return TokenType.SYMBOL;
    }

    if (token.charAt(0) == '"') { // Denotes a string
      return TokenType.STRING_CONST;
    }

    if (JackSyntax.keywords.contains(token)) { // Token is a keyword
      return TokenType.KEYWORD;
    }

    return TokenType.IDENTIFIER; // If all else fails
  }

  /**
   * Returns the string represenation of the curent token. Should be called when
   * tokenType() is not INT_CONST or STRING_CONST
   */
  public String value() {
    return token;
  }

  /**
   * Peeks at the next token, if it exists. Does not advance to the next token
   */
  public String peekNext() {
    int currentIndex = jackProgram.start();
    jackProgram.find();
    String result = jackProgram.group();
    jackProgram.find(currentIndex);
    return result;
  }

  /**
   * Returns the integer value of the current token. Should be called only when
   * tokenType() is INT_CONST
   */
  public int intVal() {
    return Integer.parseInt(token);
  }

  /**
   * Returns the value inside a string if the current token is a string. In other
   * words, removes the double quotes from both sides. Should only be called when
   * tokenType() is STRING_CONST
   */
  public String stringValue() {
    return token.substring(1, token.length() - 1);
  }
}
