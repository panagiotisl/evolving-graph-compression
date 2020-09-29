package gr.uoa.di.networkanalysis.graphtests;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;

import org.junit.Assert;
import org.junit.Test;

import gr.uoa.di.networkanalysis.EvolvingMultiGraph;
import gr.uoa.di.networkanalysis.Successor;
import gr.uoa.di.networkanalysis.TimestampComparer;
import gr.uoa.di.networkanalysis.TimestampComparerAggregator;
import gr.uoa.di.networkanalysis.EvolvingMultiGraph.SuccessorIterator;

public class YahooTest {

	private static TimestampComparer ic = new TimestampComparerAggregator(1);

	@Test
	public void testAll() throws Exception {
		testStore();
		testLoadAndSuccesors();
	}
	
	public void testStore() throws Exception {
		EvolvingMultiGraph emg = new EvolvingMultiGraph(
				"yahoo-G5-sorted.tsv.gz",
				false,
				2,
				"yahoo",
				ic
		);

		emg.store();
	}

	public void testLoadAndSuccesors() throws Exception {

		EvolvingMultiGraph emg = new EvolvingMultiGraph(
				"yahoo-G5-sorted.tsv.gz",
				false,
				2,
				"yahoo",
				ic
		);

		emg.load();

		FileInputStream fileStream = new FileInputStream("yahoo-G5-sorted.tsv.gz");
		GZIPInputStream gzipStream = new GZIPInputStream(fileStream);
		InputStreamReader decoder = new InputStreamReader(gzipStream, "UTF-8");
		BufferedReader buffered = new BufferedReader(decoder);

		int current = 1;
		String line = "";//buffered.readLine(); // Get rid of headers
		ArrayList<Successor> list = new ArrayList<Successor>();

		while ((line = buffered.readLine()) != null) {
			String[] tokens = line.split("\\s+");
			int node = Integer.parseInt(tokens[0]);
			int neighbor = Integer.parseInt(tokens[1]);
			long timestamp = Long.parseLong(tokens[3]);

			if(node == current) {
				list.add(new Successor(neighbor, timestamp));
			}
			else {
				// Check the list so far
				SuccessorIterator it = emg.successors(current);
				int i = 0;
				while(true) {
					try {
						Successor s = it.next();
						Assert.assertEquals(s.getTimestamp(), list.get(i).getTimestamp());
						i++;
					}
					catch(NoSuchElementException e) {
						break;
					}
				}

				list = new ArrayList<Successor>();
				list.add(new Successor(neighbor, timestamp));
				current = node;
			}
		}

		SuccessorIterator it = emg.successors(current);
		int i = 0;
		while(true) {
			try {
				Successor s = it.next();
				Assert.assertEquals(s.getNeighbor(), list.get(i).getNeighbor());
				Assert.assertEquals(s.getTimestamp(), list.get(i).getTimestamp(), 86400);
				i++;
			}
			catch(NoSuchElementException e) {
				break;
			}
		}
		
		buffered.close();

	}
}
