import static java.lang.System.out;

public class TestIndexing {
	public static void main(String[] args) {

		testBPlusTree(args);
        
		testLinHashMap(args);

		testExtHash(args);
	}

	private static void testExtHash(String[] args) {
		// test ext hash
		// test put and print and get
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
		// test set
		ht.entrySet();
	}

	private static void testLinHashMap(String[] args) {
		// test lin hash
		//test put and print
        LinHashMap <Integer, Integer> ht = new LinHashMap <> (Integer.class, Integer.class, 11);
        int nKeys = 30;
        if (args.length == 1) nKeys = Integer.valueOf (args [0]);
        for (int i = 1; i < nKeys; i += 2){
        	ht.put (i, i * i);
        	ht.print();
        }
        //for (int i = 0; i < nKeys; i ++) ht.put (2, 2*i);
        ht.print ();
        for (int i = 0; i < nKeys; i++) {
            out.println ("key = " + i + " value = " + ht.get (i));
        } // for
        out.println ("-------------------------------------------");
        out.println ("Average number of buckets accessed = " + ht.getCount() / (double) nKeys);
		// test set
        ht.entrySet();
		// test get
        System.out.println(ht.get(1));
        System.out.println(ht.get(3));
        System.out.println(ht.get(4));
	}

	private static void testBPlusTree(String[] args) {
		// test b+tree
		//tests put/insert and split
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
		// test entry set
        System.out.println("testing entry set");
        bpt.entrySet();
		// test first key
        System.out.println("testing first key");
        bpt.firstKey();
		// test last key
        System.out.println("testing last key");
        bpt.lastKey();
		// test head map/ submap
        System.out.println("testing headmap/submap");
        bpt.headMap(1);
		// test tail map
        System.out.println("testing tailmap");
        bpt.tailMap(1);
		// test size
        System.out.println("Size: " + bpt.size());
	}
}
