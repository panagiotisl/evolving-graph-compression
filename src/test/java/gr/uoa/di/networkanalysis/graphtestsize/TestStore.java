package gr.uoa.di.networkanalysis.graphtestsize;


import org.junit.Test;

import gr.uoa.di.networkanalysis.EvolvingMultiGraph;

public class TestStore {

	// Flickr
	private static final String graphFile =  "out.flickr-growth-sorted.gz";
	private static final String basename =  "flickr";
	private static final boolean headers = true;
	private static final int k = 2;
	private static int aggregation = 24*60*60;

	// Wiki
//	private static final String graphFile =  "out.edit-enwiki.sorted.gz";
//	private static final String basename =  "wiki";
//	private static final boolean headers = true;
//	private static final int k = 2;
//	private static int aggregation = 60*60;

	// Yahoo
//	private static final String graphFile =  "yahoo-G5-sorted.tsv.gz";
//	private static final String basename =  "yahoo";
//	private static final boolean headers = false;
//	private static final int k = 2;
//	private static int aggregation = 15*60;

	// cbtComm
//	private static final String graphFile =  "cbtComm-sorted.txt.gz";
//	private static final String basename =  "cbtComm";
//	private static final boolean headers = false;
//	private static final int k = 2;
//	private static int aggregation = 1;

	// cbtPow
//	private static final String graphFile =  "cbtPow-sorted.txt.gz";
//	private static final String basename =  "cbtPow";
//	private static final boolean headers = false;
//	private static final int k = 2;
//	private static int aggregation = 1;

	
	@Test
	public void testStore() throws Exception {
		
		EvolvingMultiGraph emg = new EvolvingMultiGraph(
				graphFile,
				headers,
				k,
				basename,
				aggregation
		);
		
		long t1 = System.nanoTime();
		emg.store();
		long t2 = System.nanoTime();
		System.out.println("Compression took: " + (t2-t1) + " nanoseconds");
	}
}
