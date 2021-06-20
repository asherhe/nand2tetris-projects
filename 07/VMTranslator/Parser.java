import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * A useful thing to help you rip apart VM instructions.
 * @author asherhe
 */
public class Parser {
  /**
   * Basically all the VM command types in existence. Note that
   * <code>C_ARITHMETIC</code> includes all arithmetic/logic operations
   * (<code>add</code>, <code>eq</code>, <code>and</code>, etc.)
   */
  public enum CommandType {
    C_ARITHMETIC,
    C_PUSH, C_POP,
    C_LABEL, C_GOTO, C_IF,
    C_FUNCTION, C_RETURN, C_CALL
  };

  /**
   * The current line this <code>Parser</code> is processing.
   */
  private String instruction;

  /**
   * The current instruction, split into the important bits
   */
  private String[] instructionChunks;

  /**
   * The VM file that this <code>Parser</code> reads from.
   */
  private Scanner fileStream;

  /**
   * The magic delimiter for our scanner.
   */
  /*
   * Regex breakdown:
   * \s*     : As much whitespace as possible (but none is okay)
   * (//.*)? : There is possibly a string that starts with a "//" somewhere.
   * \n      : Then comes the end of the line
   * +       : All of that at least once
   */
  private static String whitespace = "(\\s*(//.*)?\n)+";

  /**
   * Taking a <code>Scanner</code> as input (<code>Main</code> will sort out
   * the nitty-gritty of dealing with the files), the constructor for a
   * <code>Parser</code> will create a <code>Parser</code> and deal with stuff.
   * <p>
   * <b>Note:</b> The input must have LF line endings. Hope <code>Main</code>
   * deals with that.
   * @param fileStream The VM file represented as a scanner, helpfully supplied
   * by <code>Main</code>.
   */
  public Parser(Scanner fileStream) {
    // Save fileStream for later
    this.fileStream = fileStream;
    // Set the delimiter so that actual instructions are read, instead of the whitespace
    this.fileStream.useDelimiter(Pattern.compile(whitespace, Pattern.MULTILINE));
  }

  /**
   * Gets rid of all whitespace, leaving only the pure VM code.
   * Whitespace includes comments (Starts with "//") and space characters.
   */
  private void removeWhitespace() {
    /*
     * Get rid of comments.
     * Regex breakdown:
     * //.* : Some string that starts with "//"
     */
    instruction = instruction.replaceAll("//.*", "");

    /*
     * Now replace all whitespace with a single space
     * Regex breakdown:
     * \s+ : Find all whitespace that appear in a chunk
     */
    instruction = instruction.replaceAll("\\s+", " ");

    // Get rid of leading/trailing whitespace (if there is any whitespace at
    // the start/end of a line, it will be converted into one space character
    // as a result of the previous line)
    /*
     * Regex breakdown:
     * ^  : At the start of the line...
     * \s+ : Match whitespace that occurs in a glob
     */
    instruction = instruction.replaceAll("^\\s+", "");
    /*
     * Regex breakdown:
     * \s+ : Match whitespace that occurs in a glob
     * $  : At the end of the line
     */
    instruction = instruction.replaceAll("\\s+$", "");
  }

  /**
   * Whether we've wrung this VM file dry. If so, I guess we have to pick on
   * something else.
   * @return Whether this VM file still has commands
   */
  public boolean hasMoreCommands() {
    return fileStream.hasNext();
  }

  /**
   * Advances the <code>Parser</code>. Basically skips over to the next actual
   * instruction.
   */
  public void advance() {
    // Get the next instruction
    instruction = fileStream.next();
    removeWhitespace();
    // Split instruction apart
    instructionChunks = instruction.split(" ");
  }

  /**
   * Gets the current command type for this current instruction
   * @return The command type of the current instruction
   */
  public CommandType commandType() {
    // Logik: Deal with the specific types first, then assume that whatever's left is arithmetic

    // Default case
    CommandType type = CommandType.C_ARITHMETIC;
    // Deal with instruction
    switch (instructionChunks[0]) {
      case "push":
        type = CommandType.C_PUSH;
        break;
      case "pop":
        type = CommandType.C_POP;
    }
    return type;
  }

  /**
   * Returns the first argument of the current command. In the case of
   * <code>C_ARITHMETIC</code>, the command itself is returned.
   * @return
   */
  public String arg1() {
    CommandType type = this.commandType();
    if (type == CommandType.C_ARITHMETIC) {
      return instructionChunks[0];
    }
    return instructionChunks[1];
  }

  /**
   * Returns the second argument of the current command. Should be called only
   * if the current command is <code>C_PUSH</code>, <code>C_POP</code>,
   * <code>C_FUNCTON</code>, or <code>C_CALL</code>.
   * @return The second argument of the command
   */
  public int arg2() {
    return Integer.parseInt(instructionChunks[2]);
  }
}