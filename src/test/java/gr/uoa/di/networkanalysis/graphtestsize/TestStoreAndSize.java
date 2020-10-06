package gr.uoa.di.networkanalysis.graphtestsize;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import org.junit.Test;

import com.sun.net.httpserver.Headers;

import gr.uoa.di.networkanalysis.EvolvingMultiGraph;

public class TestStoreAndSize {

	// Flickr
//	private static final String graphFile =  "out.flickr-growth-sorted.gz";
//	private static final String basename =  "flickr";
//	private static final boolean headers = true;
//	private static final long factor = 1;
//	private static final int k = 2;
//	private static int[] aggregations = new int[]{1, 24*60*60, 60, 30*60, 60*60, 4*60*60, 2*24*60*60};
//	private static String[] aggregationsStr = new String[]{"1", "24*60*60", "60", "30*60", "60*60", "4*60*60", "2*24*60*60"};

	// Wiki
//	private static final String graphFile =  "out.edit-enwiki.sorted.gz";
//	private static final String basename =  "wiki";
//	private static final boolean headers = true;
//	private static final long factor = 1;
//	private static final int k = 2;
//	private static int[] aggregations = new int[]{1, 24*60*60, 60, 30*60, 60*60, 4*60*60, 2*24*60*60};
//	private static String[] aggregationsStr = new String[]{"1", "24*60*60", "60", "30*60", "60*60", "4*60*60", "2*24*60*60"};

	// Yahoo
//	private static final String graphFile =  "yahoo-G5-sorted.tsv.gz";
//	private static final String basename =  "flickr";
//	private static final boolean headers = false;
//	private static final long factor = 1;
//	private static final int k = 2;
//	private static int[] aggregations = new int[]{1, 15*60, 24*60*60, 60, 30*60, 60*60, 4*60*60, 2*24*60*60};
//	private static String[] aggregationsStr = new String[]{"1", "15*60", "24*60*60", "60", "30*60", "60*60", "4*60*60", "2*24*60*60"};

	// cbtComm
//	private static final String graphFile =  "cbtComm-sorted.txt.gz";
//	private static final String basename =  "cbtComm";
//	private static final boolean headers = false;
//	private static final long factor = 1;
//	private static final int k = 2;
//	private static int[] aggregations = new int[]{1};
//	private static String[] aggregationsStr = new String[]{"1"};

	// cbtPow
	private static final String graphFile =  "cbtPow-sorted.txt.gz";
	private static final String basename =  "cbtPow";
	private static final boolean headers = false;
	private static final long factor = 1;
	private static final int k = 2;
	private static int[] aggregations = new int[]{1};
	private static String[] aggregationsStr = new String[]{"1"};

//	@Test
	public void testStore() throws Exception {
		EvolvingMultiGraph emg = new EvolvingMultiGraph(
				graphFile,
				headers,
				k,
				basename,
				factor
		);

		emg.store();
	}
	
	@Test
	public void sizesForKAndAggregations() throws Exception {
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(basename+"-results.txt"));
		
		for(int k = 2; k < 8; k++) {
			for(int j = 0; j < aggregations.length; j++) {
				
				EvolvingMultiGraph emg = new EvolvingMultiGraph(
						graphFile,
						headers,
						k,
						basename,
						aggregations[j]
				);
				
				emg.storeTimestampsAndIndex();
				
				writer.append(String.format("k: %d, aggregation: %s, timestamps: %d, index: %d", k, aggregationsStr[j], new File(basename+".timestamps").length(), new File(basename+".efindex").length()));
				writer.newLine();
				writer.flush();
			}
		}
		
		writer.close();
	}
}
