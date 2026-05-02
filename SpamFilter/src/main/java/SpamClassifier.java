import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SpamClassifier.java
 *
 * R10/R11: nearest-centroid classifier. builds a mean vector for spam
 * and ham groups, then classifies new emails by distance to each centroid.
 * uses z-score normalization and bayesian prior correction for class imbalance.
 *
 * Authors: Conor Burns & Patrick Clisham
 * CSC 240 Text Processing Project -- Spam Filter
 */
public class SpamClassifier {

    public static final String SPAM     = "spam";
    public static final String NOT_SPAM = "not spam";

    private EmailGroup spamModel;
    private EmailGroup notSpamModel;
    private final FeatureExtractor extractor;

    // per-feature stddev from training, used to normalize distances
    private HashMap<String, Double> featureStddev;

    public SpamClassifier() {
        this.extractor = new FeatureExtractor();
    }

    /** R10: train on labeled data, builds spam + ham group models */
    public void train(List<Email> trainingData) {
        spamModel    = new EmailGroup(SPAM);
        notSpamModel = new EmailGroup(NOT_SPAM);
        for (Email e : trainingData) {
            if (e.getFeatures() == null) {
                e.extractFeatures(extractor);
            }
            if (e.getLabel() == Email.LABEL_SPAM) {
                spamModel.addEmail(e);
            } else if (e.getLabel() == Email.LABEL_NOT_SPAM) {
                notSpamModel.addEmail(e);
            }
            // skip unlabeled emails
        }
        spamModel.computeSummary();
        notSpamModel.computeSummary();
        computeFeatureStddev(trainingData);
    }

    /** compute stddev per feature across all training emails */
    private void computeFeatureStddev(List<Email> trainingData) {
        HashMap<String, List<Double>> allValues = new HashMap<>();
        for (Email e : trainingData) {
            FeatureVector fv = e.getFeatures();
            if (fv == null) continue;
            for (String feat : fv.featureNames()) {
                allValues.computeIfAbsent(feat, k -> new ArrayList<>()).add(fv.get(feat));
            }
        }
        featureStddev = new HashMap<>();
        for (Map.Entry<String, List<Double>> entry : allValues.entrySet()) {
            List<Double> vals = entry.getValue();
            double mean = 0.0;
            for (double v : vals) mean += v;
            mean /= vals.size();
            double variance = 0.0;
            for (double v : vals) variance += (v - mean) * (v - mean);
            variance /= vals.size();
            featureStddev.put(entry.getKey(), Math.sqrt(variance));
        }
    }

    /**
     * euclidean distance but each diff is divided by that features stddev.
     * stops high-magnitude features from drowning out everything else.
     */
    private double normalizedDistance(FeatureVector query, FeatureVector centroid) {
        java.util.HashSet<String> allKeys = new java.util.HashSet<>();
        allKeys.addAll(query.featureNames());
        allKeys.addAll(centroid.featureNames());
        double sumSq = 0.0;
        for (String key : allKeys) {
            Double sd = featureStddev.get(key);
            double scale = (sd != null && sd > 0) ? sd : 1.0;
            double diff = (query.get(key) - centroid.get(key)) / scale;
            sumSq += diff * diff;
        }
        return Math.sqrt(sumSq);
    }

    public EmailGroup getSpamModel()    { return spamModel;    }
    public EmailGroup getNotSpamModel() { return notSpamModel; }

    /** R11: classify one email, extracts features if they havent been yet */
    public String classify(Email email) {
        if (spamModel == null || notSpamModel == null) {
            throw new IllegalStateException("Classifier has not been trained.");
        }
        if (email.getFeatures() == null) {
            email.extractFeatures(extractor);
        }
        double dSpam    = normalizedDistance(email.getFeatures(), spamModel.getMeanVector());
        double dNotSpam = normalizedDistance(email.getFeatures(), notSpamModel.getMeanVector());

        // bayesian correction for class imbalance (1:5 spam:ham).
        // without this the classifier just predicts ham for almost everything.
        // formula: predict spam if dSpam^2 - 2*log(P(spam)) < dHam^2 - 2*log(P(ham))
        int nSpam = spamModel.getEmails().size();
        int nHam  = notSpamModel.getEmails().size();
        double total = nSpam + nHam;
        double spamScore = dSpam * dSpam - 2.0 * Math.log(nSpam / total);
        double hamScore  = dNotSpam * dNotSpam - 2.0 * Math.log(nHam / total);
        return (spamScore <= hamScore) ? SPAM : NOT_SPAM;
    }

    /** R11: classify a list, same order as input */
    public List<String> classifyAll(List<Email> emails) {
        List<String> preds = new ArrayList<>(emails.size());
        for (Email e : emails) {
            preds.add(classify(e));
        }
        return preds;
    }

    /** classify and write predictions to file */
    public void savePredictions(List<Email> testEmails, String filename) throws IOException {
        List<String> preds = classifyAll(testEmails);
        DataWriter.savePredictions(preds, filename);
    }
}
