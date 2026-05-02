#CSC 240 Text Processing Project 

##Self Assessment

###By Conor Burns

This project taught me a lot. Me and my partner, Patrick Clisham, split the project up roughly 
55/45, with me doing the bigger portion. I wrote FeatureExtractor and SpamClassifier, which covered 
the feature extraction, classification, and the training. Patrick handled the data loading, email 
model, and others. We both worked on the project together meeting many times throughout month. We 
both shared the smaller utility classes. In our plan, we made up 11 requirements that are 
implemented with passing JUnit tests. Our classifier is a nearest-centroid model, trains the 
averages of each class's feature vectors into a centroid, and classification picks the closer one. 
Our initial versions were rough, we were getting results as low as 6%. After many tweaks we got to 
71.83%, which was worse than the baseline of 83% on the 1:5 imbalance dataset. After layering on 
stop word removal, z score normalization, bigrams, and a Bayesian prior correction to handle the 
class imbalances, we got a score of 93.67% on the 600 email held out test set. The massive 
progression of accuracy can be seen as iterative refinement. 
The hardest part was trying to diagnose the class imbalance, with the 71.83% accuracy seemed
deceptively close to working, until I saw the 83% majority class baseline, and finding
the right fix took longer than writing the original classifier did.
