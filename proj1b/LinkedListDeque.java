public class LinkedListDeque<T> implements Deque<T> {
    public class Node {
        private Node prev;
        private T item;
        private Node next;

        public Node(Node p, T i, Node n) {
            prev = p;
            item = i;
            next = n;
        }
    }

    private Node sentinel;
    private int size;

    /** ==============
      * CONSTRUCTOR 
      * ============== */
      
    /** Creates an empty LinkedListDeque. */
    public LinkedListDeque() {
        sentinel = new Node(null, null, null);
        sentinel.prev = sentinel;
        sentinel.next = sentinel;
        size = 0;
    }

    /** =============
      * OPERATIONS
      * ============= */

    /** Adds an item of type T to the front of the deque. */
    @Override
    public void addFirst(T item) {
        sentinel.next = new Node(sentinel, item, sentinel.next);
        sentinel.next.next.prev = sentinel.next;
        size++;
    }

    /** Adds an item of type T to the back of the deque. */
    @Override
    public void addLast(T item) {
        sentinel.prev = new Node(sentinel.prev, item, sentinel);
        sentinel.prev.prev.next = sentinel.prev;
        size++;
    }

    /** Returns true if deque is empty, false otherwise. */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /** Returns the number of items in the deque. */
    @Override
    public int size() {
        return size;
    }

    /** Prints the items in the deque from first to last, separated by a space. */
    @Override
    public void printDeque() {
        Node p = sentinel.next;
        /** Stop at the second-to-last item so that there won't be extra whitespace
          * but a new line. */
        for (int i = 0; i < (size - 1); i++) {
            System.out.print(p.item + " ");
            p = p.next;
        }
        System.out.println(p.item);
    }

    /** Removes and returns the item at the front of the deque.
      * If no such item exists, returns null. */
    @Override
    public T removeFirst() {
        if (this.isEmpty()) {
            return null;
        }

        T removedItem = sentinel.next.item;
        sentinel.next = sentinel.next.next;
        sentinel.next.prev = sentinel;
        size--;
        return removedItem;
    }

    /** Removes and returns the item at the back of the deque.
      * If no such item exists, returns null. */
    @Override
    public T removeLast() {
        if (this.isEmpty()) {
            return null;
        }
        
        T removedItem = sentinel.prev.item;
        sentinel.prev = sentinel.prev.prev;
        sentinel.prev.next = sentinel;
        size--;
        return removedItem;
    }

    /** Gets the item at the given index, where 0 is the front, 1 is the next item,
      * and so forth. If no such item exists, returns null. Must not alter the deque! */

    /** Use iteration. */
    @Override
    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }

        Node p = sentinel.next;
        while (index > 0) {
            p = p.next;
            index--;
        }
        return p.item;
    }

    /** Same as get, but uses recursion. */
    private T getRecursive(Node p, int index) {
        if (index == 0) {
            return p.item;
        }
        return getRecursive(p.next, index - 1);
    }
    
    public T getRecursive(int index) {
        if (index < 0 || index >= size) {
            return null;
        }

        return getRecursive(sentinel.next, index);
    }
    
}
