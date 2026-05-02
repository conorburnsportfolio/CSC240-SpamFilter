import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FeatureExtractor.java
 *
 * R3: pulls numeric features out of raw email text.
 * produces bag-of-words counts (stop words removed), bigrams,
 * and the 5 compact features (word count, unique words, etc.)
 *
 * Authors: Conor Burns & Patrick Clisham
 * CSC 240 Text Processing Project -- Spam Filter
 */
public class FeatureExtractor {

    // word tokens: letters only, allows apostrophes
    private static final Pattern WORD_PATTERN = Pattern.compile("[A-Za-z][A-Za-z0-9']*");

    // catches http/https and bare www. links
    private static final Pattern URL_PATTERN =
        Pattern.compile("\\b(?:https?://\\S+|www\\.\\S+)", Pattern.CASE_INSENSITIVE);

    // stop words - these add noise, not signal
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "a","about","above","after","again","also","an","and","any","are","as","at",
        "be","been","before","being","between","but","by","can","did","do","does",
        "doing","during","each","few","for","from","further","get","had","has","have",
        "having","he","her","here","him","his","how","i","if","in","into","is","it",
        "its","itself","just","me","more","my","no","not","now","of","on","once",
        "only","or","other","our","out","own","s","same","she","should","so","some",
        "such","than","that","the","their","them","then","there","these","they",
        "this","those","through","to","too","under","until","up","us","very","was",
        "we","were","what","when","where","which","while","who","whom","why","will",
        "with","would","you","your"
    ));

    /** extract all features from an email's raw text */
    public FeatureVector extract(Email email) {
        FeatureVector fv = new FeatureVector();
        String text = email.getRawText();
        if (text == null) text = "";

        // strip URLs first so they dont get broken into weird word tokens
        int urlCount = countURLs(text);
        String textNoUrls = URL_PATTERN.matcher(text).replaceAll(" ");

        // get all words before stop word removal - need raw counts for the compact features
        Map<String, Integer> allWordCounts = countWords(textNoUrls);
        int wordTokens = 0;
        int totalChars = 0;
        for (Map.Entry<String, Integer> e : allWordCounts.entrySet()) {
            int c = e.getValue();
            wordTokens += c;
            totalChars += e.getKey().length() * c;
        }
        int totalWords = wordTokens + urlCount;
        int uniqueWords = allWordCounts.size();
        int exclamations = countExclamations(text);
        double avgWordLen = (wordTokens == 0) ? 0.0 : ((double) totalChars / wordTokens);

        fv.put("total_word_count",   totalWords);
        fv.put("unique_word_count",  uniqueWords);
        fv.put("exclamation_count",  exclamations);
        fv.put("url_count",          urlCount);
        fv.put("avg_word_length",    avgWordLen);

        // bag of words with stop words removed
        Map<String, Integer> contentWords = new HashMap<>(allWordCounts);
        contentWords.keySet().removeIf(STOP_WORDS::contains);
        for (Map.Entry<String, Integer> e : contentWords.entrySet()) {
            fv.put(e.getKey() + "_count", (double) e.getValue());
        }

        // bigrams catch phrases like "click here" that unigrams miss
        Map<String, Integer> bigrams = countBigrams(textNoUrls);
        for (Map.Entry<String, Integer> e : bigrams.entrySet()) {
            fv.put("bigram_" + e.getKey() + "_count", (double) e.getValue());
        }

        return fv;
    }

    /** lowercased word counts */
    Map<String, Integer> countWords(String text) {
        HashMap<String, Integer> counts = new HashMap<>();
        Matcher m = WORD_PATTERN.matcher(text);
        while (m.find()) {
            String w = m.group().toLowerCase();
            counts.merge(w, 1, Integer::sum);
        }
        return counts;
    }

    /**
     * bigrams of adjacent content words (stop words skipped).
     * "click here now" -> click_here, click_now
     */
    Map<String, Integer> countBigrams(String text) {
        HashMap<String, Integer> counts = new HashMap<>();
        Matcher m = WORD_PATTERN.matcher(text);
        String prev = null;
        while (m.find()) {
            String w = m.group().toLowerCase();
            if (STOP_WORDS.contains(w)) { prev = null; continue; }
            if (prev != null) {
                counts.merge(prev + "_" + w, 1, Integer::sum);
            }
            prev = w;
        }
        return counts;
    }

    /** count url tokens */
    int countURLs(String text) {
        int n = 0;
        Matcher m = URL_PATTERN.matcher(text);
        while (m.find()) n++;
        return n;
    }

    /** count exclamation marks */
    int countExclamations(String text) {
        int n = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '!') n++;
        }
        return n;
    }

    /** average word length in characters */
    double avgWordLength(String text) {
        Matcher m = WORD_PATTERN.matcher(text);
        int total = 0;
        int count = 0;
        while (m.find()) {
            total += m.group().length();
            count++;
        }
        return (count == 0) ? 0.0 : ((double) total / count);
    }
}
