/************************************************************************************
 * @file LinHashMap.java
 * // TODO 
 * set(); put() first
 * // Jun24th 2015 SZ first Version
 * 
 *
 * @author  John Miller
 */

import java.io.*;
import java.lang.reflect.Array;
import static java.lang.System.out;
import java.util.*;

/************************************************************************************
 * This class provides hash maps that use the Linear Hashing algorithm.
 * A hash table is created that is an array of buckets.
 */
/**
 * @author esc
 *
 * @param <K>
 * @param <V>
 */
public class LinHashMap <K, V>
       extends AbstractMap <K, V>
       implements Serializable, Cloneable, Map <K, V>
{
    /** The number of slots (for key-value pairs) per bucket.
    /** The number of slots (for key-value pairs) per 
     */
    private static final int SLOTS = 4;

    /** The class for type K.
     */
    private final Class <K> classK;

    /** The class for type V.
     */
    private final Class <V> classV;

    /********************************************************************************
     * This inner class defines buckets that are stored in the hash table.
     */
    private class Bucket
    {
        int    nKeys;
        K []   key;
        V []   value;
        Bucket next; //reference to 
        @SuppressWarnings("unchecked")
                // Constructor ~~~~
        Bucket (Bucket n)
        {
            nKeys = 0;
            key   = (K []) Array.newInstance (classK, SLOTS);
            value = (V []) Array.newInstance (classV, SLOTS);
            next  = n;
        } // constructor
    } // Bucket inner class

    /** The list of buckets making up the hash table.
     */
    private final List <Bucket> hTable;

    /** The modulus for low resolution hashing
     */
    private int mod1;

    /** The modulus for high resolution hashing
     */
    private int mod2;

    /** Counter for the number buckets accessed (for performance testing).
     */
    private int count = 0;

    /** The index of the next bucket to split.
     */
    private int split = 0;

    /********************************************************************************
     * Construct a hash table that uses Linear Hashing.
     * @param classK    the class for keys (K)
     * @param classV    the class for keys (V)
     * @param initSize  the initial number of home buckets (a power of 2, e.g., 4)
     */
    public LinHashMap (Class <K> _classK, Class <V> _classV, int initSize)
    {
        classK = _classK;
        classV = _classV;
        hTable = new ArrayList <> ();
        mod1   = initSize;
        mod2   = 2 * mod1;
        for (int i = 0; i < initSize; i++){ //initialize empty buckets
            hTable.add(new Bucket(null));
        }
    } // constructor

    /********************************************************************************
     * Return a set containing all the entries as pairs of keys and values.
     * @return  the set view of the map
     */
    public Set <Map.Entry <K, V>> entrySet ()
    {
        Set <Map.Entry <K, V>> enSet = new HashSet <> (); //return set of (K,V)

        //  T O   B E   I M P L E M E N T E D
        for (Bucket b : hTable) {
            for (int j=0; j < b.nKeys; j++){
                enSet.add (new AbstractMap.SimpleEntry <> (b.key[j], b.value[j]));
            }
            while (b.next != null){
                b = b.next;
                for (int j=0; j < b.nKeys; j++){
                    enSet.add (new AbstractMap.SimpleEntry <> (b.key[j], b.value[j]));
                }
            }
        }
            
        return enSet;
    } // entrySet

    /********************************************************************************
     * Given the key, look up the value in the hash table.
     * @param key  the key used for look up
     * @return  the value associated with the key
     */
    public V get (Object key) // input Key --> (lookup)--> Value
    {
        int i = h (key);

        //  T O   B E   I M P L E M E N T E D
        if (i < split){
            i = h2(key);
        }
        for (Bucket b = hTable.get(i); b != null; b = b.next) {
            setCount(getCount() + 1);
            for (int j = 0; j < b.nKeys; j++){
                if (key.equals(b.key[j])){
                    return b.value[j];
                }
            }
        }

        return null;
    } // get

    /********************************************************************************
     * Put the key-value pair in the hash table.
     * @param key    the key to insert
     * @param value  the value to insert
     * @return  null (not the previous value)
     */
    public V put (K key, V value)
    {
        int i = h (key);
        
        //  T O   B E   I M P L E M E N T E D
        if (i < split){
            i = h2(key);
        }
        
        Bucket b = hTable.get(i);
        if (b.nKeys < SLOTS){
            b.key[b.nKeys] = key;
            b.value[b.nKeys] = value;
            b.nKeys++;
        }
        else {
            hTable.add(new Bucket(null));
            while(b.next != null){
                b = b.next;
            }
            if (b.nKeys < SLOTS){
                b.key[b.nKeys] = key;
                b.value[b.nKeys] = value;
                b.nKeys++;
            }
            else {
                
                Bucket a = new Bucket(null);
                //add overflow chain
                a.key[0] = key;
                a.value[0] = value;
                a.nKeys++;
                b.next = a;
            }
            //split the bucket
            Bucket bSplit=hTable.get(split);
            ArrayList<K> keysCopy = new ArrayList<>();
            ArrayList<V> valuesCopy = new ArrayList<>();
                
            //store all the key/value of the splitted buckets in lists
            for (; bSplit!=null; bSplit=bSplit.next){
                for(int k=0; k<bSplit.nKeys;k++){
                    keysCopy.add(bSplit.key[k]);
                    valuesCopy.add(bSplit.value[k]);
                }
            }
            hTable.set(split, new Bucket(null));
            
            split++;

            for (int j=0; j<keysCopy.size(); j++){
                put(keysCopy.get(j), valuesCopy.get(j));
            }
            
            if (split == mod1) {
                split = 0;
                mod1 = mod2;
                mod2 = 2*mod1;
            }
        }
        return null;
    } // put

    /********************************************************************************
     * Return the size (SLOTS * number of home buckets) of the hash table. 
     * @return  the size of the hash table
     */
    public int size ()
    {
        return SLOTS * (mod1 + split);
    } // size

    /********************************************************************************
     * Print the hash table.
     */
    public void print ()
    {
        out.println ("Hash Table (Linear Hashing)");
        out.println ("-------------------------------------------");

        //  T O   B E   I M P L E M E N T E D
        for (int i = 0; i < hTable.size(); i++) {
            out.print (i + ":\t");
            boolean isFirstBucket = true;
            for (Bucket b = hTable.get(i); b != null; b = b.next) {
                if (!isFirstBucket) 
                	out.print ("-->");
				out.print("[");
                for (int j = 0; j < b.nKeys; j++){
                    out.print (" (" + b.key[j] +","+ b.value[j] + ") ");
                }
				out.print("]");
                isFirstBucket = false;
            } // for
            out.println();
        } // for

        out.println ("-------------------------------------------");
    } // print

    /********************************************************************************
     * Hash the key using the low resolution hash function.
     * @param key  the key to hash
     * @return  the location of the bucket chain containing the key-value pair
     */
    private int h (Object key)
    {
        return key.hashCode () % mod1;
    } // h

    /********************************************************************************
     * Hash the key using the high resolution hash function.
     * @param key  the key to hash
     * @return  the location of the bucket chain containing the key-value pair
     */
    private int h2 (Object key)
    {
        return key.hashCode () % mod2;
    } // h2

    /********************************************************************************
     * The main method used for testing.
     * @param  the command-line arguments (args [0] gives number of keys to insert)
     */
    public static void main (String [] args)
    {
        LinHashMap <Integer, Integer> ht = new LinHashMap <> (Integer.class, Integer.class, 11);
        int nKeys = 30;
        if (args.length == 1) nKeys = Integer.valueOf (args [0]);
        for (int i = 1; i < nKeys; i += 2) ht.put (i, i * i);
        //for (int i = 0; i < nKeys; i ++) ht.put (2, 2*i);
        ht.print ();
        for (int i = 0; i < nKeys; i++) {
            out.println ("key = " + i + " value = " + ht.get (i));
        } // for
        out.println ("-------------------------------------------");
        out.println ("Average number of buckets accessed = " + ht.getCount() / (double) nKeys);
    } // main

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

} // LinHashMap class
