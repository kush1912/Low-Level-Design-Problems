package lld.concurrency.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * =====================================================================================
 * INTERVIEW QUESTION: Design a Thread-Safe LFU Cache
 * =====================================================================================
 *
 * Design a Least Frequently Used (LFU) cache that is thread-safe.
 *
 * Requirements:
 *   - get(key)        → returns value if exists, else -1
 *   - put(key, value) → inserts/updates; evicts LFU if at capacity
 *   - O(1) for both operations
 *   - Thread-safe for concurrent access
 *
 * Eviction Policy:
 *   - Evict the key with the lowest frequency count
 *   - If tie, evict the least recently used among them (LRU within same frequency)
 *
 * Constraints:
 *   - 0 <= capacity <= 10^4
 *   - 0 <= key, value <= 10^5
 *   - At most 2 * 10^5 calls to get and put
 *
 * Example:
 *   LFUCache cache = new LFUCache(2);
 *   cache.put(1, 1);    // freq(1)=1
 *   cache.put(2, 2);    // freq(2)=1
 *   cache.get(1);       // returns 1, freq(1)=2
 *   cache.put(3, 3);    // evicts key 2 (LFU), freq(3)=1
 *   cache.get(2);       // returns -1
 *   cache.get(3);       // returns 3, freq(3)=2
 *
 * Hint (Data Structures):
 *   - Map<Integer, Node> keyToNode      → O(1) lookup
 *   - Map<Integer, DoublyLinkedList> freqToList → each frequency has its own DLL
 *   - int minFreq → track minimum frequency for O(1) eviction
 *
 * =====================================================================================
 */
public class LFUCache {
    class Node{
        int key;
        int val;
        int freq;
        Node prev;
        Node next;

        Node(int key, int val){
            this.key = key;
            this.val = val;
            this.freq = 1;
            prev = null;
            next = null;
        }
    }

    class DoublyLinkedList{
        Node dummyHead;
        Node dummyTail;
        int size;

        DoublyLinkedList(){
            this.dummyHead = new Node(-1, -1);
            this.dummyTail = new Node(-1, -1);

            dummyHead.prev = null;
            dummyHead.next = dummyTail;
            dummyTail.prev = dummyHead;
            dummyTail.next = null;
            size =0;
        }

        void insertAtHead(Node node){
            Node realHead = dummyHead.next;
            realHead.prev = node;
            node.next = realHead;
            node.prev = dummyHead;
            dummyHead.next = node;
            size++;
        }

        void remove(Node node){
            node.prev.next = node.next;
            node.next.prev = node.prev;
            size--;
        }

        Node removeFromTail(){
            Node prevNode = dummyTail.prev;
            prevNode.prev.next = dummyTail;
            dummyTail.prev = prevNode.prev;
            size--;
            return prevNode;
        }
    }
    int capacity;
    int currSize;
    int minFreq;
    Map<Integer, Node> keyToNode = new HashMap<>();
    Map<Integer, DoublyLinkedList> freqToList = new HashMap<>();

    // 1. Make lock final
    private final ReentrantLock lock = new ReentrantLock();

    private void updateFrequency(Node node){
        int oldFreq = node.freq;
        DoublyLinkedList oldList = freqToList.get(oldFreq);
        oldList.remove(node);
        node.freq++;
        if(oldFreq==minFreq && oldList.size==0){
            minFreq++;
        }
        int newFreq = node.freq;
        freqToList.computeIfAbsent(newFreq, k -> new  DoublyLinkedList());
        freqToList.get(newFreq).insertAtHead(node);
    }

    public LFUCache(int capacity) {
        this.capacity = capacity;
        this.minFreq = 0;
        this.currSize = 0;
    }

    public int get(int key) {
        lock.lock();
        try {
            if(!keyToNode.containsKey(key)) return -1;
            Node node = keyToNode.get(key);
            updateFrequency(node);
            return node.val;
        }finally {
            lock.unlock();
        }

    }

    public void put(int key, int value) {
       lock.lock();
       try{
           if (capacity == 0) return;
           if(keyToNode.containsKey(key)){
               Node node = keyToNode.get(key);
               updateFrequency(node);
               node.val = value;
               return;
           }

           if(currSize == capacity){
               DoublyLinkedList minFreqList = freqToList.get(minFreq);
               Node nodeToBeRemoved = minFreqList.removeFromTail();
               keyToNode.remove(nodeToBeRemoved.key);
               nodeToBeRemoved = null;
               currSize--;
           }

           Node node = new Node(key, value);
           keyToNode.put(key, node);
           minFreq = 1;
           if(!freqToList.containsKey(minFreq)){
               DoublyLinkedList newList = new DoublyLinkedList();
               freqToList.put(minFreq, newList);
           }
           freqToList.get(minFreq).insertAtHead(node);
           currSize++;
           return;
       }finally {
           lock.unlock();
       }
    }


    // ========================= TEST CASES =========================

    public static void main(String[] args) {
        System.out.println("=== Thread-Safe LFU Cache Tests ===\n");

        testBasicOperations();
        testLFUEviction();
        testTieBreaker();
        testUpdateExistingKey();
        testZeroCapacity();
        testConcurrentAccess();

        System.out.println("=== All tests passed! ===");
    }

    private static void testBasicOperations() {
        System.out.println("Test 1: Basic Operations");
        LFUCache cache = new LFUCache(2);

        cache.put(1, 10);
        cache.put(2, 20);

        assertEq(10, cache.get(1), "get(1) should return 10");
        assertEq(20, cache.get(2), "get(2) should return 20");
        assertEq(-1, cache.get(3), "get(3) should return -1");

        System.out.println("✓ Test 1 passed\n");
    }

    private static void testLFUEviction() {
        System.out.println("Test 2: LFU Eviction");
        LFUCache cache = new LFUCache(2);

        cache.put(1, 10);
        cache.put(2, 20);
        cache.get(1);          // freq(1)=2, freq(2)=1
        cache.put(3, 30);      // evicts key 2 (lowest freq)

        assertEq(10, cache.get(1), "key 1 should exist");
        assertEq(-1, cache.get(2), "key 2 should be evicted (LFU)");
        assertEq(30, cache.get(3), "key 3 should exist");

        System.out.println("✓ Test 2 passed\n");
    }

    private static void testTieBreaker() {
        System.out.println("Test 3: Tie Breaker (LRU among same frequency)");
        LFUCache cache = new LFUCache(2);

        cache.put(1, 10);      // freq(1)=1
        cache.put(2, 20);      // freq(2)=1, both have freq=1, key 1 is older
        cache.put(3, 30);      // evicts key 1 (same freq, but LRU)

        assertEq(-1, cache.get(1), "key 1 should be evicted (LRU tie-breaker)");
        assertEq(20, cache.get(2), "key 2 should exist");
        assertEq(30, cache.get(3), "key 3 should exist");

        System.out.println("✓ Test 3 passed\n");
    }

    private static void testUpdateExistingKey() {
        System.out.println("Test 4: Update Existing Key");
        LFUCache cache = new LFUCache(2);

        cache.put(1, 10);
        cache.put(2, 20);
        cache.put(1, 100);     // update key 1, freq(1)=2, freq(2)=1
        cache.put(3, 30);      // evicts key 2 (LFU)

        assertEq(100, cache.get(1), "key 1 should have updated value");
        assertEq(-1, cache.get(2), "key 2 should be evicted");
        assertEq(30, cache.get(3), "key 3 should exist");

        System.out.println("✓ Test 4 passed\n");
    }

    private static void testZeroCapacity() {
        System.out.println("Test 5: Zero Capacity Edge Case");
        LFUCache cache = new LFUCache(0);

        cache.put(1, 10);
        assertEq(-1, cache.get(1), "cache with 0 capacity should always return -1");

        System.out.println("✓ Test 5 passed\n");
    }

    private static void testConcurrentAccess() {
        System.out.println("Test 6: Concurrent Access");
        LFUCache cache = new LFUCache(100);

        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            final int id = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    cache.put(id * 100 + j, j);
                    cache.get(id * 100 + j);
                }
            });
        }

        for (Thread t : threads) t.start();
        for (Thread t : threads) {
            try { t.join(); } catch (InterruptedException e) { }
        }

        System.out.println("  No exceptions during concurrent access");
        System.out.println("✓ Test 6 passed\n");
    }

    private static void assertEq(int expected, int actual, String msg) {
        if (expected != actual) {
            throw new AssertionError(msg + " | Expected: " + expected + ", Got: " + actual);
        }
        System.out.println("  ✓ " + msg);
    }
}
