import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompilationEngine {
  private JackTokenizer tokenizer;
  private VMWriter vmWriter;
  private SymbolTable symbolTable;

  private String className;

  private int whileCount = 0;
  private int ifCount = 0;

  private static class SyntaxElements {
    public static Map<JackTokenizer.TokenType, String> typeTags = new HashMap<>();
    static {
      typeTags.put(JackTokenizer.TokenType.KEYWORD, "keyword");
      typeTags.put(JackTokenizer.TokenType.SYMBOL, "symbol");
      typeTags.put(JackTokenizer.TokenType.INT_CONST, "integerConstant");
      typeTags.put(JackTokenizer.TokenType.STRING_CONST, "stringConstant");
      typeTags.put(JackTokenizer.TokenType.IDENTIFIER, "identifier");
    }

    public static List<String> op = Arrays.asList("+", "-", "*", "/", "&", "|", "<", ">", "=");

    public static Map<String, String> opArithmetic = new HashMap<>();
    static {
      opArithmetic.put("+", "add");
      opArithmetic.put("-", "sub");
      opArithmetic.put("&", "and");
      opArithmetic.put("|", "or");
      opArithmetic.put("<", "lt");
      opArithmetic.put(">", "gt");
      opArithmetic.put("=", "eq");
    }

    public static Map<String, String> unaryOp = new HashMap<>();
    static {
      unaryOp.put("-", "neg");
      unaryOp.put("~", "not");
    }
  }

  /**
   * Creates a new compilation engine with the given input file, and sets up a
   * output file and a tokenizer. The next routine called should be compileClass()
   * 
   * @param src The Jack source file in which we write code
   */
  public CompilationEngine(Path src) {
    tokenizer = new JackTokenizer(src);
    vmWriter = new VMWriter(src);
    symbolTable = new SymbolTable();
  }

  /**
   * Advances the tokenizer and returns the current token
   */
  private String getNext() {
    tokenizer.advance();
    return tokenizer.value();
  }

  /**
   * Advances the tokenizer. If the current token is not the same as specified,
   */
  private String getNext(String... token) {
    tokenizer.advance();
    String value = tokenizer.value();
    if (!Arrays.asList(token).contains(value)) {
      if (token.length == 1) {
        throw new RuntimeException("Expected token \"" + token + "\", got \"" + value + "\" instead");
      } else {
        throw new RuntimeException(
            "Expected one of \"" + String.join("\", \"", token) + "\", got \"" + value + "\" instead.");
      }
    }

    return value;
  }

  /**
   * Compiles a complete class
   */
  public void compileClass() {
    // Class declaration
    getNext("class");
    className = getNext(); // className

    // Class body
    getNext("{");
    String peek = tokenizer.peekNext();
    while (!peek.equals("}")) {
      switch (peek) {
        case "static":
        case "field":
          compileVarDec();
          break;
        case "constructor":
        case "function":
        case "method":
          compileSubroutine();
      }
      peek = tokenizer.peekNext();
    }

    getNext("}");
  }

  /**
   * Registers a variable declaration statement, and returns the amount of
   * variables taken
   */
  public int compileVarDec() {
    int varCount = 1;
    String kind = getNext();
    String type = getNext();
    String name = getNext();
    symbolTable.define(name, type, kind);
    while (!tokenizer.peekNext().equals(";")) {
      getNext(",");
      getNext(); // varName
      name = tokenizer.value();
      symbolTable.define(name, type, kind);
      varCount++;
    }
    getNext(";");
    return varCount;
  }

  /**
   * Compiles a complete method, function, or constructor
   */
  public void compileSubroutine() {
    symbolTable.startSubroutine();
    int varCount = 0;

    // Subroutine declaration
    String subroutineType = getNext();
    if (subroutineType.equals("method")) {
      symbolTable.define("this", className, "arg");
      varCount++;
    }
    getNext(); // returnType
    String name = className + "." + getNext();
    getNext("(");
    compileParameterList();
    getNext(")");

    // Subroutine body
    getNext("{");

    // Variables
    while (tokenizer.peekNext().equals("var")) {
      varCount += compileVarDec();
    }
    vmWriter.writeFunction(name, varCount);
    if (subroutineType.equals("constructor")) {
      vmWriter.writeCall("Memory.alloc", symbolTable.getCount("this"));
    }

    compileStatements();
    getNext("}");
  }

  /**
   * Registers a list of parameters
   */
  public void compileParameterList() {
    if (tokenizer.peekNext().equals(")")) {
      return;
    }

    while (true) {
      String type = getNext(); // type
      String name = getNext(); // varName
      symbolTable.define(name, type, "arg");
      if (!tokenizer.peekNext().equals(",")) {
        return;
      }
      getNext(",");
    }
  }

  /**
   * Compiles a sequence of statements, not including the enclosing "{}"
   */
  public void compileStatements() {
    while (!tokenizer.peekNext().equals("}")) {
      String statement = getNext("do", "let", "while", "return", "if");
      vmWriter.writeArithmetic("// " + statement); // INFO: Hack comment statements
      switch (statement) {
        case "do":
          compileDo();
          break;
        case "let":
          compileLet();
          break;
        case "while":
          compileWhile();
          break;
        case "return":
          compileReturn();
          break;
        case "if":
          compileIf();
          break;
        default:
          break;
      }
    }
  }

  /**
   * Compiles a do statement
   */
  public void compileDo() {
    String name = getNext(); // subroutineName or className or varName
    String nameClass = name;
    String subroutineName = name;
    int argCount = 0;
    switch (getNext()) {
      case "(": // Method in this class
        vmWriter.writePushPop("push", symbolTable.kindOf("this"), symbolTable.indexOf("this")); // Push this to the top
                                                                                                // of the stack
        argCount = compileExpressionList() + 1;
        break;
      case ".": // Any subroutine from some other class
        subroutineName = getNext(); // subroutineName
        if (symbolTable.exists(name)) { // Check if name is a class or a variable
          vmWriter.writePushPop("push", symbolTable.kindOf(name), symbolTable.indexOf(name));
          nameClass = symbolTable.typeOf(name);
          argCount = 1;
        }
        getNext("(");
        argCount += compileExpressionList();
        break;
      default:
        break;
    }

    vmWriter.writeCall(nameClass + "." + subroutineName, argCount);
    vmWriter.writePushPop("pop", "temp", 0); // Return values will end up clogging the stack
    getNext(")");
    getNext(";");
  }

  /**
   * Compiles a let statement
   */
  public void compileLet() {
    String name = getNext(); // varName

    if (tokenizer.peekNext().equals("[")) { // Array access
      vmWriter.writePushPop("push", symbolTable.kindOf(name), symbolTable.indexOf(name));
      getNext("[");
      compileExpression();
      getNext("]");
      vmWriter.writeArithmetic("add");

      getNext("=");
      compileExpression();
      vmWriter.writePushPop("pop", "temp", 0);

      vmWriter.writePushPop("pop", "pointer", 1);
      vmWriter.writePushPop("push", "temp", 0);
      vmWriter.writePushPop("pop", "that", 0);
    } else {
      getNext("=");
      compileExpression();
      vmWriter.writePushPop("pop", symbolTable.kindOf(name), symbolTable.indexOf(name));
    }
    getNext(";");
  }

  /**
   * Compiles a while statement
   */
  public void compileWhile() {
    vmWriter.writeLabel("LOOP" + whileCount);
    getNext("(");
    compileExpression();
    vmWriter.writeArithmetic("not");
    vmWriter.writeIf("LOOP-END" + whileCount);
    getNext(")");
    getNext("{");
    compileStatements();
    vmWriter.writeGoto("LOOP" + whileCount);
    getNext("}");
    vmWriter.writeLabel("LOOP-END" + whileCount);
    whileCount++;
  }

  /**
   * Compiles a return statemet
   */
  public void compileReturn() {
    if (!tokenizer.peekNext().equals(";")) {
      compileExpression();
    }
    vmWriter.writeReturn();
    getNext(";");
  }

  /**
   * Compiles an if statemnt, possibly with a trailing else clause
   */
  public void compileIf() {
    getNext("(");
    compileExpression();
    vmWriter.writeArithmetic("not");
    vmWriter.writeIf("IF" + ifCount + "-1");
    getNext(")");
    getNext("{");
    compileStatements();
    getNext("}");
    if (tokenizer.peekNext().equals("else")) {
      vmWriter.writeGoto("IF" + ifCount + "-2");
      vmWriter.writeLabel("IF" + ifCount + "-1");
      getNext("else");
      getNext("{");
      compileStatements();
      getNext("}");
      vmWriter.writeLabel("IF" + ifCount + "-2");
    } else {
      vmWriter.writeLabel("IF" + ifCount + "-1");
    }
  }

  /**
   * Compiles an expression and pushes it onto the stack
   */
  public void compileExpression() {
    compileTerm();

    while (SyntaxElements.op.contains(tokenizer.peekNext())) {
      String op = getNext(); // op
      compileTerm();
      if (SyntaxElements.opArithmetic.containsKey(op)) {
        vmWriter.writeArithmetic(SyntaxElements.opArithmetic.get(op));
      } else {
        String function = "";
        switch (op) {
          case "*":
            function = "multiply";
            break;
          case "/":
            function = "divide";
            break;
          default:
            break;
        }

        vmWriter.writeCall("Math." + function, 2);
      }
    }
  }

  /**
   * Compiles a term
   */
  public void compileTerm() {
    // integerConstant or stringConstant or keywordConstant or varName or
    // subroutineName or
    // className or "(" or unaryOp
    String value = getNext();
    switch (tokenizer.tokenType()) {
      case SYMBOL:
        if (value.equals("(")) { // Parenthesized expression
          compileExpression();
          getNext(")");
        } else { // Unary operation
          compileTerm();
          vmWriter.writeArithmetic(SyntaxElements.unaryOp.get(value));
        }
        break;
      case IDENTIFIER:
        switch (tokenizer.peekNext()) {
          case "[": // Array access
            vmWriter.writePushPop("push", symbolTable.kindOf(value), symbolTable.indexOf(value));
            getNext("[");
            compileExpression();
            getNext("]");
            vmWriter.writeArithmetic("add");
            vmWriter.writePushPop("pop", "pointer", 1);
            vmWriter.writePushPop("push", "that", 0);
            break;
          case ".":
          case "(": // Function call
            String nameClass = value;
            String subroutineName = value;
            int argCount = 0;
            switch (getNext()) {
              case "(": // Method in this class
                vmWriter.writePushPop("push", symbolTable.kindOf("this"), symbolTable.indexOf("this")); // Push this to
                                                                                                        // the top of
                                                                                                        // the stack
                                                                                                        // argCount =
                                                                                                        // compileExpressionList()
                                                                                                        // + 1;
                break;
              case ".": // Any subroutine from some other class
                subroutineName = getNext(); // subroutineName
                if (symbolTable.exists(value)) { // Check if name is a class or a variable
                  vmWriter.writePushPop("push", symbolTable.kindOf(value), symbolTable.indexOf(value));
                  nameClass = symbolTable.typeOf(value);
                  argCount = 1;
                }
                getNext("(");
                argCount += compileExpressionList();
                break;
              default:
                break;
            }

            vmWriter.writeCall(nameClass + "." + subroutineName, argCount);
            getNext(")");
            break;
          default: // Variable
            vmWriter.writePushPop("push", symbolTable.kindOf(value), symbolTable.indexOf(value));
            break;
        }
        break;
      case INT_CONST:
        vmWriter.writePushPop("push", "constant", Integer.parseInt(value));
        break;
      case STRING_CONST:
        value = tokenizer.stringValue();
        vmWriter.writePushPop("push", "constant", value.length());
        vmWriter.writeCall("String.new", 1);
        for (char c : value.toCharArray()) {
          vmWriter.writePushPop("push", "constant", (int) c);
          vmWriter.writeCall("String.appendChar", 2);
        }
        break;
      default:
        break;
    }
  }

  /**
   * Pushes a list of expressions onto the stack, and returns the amount of
   * expressions compiled
   */
  public int compileExpressionList() {
    if (tokenizer.peekNext().equals(")")) {
      return 0;
    }

    int expressionCount = 0;

    while (true) {
      compileExpression();
      expressionCount++;
      if (!tokenizer.peekNext().equals(",")) {
        break;
      }
      getNext(",");
    }

    return expressionCount;
  }

  /**
   * Closes the output file
   */
  public void close() {
    vmWriter.close();
  }
}
