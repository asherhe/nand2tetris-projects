import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Writes assembly code from VM commands.
 */
public class CodeWriter {
  /**
   * The output file in which we write assembly code
   */
  private FileWriter output;

  /**
   * The name of the current file. Used by methods with the <code>static</code> segment
   */
  public String currentFileName;

  /**
   * Key memory addresses for each segment
   */
  private Map<String, String> segIndices;

  /**
   * The operations represented by the arithmetic commands
   */
  private Map<String, String> arithSymbols;

  /**
   * Some asm code
   */
  private Map<String, String> asmCodez;

  /**
   * The amount of comparisons done, for use by <code>eq</code>,
   * <code>gt</code>, and <code>lt</code> instructions.
   */
  private int cmpCount;

  /**
   * Taking a <code>FileWriter</code> as input, set up all necessary stuff
   * @param outFile
   */
  public CodeWriter(FileWriter outFile) {
    output = outFile;

    // Number of compare instructions
    cmpCount = 0;

    // Set up arithmetic symbols
    arithSymbols = new HashMap<String, String>();
    arithSymbols.put("add", "+" );
    arithSymbols.put("sub", "-" );
    arithSymbols.put("neg", "-" );
    arithSymbols.put("eq" , "EQ");
    arithSymbols.put("gt" , "GT");
    arithSymbols.put("lt" , "LT");
    arithSymbols.put("and", "&" );
    arithSymbols.put("or" , "|" );
    arithSymbols.put("not", "!" );
    
    // Set up base addresses. A value of zero means that it doesn't matter
    segIndices = new HashMap<String,String>();
    segIndices.put("argument", "ARG" );
    segIndices.put("local"   , "LCL" );
    segIndices.put("static"  , "0"   );
    segIndices.put("constant", "0"   );
    segIndices.put("this"    , "THIS");
    segIndices.put("that"    , "THAT");
    segIndices.put("pointer" , "3"   );
    segIndices.put("temp"    , "5"   );

    // Load assembly codez
    try {
      asmCodez = new HashMap<String, String>();
      String[] asmFiles = new String[] {
        "seg", "fixedIndex", "const", "static",
        "push", "pop",
        "unaryOp", "binaryOp", "cmp"
      };

      for (String fileName : asmFiles) {
        Path p = Paths.get(String.format("./%s.asm", fileName));
        asmCodez.put(fileName, Files.readString(p));
      }
    } catch (Exception e) {
      System.out.println("Something wrong happened with reading asm source files");
      e.printStackTrace();
    }
  }

  /**
   * Informs the <code>CodeWriter</code> that we are now on a new file
   * @param fileName The name of the new file, stripped of the <code>.vm</code> extension
   */
  public void setFileName(String fileName) {
    currentFileName = fileName;
    System.out.println(String.format("Code writing started: %s", fileName));
  }

  /**
   * Writes the assembly code for an arithmetic command, given the command.
   * @param command The arithmetic command to write code for
   */
  public void writeArithmetic(String command) {
    String code = "";

    switch (command) {
      case "neg": case "not":
        code = asmCodez.get("unaryOp");
        break;
      case "add": case "sub": case "and": case "or":
        code = asmCodez.get("binaryOp");
        break;
      case "eq": case "gt": case "lt":
        code = asmCodez.get("cmp");
        cmpCount++;
        break;
      default:
        // This shouldn't happen
        break;
    }

    // Write code to file
    Object[] codeArgs = new Object[] {
      command, arithSymbols.get(command), cmpCount
    };
    code = MessageFormat.format(code, codeArgs);
    try {
      output.write(code);
    } catch (IOException e) {
      System.out.println("Something went wrong with writing to the file");
      e.printStackTrace();
    }
  }

  /**
   * Writes <code>push</code>/<code>pop</code> assembly code with some info.
   * @param command The type of command (<code>C_PUSH</code> or <code>C_POP</code>)
   * @param segment The memory segment of the operation
   * @param index The index of the segment
   */
  public void writePushPop(Parser.CommandType command, String segment, int index) {
    String code = "";

    // Generate code for getting memory addresses
    switch (segment) {
      case "constant":
        code = asmCodez.get("const");
        break;
      case "static":
        code = asmCodez.get("static");
        break;
      case "pointer": case "temp":
        code = asmCodez.get("fixedIndex");
        break;
      default:
        code = asmCodez.get("seg");
        break;
    }

    // Code for push or pop?
    switch (command) {
      case C_PUSH:
        code += asmCodez.get("push");
        break;
      case C_POP:
        code += asmCodez.get("pop");
        break;
      default:
        // This ain't supposed to happen
        break;
    }

    // Write code to file
    Object[] codeArgs = new Object[] {
      segment, Integer.toString(index),
      segIndices.get(segment),
      currentFileName,
      command == Parser.CommandType.C_PUSH ? "push" : "pop"
    };
    code = MessageFormat.format(code, codeArgs);
    try {
      output.write(code);
    } catch (IOException e) {
      System.out.println("Something went wrong with writing to the file");
      e.printStackTrace();
    }
  }

  /**
   * Closes the output file
   */
  public void close() {
    try {
      output.close();
    } catch (IOException e) {
      System.out.println("An error occurred closing the file");
      e.printStackTrace();
    }
  }
}
