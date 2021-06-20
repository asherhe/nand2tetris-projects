import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class CompilationEngine {
  private JackTokenizer tokenizer;

  private FileWriter compilerOutput;

  private Document jackDocument;

  private static class SyntaxElements {
    public static Map<JackTokenizer.TokenType, String> typeTags = new HashMap<JackTokenizer.TokenType, String>();
    static {
      typeTags.put(JackTokenizer.TokenType.KEYWORD, "keyword");
      typeTags.put(JackTokenizer.TokenType.SYMBOL, "symbol");
      typeTags.put(JackTokenizer.TokenType.INT_CONST, "integerConstant");
      typeTags.put(JackTokenizer.TokenType.STRING_CONST, "stringConstant");
      typeTags.put(JackTokenizer.TokenType.IDENTIFIER, "identifier");
    }

    public static List<String> op = Arrays.asList("+", "-", "*", "/", "&", "|", "<", ">", "=");
  }

  /**
   * Creates a new compilation engine with the given input file, and sets up a
   * output file and a tokenizer. The next routine called should be compileClass()
   * 
   * @param src The Jack source file in which we write code
   */
  public CompilationEngine(Path src) {
    tokenizer = new JackTokenizer(src);

    try {
      // Create output stram
      File f = new File(src.getParent().toString() + "/"
          + src.getFileName().toString().replaceAll("\\.[^.]*?$", "") + ".xml");
      f.createNewFile();
      compilerOutput = new FileWriter(f);

      // Create the entire XML document
      jackDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

      // Create the root element of the XML document
      jackDocument.appendChild(compileClass());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static String fixClosedTag(String rawXml) {
    LinkedList<String[]> listTags = new LinkedList<String[]>();
    String splittato[] = rawXml.split("<");

    String prettyXML = "";

    for (int x = 0; x < splittato.length; x++) {
      String tmpStr = splittato[x];
      int indexEnd = tmpStr.indexOf("/>");
      if (indexEnd > -1) {
        String nameTag = tmpStr.substring(0, (indexEnd));
        String oldTag = "<" + nameTag + "/>";
        String newTag = "<" + nameTag + ">\n</" + nameTag + ">";
        String tag[] = new String[2];
        tag[0] = oldTag;
        tag[1] = newTag;
        listTags.add(tag);
      }
    }
    prettyXML = rawXml;

    for (int y = 0; y < listTags.size(); y++) {
      String el[] = listTags.get(y);

      prettyXML = prettyXML.replaceAll(el[0], el[1]);
    }

    return prettyXML;
  }

  private Element newElement(String tagName) {
    Element e = jackDocument.createElement(tagName);
    return e;
  }

  private Node eat() {
    Element tokenElement;

    tokenizer.advance();

    String tagName = SyntaxElements.typeTags.get(tokenizer.tokenType());
    String value = tokenizer.value();
    System.out.println(value);
    if (tokenizer.tokenType() == JackTokenizer.TokenType.STRING_CONST) {
      value = tokenizer.stringValue();
    }

    tokenElement = newElement(tagName);
    tokenElement.appendChild(jackDocument.createTextNode(value));

    return tokenElement;
  }

  private Node compileType() {
    String tagName = "";

    tokenizer.advance();

    String token = tokenizer.value();
    switch (token) {
      case "int":
      case "char":
      case "boolean":
        tagName = SyntaxElements.typeTags.get(JackTokenizer.TokenType.KEYWORD);
        break;
      default:
        tagName = SyntaxElements.typeTags.get(JackTokenizer.TokenType.IDENTIFIER);
        break;
    }

    Element typeElement = newElement(tagName);
    typeElement.appendChild(jackDocument.createTextNode(token));
    return typeElement;
  }

  /**
   * Compiles a complete class
   */
  public Node compileClass() {
    Element classElement = newElement("class");

    classElement.appendChild(eat()); // "class"
    classElement.appendChild(eat()); // className
    classElement.appendChild(eat()); // "{"

    String peek = tokenizer.peekNext();
    while (!peek.equals("}")) {
      switch (peek) {
        case "static":
        case "field":
          classElement.appendChild(compileClassVarDec());
          break;
        case "constructor":
        case "function":
        case "method":
          classElement.appendChild(compileSubroutine());
      }
      peek = tokenizer.peekNext();
    }

    classElement.appendChild(eat()); // "}"

    return classElement;
  }

  /**
   * Compiles a static declaration or a field declaration
   */
  public Node compileClassVarDec() {
    Element varDecElement = newElement("classVarDec");

    varDecElement.appendChild(eat()); // "static" or "field"
    varDecElement.appendChild(compileType());
    varDecElement.appendChild(eat()); // varName
    while (!tokenizer.peekNext().equals(";")) {
      varDecElement.appendChild(eat()); // ","
      varDecElement.appendChild(eat()); // varName
    }
    varDecElement.appendChild(eat()); // ";"

    return varDecElement;
  }

  /**
   * Compiles a complete method, function, or constructor
   */
  public Node compileSubroutine() {
    Element subroutineElement = newElement("subroutineDec");

    subroutineElement.appendChild(eat()); // "constructor" or "function" or "method"
    subroutineElement.appendChild(eat()); // type or "void"
    subroutineElement.appendChild(eat()); // subroutineName
    subroutineElement.appendChild(eat()); // "("
    subroutineElement.appendChild(compileParameterList());
    subroutineElement.appendChild(eat()); // ")"

    Element subroutineBodyElement = newElement("subroutineBody");
    subroutineBodyElement.appendChild(eat()); // "{"
    while (tokenizer.peekNext().equals("var")) {
      subroutineBodyElement.appendChild(compileVarDec());
    }
    subroutineBodyElement.appendChild(compileStatements());
    subroutineBodyElement.appendChild(eat()); //

    subroutineElement.appendChild(subroutineBodyElement);

    return subroutineElement;
  }

  /**
   * Compiles a (possibly empty) parameter list, not including the enclosing "()"
   */
  public Node compileParameterList() {
    Element parameterListElement = newElement("parameterList");

    if (tokenizer.peekNext().equals(")")) {
      return parameterListElement;
    }

    while (true) {
      parameterListElement.appendChild(compileType());
      parameterListElement.appendChild(eat()); // varName
      if (!tokenizer.peekNext().equals(",")) {
        return parameterListElement;
      }
      parameterListElement.appendChild(eat()); // ","
    }
  }

  /**
   * Compiles a var declaration
   */
  public Node compileVarDec() {
    Element varDecElement = newElement("varDec");

    varDecElement.appendChild(eat()); // "var"
    varDecElement.appendChild(compileType());
    varDecElement.appendChild(eat()); // varName
    while (!tokenizer.peekNext().equals(";")) {
      varDecElement.appendChild(eat()); // ","
      varDecElement.appendChild(eat()); // varName
    }
    varDecElement.appendChild(eat()); // ";"

    return varDecElement;
  }

  /**
   * Compiles a sequence of statements, not including the enclosing "{}"
   */
  public Node compileStatements() {
    Element statementsElement = newElement("statements");

    while (!tokenizer.peekNext().equals("}")) {
      String statement = tokenizer.peekNext();
      switch (statement) {
        case "do":
          statementsElement.appendChild(compileDo());
          break;
        case "let":
          statementsElement.appendChild(compileLet());
          break;
        case "while":
          statementsElement.appendChild(compileWhile());
          break;
        case "return":
          statementsElement.appendChild(compileReturn());
          break;
        case "if":
          statementsElement.appendChild(compileIf());
          break;
        default:
          break;
      }
    }

    return statementsElement;
  }

  /**
   * Compiles a do statement
   */
  public Node compileDo() {
    Element doElement = newElement("doStatement");

    doElement.appendChild(eat()); // "do"
    doElement.appendChild(eat()); // subroutineName or className or varName
    switch (tokenizer.peekNext()) {
      case "(":
        doElement.appendChild(eat()); // "("
        doElement.appendChild(compileExpressionList());
        doElement.appendChild(eat()); // ")"
        break;
      case ".":
        doElement.appendChild(eat()); // "."
        doElement.appendChild(eat()); // subroutineName
        doElement.appendChild(eat()); // "("
        doElement.appendChild(compileExpressionList());
        doElement.appendChild(eat()); // ")"
        break;
      default:
        break;
    }

    doElement.appendChild(eat()); // ";"

    return doElement;
  }

  /**
   * Compiles a let statement
   */
  public Node compileLet() {
    Element letElement = newElement("letStatement");

    letElement.appendChild(eat()); // "let"
    letElement.appendChild(eat()); // varName
    if (tokenizer.peekNext().equals("[")) {
      letElement.appendChild(eat()); // "["
      letElement.appendChild(compileExpression());
      letElement.appendChild(eat()); // "]"
    }

    letElement.appendChild(eat()); // "="
    letElement.appendChild(compileExpression());
    letElement.appendChild(eat()); // ";"

    return letElement;
  }

  /**
   * Compiles a while statement
   */
  public Node compileWhile() {
    Element whileElement = newElement("whileStatement");

    whileElement.appendChild(eat()); // "while"
    whileElement.appendChild(eat()); // "("
    whileElement.appendChild(compileExpression());
    whileElement.appendChild(eat()); // ")"
    whileElement.appendChild(eat()); // "{"
    whileElement.appendChild(compileStatements());
    whileElement.appendChild(eat()); // "}"

    return whileElement;
  }

  /**
   * Compiles a return statemet
   */
  public Node compileReturn() {
    Element returnElement = newElement("returnStatement");

    returnElement.appendChild(eat()); // "return"
    if (!tokenizer.peekNext().equals(";")) {
      returnElement.appendChild(compileExpression());
    }
    returnElement.appendChild(eat()); // ";"

    return returnElement;
  }

  /**
   * Compiles an if statemnt, possibly with a trailing else clause
   */
  public Node compileIf() {
    Element ifElement = newElement("ifStatement");

    ifElement.appendChild(eat()); // "if"
    ifElement.appendChild(eat()); // "("
    ifElement.appendChild(compileExpression());
    ifElement.appendChild(eat()); // ")"
    ifElement.appendChild(eat()); // "{"
    ifElement.appendChild(compileStatements());
    ifElement.appendChild(eat()); // "}"

    if (tokenizer.peekNext().equals("else")) {
      ifElement.appendChild(eat()); // "else"
      ifElement.appendChild(eat()); // "{"
      ifElement.appendChild(compileStatements());
      ifElement.appendChild(eat()); // "}"
    }

    return ifElement;
  }

  /**
   * Compiles an expression
   */
  public Node compileExpression() {
    Element expressionElement = newElement("expression");

    expressionElement.appendChild(compileTerm());

    while (SyntaxElements.op.contains(tokenizer.peekNext())) {
      expressionElement.appendChild(eat()); // op
      expressionElement.appendChild(compileTerm());
    }

    return expressionElement;
  }

  /**
   * Compiles a term
   */
  public Node compileTerm() {
    Element termElement = newElement("term");

    // integerConstant or stringConstant or keywordConstant or varName or
    // subroutineName or
    // className or "(" or unaryOp
    termElement.appendChild(eat());
    switch (tokenizer.tokenType()) {
      case SYMBOL:
        if (tokenizer.value().equals("(")) {
          termElement.appendChild(compileExpression());
          termElement.appendChild(eat()); // ")"
        } else {
          termElement.appendChild(compileTerm());
        }
        break;
      case IDENTIFIER:
        switch (tokenizer.peekNext()) {
          case "[":
            termElement.appendChild(eat()); // "["
            termElement.appendChild(compileExpression());
            termElement.appendChild(eat()); // "]"
            break;
          case "(":
            termElement.appendChild(eat()); // "("
            termElement.appendChild(compileExpressionList());
            termElement.appendChild(eat()); // ")"
            break;
          case ".":
            termElement.appendChild(eat()); // "."
            termElement.appendChild(eat()); // subroutineName
            termElement.appendChild(eat()); // "("
            termElement.appendChild(compileExpressionList());
            termElement.appendChild(eat()); // ")"
            break;
          default:
            break;
        }
        break;
      default:
        break;
    }

    return termElement;
  }

  /**
   * Compiles a (possibly empty) comma-separated list of expressions
   */
  public Node compileExpressionList() {
    Element listElement = newElement("expressionList");
    if (tokenizer.peekNext().equals(")")) {
      return listElement;
    }

    while (true) {
      listElement.appendChild(compileExpression());
      if (!tokenizer.peekNext().equals(",")) {
        return listElement;
      }
      listElement.appendChild(eat()); // ","
    }
  }

  /**
   * Writes the XML output to the output file after all compilation is com plete
   */
  public void writeOutput() {
    try {
      // Configure the transformer
      Transformer jackTransformer = TransformerFactory.newInstance().newTransformer();
      jackTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
      jackTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

      DOMSource source = new DOMSource(jackDocument);
      StringWriter sw = new StringWriter();
      StreamResult output = new StreamResult(sw);
      jackTransformer.transform(source, output);

      String xmlString = sw.toString();
      compilerOutput.write(fixClosedTag(xmlString));

      compilerOutput.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
