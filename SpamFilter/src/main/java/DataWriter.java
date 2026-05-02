import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * DataWriter.java
 *
 * writes output files for R6, R7, R11.
 * all static, nothing to instantiate.
 *
 * Authors: Conor Burns & Patrick Clisham
 * CSC 240 Text Processing Project -- Spam Filter
 */
public final class DataWriter {

    private DataWriter() {} // non-instantiable

    /**
     * R6: one row per email, columns are union of all feature names.
     * missing features just get 0.
     */
    public static void saveEmailFeatures(List<Email> emails, String filename) throws IOException {
        // collect all feature names across every email
        TreeSet<String> allFeatures = new TreeSet<>();
        for (Email e : emails) {
            FeatureVector fv = e.getFeatures();
            if (fv != null) allFeatures.addAll(fv.featureNames());
        }
        List<String> orderedKeys = new ArrayList<>(allFeatures);

        try (PrintWriter pw = new PrintWriter(
                Files.newBufferedWriter(Paths.get(filename), StandardCharsets.UTF_8))) {
            // write header
            StringBuilder header = new StringBuilder("id,label");
            for (String f : orderedKeys) {
                header.append(",").append(f);
            }
            pw.println(header);

            for (Email e : emails) {
                StringBuilder row = new StringBuilder();
                row.append(e.getId()).append(",").append(e.getLabel());
                FeatureVector fv = e.getFeatures();
                if (fv == null) {
                    for (int i = 0; i < orderedKeys.size(); i++) row.append(",0");
                } else {
                    row.append(",").append(fv.toCsvRow(orderedKeys));
                }
                pw.println(row);
            }
        }
    }

    /** R11: one prediciton per line, matches order of input emails */
    public static void savePredictions(List<String> preds, String filename) throws IOException {
        try (PrintWriter pw = new PrintWriter(
                Files.newBufferedWriter(Paths.get(filename), StandardCharsets.UTF_8))) {
            for (String p : preds) {
                pw.println(p);
            }
        }
    }
}
