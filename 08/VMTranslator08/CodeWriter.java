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
   * The name of the current function
   * For use by branching commands
   */
  private String currentFunctionName;

  /**
   * The amount of call statements written, for use by the <code>call</code>
   * command's return address system
   */
  private int callCount;

  /**
   * Taking a <code>FileWriter</code> as input, set up all necessary stuff
   * @param outFile
   */
  public CodeWriter(FileWriter outFile) {
    output = outFile;

    cmpCount = 0;
    callCount = 0;

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
        "bootstrap",
        "seg", "fixedIndex", "const", "static",
        "push", "pop",
        "unaryOp", "binaryOp", "cmp",
        "label", "goto", "if-goto",
        "call", "return", "function"
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
   * Writes the bootstrap code for the program, which sets SP to 256 and also calls <code>Sys.init</code>
   */
  public void writeBootstrapCode() {
    String code = asmCodez.get("bootstrap");
    try {
      output.write(code);
    } catch (IOException e) {
      System.out.println("Something went wrong with writing to the file");
      e.printStackTrace();
    }

    writeCall("Sys.init", 0);
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
      command, arithSymbols.get(command), Integer.toString(cmpCount)
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
   * Writes a label command. Nothing to complicated. For the implementation,
   * labels in functions will be named <code>functionName$label</code>.
   * @param label The name of the label
   */
  public void writeLabel(String label) {
    // Write code to file
    Object[] codeArgs = new Object[] {
      currentFunctionName, label
    };
    String code = MessageFormat.format(asmCodez.get("label"), codeArgs);
    try {
      output.write(code);
    } catch (IOException e) {
      System.out.println("Something went wrong with writing to the file");
      e.printStackTrace();
    }
  }

  /**
   * Writes the assembly code for a <code>goto</code> statement.
   * @param label
   */
  public void writeGoto(String label) {
    // Write code to file
    Object[] codeArgs = new Object[] {
      currentFunctionName, label
    };
    String code = MessageFormat.format(asmCodez.get("goto"), codeArgs);
    try {
      output.write(code);
    } catch (IOException e) {
      System.out.println("Something went wrong with writing to the file");
      e.printStackTrace();
    }
  }

  /**
   * Writes the assembly code for a <code>if-goto</code> statement.
   * @param label
   */
  public void writeIf(String label) {
    // Write code to file
    Object[] codeArgs = new Object[] {
      currentFunctionName, label
    };
    String code = MessageFormat.format(asmCodez.get("if-goto"), codeArgs);
    try {
      output.write(code);
    } catch (IOException e) {
      System.out.println("Something went wrong with writing to the file");
      e.printStackTrace();
    }
  }

  /**
   * Writes assembly code that implements the <code>call</code> command,
   * preserving some important memory segments in memory.
   * @param functionName The name of the function to call
   * @param numArgs The number of arguments supplied for this function
   */
  public void writeCall(String functionName, int numArgs) {
    callCount++;

    // Write code to file
    Object[] codeArgs = new Object[] {
      functionName, Integer.toString(numArgs), Integer.toString(callCount)
    };
    String code = MessageFormat.format(asmCodez.get("call"), codeArgs);
    try {
      output.write(code);
    } catch (IOException e) {
      System.out.println("Something went wrong with writing to the file");
      e.printStackTrace();
    }
  }

  /**
   * Writes assembly code for a <code>return</code> statement, "defrosting" all
   * the preserved memory segments
   */
  public void writeReturn() { // TODO: Return code is broken
    // Write code to file
    String code = asmCodez.get("return");
    try {
      output.write(code);
    } catch (IOException e) {
      System.out.println("Something went wrong with writing to the file");
      e.printStackTrace();
    }
  }

  /**
   * Writes assembly code that implements the <code>function</code> command,
   * creating the local segment
   * @param functionName The name of the function to call
   * @param numArgs The number of arguments supplied for this function
   */
  public void writeFunction(String functionName, int numLocals) {
    // Write code to file
    Object[] codeArgs = new Object[] {
      functionName, Integer.toString(numLocals)
    };
    String code = MessageFormat.format(asmCodez.get("function"), codeArgs);
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
