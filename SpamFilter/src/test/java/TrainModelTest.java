import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T10 -- R10 (Train spam / not-spam models).
 *
 * Load a small labeled CSV with 10 spam + 10 not-spam rows, call
 * classifier.train, then assert spamModel.emails.size() == 10 and
 * notSpamModel.emails.size() == 10.
 */
public class TrainModelTest {

    @Test
    public void trainsBothGroupsWithExpectedCounts() throws IOException {
        StringBuilder csv = new StringBuilder("id,text,label\n");
        for (int i = 1; i <= 10; i++) {
            csv.append(i).append(",\"spam spam win free win now offer\",1\n");
        }
        for (int i = 11; i <= 20; i++) {
            csv.append(i).append(",\"lunch plans meeting notes project\",0\n");
        }
        Path tmp = Files.createTempFile("train-test", ".csv");
        Files.writeString(tmp, csv.toString(), StandardCharsets.UTF_8);
        try {
            List<Email> emails = DataLoader.loadCSV(tmp.toString());
            SpamClassifier clf = new SpamClassifier();
            clf.train(emails);
            assertEquals(10, clf.getSpamModel().getEmails().size());
            assertEquals(10, clf.getNotSpamModel().getEmails().size());
            // sanity: group means exist
            assertNotNull(clf.getSpamModel().getMeanVector());
            assertNotNull(clf.getNotSpamModel().getMeanVector());
        } finally {
            Files.deleteIfExists(tmp);
        }
    }
}
