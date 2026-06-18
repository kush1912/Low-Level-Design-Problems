package lld.concurrency.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
/*
    1. Would you use ConcurrentHashMap?
        I could, but with a single global lock protecting the cache, ConcurrentHashMap doesn't provide much benefit because every get() and put() still acquires the lock to update recency. For this design, HashMap + ReentrantLock is simpler and performs similarly

            ConcurrentHashMap only protects access to the hashmap. The LRU cache also maintains a doubly linked list for recency ordering. Since every get() and put() modifies the linked list, we need synchronization across both the map and the list to keep them consistent. Therefore a lock around the entire cache operation is still required.
*/


/**
 * =====================================================================================
 * INTERVIEW QUESTION: Design a Thread-Safe LRU Cache
 * =====================================================================================
 *
 * Design a Least Recently Used (LRU) cache that is thread-safe.
 *
 * Requirements:
 *   - get(key)       → returns value if exists, else -1
 *   - put(key, value) → inserts/updates; evicts LRU if at capacity
 *   - O(1) for both operations
 *   - Thread-safe for concurrent access
 *
 * Constraints:
 *   - 1 <= capacity <= 3000
 *   - 0 <= key, value <= 10^4
 *
 * Example:
 *   LRUCache cache = new LRUCache(2);
 *   cache.put(1, 1);
 *   cache.put(2, 2);
 *   cache.get(1);       // returns 1
 *   cache.put(3, 3);    // evicts key 2
 *   cache.get(2);       // returns -1
 *
 * =====================================================================================
 */
public class LRUCache {

    class Node{
      int key;
      int val;
      Node prev;
      Node next;

      Node(int key, int val){
          this.key = key;
          this.val = val;
      }
    }

    // TODO: Define instance variables (capacity, map, head, tail, lock)
    private final int capacity;
    private final Map<Integer, Node> mp = new HashMap<>();;
    private final ReentrantLock lock = new ReentrantLock();
    private Node head, tail;


    public LRUCache(int capacity) {
        this.capacity = capacity;
    }

    public int get(int key) {
        lock.lock();
        try{
            Node node = mp.get(key);
            if(node == null) return -1;
            if(node!=head){
                removeNode(node);
                insertAtHead(node);
            }
            return node.val;

        }finally {
            lock.unlock();
        }
    }

    public void put(int key, int val) {
        lock.lock();
        try{
            // Key already exists
            if (mp.containsKey(key)) {
                Node node = mp.get(key);

                node.val = val;

                if (node != head) {
                    removeNode(node);
                    insertAtHead(node);
                }
                return;
            }

            // Cache full, evict LRU
            if (mp.size() == capacity) {
                Node lru = tail;
                removeNode(lru);
                mp.remove(lru.key);
            }
            Node newNode = new Node(key, val);
            insertAtHead(newNode);
            mp.put(key, newNode);
        }finally {
            lock.unlock();
        }
    }

    // TODO: Add helper methods (addToFront, removeNode, etc.)
    private void insertAtHead(Node node) {
        if (head == null) {
            head = tail = node;
            return;
        }

        node.prev = null;
        node.next = head;

        head.prev = node;
        head = node;
    }

    private void removeNode(Node node) {

        // only node
        if (node == head && node == tail) {
            head = tail = null;
        }

        // head node
        else if (node == head) {
            head = head.next;
            if (head != null) {
                head.prev = null;
            }
        }

        // tail node
        else if (node == tail) {
            tail = tail.prev;
            if (tail != null) {
                tail.next = null;
            }
        }

        // middle node
        else {
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }

        node.prev = null;
        node.next = null;
    }

    // ========================= TEST CASES =========================

    public static void main(String[] args) {
        System.out.println("=== Thread-Safe LRU Cache Tests ===\n");

        testBasicOperations();
        testLRUEviction();
        testUpdateExistingKey();
        testConcurrentAccess();

        System.out.println("=== All tests passed! ===");
    }

    private static void testBasicOperations() {
        System.out.println("Test 1: Basic Operations");
        LRUCache cache = new LRUCache(2);

        cache.put(1, 10);
        cache.put(2, 20);

        assertEq(10, cache.get(1), "get(1) should return 10");
        assertEq(20, cache.get(2), "get(2) should return 20");
        assertEq(-1, cache.get(3), "get(3) should return -1");

        System.out.println("✓ Test 1 passed\n");
    }

    private static void testLRUEviction() {
        System.out.println("Test 2: LRU Eviction");
        LRUCache cache = new LRUCache(2);

        cache.put(1, 10);
        cache.put(2, 20);
        cache.get(1);          // key 1 accessed, key 2 is now LRU
        cache.put(3, 30);      // evicts key 2

        assertEq(10, cache.get(1), "key 1 should exist");
        assertEq(-1, cache.get(2), "key 2 should be evicted");
        assertEq(30, cache.get(3), "key 3 should exist");

        System.out.println("✓ Test 2 passed\n");
    }

    private static void testUpdateExistingKey() {
        System.out.println("Test 3: Update Existing Key");
        LRUCache cache = new LRUCache(2);

        cache.put(1, 10);
        cache.put(2, 20);
        cache.put(1, 100);     // update key 1, key 2 is now LRU
        cache.put(3, 30);      // evicts key 2

        assertEq(100, cache.get(1), "key 1 should have updated value");
        assertEq(-1, cache.get(2), "key 2 should be evicted");
        assertEq(30, cache.get(3), "key 3 should exist");

        System.out.println("✓ Test 3 passed\n");
    }

    private static void testConcurrentAccess() {
        System.out.println("Test 4: Concurrent Access");
        LRUCache cache = new LRUCache(100);

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
        System.out.println("✓ Test 4 passed\n");
    }

    private static void assertEq(int expected, int actual, String msg) {
        if (expected != actual) {
            throw new AssertionError(msg + " | Expected: " + expected + ", Got: " + actual);
        }
        System.out.println("  ✓ " + msg);
    }
}
