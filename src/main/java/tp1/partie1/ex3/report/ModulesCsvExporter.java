package tp1.partie1.ex3.report;

import tp1.partie1.ex3.service.ModuleExtractor.Module;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ModulesCsvExporter {
    public void export(List<Module> modules, String outPath) throws Exception {
        String header = "module_id,avgSimilarity,classes\n";
        String body = "";
        for (int i=0;i<modules.size();i++) {
            Module m = modules.get(i);
            String line = (i+1) + "," + String.format(Locale.US,"%.6f", m.avgSimilarity) + ",\"" +
                    m.classes.stream().collect(Collectors.joining(" ")) + "\"\n";
            body += line;
        }
        File out = new File(outPath); out.getParentFile().mkdirs();
        Files.writeString(out.toPath(), header+body, StandardCharsets.UTF_8);
    }
}
