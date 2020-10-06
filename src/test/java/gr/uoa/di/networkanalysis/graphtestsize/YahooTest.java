package gr.uoa.di.networkanalysis.graphtestsize;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.junit.Test;

import gr.uoa.di.networkanalysis.EvolvingMultiGraph;
import gr.uoa.di.networkanalysis.TimestampComparerAggregator;

public class YahooTest {

	private static final int factor = 1;

	public void testStore() throws Exception {
		EvolvingMultiGraph emg = new EvolvingMultiGraph(
				"yahoo-G5-sorted.tsv.gz",
				false,
				2,
				"yahoo",
				factor
		);

		emg.storeBVMultiGraph();
	}

	@Test
	public void testLoadAndSuccesors() throws Exception {

		int[] aggregations = new int[]{1, 15*60, 24*60*60, 60, 30*60, 60*60, 4*60*60, 2*24*60*60};
		String[] aggregationsStr = new String[]{"1", "15*60", "24*60*60", "60", "30*60", "60*60", "4*60*60", "2*24*60*60"};
		
		BufferedWriter writer = new BufferedWriter(new FileWriter("yahoo-results.txt"));
		
		for(int k = 2; k < 8; k++) {
			for(int j = 0; j < aggregations.length; j++) {
				
				EvolvingMultiGraph emg = new EvolvingMultiGraph(
						"yahoo-G5-sorted.tsv.gz",
						false,
						k,
						"yahoo",
						aggregations[j]
				);
				
				emg.storeTimestampsAndIndex();
				
				String out = String.format("k: %d, aggregation: %s, timestamps: %d, index: %d", k, aggregationsStr[j], new File("yahoo.timestamps").length(), new File("yahoo.efindex").length());
				System.out.println(out);
				writer.append(out);
				writer.newLine();
				writer.flush();
			}
		}
		
		writer.close();

//		emg.load();
//
//		FileInputStream fileStream = new FileInputStream("yahoo-G5-sorted.tsv.gz");
//		GZIPInputStream gzipStream = new GZIPInputStream(fileStream);
//		InputStreamReader decoder = new InputStreamReader(gzipStream, "UTF-8");
//		BufferedReader buffered = new BufferedReader(decoder);
//
//		int current = 1;
//		String line = "";//buffered.readLine(); // Get rid of headers
//		ArrayList<Successor> list = new ArrayList<Successor>();
//
//		while ((line = buffered.readLine()) != null) {
//			String[] tokens = line.split("\\s+");
//			int node = Integer.parseInt(tokens[0]);
//			int neighbor = Integer.parseInt(tokens[1]);
//			long timestamp = Long.parseLong(tokens[3]);
//
//			if(node == current) {
//				list.add(new Successor(neighbor, timestamp));
//			}
//			else {
//				// Check the list so far
//				SuccessorIterator it = emg.successors(current);
//				int i = 0;
//				while(true) {
//					try {
//						Successor s = it.next();
//						Assert.assertEquals(s.getTimestamp(), list.get(i).getTimestamp());
//						i++;
//					}
//					catch(NoSuchElementException e) {
//						break;
//					}
//				}
//
//				list = new ArrayList<Successor>();
//				list.add(new Successor(neighbor, timestamp));
//				current = node;
//			}
//		}
//
//		SuccessorIterator it = emg.successors(current);
//		int i = 0;
//		while(true) {
//			try {
//				Successor s = it.next();
//				Assert.assertEquals(s.getNeighbor(), list.get(i).getNeighbor());
//				Assert.assertEquals(s.getTimestamp(), list.get(i).getTimestamp(), 86400);
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
