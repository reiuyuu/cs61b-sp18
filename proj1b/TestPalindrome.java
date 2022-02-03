import org.junit.Test;
import static org.junit.Assert.*;

public class TestPalindrome {
    // You must use this palindrome, and not instantiate
    // new Palindromes, or the autograder might be upset.
    static Palindrome palindrome = new Palindrome();

    @Test
    public void testWordToDeque() {
        Deque d = palindrome.wordToDeque("persiflage");
        String actual = "";
        for (int i = 0; i < "persiflage".length(); i++) {
            actual += d.removeFirst();
        }
        assertEquals("persiflage", actual);
    }

    @Test
    public void testIsPalindrome() {
        assertTrue(palindrome.isPalindrome("qwq"));
        assertFalse(palindrome.isPalindrome("cat"));
        // corner cases
        assertTrue(palindrome.isPalindrome(""));
        assertTrue(palindrome.isPalindrome("u"));
    }

    @Test
    public void testIsOffByOnePalindrome() {
        CharacterComparator cc = new OffByOne();
        assertTrue(palindrome.isPalindrome("flake", cc));
        assertTrue(palindrome.isPalindrome("unpot", cc));
        assertTrue(palindrome.isPalindrome("tress", cc));
        assertTrue(palindrome.isPalindrome("%&", cc));

        assertFalse(palindrome.isPalindrome("aB", cc));
        assertFalse(palindrome.isPalindrome("ayanami", cc));
    }

}
