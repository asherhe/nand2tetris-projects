import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JackAnalyzer {

  private static String getFileExtension(String s) {
    int extensionIndex = s.lastIndexOf(".");
    if (extensionIndex == -1) {
      return "";
    }
    return s.substring(extensionIndex);
  }

  private static void analyzeFile(Path src) {
    if (getFileExtension(src.toString()).equals(".jack")) {
      System.out.println(String.format("Compiling file: %s", src.getFileName()));
      CompilationEngine cEngine = new CompilationEngine(src);
      cEngine.writeOutput();
    }
  }

  public static void main(String[] args) {
    Path p = Paths.get(args[0]);
    
    if (Files.isRegularFile(p)) {
      analyzeFile(p);
    }

    if (Files.isDirectory(p)) {
      try {
        Files.walk(p).forEach(file -> {
          analyzeFile(file);
        });
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
