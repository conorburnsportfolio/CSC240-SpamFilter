import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Main.java
 *
 * runs the full pipeline: load -> extract -> split -> train -> classify -> write output.
 * usage: java -cp out Main <input.csv> [<output-dir>]
 *
 * Authors: Conor Burns & Patrick Clisham
 * CSC 240 Text Processing Project -- Spam Filter
 */
public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: java Main <input.csv> [<output-dir>]");
            System.exit(1);
        }
        String inputCsv  = args[0];
        String outputDir = (args.length >= 2) ? args[1] : "output";
        java.nio.file.Files.createDirectories(java.nio.file.Paths.get(outputDir));

        // load
        System.out.println("Loading " + inputCsv + " ...");
        List<Email> all = DataLoader.loadCSV(inputCsv);
        System.out.println("  Loaded " + all.size() + " emails.");

        // feature extraction
        FeatureExtractor fe = new FeatureExtractor();
        for (Email e : all) {
            e.extractFeatures(fe);
        }

        // 80/20 split
        List<Email>[] split = DataLoader.splitTrainTest(all, 0.8);
        List<Email> train = split[0];
        List<Email> test  = split[1];
        System.out.println("  Train: " + train.size() + "  Test: " + test.size());

        // train
        SpamClassifier clf = new SpamClassifier();
        clf.train(train);

        // only print the 5 compact features to console - full summary goes to csv
        // printing all word features would flood the terminal
        java.util.List<String> compact = java.util.Arrays.asList(
            "total_word_count", "unique_word_count", "exclamation_count",
            "url_count",        "avg_word_length");
        clf.getSpamModel().displaySummary(compact);
        clf.getNotSpamModel().displaySummary(compact);

        // show top discriminitive words so we can sanity check the model
        printTopDiscriminativeWords(clf, 10);

        // classify test set
        List<String> preds = clf.classifyAll(test);

        // write outputs
        String featuresCsv = outputDir + "/email_features.csv";
        String summaryCsv  = outputDir + "/summary_features.csv";
        String predsTxt    = outputDir + "/predictions.txt";

        DataWriter.saveEmailFeatures(all, featuresCsv);
        // spam first, then append ham
        clf.getSpamModel().saveSummaryCSV(summaryCsv, false);
        clf.getNotSpamModel().saveSummaryCSV(summaryCsv, true);
        DataWriter.savePredictions(preds, predsTxt);

        System.out.println("Wrote: " + featuresCsv);
        System.out.println("Wrote: " + summaryCsv);
        System.out.println("Wrote: " + predsTxt);

        // accuracy (only if we have ground truth labels)
        int correct = 0;
        int labeled = 0;
        List<String> mistakes = new ArrayList<>();
        for (int i = 0; i < test.size(); i++) {
            Email e = test.get(i);
            if (e.getLabel() == Email.LABEL_UNKNOWN) continue;
            labeled++;
            int predLabel = SpamClassifier.SPAM.equals(preds.get(i))
                ? Email.LABEL_SPAM : Email.LABEL_NOT_SPAM;
            if (predLabel == e.getLabel()) correct++;
            else if (mistakes.size() < 5) mistakes.add("  email #" + e.getId()
                + " actual=" + e.getLabel() + " pred=" + preds.get(i));
        }
        if (labeled > 0) {
            double acc = 100.0 * correct / labeled;
            System.out.println(String.format("Accuracy on test set: %d / %d = %.2f%%",
                                             correct, labeled, acc));
            if (!mistakes.isEmpty()) {
                System.out.println("First mistakes:");
                for (String m : mistakes) System.out.println(m);
            }
        } else {
            System.out.println("Test set has no ground-truth labels; accuracy not computed.");
        }
    }

    /** Print the top-N words ranked by |spam_mean - ham_mean|. */
    private static void printTopDiscriminativeWords(SpamClassifier clf, int topN) {
        FeatureVector spamMean = clf.getSpamModel().getMeanVector();
        FeatureVector hamMean  = clf.getNotSpamModel().getMeanVector();
        java.util.TreeSet<String> keys = new java.util.TreeSet<>();
        keys.addAll(spamMean.featureNames());
        keys.addAll(hamMean.featureNames());

        java.util.List<double[]> rowsAsDoubles = new java.util.ArrayList<>();
        java.util.List<String>   rowWords       = new java.util.ArrayList<>();
        for (String k : keys) {
            if (!k.endsWith("_count")) continue;
            if (k.equals("total_word_count") || k.equals("unique_word_count")
                || k.equals("exclamation_count") || k.equals("url_count")) continue;
            double s = spamMean.get(k);
            double h = hamMean.get(k);
            double score = Math.abs(s - h);
            if (score == 0.0) continue;
            rowsAsDoubles.add(new double[]{s, h, score});
            rowWords.add(k.substring(0, k.length() - "_count".length()));
        }
        Integer[] order = new Integer[rowsAsDoubles.size()];
        for (int i = 0; i < order.length; i++) order[i] = i;
        java.util.Arrays.sort(order, (a, b) ->
            Double.compare(rowsAsDoubles.get(b)[2], rowsAsDoubles.get(a)[2]));

        System.out.println("==== Top " + topN + " discriminative words (|spam_mean - ham_mean|) ====");
        System.out.println(String.format("  %-20s %10s %10s %10s", "word", "spam mean", "ham mean", "score"));
        int limit = Math.min(topN, order.length);
        for (int i = 0; i < limit; i++) {
            int idx = order[i];
            double[] r = rowsAsDoubles.get(idx);
            System.out.println(String.format("  %-20s %10.4f %10.4f %10.4f",
                rowWords.get(idx), r[0], r[1], r[2]));
        }
    }
}
