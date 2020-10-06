package gr.uoa.di.networkanalysis.graphtestsize;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;

import org.junit.Assert;
import org.junit.Test;

import gr.uoa.di.networkanalysis.EvolvingMultiGraph;
import gr.uoa.di.networkanalysis.EvolvingMultiGraph.SuccessorIterator;
import gr.uoa.di.networkanalysis.TimestampComparer;
import gr.uoa.di.networkanalysis.TimestampComparerAggregator;
import gr.uoa.di.networkanalysis.Successor;

public class FlickrTest {

	private static final int factor = 1;
	
	private static TimestampComparer ic = new TimestampComparerAggregator(factor);
	
//	@Test
	public void testAll() throws Exception {
		testStore();
		testLoadAndSuccesors();
	}
	
	@Test
	public void testStore() throws Exception {
		EvolvingMultiGraph emg = new EvolvingMultiGraph(
				"out.flickr-growth-sorted.gz",
				true,
				2,
				"flickr",
				ic
		);

		emg.store();
	}
	
//	@Test
	public void testLoadAndSuccesors() throws Exception {
		
		int[] aggregations = new int[]{1, 15*60, 24*60*60, 60, 30*60, 60*60, 4*60*60, 2*24*60*60};
		String[] aggregationsStr = new String[]{"1", "15*60", "24*60*60", "60", "30*60", "60*60", "4*60*60", "2*24*60*60"};
		
		BufferedWriter writer = new BufferedWriter(new FileWriter("flickr-results.txt"));
		
		for(int k = 2; k < 8; k++) {
			for(int j = 0; j < aggregations.length; j++) {
				
				EvolvingMultiGraph emg = new EvolvingMultiGraph(
						"out.flickr-growth-sorted.gz",
						true,
						k,
						"flickr",
						new TimestampComparerAggregator(aggregations[j])
				);
				
				emg.storeTimestampsAndIndex();
				
				writer.append(String.format("k: %d, aggregation: %s, timestamps: %d, index: %d", k, aggregationsStr[j], new File("flickr.timestamps").length(), new File("flickr.efindex").length()));
				writer.newLine();
				writer.flush();
			}
		}
		
		writer.close();
		
//		emg.load();
//		
//		FileInputStream fileStream = new FileInputStream("out.flickr-growth-sorted.gz");
//        GZIPInputStream gzipStream = new GZIPInputStream(fileStream);
//        InputStreamReader decoder = new InputStreamReader(gzipStream, "UTF-8");
//        BufferedReader buffered = new BufferedReader(decoder);
//		
//        int current = 1;
//        String line = buffered.readLine(); // Get rid of headers
//        ArrayList<Successor> list = new ArrayList<Successor>();
//        
//        while ((line = buffered.readLine()) != null) {
//        	String[] tokens = line.split("\\s+");
//        	int node = Integer.parseInt(tokens[0]);
//            int neighbor = Integer.parseInt(tokens[1]);
//            long timestamp = Long.parseLong(tokens[3]);
//            
//            if(node == current) {
//            	list.add(new Successor(neighbor, timestamp));
//            }
//            else {
//            	// Check the list so far
//            	SuccessorIterator it = emg.successors(current);
//            	int i = 0;
//        		while(true) {
//        			try {
//        				Successor s = it.next();
//        				Assert.assertEquals((double) s.getTimestamp(), (double) list.get(i).getTimestamp(), factor);
//        				i++;
//        			}
//        			catch(NoSuchElementException e) {
//        				break;
//        			}
//        		}
//            	
//            	list = new ArrayList<Successor>();
//            	list.add(new Successor(neighbor, timestamp));
//            	current = node;
//            }
//        }
//        
//        SuccessorIterator it = emg.successors(current);
//    	int i = 0;
//		while(true) {
//			try {
//				Successor s = it.next();
//				Assert.assertEquals((double) s.getTimestamp(), (double) list.get(i).getTimestamp(), factor);
//				i++;
//			}
//			catch(NoSuchElementException e) {
//				break;
//			}
//		}
//        
//		buffered.close();
	}
}
