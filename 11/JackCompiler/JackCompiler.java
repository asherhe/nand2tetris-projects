import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * If given a directory, compiles all Jack source files inside of it. If given
 * an individual file, only compiles the stuff in the file.
 */
public class JackCompiler {
  private static String getFileExtension(String s) {
    int extensionIndex = s.lastIndexOf(".");
    if (extensionIndex == -1) {
      return "";
    }
    return s.substring(extensionIndex);
  }

  private static void compileFile(Path src) {
    if (getFileExtension(src.toString()).equals(".jack")) {
      CompilationEngine cEngine = new CompilationEngine(src);
      cEngine.compileClass();
      cEngine.close();
    }
  }

  public static void main(String[] args) {
    Path p = Paths.get(args[0]);

    if (Files.isRegularFile(p)) {
      compileFile(p);
    }

    if (Files.isDirectory(p)) {
      try {
        Files.walk(p).forEach(file -> {
          compileFile(file);
        });
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
