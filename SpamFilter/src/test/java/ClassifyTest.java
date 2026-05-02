import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T11 R11 (Classify and output predictions).
 *
 * Train on a tiny dataset where spam emails all contain "win" many
 * times. Create a test email with win_count=50 and assert that the
 * classifier labels it "spam".
 */
public class ClassifyTest {

    @Test
    public void classifiesWinHeavyEmailAsSpam() {
        FeatureExtractor fe = new FeatureExtractor();
        List<Email> train = new ArrayList<>();
        // 5 spam emails saturated with "win"
        for (int i = 0; i < 5; i++) {
            StringBuilder s = new StringBuilder();
            for (int k = 0; k < 20; k++) s.append("win ");
            Email e = new Email(i, s.toString().trim(), Email.LABEL_SPAM);
            e.extractFeatures(fe);
            train.add(e);
        }
        String[] benign = {
            "lunch with team", "project review next week", "meeting notes attached",
            "please review the draft", "thanks for the update"
        };
        for (int i = 0; i < benign.length; i++) {
            Email e = new Email(100 + i, benign[i], Email.LABEL_NOT_SPAM);
            e.extractFeatures(fe);
            train.add(e);
        }

        SpamClassifier clf = new SpamClassifier();
        clf.train(train);

        // Probe email with win_count = 50
        Email probe = new Email(999, "", Email.LABEL_UNKNOWN);
        FeatureVector fv = new FeatureVector();
        fv.put("win_count",         50.0);
        fv.put("total_word_count",  50.0);
        fv.put("unique_word_count", 1.0);
        fv.put("exclamation_count", 0.0);
        fv.put("url_count",         0.0);
        fv.put("avg_word_length",   3.0);
        probe.setFeatures(fv);

        assertEquals(SpamClassifier.SPAM, clf.classify(probe));
    }
}
