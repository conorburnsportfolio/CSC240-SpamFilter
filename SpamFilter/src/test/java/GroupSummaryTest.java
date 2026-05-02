import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T5 -- R5 (Group summary statistics).
 *
 * Create 3 emails with known feature values, build an EmailGroup,
 * call computeSummary(), and assert min/max/mean/stddev are correct
 * for a chosen feature.
 */
public class GroupSummaryTest {

    @Test
    public void summaryStatsAreCorrect() {
        Email e1 = new Email(1, "", Email.LABEL_SPAM);
        Email e2 = new Email(2, "", Email.LABEL_SPAM);
        Email e3 = new Email(3, "", Email.LABEL_SPAM);
        FeatureVector v1 = new FeatureVector(); v1.put("x", 2.0);
        FeatureVector v2 = new FeatureVector(); v2.put("x", 4.0);
        FeatureVector v3 = new FeatureVector(); v3.put("x", 6.0);
        e1.setFeatures(v1);
        e2.setFeatures(v2);
        e3.setFeatures(v3);

        EmailGroup g = new EmailGroup("spam");
        g.addEmail(e1); g.addEmail(e2); g.addEmail(e3);
        g.computeSummary();

        SummaryStats s = g.getStats().get("x");
        assertEquals(2.0, s.getMin(),  1e-9);
        assertEquals(6.0, s.getMax(),  1e-9);
        assertEquals(4.0, s.getMean(), 1e-9);
        // population stddev: sqrt(((2-4)^2 + (4-4)^2 + (6-4)^2) / 3)
        //                  = sqrt((4 + 0 + 4) / 3) = sqrt(8/3)
        assertEquals(Math.sqrt(8.0 / 3.0), s.getStddev(), 1e-9);
    }
}
