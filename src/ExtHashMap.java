/************************************************************************************
 * @file ExtHashMap.java
 *
 * @author  John Miller
 */

import java.io.*;
import java.lang.reflect.Array;
import static java.lang.System.out;
import java.util.*;

/************************************************************************************
 * This class provides hash maps that use the Extendable Hashing algorithm.
 * Buckets are allocated and stored in a hash table and are referenced using
 * directory dir.
 */
/**
 * @author esc
 *
 * @param <K>
 * @param <V>
 */
public class ExtHashMap<K, V> extends AbstractMap<K, V> implements
		Serializable, Cloneable, Map<K, V> {
	/**
	 * The number of slots (for key-value pairs) per bucket.
	 */
	private static final int SLOTS = 4;

	/**
	 * The class for type K.
	 */
	private final Class<K> classK;

	/**
	 * The class for type V.
	 */
	private final Class<V> classV;

	/********************************************************************************
	 * This inner class defines buckets that are stored in the hash table.
	 */
	private class Bucket {
		int nKeys;
		int nSplit;
		K[] key;
		V[] value;

		@SuppressWarnings("unchecked")
		Bucket() {
			nKeys = 0;
			nSplit = 0;
			key = (K[]) Array.newInstance(classK, SLOTS);
			value = (V[]) Array.newInstance(classV, SLOTS);
		} // constructor
	} // Bucket inner class

	/**
	 * The hash table storing the buckets (buckets in physical order)
	 */
	private final List<Bucket> hTable;

	/**
	 * The directory providing access paths to the buckets (buckets in logical
	 * oder)
	 */
	private final List<Bucket> dir;

	/**
	 * The modulus for hashing (= 2^D) where D is the global depth
	 */
	private int mod;

	/**
	 * The number of buckets
	 */
	private int nBuckets;

	/**
	 * Counter for the number buckets accessed (for performance testing).
	 */
	private int count = 0;
	/**
	 * the initial modulus value
	 */
	private int intiMod;
	/********************************************************************************
	 * Construct a hash table that uses Extendable Hashing.
	 * 
	 * @param classK
	 *            the class for keys (K)
	 * @param classV
	 *            the class for keys (V)
	 * @param initSize
	 *            the initial number of buckets (a power of 2, e.g., 4)
	 */
	public ExtHashMap(Class<K> _classK, Class<V> _classV, int initSize) {
		classK = _classK;
		classV = _classV;
		hTable = new ArrayList<>(); // for bucket storage
		dir = new ArrayList<>(); // for bucket access
		nBuckets = initSize;
		mod = (int) Math.pow(2, Math.ceil(Math.log(initSize) / Math.log(2)));
		intiMod = (int) Math.pow(2,Math.ceil(Math.log(initSize) / Math.log(2)));				
		for (int i = 0; i < mod; i++) {
			dir.add(new Bucket());
		}
		for (int i = 0; i < nBuckets; i++) { // initialize empty buckets
			hTable.add(new Bucket());
			dir.set(i, hTable.get(i));
		}

	} // constructor

	/********************************************************************************
	 * Return a set containing all the entries as pairs of keys and values.
	 * 
	 * @return the set view of the map
	 */
	public Set<Map.Entry<K, V>> entrySet() {
		Set<Map.Entry<K, V>> enSet = new HashSet<>();
		// T O B E I M P L E M E N T E D
		// go through hash table
		for (int i = 0; i < dir.size(); i++) {
			Bucket b = dir.get(i);
			// go through key in each bucket
			for (int j = 0; j < b.nKeys; j++) {
				enSet.add(new AbstractMap.SimpleEntry<>(b.key[j], b.value[j]));
			}// for
		}// for
		return enSet;
	} // entrySet

	/********************************************************************************
	 * Given the key, look up the value in the hash table.
	 * 
	 * @param key
	 *            the key used for look up
	 * @return the value associated with the key
	 */
	public V get(Object key) {
		setCount(getCount() + 1);
		int i = h(key);
		Bucket b = dir.get(i);
		// T O B E I M P L E M E N T E D
		// go through the bucket to find the value
		for (int j = 0; j < b.nKeys; j++) {
			if (key.equals(b.key[j])) {
				return b.value[j];
			}
		}// for
		return null;
	} // get

	/********************************************************************************
	 * Put the key-value pair in the hash table.
	 * 
	 * @param key
	 *            the key to insert
	 * @param value
	 *            the value to insert
	 * @return null (not the previous value)
	 */
	public V put(K key, V value) {
		int i = h(key);
		Bucket b = dir.get(i);
		// T O B E I M P L E M E N T E D

		// check if the bucket is overflow
		if (b.nKeys < SLOTS) {
			b.key[b.nKeys] = key;
			b.value[b.nKeys] = value;
			b.nKeys++;
		} else {
			// split bucket b
			Bucket b1 = new Bucket();
			Bucket b2 = new Bucket();
			// put new thing in b2
			b2.key[b2.nKeys] = key;
			b2.value[b2.nKeys] = value;
			b2.nKeys++;
			// check if it is supposed to double directory
			if (b.nSplit >= (int) ((Math.log(mod) / Math.log(2)) - (Math.log(intiMod) / Math.log(2)))) {					
				mod = mod * 2;
				// doubling directory
				for (int j = mod / 2; j < mod; j++) {
					dir.add(new Bucket());
					dir.set(j, dir.get(j % (mod / 2)));
				}// for
			}// if
			// for loop
			// rehash where to rearrange value
			for (int k = 0; k < b.nKeys; k++) {
				i = h(b.key[k]);
				// see whether the values in old buckets should be moved to new
				// buckets.
				// if so, move and clear; if not, keep.
				if (h(key) == i) {
					// move to b2
					b2.key[b2.nKeys] = b.key[k];
					b2.value[b2.nKeys] = b.value[k];
					b2.nKeys++;
				} else {
					// move to b1
					b1.key[b1.nKeys] = b.key[k];
					b1.value[b1.nKeys] = b.value[k];
					b1.nKeys++;
				}// ifelse
			}// for
			// update bucket level
			b1.nSplit++;
			b2.nSplit++;
			// update hTable
			hTable.remove(b);
			hTable.add(b1);
			hTable.add(b2);
			// update the number of buckets
			nBuckets++;
			// update directory
			dir.set(h(b1.key[0]), b1);
			dir.set(h(key), b2);
		}// ifelse

		return null;
	} // put

	/********************************************************************************
	 * Return the size (SLOTS * number of buckets) of the hash table.
	 * 
	 * @return the size of the hash table
	 */
	public int size() {
		return SLOTS * nBuckets;
	} // size

	/********************************************************************************
	 * Print the hash table.
	 */
	public void print() {
		out.println("Hash Table (Extendable Hashing)");
		out.println("-------------------------------------------");

		// T O B E I M P L E M E N T E D
		for (int i = 0; i < dir.size(); i++) {
			out.print(i + ":\t" + "[");
			// check if it is the first value of bucket
			boolean isFirstValue = true;
			Bucket b = dir.get(i);
			for (int j = 0; j < b.nKeys; j++) {
				if (!isFirstValue)
					out.print("-->");// if
				out.print(" (" + b.key[j] + "," + b.value[j] + ") ");
				isFirstValue = false;
			}// for
			out.print("]");
			out.println();
		} // for

		out.println("-------------------------------------------");
	} // print

	/********************************************************************************
	 * Hash the key using the hash function.
	 * 
	 * @param key
	 *            the key to hash
	 * @return the location of the directory entry referencing the bucket
	 */
	private int h(Object key) {
		return key.hashCode() % mod;
	} // h

	/********************************************************************************
	 * The main method used for testing.
	 * 
	 * @param the
	 *            command-line arguments (args [0] gives number of keys to
	 *            insert)
	 */
	public static void main(String[] args) {
		ExtHashMap<Integer, Integer> ht = new ExtHashMap<>(Integer.class,
				Integer.class, 11);
		//change 30 to 100 to test the split
		//int nKeys = 30;
		int nKeys = 100;
		if (args.length == 1)
			nKeys = Integer.valueOf(args[0]);
		for (int i = 1; i < nKeys; i += 2)
			ht.put(i, i * i);
		ht.print();
		for (int i = 0; i < nKeys; i++) {
			out.println("key = " + i + " value = " + ht.get(i));
		} // for
		out.println("-------------------------------------------");
		out.println("Average number of buckets accessed = " + ht.getCount()
				/ (double) nKeys);
	} // main

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

} // ExtHashMap class

