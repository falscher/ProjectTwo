/************************************************************************************
 * @file BpTreeMap.java
 * @author John Miller
 */

import java.io.*;
import java.lang.reflect.Array;
import static java.lang.System.out;
import java.util.*;

/************************************************************************************
 * This class provides B+Tree maps.  B+Trees are used as multi-level index structures
 * that provide efficient access for both point queries and range queries.
 */
/**
 * @author esc
 *
 * @param <K>
 * @param <V>
 */
public class BpTreeMap<K extends Comparable<K>, V>
        extends AbstractMap<K, V>
        implements Serializable, Cloneable, SortedMap<K, V> {
    /**
     * The maximum fanout for a B+Tree node.
     */
    private static final int ORDER = 5;

    /**
     * The class for type K.
     */
    private final Class<K> classK;

    /**
     * The class for type V.
     */
    private final Class<V> classV;

    /********************************************************************************
     * This inner class defines nodes that are stored in the B+tree map.
     */
    private class Node {
        boolean isLeaf;
        int nKeys;
        K[] key;
        Object[] ref;

        @SuppressWarnings("unchecked")
        Node(boolean _isLeaf) {
            isLeaf = _isLeaf;
            nKeys = 0;
            key = (K[]) Array.newInstance(classK, ORDER - 1);
            if (isLeaf) {
                //ref = (V []) Array.newInstance (classV, ORDER);
                ref = new Object[ORDER];
            } else {
                ref = (Node[]) Array.newInstance(Node.class, ORDER);
            } // if
        } // constructor
    } // Node inner class

    /**
     * The root of the B+Tree
     */
    private Node root;

    /**
     * The counter for the number nodes accessed (for performance testing).
     */
    private int count = 0;

    /********************************************************************************
     * Construct an empty B+Tree map.
     *
     * @param _classK the class for keys (K)
     * @param _classV the class for values (V)
     */
    public BpTreeMap(Class<K> _classK, Class<V> _classV) {
        classK = _classK;
        classV = _classV;
        setRoot(new Node(true));
    } // constructor

    /********************************************************************************
     * Return null to use the natural order based on the key type.  This requires the
     * key type to implement Comparable.
     */
    public Comparator<? super K> comparator() {
        return null;
    } // comparator

    /********************************************************************************
     * Return a set containing all the entries as pairs of keys and values.
     *
     * @return the set view of the map
     */
    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> enSet = new HashSet<>();

        //get first key
        Node node = firstKeyNode();

        //reach to left bottom of the tree.

        do {
            // add key value pair to set
            for (int i = 0; i < node.nKeys; i++) {
                enSet.add(new AbstractMap.SimpleEntry<>(node.key[i], (V) node.ref[i]));
            }
            node = (Node) node.ref[node.nKeys];
        } while (node != null);


        return enSet;
    } // entrySet

    /********************************************************************************
     * Given the key, look up the value in the B+Tree map.
     *
     * @param key the key used for look up
     * @return the value associated with the key
     */
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        return find((K) key, getRoot());
    } // get

    /********************************************************************************
     * Put the key-value pair in the B+Tree map.
     *
     * @param key   the key to insert
     * @param value the value to insert
     * @return null (not the previous value)
     */
    public V put(K key, V value) {
        insert(key, value, getRoot(), null);
        return null;
    } // put

    /********************************************************************************
     * Return the first (smallest) key in the B+Tree map.
     *
     * @return the first key in the B+Tree map.
     */
    public K firstKey() {
        Node node = this.getRoot();

        while (!node.isLeaf) {
            node = (Node) node.ref[0];
        }

        return node.key[0];
    } // firstKey

    public BpTreeMap<K, V>.Node firstKeyNode() {
        Node node = this.getRoot();

        while (!node.isLeaf) {
            node = (Node) node.ref[0];
        }

        return node;
    } // firstKey
    /********************************************************************************
     * Return the last (largest) key in the B+Tree map.
     *
     * @return the last key in the B+Tree map.
     */
    public K lastKey() {
        Node node = this.getRoot();

        while (!node.isLeaf) {
            node = (Node) node.ref[node.nKeys];
        }
        return node.key[node.nKeys - 1];
    } // lastKey

    /********************************************************************************
     * Return the portion of the B+Tree map where key less than toKey.
     *
     * @return the submap with keys in the range [firstKey, toKey)
     */
    public SortedMap<K, V> headMap(K toKey) {
        return subMap(firstKey(), toKey);
    } // headMap

    /********************************************************************************
     * Return the portion of the B+Tree map where fromKey less or equal than key.
     *
     * @return the submap with keys in the range [fromKey, lastKey]
     */
    public SortedMap<K, V> tailMap(K fromKey) {
        SortedMap<K, V> tailMap = subMap(fromKey, lastKey());
        if (fromKey.compareTo(lastKey()) <= 0) {
            tailMap.put(lastKey(), get(lastKey()));
        }

        return tailMap;
    } // tailMap

    /********************************************************************************
     * Return the portion of the B+Tree map whose keys are between fromKey and toKey,
     * i.e., fromKey less or equal than key less than toKey.
     *
     * @return the submap with keys in the range [fromKey, toKey)
     */
    public SortedMap<K, V> subMap(K fromKey, K toKey) {
        SortedMap<K, V> subMap = new TreeMap<>();

        Node node = firstKeyNode();

        //left most bottom

        do {
            for (int i = 0; i < node.nKeys; i++) {
                if (node.key[i].compareTo(fromKey) >= 0 && node.key[i].compareTo(toKey) < 0) {
                    subMap.put(node.key[i], (V) node.ref[i]);
                }
            } // for

            // traverse across the leaves
            node = (Node) node.ref[node.nKeys];
        } while (node != null);


        return subMap;
    } // subMap

    /********************************************************************************
     * Return the size (number of keys) in the B+Tree.
     *
     * @return the size of the B+Tree
     */
    public int size() {
        int sum = 0;
        Node node = firstKeyNode();
        do {
            // add number of keys in current leaf to sum
            sum += node.nKeys;
            // traverse across the leaf pointers
            node = (Node) node.ref[node.nKeys];

        } while (node != null);

        return sum;
    } // size

    /********************************************************************************
     * Print the B+Tree using a pre-order traveral and indenting each level.
     *
     * @param n     the current node to print
     * @param level the current level of the B+Tree
     */
    @SuppressWarnings("unchecked")
    public void print(Node n, int level) {
        out.println("BpTreeMap");
        out.println("-------------------------------------------");

        for (int j = 0; j < level; j++) out.print("\t");
        out.print("[ . ");
        for (int i = 0; i < n.nKeys; i++) out.print(n.key[i] + " . ");
        out.println("]");
        if (!n.isLeaf) {
            for (int i = 0; i <= n.nKeys; i++) print((Node) n.ref[i], level + 1);
        } // if

        out.println("-------------------------------------------");
    } // print

    /********************************************************************************
     * Recursive helper function for finding a key in B+trees.
     *
     * @param key the key to find
     * @param n   the current node
     */
    @SuppressWarnings("unchecked")
    private V find(K key, Node n) {
        setCount(getCount() + 1);
        for (int i = 0; i < n.nKeys; i++) {
            K k_i = n.key [i];
            if ((key.compareTo (k_i) == 0) && (n.isLeaf)) {
                return (key.equals (k_i)) ? (V) n.ref [i] : null;
            } else if (key.compareTo (k_i) < 0 && (!n.isLeaf)) {
                return find (key, (Node) n.ref [i]);
            } // if
        } // for
        return (n.isLeaf) ? null : find (key, (Node) n.ref [n.nKeys]);
    } // find

    /********************************************************************************
     * Recursive helper function for inserting a key in B+trees.
     *
     * @param key the key to insert
     * @param ref the value/node to insert
     * @param n   the current node
     * @param p   the parent node
     */
    private void insert (K key, V ref, Node n, Node p)
    {
        // initial insert
        if ((n.nKeys == 0) && (n.isLeaf)) {
            n.key[0] = key;
            n.ref[0] = ref;
            n.nKeys++;
            return;
        } // if

        //use to store the parent ref/pointer
        ArrayList<Object> parent = new ArrayList<>();
        int index = 0;
        parent.add(index, p);
        index++;
        // find the leaf node to insert into
        while (!n.isLeaf) {
            boolean found = false;
            parent.add(index, n);
            index++;
            for (int i = 0; i < n.nKeys; i++) {
                if (key.compareTo(n.key[i]) < 0) {
                    n = (Node) n.ref[i];
                    found = true;
                    break;
                } else if (key.equals(n.key[i])) {
                    out.println("BpTreeMap:insert: attempt to insert duplicate key = " + key);
                    return;
                } // if           
            } // for
            if (!found) {
                n = (Node) n.ref[n.nKeys];
            } // if                      
        } // while

        // make sure it's not a duplicate key
        for (int i = 0; i < n.nKeys; i++) {

            if (key.equals(n.key[i])) {
                out.println("BpTreeMap:insert: attempt to insert duplicate key = " + key);
                return;
            } // if

        } // for

        // insert if there's room in the node
        if (n.nKeys < (ORDER - 1)) {
            for (int i = 0; i < n.nKeys; i++) {
                K k_i = n.key [i];
                if (key.compareTo (k_i) < 0) {
                    wedge (key, ref, n, i);
                    return;
                } // if
            } // for
            wedge (key, ref, n, n.nKeys);
            return;
        } // if

        // split the node and pass the key up until
        // there's room for it or a new node is created
        boolean finished = false;

        while (!finished) {

            // get parent
            index--;
            p = (Node) parent.get(index);

            // split the node
            Node sib = split (key, ref, n);

            // if current node is the root
            if (p == null) {

                Node newRoot = new Node(false);

                newRoot.ref[0] = (Node) n;
                newRoot.ref[1] = (Node) sib;

                // copy split value to root
                newRoot.key[0] = sib.key[0];
                newRoot.nKeys = 1;

                // remove redundant keys
                if (!sib.isLeaf) {

                    for (int i = 0; i < sib.nKeys-1; i++) {
                        sib.ref[i] = sib.ref[i+1];
                        sib.key[i] = sib.key[i+1];
                    } // for

                    sib.ref[sib.nKeys-1] =  sib.ref[sib.nKeys];

                    sib.nKeys--;
                } // if

                // reassign root
                setRoot(newRoot);
                finished = true;

            } else if (p.nKeys < ORDER - 1) {
                // if the parent has room, the key is wedged in at position pos
                int pos = 0;
                while ((pos < p.nKeys) && (sib.key[0].compareTo(p.key[pos]) > 0)) { pos++; }
                wedge (sib.key[0], (V) sib, p, pos);

                // remove redundant keys
                if (!sib.isLeaf) {
                    for (int i = 0; i < sib.nKeys-1; i++) {
                        sib.ref[i] = sib.ref[i+1];
                        sib.key[i] = sib.key[i+1];
                    }

                    sib.ref[sib.nKeys-1] =  sib.ref[sib.nKeys];

                    sib.nKeys--;

                }

                finished = true;

            } else {
                // if the parent has no room, the key gets passed up and the parent gets split
                key = sib.key[0];
                n = p;
                ref = (V) sib;

                // remove redundant keys
                if (!sib.isLeaf) {
                    for (int i = 0; i < sib.nKeys-1; i++) {
                        sib.ref[i] = sib.ref[i+1];
                        sib.key[i] = sib.key[i+1];
                    } // for

                    sib.ref[sib.nKeys-1] =  sib.ref[sib.nKeys];

                    sib.nKeys--;

                } // if 

            } // if else
        } // while
    } // insert

    /********************************************************************************
     * Wedge the key-ref pair into node n.
     * @param key  the key to insert
     * @param ref  the value/node to insert
     * @param n    the current node
     * @param i    the insertion position within node n
     */
    private void wedge (K key, V ref, Node n, int i)
    {
        for (int j = n.nKeys; j > i; j--) {
            n.key [j] = n.key [j - 1];
            if (!n.isLeaf) { n.ref [j + 1] = n.ref [j]; }
            else { n.ref [j] = n.ref [j - 1]; }
        } // for
        n.key [i] = key;
        if (!n.isLeaf) { n.ref [i + 1] = ref; }
        else { n.ref [i] = ref; }
        n.nKeys++;
    } // wedge

    /********************************************************************************
     * Split node n and return the newly created node.
     * @param key  the key to insert
     * @param ref  the value/node to insert
     * @param n    the current node
     */
    private Node split (K key, V ref, Node n)
    {
        // find middle key in n
        int numKeys = n.nKeys;
        int mid = numKeys / 2;

        // find position to insert key
        int pos = 0;
        while ((pos < numKeys) && (key.compareTo(n.key[pos]) > 0)) { pos++; }

        // create new sibling node
        Node sib = new Node(n.isLeaf);

        // transfer keys and values to sibling node
        for (int i = mid; i < numKeys; i++)
        {
            sib.key[i-mid] = n.key[i];
            //n.key[i] = null;

            if (!n.isLeaf) {
                sib.ref[i-mid+1] = n.ref[i+1];
            } else {
                sib.ref[i-mid] = n.ref[i];

            } // if else

            sib.nKeys++;
            n.nKeys--;
        } // for


        // insert key in appropriate node
        if (pos >= mid)
        {
            pos -= mid;
            wedge (key, ref, sib, pos);
        }
        else
        {
            wedge (key, ref, n, pos);
        } // if else

        // if the nodes are leaves, assign sibling pointers
        if (sib.isLeaf)
        {
            // new node points to right sibling (if it exists)
            sib.ref[sib.nKeys] = n.ref[numKeys];

            // current node points to new sibling
            n.ref[n.nKeys] = sib;
        } // if

        return sib;
    } // split

    /********************************************************************************
     * The main method used for testing.
     *
     * @param args the command-line arguments (args [0] gives number of keys to insert)
     */
    public static void main(String[] args) {
        BpTreeMap<Integer, Integer> bpt = new BpTreeMap<>(Integer.class, Integer.class);
        int totKeys = 10;
        if (args.length == 1) totKeys = Integer.valueOf(args[0]);

        for (int i = 1; i < totKeys; i++) {
            bpt.put(i, i * i);
            bpt.print(bpt.getRoot(), 0);
        } // for

        for (int i = 1; i < totKeys; i++) {
            out.println("key = " + i + " value = " + bpt.get(i));
        } // for

        out.println("-------------------------------------------");
        out.println("Average number of nodes accessed = " + bpt.getCount() / (double) totKeys);
       

    } // main

	public Node getRoot() {
		return root;
	}

	public void setRoot(Node root) {
		this.root = root;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

} // BpTreeMap class