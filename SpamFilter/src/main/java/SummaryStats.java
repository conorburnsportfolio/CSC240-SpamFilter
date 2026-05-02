import java.util.List;

/**
 * SummaryStats.java
 *
 * R5: computes min/max/mean/stddev for one feature over a group of emails.
 *
 * Authors: Conor Burns & Patrick Clisham
 * CSC 240 Text Processing Project -- Spam Filter
 */
public class SummaryStats {

    private final double min;
    private final double max;
    private final double mean;
    private final double stddev;
    private final int    count;

    /**
     * population stddev (divide by N, not N-1).
     * empty list returns all zeros.
     */
    public SummaryStats(List<Double> values) {
        if (values == null || values.isEmpty()) {
            this.min = 0.0;
            this.max = 0.0;
            this.mean = 0.0;
            this.stddev = 0.0;
            this.count = 0;
            return;
        }
        double lo = Double.POSITIVE_INFINITY;
        double hi = Double.NEGATIVE_INFINITY;
        double sum = 0.0;
        int n = values.size();
        for (double v : values) {
            if (v < lo) lo = v;
            if (v > hi) hi = v;
            sum += v;
        }
        double mu = sum / n;
        double sqSum = 0.0;
        for (double v : values) {
            double d = v - mu;
            sqSum += d * d;
        }
        this.min = lo;
        this.max = hi;
        this.mean = mu;
        this.stddev = Math.sqrt(sqSum / n);
        this.count = n;
    }

    public double getMin()    { return min;    }
    public double getMax()    { return max;    }
    public double getMean()   { return mean;   }
    public double getStddev() { return stddev; }
    public int    getCount()  { return count;  }

    /** csv columns: min,max,mean,stddev */
    public String toCsvRow() {
        return String.format("%.6f,%.6f,%.6f,%.6f", min, max, mean, stddev);
    }

    @Override
    public String toString() {
        return String.format("SummaryStats{n=%d min=%.4f max=%.4f mean=%.4f stddev=%.4f}",
                             count, min, max, mean, stddev);
    }
}
