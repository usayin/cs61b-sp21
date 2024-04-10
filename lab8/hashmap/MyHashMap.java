package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    private double maxLoad;
    private double defaultMaxLoad = 0.75;
    private int defaultTableSize = 16;
    private int size;

    /** Constructors */
    public MyHashMap() {
        this.buckets = createTable(defaultTableSize);
        this.maxLoad = defaultMaxLoad;
        this.size = 0;
    }

   public MyHashMap(int initialSize) {
        this.buckets = createTable(initialSize);
        this.maxLoad = defaultMaxLoad;
        this.size = 0;
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        this.buckets = createTable(initialSize);
        this.maxLoad = maxLoad;
        this.size = 0;
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection[] table = new Collection[tableSize];

        // fill bucket into table
        for (int i = 0; i < tableSize; i++) {
            table[i] = createBucket();
        }

        return table;
    }

    @Override
    public void clear() {
        for (Collection<Node> bucket : buckets) {
            bucket.clear();
        }

        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        int hashCode = hashFunction(key, buckets.length);

        Collection<Node> bucket = buckets[hashCode];
        for (Node item : bucket) {
            if (item.key.equals(key)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public V get(K key) {
        if (key == null) {
            return null;
        }

        int hashCode = hashFunction(key, buckets.length);

        Collection<Node> bucket = buckets[hashCode];
        for (Node item : bucket) {
            if (item.key.equals(key)) {
                return item.value;
            }
        }

        return null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        int hashCode = hashFunction(key, buckets.length);
        Collection<Node> bucket = buckets[hashCode];

        for (Node item : bucket) {
            if (item.key == key) {
                // update node
                item.value = value;
                return;
            }
        }

        // add node
        bucket.add(createNode(key, value));
        size++;

        double loadFactor= 1.0 * size / buckets.length;
        if (loadFactor >= maxLoad) {
            doResize();
        }
    }

    private void doResize() {
        // 1. double the array size
        Collection[] resizedBuckets = createTable(buckets.length * 2);

        // 2. move elements
        for (Collection<Node> bucket : buckets) {
            for (Node item : bucket) {
                int hashCode = hashFunction(item.key, buckets.length * 2);
                resizedBuckets[hashCode].add(item);
            }
        }

        // 3. reset pointer
        buckets = resizedBuckets;
    }

    @Override
    public Set<K> keySet() {
        HashSet<K> keys = new HashSet<>();

        for (Collection<Node> bucket : buckets) {
            for (Node item : bucket) {
                keys.add(item.key);
            }
        }

        return keys;
    }

    @Override
    public V remove(K key) {
        if (key == null) {
            return null;
        }

        int hashCode = hashFunction(key, buckets.length);
        Collection<Node> bucket = buckets[hashCode];

        V removeValue = null;
        for (Node item : bucket) {
            if (item.key.equals(key)) {
                size--;
                bucket.remove(item);
                removeValue = item.value;
            }
        }

        return removeValue;
    }

    @Override
    public V remove(K key, V value) {
        if (containsKey(key) && get(key).equals(value)) {
            return remove(key);
        }

        return null;
    }

    @Override
    public Iterator<K> iterator() {
        return keySet().iterator();
    }

    private int hashFunction(Object obj, int mod) {
        return Math.abs(obj.hashCode()) % mod;
    }
}
