import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T3 -- R3 (Feature extraction).
 *
 * Pass "Buy now! Visit http://spam.com today today today" to the
 * extractor and assert today_count=3, exclamation_count=1, url_count=1,
 * total_word_count=7.
 */
public class FeatureExtractorTest {

    @Test
    public void extractsExpectedCounts() {
        Email e = new Email(1, "Buy now! Visit http://spam.com today today today",
                            Email.LABEL_UNKNOWN);
        FeatureExtractor fe = new FeatureExtractor();
        e.extractFeatures(fe);
        FeatureVector fv = e.getFeatures();
        assertEquals(3.0, fv.get("today_count"),       1e-9, "today appears 3x");
        assertEquals(1.0, fv.get("exclamation_count"), 1e-9, "one '!' in text");
        assertEquals(1.0, fv.get("url_count"),         1e-9, "one URL in text");
        assertEquals(7.0, fv.get("total_word_count"),  1e-9,
            "Buy/now/Visit + 3x today + 1 URL == 7");
    }

    @Test
    public void avgWordLengthIsFloat() {
        Email e = new Email(2, "ab abc abcd", Email.LABEL_UNKNOWN);
        e.extractFeatures(new FeatureExtractor());
        // (2 + 3 + 4) / 3 = 3.0
        assertEquals(3.0, e.getFeatures().get("avg_word_length"), 1e-9);
    }
}
