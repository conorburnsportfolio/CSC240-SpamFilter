import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T7 -- R7 (Save summary data to CSV).
 *
 * Compute a group summary, call saveSummaryCSV, read the output back,
 * and assert that each feature appears in the file with its min and
 * max values.
 */
public class SaveSummaryTest {

    @Test
    public void saveSummaryCsvContainsMinAndMax() throws IOException {
        Email e1 = new Email(1, "", Email.LABEL_SPAM);
        Email e2 = new Email(2, "", Email.LABEL_SPAM);
        FeatureVector v1 = new FeatureVector(); v1.put("x", 1.0); v1.put("y", 10.0);
        FeatureVector v2 = new FeatureVector(); v2.put("x", 5.0); v2.put("y", 20.0);
        e1.setFeatures(v1); e2.setFeatures(v2);

        EmailGroup g = new EmailGroup("spam");
        g.addEmail(e1); g.addEmail(e2);
        g.computeSummary();

        Path tmp = Files.createTempFile("summary", ".csv");
        try {
            g.saveSummaryCSV(tmp.toString());
            String content = Files.readString(tmp);
            assertTrue(content.contains("feature_name"));
            assertTrue(content.contains("x,spam,"));
            assertTrue(content.contains("y,spam,"));
            // Min/max for x are 1 and 5
            assertTrue(content.contains("1.000000"), "min for x = 1");
            assertTrue(content.contains("5.000000"), "max for x = 5");
            // Min/max for y are 10 and 20
            assertTrue(content.contains("10.000000"));
            assertTrue(content.contains("20.000000"));
        } finally {
            Files.deleteIfExists(tmp);
        }
    }
}
