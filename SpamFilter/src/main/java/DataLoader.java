import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * DataLoader.java
 *
 * R1: reads the kaggle CSV and turns each row into an Email.
 * handles both [email, label] and [id, text, label] layouts.
 *
 * Authors: Conor Burns & Patrick Clisham
 * CSC 240 Text Processing Project -- Spam Filter
 */
public final class DataLoader {

    private DataLoader() {} // non-instantiable

    /** load emails from csv, figures out column order from the header */
    public static List<Email> loadCSV(String filename) throws IOException {
        List<Email> emails = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(
                Paths.get(filename), StandardCharsets.UTF_8)) {
            String headerLine = reader.readLine();
            if (headerLine == null) return emails;

            List<String> columns = parseCSVLine(headerLine);
            // figure out which column is which
            int idIdx, textIdx, labelIdx;
            if (columns.size() >= 3
                    && columns.get(0).equalsIgnoreCase("id")) {
                idIdx = 0;
                textIdx = 1;
                labelIdx = 2;
            } else if (columns.size() >= 2
                    && (columns.get(0).equalsIgnoreCase("email")
                        || columns.get(0).equalsIgnoreCase("text")
                        || columns.get(0).equalsIgnoreCase("message"))) {
                // kaggle format
                idIdx = -1;
                textIdx = 0;
                labelIdx = 1;
            } else {
                // fallback, just guess based on col count
                if (columns.size() >= 3) {
                    idIdx = 0; textIdx = 1; labelIdx = 2;
                } else {
                    idIdx = -1; textIdx = 0; labelIdx = 1;
                }
            }

            String line;
            int autoId = 0;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) continue;
                List<String> cells = parseCSVLine(line);
                if (cells.size() <= textIdx) continue;

                int id = (idIdx >= 0 && idIdx < cells.size())
                    ? parseIntOr(cells.get(idIdx), autoId)
                    : autoId;
                String text = cells.get(textIdx);
                int label = Email.LABEL_UNKNOWN;
                if (labelIdx >= 0 && labelIdx < cells.size()) {
                    label = parseIntOr(cells.get(labelIdx), Email.LABEL_UNKNOWN);
                }
                emails.add(new Email(id, text, label));
                autoId++;
            }
        }
        return emails;
    }

    /**
     * splits emails into train/test. seed 42 so its always the same split.
     * returns [train, test]
     */
    @SuppressWarnings("unchecked")
    public static List<Email>[] splitTrainTest(List<Email> all, double trainRatio) {
        if (trainRatio <= 0.0 || trainRatio >= 1.0) {
            throw new IllegalArgumentException("trainRatio must be in (0, 1)");
        }
        List<Email> shuffled = new ArrayList<>(all);
        Collections.shuffle(shuffled, new Random(42));
        int cut = (int) Math.round(shuffled.size() * trainRatio);
        List<Email> train = new ArrayList<>(shuffled.subList(0, cut));
        List<Email> test  = new ArrayList<>(shuffled.subList(cut, shuffled.size()));
        return (List<Email>[]) new List[] { train, test };
    }

    // handles quoted fields w/ commas inside, "" to escape quotes
    static List<String> parseCSVLine(String line) {
        List<String> cells = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        int i = 0;
        while (i < line.length()) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i += 2;
                        continue;
                    }
                    inQuotes = false;
                    i++;
                } else {
                    cur.append(c);
                    i++;
                }
            } else {
                if (c == ',') {
                    cells.add(cur.toString());
                    cur.setLength(0);
                    i++;
                } else if (c == '"') {
                    inQuotes = true;
                    i++;
                } else {
                    cur.append(c);
                    i++;
                }
            }
        }
        cells.add(cur.toString());
        return cells;
    }

    private static int parseIntOr(String s, int fallback) {
        try {
            String t = s.trim();
            if (t.isEmpty()) return fallback;
            return Integer.parseInt(t);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
