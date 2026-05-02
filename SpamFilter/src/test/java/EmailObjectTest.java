import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T2 -- R2 (Email object).
 *
 * Construct an Email manually. Assert the getters return the correct
 * id / label / raw text, and that the feature vector is initially null.
 */
public class EmailObjectTest {

    @Test
    public void basicGettersAndInitialNullFeatures() {
        Email e = new Email(7, "raw text goes here", Email.LABEL_SPAM);
        assertEquals(7, e.getId());
        assertEquals("raw text goes here", e.getRawText());
        assertEquals(Email.LABEL_SPAM, e.getLabel());
        assertNull(e.getFeatures(), "features should be null before extraction");
    }

    @Test
    public void extractFeaturesPopulatesVector() {
        Email e = new Email(8, "one two three", Email.LABEL_NOT_SPAM);
        e.extractFeatures(new FeatureExtractor());
        assertNotNull(e.getFeatures());
        assertEquals(3.0, e.getFeatures().get("total_word_count"), 1e-9);
    }
}
