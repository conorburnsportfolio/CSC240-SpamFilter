import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * FeatureVector.java
 *
 * sparse map of feature name -> value for one email.
 * missing features return 0.0 so distance calc works across different vocab sizes.
 *
 * Authors: Conor Burns & Patrick Clisham
 * CSC 240 Text Processing Project -- Spam Filter
 */
public class FeatureVector {

    private final HashMap<String, Double> data;

    public FeatureVector() {
        this.data = new HashMap<>();
    }

    /** add/update a feature value */
    public void put(String name, double value) {
        data.put(name, value);
    }

    /** returns 0.0 for missing features so distance calc doesnt break */
    public double get(String name) {
        Double v = data.get(name);
        return (v == null) ? 0.0 : v;
    }

    /** all feature names in this vector */
    public Set<String> featureNames() {
        return data.keySet();
    }

    public int size() {
        return data.size();
    }

    /**
     * R8: euclidean distance over the union of both vectors' features.
     * absent features count as 0.
     */
    public double euclideanDistance(FeatureVector other) {
        if (other == null) {
            throw new IllegalArgumentException("other FeatureVector must not be null");
        }
        java.util.HashSet<String> allKeys = new java.util.HashSet<>();
        allKeys.addAll(this.featureNames());
        allKeys.addAll(other.featureNames());
        double sumSq = 0.0;
        for (String key : allKeys) {
            double diff = this.get(key) - other.get(key);
            sumSq += diff * diff;
        }
        return Math.sqrt(sumSq);
    }

    /**
     * CSV row in the order given by orderedKeys, missing = "0".
     * caller is responsible for prepending id/label.
     */
    public String toCsvRow(List<String> orderedKeys) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < orderedKeys.size(); i++) {
            if (i > 0) sb.append(",");
            double v = get(orderedKeys.get(i));
            // integers stay as integers, otherwise 6 decimal places
            if (v == Math.floor(v) && !Double.isInfinite(v)) {
                sb.append((long) v);
            } else {
                sb.append(String.format("%.6f", v));
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "FeatureVector" + data.toString();
    }
}
