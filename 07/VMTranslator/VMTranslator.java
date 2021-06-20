import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class VMTranslator {

  /**
   * Gets the file name of the path &mdash; that is, the name of the
   * file/directory sans file extensions.
   * @param path The path of the file/directory to get the name of
   * @return The file/directory name of <code>path</code>
   */
  public static String getFilename(String path) {
    String filename;
    
    // Remove the path up to the file/directory name
    int lastSeparatorIndex = Integer.max(path.lastIndexOf("/"), path.lastIndexOf("\\"));
    if (lastSeparatorIndex == -1) {
      filename = path;
    } else {
      filename = path.substring(lastSeparatorIndex + 1);
    }

    // Remove the extension
    int extensionIndex = filename.lastIndexOf(".");
    // No extension
    if (extensionIndex == -1) {
      return filename;
    }
    // Return everything up to the extension (i.e. remove the extension)
    return filename.substring(0, extensionIndex);
  }
  /**
   * Gets the file name of the path &mdash; that is, the name of the
   * file/directory sans file extensions.
   * @param path The path of the file/directory to get the name of
   * @return The file/directory name of <code>path</code>
   */
  public static String getFilename(Path path) {
    return getFilename(path.toString());
  }
  /**
   * Gets the file extension of a file from its path
   * @param path The file to get the extension of
   * @return The file extension
   */
  public static String getFileExtension(String path) {
    int extensionIndex = path.lastIndexOf(".");
    if (extensionIndex == -1) {
      return "";
    }
    return path.substring(extensionIndex);
  }
  /**
 * Gets the file extension of a file from its path
 * @param path The file to get the extension of
 * @return The file extension
 */
  public static String getFileExtension(Path path) {
    return getFileExtension(path.toString());
  }

  private static void printIncorrectUsage() {
    System.out.println("Usage:\n\n\tVMtranslator source\n\nWhere source is either a VM file or directory containing VM files\n");
  }

  /**
   * Generates the VM code
   * @param sourceFile The path to the source file.
   * @param codeWriter The <code>CodeWriter</code> to write the code with
   */
  public static void generateCode(Path sourceFile, CodeWriter codeWriter) {
    codeWriter.setFileName(getFilename(sourceFile));

    Parser parser;
    // Set up Parser
    try {
      String f = Files.readString(sourceFile);
      // Convert to LF endings
      f = f.replaceAll("\\r\\n?", "\n");
      Scanner s = new Scanner(f);
      parser = new Parser(s);
    } catch (IOException e) {
      System.out.println("Error with reading input file");
      e.printStackTrace();
      return;
    }

    while (parser.hasMoreCommands()) {
      parser.advance();
      Parser.CommandType commandType = parser.commandType();
      switch (commandType) {
        case C_PUSH: case C_POP:
          codeWriter.writePushPop(commandType, parser.arg1(), parser.arg2());
          break;
        case C_ARITHMETIC:
          codeWriter.writeArithmetic(parser.arg1());
          break;
        default:
          // Shouldn't happen, but okay
          break;
      }
    }
  }

  public static void main(String[] args) {
    // Incorrect argument length
    if (args.length != 1) {
      printIncorrectUsage();
      return;
    }

    Path source = new File(args[0]).toPath();
    // Check if source is actually a valid path
    if (!Files.exists(source)) {
      printIncorrectUsage();
      return;
    }

    // Source is a file
    if (Files.isRegularFile(source) && getFileExtension(args[0]).equals(".vm")) {
      CodeWriter codeWriter;

      // Set up CodeWriter
      try {
        String outputFilename = source.getParent().toString() + "/" + getFilename(args[0]) + ".asm";
        FileWriter fw = new FileWriter(outputFilename);
        codeWriter = new CodeWriter(fw);
      } catch (IOException e) {
        System.out.println("Error in creating output file");
        e.printStackTrace();
        return;
      }

      generateCode(source, codeWriter);
      codeWriter.close();
    }
    
    // Source is a directory
    if (Files.isDirectory(source)) {
      CodeWriter codeWriter;

      // Set up CodeWriter
      try {
        String outputFilename = source.toString() + "/" + getFilename(args[0]) + ".asm";
        FileWriter fw = new FileWriter(outputFilename);
        codeWriter = new CodeWriter(fw);
      } catch (IOException e) {
        System.out.println("Error in creating output file");
        e.printStackTrace();
        return;
      }

      try {
        Files.walk(source).forEach(file -> {
          if (getFileExtension(file).equals(".vm")) {
            generateCode(file, codeWriter);
          }
        });
      } catch (IOException e) {
        System.out.println("Error while reading directory contents");
        e.printStackTrace();
        return;
      }
      codeWriter.close();
    }
  }
}
