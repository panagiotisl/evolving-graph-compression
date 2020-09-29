package gr.uoa.di.networkanalysis.graphtests;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;

import org.junit.Assert;
import org.junit.Test;

import gr.uoa.di.networkanalysis.EvolvingMultiGraph;
import gr.uoa.di.networkanalysis.Successor;
import gr.uoa.di.networkanalysis.TimestampComparer;
import gr.uoa.di.networkanalysis.EvolvingMultiGraph.SuccessorIterator;
import gr.uoa.di.networkanalysis.utils.EpochUtils;

public class WikiTest {

	private static String path = System.getProperty("user.dir");
	private static TimestampComparer ic = new TimestampComparer() {

		@Override
		public long timestampsDifference(long t1, long t2) {
			return (t2-t1)/86400;
		}

		@Override
		public long reverse(long previous, long difference) {
			// difference must be a multiple of what was returned in timestampsDifference
			return previous + 86400*difference;
		}
	};


//	@Test
//	public void testAll() throws Exception {
//		testStore();
//		testLoadAndSuccesors();
//	}
	
	public void testStore() throws Exception {
		EvolvingMultiGraph emg = new EvolvingMultiGraph(
				"out.edit-enwiki.sorted.gz",
				true,
				2,
				"wiki",
				ic
		);

		emg.store();
	}

	@Test
	public void testLoadAndSuccesors() throws Exception {

		EvolvingMultiGraph emg = new EvolvingMultiGraph(
				"out.edit-enwiki.sorted.gz",
				true,
				2,
				"wiki",
				ic
		);

		emg.load();

		FileInputStream fileStream = new FileInputStream("out.edit-enwiki.sorted.gz");
		GZIPInputStream gzipStream = new GZIPInputStream(fileStream);
		InputStreamReader decoder = new InputStreamReader(gzipStream, "UTF-8");
		BufferedReader buffered = new BufferedReader(decoder);

		int current = 1;
		String line = buffered.readLine(); // Get rid of headers
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
//						
						System.out.println(list.get(i).getTimestamp()+" - "+s.getTimestamp()+" - "+current+" - "+s.getNeighbor());
						Assert.assertEquals("not equal!", (double) s.getTimestamp(), (double)list.get(i).getTimestamp(), 100*86400);
//						Assert.assertEquals(s.getTimestamp(), list.get(i).getTimestamp()); COMMENT
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

	}
}
