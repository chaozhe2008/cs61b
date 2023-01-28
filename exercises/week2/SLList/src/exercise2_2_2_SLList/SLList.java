package exercise2_2_2_SLList;

public class SLList {
    public class IntNode {
        public int item;
        public IntNode next;
        public IntNode(int i, IntNode n) {
            item = i;
            next = n;
        }
    }

    private IntNode first;

    public SLList(int x) {
        first = new IntNode(x, null);
    }

    /** Adds an item to the front of the list. */
    public void addFirst(int x) {
        first = new IntNode(x, first);
    }

    /** Retrieves the front item from the list. */
    public int getFirst() {
        return first.item;
    }

    /** Adds an item to the end of the list. */
    public void addLast(int x) {
        /* Your Code Here! */
        IntNode head = first;
        while(head.next != null){
            head = head.next;
        }
        head.next = new IntNode(x, null);
    }

    /** Returns the number of items in the list using recursion. */
    private static int size(IntNode p) {
        if (p.next == null) {
            return 1;
        }

        return 1 + size(p.next);
    }
    public int size() {
        return size(first);
    }

    public static void main(String[] args){
        SLList list = new SLList(5);
        System.out.println(list.size());
        list.addLast(5);
        System.out.println(list.size());
    }

}

