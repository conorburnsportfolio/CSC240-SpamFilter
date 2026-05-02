import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8 -- R8 (Distance between two emails).
 *
 * Create A={x:3, y:4} and B={x:0, y:0}. Assert the Euclidean distance
 * returns 5.0 (a 3-4-5 triangle).
 */
public class EuclideanDistanceTest {

    @Test
    public void threeFourFiveTriangle() {
        FeatureVector a = new FeatureVector();
        a.put("x", 3.0); a.put("y", 4.0);
        FeatureVector b = new FeatureVector();
        b.put("x", 0.0); b.put("y", 0.0);
        assertEquals(5.0, a.euclideanDistance(b), 1e-9);
        assertEquals(5.0, b.euclideanDistance(a), 1e-9, "distance is symmetric");
    }

    @Test
    public void missingFeaturesTreatedAsZero() {
        // a = {x:3}, b = {y:4} -> distance over {x,y} = sqrt(9+16) = 5
        FeatureVector a = new FeatureVector(); a.put("x", 3.0);
        FeatureVector b = new FeatureVector(); b.put("y", 4.0);
        assertEquals(5.0, a.euclideanDistance(b), 1e-9);
    }
}
