import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T9 
 *  R9 (Distance from an email to a group summary).
 *
 * Build a group of 2 emils where the mean of feature 'x' is 2.0.
 * Create a test email with x=5.0. Assert distanceTo(group) ~ 3.0.
 */
public class GroupDistanceTest {

    @Test
    public void distanceToGroupMean() {
        Email a = new Email(1, "", Email.LABEL_SPAM);
        Email b = new Email(2, "", Email.LABEL_SPAM);
        FeatureVector va = new FeatureVector(); va.put("x", 1.0);
        FeatureVector vb = new FeatureVector(); vb.put("x", 3.0);
        a.setFeatures(va);
        b.setFeatures(vb);

        EmailGroup g = new EmailGroup("spam");
        g.addEmail(a); g.addEmail(b);
        g.computeSummary();
        assertEquals(2.0, g.getMeanVector().get("x"), 1e-9);

        FeatureVector probe = new FeatureVector();
        probe.put("x", 5.0);
        assertEquals(3.0, g.distanceTo(probe), 1e-9);
    }
}
