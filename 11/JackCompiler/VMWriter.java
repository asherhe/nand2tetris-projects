import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;

// Deals with the details of output file handling and all that stuff
public class VMWriter {
  private FileWriter output;

  /**
   * Creates a file and prepares it for the writing of everything.
   */
  public VMWriter(Path src) {
    try {
      File f = new File(
          src.getParent().toString() + "/" + src.getFileName().toString().replaceAll("\\.[^.]*?$", "") + ".vm");
      f.createNewFile();
      output = new FileWriter(f);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void write(Object... s) {
    String[] str = new String[s.length];
    for (int i = 0; i < s.length; i++) {
      str[i] = s[i].toString();
    }
    try {
      output.write(String.join(" ", str) + "\n");
      System.out.println(String.join(" ", str));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // The methods below write stuff to the file
  public void writePushPop(String pushPop, String segment, int index) {
    write(pushPop, segment, index);
  }

  public void writeArithmetic(String command) {
    write(command);
  }

  public void writeLabel(String label) {
    write("label", label);
  }

  public void writeGoto(String label) {
    write("goto", label);
  }

  public void writeIf(String label) {
    write("if-goto", label);
  }

  public void writeCall(String name, int nArgs) {
    write("call", name, nArgs);
  }

  public void writeFunction(String name, int nLocals) {
    write("function", name, nLocals);
  }

  public void writeReturn() {
    write("return");
  }

  /**
   * Closes the output file so that nothing terrible happens (because bad things
   * happen when you don't close the output file)
   */
  public void close() {
    try {
      output.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
