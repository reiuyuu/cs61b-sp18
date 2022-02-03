public class Palindrome {
    /** Return a Deque where the characters appear in the same order as in the String. */
    public Deque<Character> wordToDeque(String word) {
        Deque<Character> d = new LinkedListDeque<Character>();
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            d.addLast(c);
        }
        return d;
    }
    
    /** Return true if the given word is a palindrome, and false otherwise. */
    
    // Use a Deque and recursion.
    private boolean isPalindrome(Deque<Character> deque) {
        if (deque.size() == 0 || deque.size() == 1) {
            return true;
        }
        if (deque.removeFirst() == deque.removeLast()) {
            return isPalindrome(deque);
        }
        return false;
    }
    
    public boolean isPalindrome(String word) {
        Deque<Character> d = wordToDeque(word);
        return isPalindrome(d);
    }

    // Not use a Deque and no recursion.
    // public boolean isPalindrome(String word) {
        //     int len = word.length();
        //     for (int i = 0; i < (len / 2); i++) {
            //         if (word.charAt(i) != word.charAt(len - i - 1)) {
                //             return false;
                //         }
                //     }
    //     return true;
    // }

    /** Return true if the word is a palindrome according to the character comparison test */
    private boolean isPalindrome(Deque<Character> deque, CharacterComparator cc) {
        if (deque.size() == 0 || deque.size() == 1) {
            return true;
        }
        if (cc.equalChars(deque.removeFirst(), deque.removeLast())) {
            return isPalindrome(deque, cc);
        }
        return false;
    }

    public boolean isPalindrome(String word, CharacterComparator cc) {
        Deque<Character> d = wordToDeque(word);
        return isPalindrome(d, cc);
    }

}