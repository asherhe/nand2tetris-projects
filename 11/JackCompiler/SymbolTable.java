import java.util.HashMap;

/**
 * Represents a Symbol Table for the compilation of a Jack class.
 */
public class SymbolTable {
  // A class that deals with individual symbol tables
  private class Table {
    private HashMap<String, String> type = new HashMap<>();
    private HashMap<String, String> kind = new HashMap<>();
    private HashMap<String, Integer> index = new HashMap<>();

    public void addEntry(String name, String type, String kind, int index) {
      this.type.put(name, type);
      this.kind.put(name, kind);
      this.index.put(name, index);
    }

    public boolean contains(String name) {
      return this.type.keySet().contains(name);
    }

    public String typeOf(String name) {
      return this.type.get(name);
    }

    public String kindOf(String name) {
      return this.kind.get(name);
    }

    public int indexOf(String name) {
      return this.index.get(name);
    }
  }

  // The symbol tables for classes and subroutines, respectively
  private Table classTable;
  private Table subroutineTable;

  private HashMap<String, Integer> varCounts = new HashMap<>();

  private static HashMap<String, String> kindTable = new HashMap<>();
  static {
    kindTable.put("static", "static");
    kindTable.put("field", "this");
    kindTable.put("var", "local");
    kindTable.put("arg", "argument");
  }

  public SymbolTable() {
    classTable = new Table();
    varCounts.put("static", 0);
    varCounts.put("this", 0);
    // No need to deal with data for subroutines - startSubroutine() deals with that
    // hassle
  }

  /**
   * Notifies the Symbol Table that the compilation of a new subroutine has begun
   */
  public void startSubroutine() {
    subroutineTable = new Table();
    varCounts.put("argument", 0);
    varCounts.put("local", 0);
  }

  private int nextIndex(String kind) {
    int index = varCounts.get(kind);
    varCounts.put(kind, index + 1);
    return index;
  }

  /**
   * Defines a new variable from its properties (the index of the segment will be
   * chosen by our program with a counter, so no need to worry about that)
   * 
   * @param name The name of this variable. Follows the naming rules of Jack
   *             variables
   * @param type The type of the variable - int, char, boolean, or a class name
   * @param kind Either "static," "this," "argument," or "local." basically the
   *             name of the segment it belongs to.
   */
  public void define(String name, String type, String kind) {
    kind = kindTable.get(kind);
    switch (kind) {
      case "static":
      case "this":
        classTable.addEntry(name, type, kind, nextIndex(kind));
        break;
      case "argument":
      case "local":
        subroutineTable.addEntry(name, type, kind, nextIndex(kind));
    }
  }

  /**
   * Checks whether a variable exists
   * @param name The name of the variable to check
   * @return Whether the variable exists
   */
  public boolean exists(String name) {
    if (!subroutineTable.contains(name)) {
      if (!classTable.contains(name)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Gets the type of a variable. The subroutine table will be searched first, and
   * if the variable could not be found, the class tably would be searched
   * 
   * @param name The name of the variable
   * @return The type of the variable with name name
   */
  public String typeOf(String name) {
    String type;
    try { // Attempt to retrieve data about
      type = subroutineTable.typeOf(name);
    } catch (Exception e) {
      type = classTable.typeOf(name);
    }
    return type;
  }

  /**
   * Gets the kind of a variable. The subroutine table will be searched first, and
   * if the variable could not be found, the class tably would be searched
   * 
   * @param name The name of the variable
   * @return The kind of the variable with name name
   */
  public String kindOf(String name) {
    String kind;
    try { // Attempt to retrieve data about
      kind = subroutineTable.kindOf(name);
    } catch (Exception e) {
      kind = classTable.kindOf(name);
    }
    return kind;
  }

  /**
   * Gets the index of a variable. The subroutine table will be searched first,
   * and if the variable could not be found, the class tably would be searched
   * 
   * @param name The name of the variable
   * @return The index of the variable with name name
   */
  public int indexOf(String name) {
    int index;
    try { // Attempt to retrieve data about
      index = subroutineTable.indexOf(name);
    } catch (Exception e) {
      index = classTable.indexOf(name);
    }
    return index;
  }

  /**
   * Gets the number of occurences of a certain kind of variable so far
   */
  public int getCount(String kind) {
    return varCounts.get(kind);
  }
}
