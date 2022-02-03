import org.junit.Test;
import static org.junit.Assert.*;

public class TestOffByN {
    
    // You must use this CharacterComparator and not instantiate
    // new ones, or the autograder might be upset.
    static CharacterComparator offBy2 = new OffByN(2);
    static CharacterComparator offBy3 = new OffByN(3);
    static CharacterComparator offBy4 = new OffByN(4);

    @Test
    public void testEqualChars() {
        assertTrue(offBy2.equalChars('z', 'x'));
        assertTrue(offBy3.equalChars('r', 'o'));
        assertTrue(offBy4.equalChars('w', 's'));

        assertFalse(offBy2.equalChars('z', 'y'));
        assertFalse(offBy3.equalChars('r', 'q'));
        assertFalse(offBy4.equalChars('w', 'w'));
    }
    
}
