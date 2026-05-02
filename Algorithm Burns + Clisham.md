## CSC 240 Text Processing Project Algorithm Psuedocode

Conor Burns & Patrick Clisham

### feature extract 

```
WORD_PATTERN = /[A-Za-z][A-Za-z0-9']*/
URL_PATTERN  = /https?://\S+ | www\.\S+/i
STOP_WORDS   = { a, the, and, is, of, ... }

function extractFeatures(email):
    text = email.rawText
    fv   = empty FeatureVector

    url_count    = number of matches of URL_PATTERN in text
    text_no_urls = text with URL matches replaced by space

    word_counts = {}
    for each match of WORD_PATTERN in text_no_urls:
        w = match.lowercase()
        word_counts[w] += 1

    word_tokens  = sum of word_counts.values()
    total_chars  = sum over (w, c) of length(w) * c
    unique_words = size(word_counts)
    total_words  = word_tokens + url_count
    avg_word_len = total_chars / word_tokens   (0 if no tokens)
    exclamations = count of '!' in text

    fv["total_word_count"]  = total_words
    fv["unique_word_count"] = unique_words
    fv["exclamation_count"] = exclamations
    fv["url_count"]         = url_count
    fv["avg_word_length"]   = avg_word_len

    for each (w, c) in word_counts:
        if w not in STOP_WORDS:
            fv[w + "_count"] = c

    prev = null
    for each match of WORD_PATTERN in text_no_urls:
        w = match.lowercase()
        if w in STOP_WORDS:
            prev = null
            continue
        if prev != null:
            fv["bigram_" + prev + "_" + w + "_count"] += 1
        prev = w

    return fv
```

### group summary

```
function computeSummary(group):
    all_features = union of feature names across emails in group

    group.stats    = {}
    group.centroid = empty FeatureVector

    for each feat in all_features:
        values = [email.features.get(feat) for email in group]   
        s = SummaryStats(values)                                 
        group.stats[feat]    = s
        group.centroid[feat] = s.mean
```

### training 

```
function train(training_data):
    spam_group    = new EmailGroup("spam")
    notSpam_group = new EmailGroup("not spam")

    for each e in training_data:
        if e.features is null:
            e.features = extractFeatures(e)
        if e.label == SPAM:
            spam_group.add(e)
        else if e.label == NOT_SPAM:
            notSpam_group.add(e)

    computeSummary(spam_group)
    computeSummary(notSpam_group)

    feature_stddev = {}
    for each feat seen in any training email:
        feature_stddev[feat] = populationStddev(values of feat across training_data)

    return classifier(spam_group, notSpam_group, feature_stddev)
```

### distance

```
function normalizedDistance(query_fv, centroid_fv, stddev):
    keys  = query_fv.featureNames() union centroid_fv.featureNames()
    sumSq = 0
    for each k in keys:
        scale = stddev[k] if stddev[k] > 0 else 1
        diff  = (query_fv.get(k) - centroid_fv.get(k)) / scale
        sumSq += diff * diff
    return sqrt(sumSq)
```

### classification

```
function classify(email, classifier):
    if email.features is null:
        email.features = extractFeatures(email)

    d_spam = normalizedDistance(email.features,
                                classifier.spam_group.centroid,
                                classifier.feature_stddev)
    d_ham  = normalizedDistance(email.features,
                                classifier.notSpam_group.centroid,
                                classifier.feature_stddev)

    n_spam = size(classifier.spam_group)
    n_ham  = size(classifier.notSpam_group)
    total  = n_spam + n_ham

    spam_score = d_spam^2 - 2 * log(n_spam / total)
    ham_score  = d_ham^2  - 2 * log(n_ham  / total)

    if spam_score <= ham_score:
        return "spam"
    else:
        return "not spam"
```
