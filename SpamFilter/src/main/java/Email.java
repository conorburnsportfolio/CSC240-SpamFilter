/**
 * Email.java
 *
 * R2 - holds the raw text, id, label, and feature vector for one email.
 * label is -1 if we dont know it yet.
 *
 * Authors: Conor Burns & Patrick Clisham
 * CSC 240 Text Processing Project -- Spam Filter
 */
public class Email {

    // label constants
    public static final int LABEL_NOT_SPAM = 0;
    public static final int LABEL_SPAM     = 1;
    public static final int LABEL_UNKNOWN  = -1;

    private final int id;
    private final String rawText;
    private int label;               // 1=spam, 0=not spam, -1=unknown
    private FeatureVector features;  // populated by extractFeatures()

    /**
     * features starts null, gets set when extractFeatures() is called
     */
    public Email(int id, String rawText, int label) {
        this.id = id;
        this.rawText = (rawText == null) ? "" : rawText;
        this.label = label;
        this.features = null;
    }

    /** run feature extraction and store the result */
    public void extractFeatures(FeatureExtractor fe) {
        this.features = fe.extract(this);
    }

    // getters/setters

    public int getId() {
        return id;
    }

    public String getRawText() {
        return rawText;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public FeatureVector getFeatures() {
        return features;
    }

    public void setFeatures(FeatureVector fv) {
        this.features = fv;
    }

    // R4 - print features sorted high to low
    public void displayFeatures() {
        System.out.println("---- Email #" + id + " (label=" + labelName(label) + ") ----");
        if (features == null) {
            System.out.println("  [no features extracted yet]");
            return;
        }
        java.util.List<java.util.Map.Entry<String, Double>> entries =
            new java.util.ArrayList<>();
        for (String name : features.featureNames()) {
            entries.add(new java.util.AbstractMap.SimpleEntry<>(name, features.get(name)));
        }
        entries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        for (java.util.Map.Entry<String, Double> e : entries) {
            System.out.println(String.format("  %-30s %10.4f", e.getKey(), e.getValue()));
        }
    }

    private static String labelName(int l) {
        if (l == LABEL_SPAM)     return "spam";
        if (l == LABEL_NOT_SPAM) return "not spam";
        return "unknown";
    }

    @Override
    public String toString() {
        String preview = rawText.length() > 40 ? rawText.substring(0, 40) + "..." : rawText;
        return "Email{id=" + id + ", label=" + labelName(label) + ", text=\"" + preview + "\"}";
    }
}
