import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T4 -- R4 (Individual feature display).
 *
 * Extract features for a known email, redirect System.out, call
 * displayFeatures(), and assert that the captured output contains the
 * expected feature names.
 */
public class DisplayFeaturesTest {

    @Test
    public void displayFeaturesWritesFeatureNamesToStdout() {
        Email e = new Email(42, "Hello world hello", Email.LABEL_NOT_SPAM);
        e.extractFeatures(new FeatureExtractor());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(baos));
        try {
            e.displayFeatures();
        } finally {
            System.setOut(original);
        }
        String out = baos.toString();
        assertTrue(out.contains("Email #42"),       "should mention the email id");
        assertTrue(out.contains("total_word_count"),"should list total_word_count");
        assertTrue(out.contains("hello_count"),     "should list per-word count");
    }
}
