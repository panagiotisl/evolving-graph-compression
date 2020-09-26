package gr.uoa.di.networkanalysis.graphtests;

import java.time.Duration;
import java.time.Instant;

import org.junit.Test;

import gr.uoa.di.networkanalysis.EvolvingMultiGraph;
import gr.uoa.di.networkanalysis.EvolvingMultiGraph.SuccessorIterator;
import gr.uoa.di.networkanalysis.TimestampComparer;
import gr.uoa.di.networkanalysis.Successor;

public class FlickrTest {

	private static String path = System.getProperty("user.dir");
	private static TimestampComparer ic = new TimestampComparer() {
		
		@Override
		public long timestampsDifference(long t1, long t2) {
			//return Duration.between(i1, i2).toSeconds();
			return t2-t1;
		}

		@Override
		public long reverse(long previous, long difference) {
			// difference must be a multiple of what was returned in instantsDifference
			long seconds = Duration.ofSeconds(difference).toSeconds();
			return previous + seconds;
		}
	};

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
	
	@Test
	public void testLoadAndSuccessors() throws Exception {
		EvolvingMultiGraph emg = new EvolvingMultiGraph(
				"out.flickr-growth-sorted.gz",
				true,
				2,
				"flickr",
				ic
		);
		
		emg.load();
		
		SuccessorIterator it = emg.successors(2);
		while(true) {
			try {
				Successor s = it.next();
				System.out.println(s.getNeighbor() + " " + s.getTimestamp());
			}
			catch(Exception e) {
				System.out.println(e);
				break;
			}
		}
	}
}
