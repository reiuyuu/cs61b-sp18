public class OffByN implements CharacterComparator {

    int n;
    
    OffByN(int N) {
        n = N;
    }

    @Override
    public boolean equalChars(char x, char y) {
        int diff = x - y;
        if (diff == n || diff == -n) {
            return true;
        }
        return false;
    }
    
}
