package tp1.partie1.ex3.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//util/SourceScanner.java
public class SourceScanner {
 public static class SourceFile {
     public final String unitName;
     public final String content;
     public SourceFile(String unitName, String content) {
         this.unitName = unitName; this.content = content;
     }
 }

 public static List<SourceFile> readJavaSourcesWithUnitNames(String projectSourcePath) {
     File root = new File(projectSourcePath);
     Collection<File> files = FileUtils.listFiles(root, new String[]{"java"}, true);
     List<SourceFile> out = new ArrayList<>();
     for (File f : files) {
         try {
             String content = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
             String rel = root.toPath().relativize(f.toPath()).toString()
                                .replace(File.separatorChar, '/');
             out.add(new SourceFile(rel, content));
         } catch (Exception ignored) {}
     }
     return out;
 }
}
