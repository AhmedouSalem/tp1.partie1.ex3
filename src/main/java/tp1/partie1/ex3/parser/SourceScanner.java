package tp1.partie1.ex3.parser;

import org.apache.commons.io.FileUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SourceScanner {
    public List<File> listJavaFiles(File root) {
        if (!root.exists()) return List.of();
        return new ArrayList<>(FileUtils.listFiles(root, new String[]{"java"}, true));
    }
}
