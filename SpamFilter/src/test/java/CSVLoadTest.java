import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T1 -- R1 (CSV loading).
 *
 * Create a 3-row mini CSV with known content. Call DataLoader.loadCSV.
 * Assert 3 Email objects are returned with correct id, text, label.
 */
public class CSVLoadTest {

    @Test
    public void loadsThreeRows() throws IOException {
        Path tmp = Files.createTempFile("csv-load-test", ".csv");
        String csv = "id,text,label\n"
                   + "1,\"Hello there\",0\n"
                   + "2,\"Buy now, only $99!\",1\n"
                   + "3,\"Meeting at noon\",0\n";
        Files.writeString(tmp, csv, StandardCharsets.UTF_8);
        try {
            List<Email> emails = DataLoader.loadCSV(tmp.toString());
            assertEquals(3, emails.size(), "should load 3 rows");
            assertEquals(1, emails.get(0).getId());
            assertEquals("Hello there", emails.get(0).getRawText());
            assertEquals(0, emails.get(0).getLabel());
            assertEquals(2, emails.get(1).getId());
            assertEquals("Buy now, only $99!", emails.get(1).getRawText());
            assertEquals(1, emails.get(1).getLabel());
            assertEquals(3, emails.get(2).getId());
            assertEquals(0, emails.get(2).getLabel());
        } finally {
            Files.deleteIfExists(tmp);
        }
    }
}
