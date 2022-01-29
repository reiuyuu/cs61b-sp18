public class ArrayDeque<T> {
    private T[] items;
    private int size;
    private int nextFirst;
    private int nextLast;

    private static int RFACTOR = 2;

    /** ==============
      * CONSTRUCTOR 
      * ============== */

    /** Creates an empty ArrayDeque. */
    public ArrayDeque() {
        items = (T[]) new Object[8];
        size = 0;
        nextFirst = 4;
        nextLast = 5;
    }

    /** =============
      * OPERATIONS
      * ============= */

    private void resize(int capacity) {
        T[] resizeItems = (T[]) new Object[capacity];

        int oldNextFirst = nextFirst;
        nextFirst = (capacity - size) / 2 - 1;
        nextLast = (capacity + size) / 2;
        System.arraycopy(items, oldNextFirst + 1, resizeItems, nextFirst + 1, size);
        items = resizeItems;
    }

    private void checkUsageFactor() {
        if (items.length > 16) {
            if ((size * 4) < (items.length)) {
                int capacity = items.length / RFACTOR;
                resize(capacity);
            }
        }
    }

    /** Adds an item of type T to the front of the deque. */
    public void addFirst(T item) {
        if (nextFirst < 0) {
            int capacity = items.length * RFACTOR;
            resize(capacity);
        }

        items[nextFirst] = item;
        nextFirst--;
        size++;
    }

    /** Adds an item of type T to the back of the deque. */
    public void addLast(T item) {
        if (nextLast > items.length - 1) {
            int capacity = items.length * RFACTOR;
            resize(capacity);
        }

        items[nextLast] = item;
        nextLast++;
        size++;
    }

    /** Returns true if deque is empty, false otherwise. */
    public boolean isEmpty() {
        return size == 0;
    }

    /** Returns the number of items in the deque. */
    public int size() {
        return size;
    }

    /** Prints the items in the deque from first to last, separated by a space. */
    public void printDeque() {
        /** Stop at the second-to-last item so that there won't be extra whitespace
          * but a new line. */
        for (int i = nextFirst + 1; i < (nextLast - 1); i++) {
            System.out.print(items[i] + " ");
        }
        System.out.println(items[nextLast - 1]);
    }

    /** Removes and returns the item at the front of the deque.
      * If no such item exists, returns null. */
    public T removeFirst() {
        if (this.isEmpty()) {
            return null;
        }

        T removedItem = items[nextFirst + 1];
        items[nextFirst + 1] = null;
        nextFirst++;
        size--;
        checkUsageFactor();
        return removedItem;
    }

    /** Removes and returns the item at the back of the deque.
      * If no such item exists, returns null. */
    public T removeLast() {
        if (this.isEmpty()) {
            return null;
        }

        T removedItem = items[nextLast - 1];
        items[nextLast - 1] = null;
        nextLast--;
        size--;
        checkUsageFactor();
        return removedItem;
    }

    /** Gets the item at the given index, where 0 is the front, 1 is the next item,
      * and so forth. If no such item exists, returns null. Must not alter the deque! */
    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        
        int actualIndex = nextFirst + 1 + index;
        return items[actualIndex];
    }
}
