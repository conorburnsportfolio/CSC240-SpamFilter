import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * EmailGroup.java
 *
 * R5/R9/R10: a labeled group of emails (spam or not spam).
 * call computeSummary() to build the mean vector and stats,
 * then use distanceTo() to compare new emails against the group.
 *
 * Authors: Conor Burns & Patrick Clisham
 * CSC 240 Text Processing Project -- Spam Filter
 */
public class EmailGroup {

    private final List<Email> emails;
    private final String groupLabel;

    // populated by computeSummary()
    private Map<String, SummaryStats> stats;

    // cached so distanceTo() doesnt recompute it every call
    private FeatureVector meanVector;

    public EmailGroup(String groupLabel) {
        this.groupLabel = groupLabel;
        this.emails = new ArrayList<>();
        this.stats = new LinkedHashMap<>();
        this.meanVector = new FeatureVector();
    }

    public void addEmail(Email e) {
        emails.add(e);
    }

    public List<Email> getEmails() {
        return emails;
    }

    public String getGroupLabel() {
        return groupLabel;
    }

    public Map<String, SummaryStats> getStats() {
        return stats;
    }

    public FeatureVector getMeanVector() {
        return meanVector;
    }

    /**
     * R5: compute stats for every feature across the group.
     * also builds the mean vector used by distanceTo().
     */
    public void computeSummary() {
        // union of all feature names in the group
        Set<String> allFeatures = new TreeSet<>();
        for (Email e : emails) {
            FeatureVector fv = e.getFeatures();
            if (fv != null) {
                allFeatures.addAll(fv.featureNames());
            }
        }

        // compute stats per feature, missing values treated as 0
        stats = new LinkedHashMap<>();
        meanVector = new FeatureVector();
        for (String feat : allFeatures) {
            List<Double> values = new ArrayList<>(emails.size());
            for (Email e : emails) {
                FeatureVector fv = e.getFeatures();
                values.add(fv == null ? 0.0 : fv.get(feat));
            }
            SummaryStats s = new SummaryStats(values);
            stats.put(feat, s);
            meanVector.put(feat, s.getMean());
        }
    }

    /** R9: distance from a feature vector to this groups mean. needs computeSummary() first. */
    public double distanceTo(FeatureVector fv) {
        if (meanVector == null) {
            throw new IllegalStateException("Call computeSummary() before distanceTo().");
        }
        return meanVector.euclideanDistance(fv);
    }

    /** prints full summary - warning: can be thousands of lines on real data */
    public void displaySummary() {
        displaySummary(Integer.MAX_VALUE);
    }

    /** print up to maxFeatures rows */
    public void displaySummary(int maxFeatures) {
        System.out.println("==== Group: " + groupLabel + " (" + emails.size() + " emails) ====");
        if (stats == null || stats.isEmpty()) {
            System.out.println("  [computeSummary() has not been called or group is empty]");
            return;
        }
        System.out.println(String.format("  %-30s %10s %10s %10s %10s",
                            "feature", "min", "max", "mean", "stddev"));
        int shown = 0;
        for (Map.Entry<String, SummaryStats> e : stats.entrySet()) {
            if (shown >= maxFeatures) {
                int remaining = stats.size() - shown;
                System.out.println("  ... (" + remaining
                    + " more features omitted; see summary_features.csv)");
                break;
            }
            SummaryStats s = e.getValue();
            System.out.println(String.format("  %-30s %10.4f %10.4f %10.4f %10.4f",
                                 e.getKey(), s.getMin(), s.getMax(), s.getMean(), s.getStddev()));
            shown++;
        }
    }

    /** print summary for a specific subset of features */
    public void displaySummary(java.util.List<String> featureNames) {
        System.out.println("==== Group: " + groupLabel + " (" + emails.size() + " emails) ====");
        if (stats == null || stats.isEmpty()) {
            System.out.println("  [computeSummary() has not been called or group is empty]");
            return;
        }
        System.out.println(String.format("  %-30s %10s %10s %10s %10s",
                            "feature", "min", "max", "mean", "stddev"));
        for (String name : featureNames) {
            SummaryStats s = stats.get(name);
            if (s == null) continue;
            System.out.println(String.format("  %-30s %10.4f %10.4f %10.4f %10.4f",
                                 name, s.getMin(), s.getMax(), s.getMean(), s.getStddev()));
        }
    }

    /**
     * R7: save summary stats for this group to csv.
     * if append=true, skip the header (so both groups can write to the same file)
     */
    public void saveSummaryCSV(String filename, boolean append) throws IOException {
        boolean writeHeader = !append || !Files.exists(Paths.get(filename));
        try (PrintWriter pw = new PrintWriter(
                Files.newBufferedWriter(
                    Paths.get(filename),
                    StandardCharsets.UTF_8,
                    append ? java.nio.file.StandardOpenOption.CREATE
                           : java.nio.file.StandardOpenOption.CREATE,
                    append ? java.nio.file.StandardOpenOption.APPEND
                           : java.nio.file.StandardOpenOption.TRUNCATE_EXISTING))) {
            if (writeHeader) {
                pw.println("feature_name,group,min,max,mean,stddev");
            }
            if (stats != null) {
                for (Map.Entry<String, SummaryStats> e : stats.entrySet()) {
                    pw.println(e.getKey() + "," + groupLabel + "," + e.getValue().toCsvRow());
                }
            }
        }
    }

    /** overwrites file (non-append) */
    public void saveSummaryCSV(String filename) throws IOException {
        saveSummaryCSV(filename, false);
    }
}
