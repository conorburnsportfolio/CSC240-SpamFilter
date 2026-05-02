import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T6 -- R6 (Save email features to CSV).
 *
 * Extract features for 2 emails, call DataWriter.saveEmailFeatures,
 * read the output file back, and assert that the header contains the
 * expected feature names and the row count equals 2.
 */
public class SaveEmailFeaturesTest {

    @Test
    public void saveEmailFeaturesProducesExpectedCsv() throws IOException {
        Email a = new Email(1, "hello there", Email.LABEL_NOT_SPAM);
        Email b = new Email(2, "free money!!!", Email.LABEL_SPAM);
        FeatureExtractor fe = new FeatureExtractor();
        a.extractFeatures(fe);
        b.extractFeatures(fe);

        Path tmp = Files.createTempFile("email-features", ".csv");
        try {
            DataWriter.saveEmailFeatures(Arrays.asList(a, b), tmp.toString());
            List<String> lines = Files.readAllLines(tmp);
            assertEquals(3, lines.size(), "header + 2 data rows");

            String header = lines.get(0);
            assertTrue(header.startsWith("id,label,"), "header starts with id,label");
            assertTrue(header.contains("total_word_count"));
            assertTrue(header.contains("exclamation_count"));
            // The union of features must be reflected: "free_count" is
            // present only in row b but still appears in the header.
            assertTrue(header.contains("free_count"),
                "header should include feature that appears in only one email");

            assertTrue(lines.get(1).startsWith("1,0,"));
            assertTrue(lines.get(2).startsWith("2,1,"));
        } finally {
            Files.deleteIfExists(tmp);
        }
    }
}
