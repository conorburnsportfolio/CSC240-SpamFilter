# Spam Filter Text Processor 

By Conor Burns & Patrick Clisham

Reads `data/spam_or_not_spam.csv`, splits 80/20, trains a nearest-centroid
classifier, predicts on the 20%. Output: `output/predictions.txt`.

## Run


Mac / Linux:

```
./build.sh test    # run all 11 unit tests
./build.sh run     # run on the dataset
```

Windows:

```
build.bat test
build.bat run
```
