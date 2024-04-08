package bstmap;

import java.util.*;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    private int size;
    private Node<K, V> root;

    private static class Node<K extends Comparable, V> {
        private K key;
        private V value;
        private Node<K, V> left;
        private Node<K, V> right;

        public Node() {

        }

        public Node(K key, V value, Node<K, V> left, Node<K, V> right) {
            this.key = key;
            this.value = value;
            this.left = left;
            this.right = right;
        }

        public void copyValue(Node<K, V> node) {
            if (node == null) {
                return;
            }

            this.key = node.key;
            this.value = node.value;
        }
    }

    public BSTMap() {
        root = null; // set root to null for empty tree
        size = 0;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        return containsKey(key, root);
    }

    private boolean containsKey(K key, Node<K, V> node) {
        if (key == null) {
            throw new IllegalArgumentException("key for containsKey must be provided");
        }

        if (node == null) {
            return false;
        }

        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            return containsKey(key, node.left);
        } else if (cmp > 0) {
            return containsKey(key, node.right);
        }

        return true;
    }

    @Override
    public V get(K key) {
        return get(key, root);
    }

    private V get(K key, Node<K, V> node) {
        if (node == null) {
            return null;
        }

        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            return get(key, node.left);
        } else if (cmp > 0) {
            return get(key, node.right);
        }

        return node.value;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        root = put(key, value, root);
        size += 1;
    }

    private Node<K, V> put(K key, V value, Node<K, V> node) {
        if (key == null) {
            throw new IllegalArgumentException("calls put() with a null key");
        }

        if (node == null) {
            return new Node<K, V>(key, value, null, null);
        }

        if (value == null) {
            remove(key);
        }

        int cmp = key.compareTo(node.key);
        if (cmp > 0) {
            // key > node.key search right
            node.right = put(key, value, node.right);
        } else if (cmp < 0) {
            // key < node.key search left
            node.left = put(key, value, node.left);
        }

        return node;
    }

    /**
     * Prints out BSTMap in order of increasing Key.
     */
    private void printInOrder(Node<K, V> node) {
        // dfs the tree print out each node when it's left is printed.
        if (node == null) {
            return;
        }

        printInOrder(node.left);
        System.out.println(node.key);
        printInOrder(node.right);
    }

    private void printInOrder() {
        printInOrder(root);
    }

    @Override
    public Set<K> keySet() {
        TreeSet<K> keys = new TreeSet<>();
        Deque<Node<K, V>> nodes = new LinkedList<>();

        nodes.push(root);
        while (!nodes.isEmpty()) {
            Node<K, V> node = nodes.remove();

            if (node != null) {
                nodes.add(node.left);
                nodes.add(node.right);
                keys.add(node.key);
            }
        }

        return keys;
    }

    private ArrayList<K> keySetIterativeDFS() {
        Stack<Node<K, V>> nodes = new Stack<>();
        ArrayList<K> keySets = new ArrayList<>();
        Node<K, V> popNode = root;

        nodes.push(root);
        while (!nodes.empty()) {
            Node<K, V> top = nodes.peek();

            if (top == null) {
                popNode = nodes.pop();
            } else {
                if (popNode.equals(top.right)) {
                    popNode = nodes.pop();
                    keySets.add(popNode.key);
                } else if (popNode.equals(top.left)) {
                    nodes.push(top.right);
                } else {
                    nodes.push(top.left);
                }
            }
        }

        return keySets;
    }

    @Override
    public V remove(K key) {
        // input check
        if (key == null) {
            return null;
        }

        // search the node and its prev
        BSTMap<String, Node<K, V>> searchResult = searchPrev(key, root, root);
        if (searchResult == null) {
            return null;
        }

        size--; // don't forget size
        Node<K, V> node = searchResult.get("node");
        Node<K, V> prev = searchResult.get("prev");

        // 2. remove the key
        return deleteNode(node, prev);
    }

    /**
     * delete the provided node
     * @param node node to delete
     * @param prev node's prev node
     * @return the deleted node's value field
     */
    private V deleteNode(Node<K, V> node, Node<K, V> prev) {
        if (node == null || prev == null) {
            throw new IllegalArgumentException("the node to delete and its prev should be provided");
        }

        V removedValue = node.value;
        if (isTwoChildNode(node)) {
            // find biggest of left subtree
            BSTMap<String, Node<K, V>> leftAndPrev = largestLeft(node);
            Node<K, V> leftLargest = leftAndPrev.get("leftLargest");
            Node<K, V> leftLargestPrev = leftAndPrev.get("prev");

            // swap the node to the delete node position
            swap(leftLargest, node);

            // delete node again
            deleteNode(leftLargest, leftLargestPrev);
        } else if (isSingleChildNode(node)) {
            Node<K, V> child = getSingleChild(node);
            if (node.equals(prev)) {
                root = child;
            } else if (isLeftChild(node, prev)) {
                prev.left = child;
            } else if (isRightChild(node, prev)){
                prev.right = child;
            }
        } else if (isLeafNode(node)) {
            if (prev.equals(node)) {
                root = null; // single root
            } else if (isLeftChild(node, prev)) {
                prev.left = null;
            } else if (isRightChild(node, prev)) {
                prev.right = null;
            }
        }

        return removedValue;
    }

    private boolean isLeftChild(Node<K, V> child, Node<K, V> parent) {
        return parent.left != null && parent.left.equals(child);
    }

    private boolean isRightChild(Node<K, V> child, Node<K, V> parent) {
        return parent.right != null && parent.right.equals(child);
    }

    /**
     * find the largest node in the provided node's left subtree
     * @param node root node of the search subtree
     * @return largest node and its prev node
     */
    private BSTMap<String, Node<K, V>> largestLeft(Node<K, V> node) {
        if (node == null || node.left == null) {
            return null;
        }

        // start with node's left subtree
        Node<K, V> ptr = node.left;
        Node<K, V> prev = node;
        while (ptr.right!= null) {
            prev = ptr;
            ptr = ptr.right;
        }

        BSTMap<String, Node<K, V>> ret = new BSTMap<>();
        ret.put("prev", prev);
        ret.put("leftLargest", ptr);

        return ret;
    }

    private void swap(Node<K, V> a, Node<K, V> b) {
        // object swap, just copy data
        // copy b's data to a object
        Node<K, V> temp = new Node<>(a.key, a.value, null, null);
        a.copyValue(b);
        b.copyValue(temp);
    }

    private boolean isTwoChildNode(Node<K, V> node) {
        return null != node.left && null != node.right;
    }

    private boolean isSingleChildNode(Node<K, V> node) {
        return !isLeafNode(node) && !isTwoChildNode(node);
    }

    private boolean isLeafNode(Node<K, V> node) {
        return null == node.left && null == node.right;
    }

    private Node<K, V> getSingleChild(Node<K, V> node) {
        if (!isSingleChildNode(node)) {
            throw new IllegalArgumentException("getSingleChild can only accept node with single child");
        }

        return node.left == null ? node.right : node.left;
    }

    /**
     * Search key, return prev node of that key-node if exists
     * @param key search key
     * @param node start node each turn
     * @return return a dict contain both node and prev
     *
     * Invariant: if root != null, root's prev is root itself
     */
    private BSTMap<String, Node<K, V>> searchPrev(K key, Node<K, V> node, Node<K, V> prev) {
        if (key == null || node == null) {
            return null;
        }

        int cmp = key.compareTo(node.key);
        BSTMap<String, Node<K, V>> searchResult;
        if (cmp < 0) {
            searchResult = searchPrev(key, node.left, node);
        } else if (cmp > 0) {
            searchResult = searchPrev(key, node.right, node);
        } else {
            searchResult = new BSTMap<>();
            searchResult.put("node", node);
            searchResult.put("prev", prev);
        }

        return searchResult;
    }

    @Override
    public V remove(K key, V value) {
        if (key == null || value == null) {
            return null;
        }

        V searchValue = get(key);
        if (value.equals(searchValue)) {
            return remove(key);
        }

        return null;
    }

    @Override
    public Iterator<K> iterator() {
        TreeSet<K> keys = (TreeSet<K>) keySet();

        return keys.iterator();
    }
}
