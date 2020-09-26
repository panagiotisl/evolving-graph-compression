package gr.uoa.di.networkanalysis.graphtests;

import java.time.Duration;
import java.time.Instant;

import org.junit.Test;

import gr.uoa.di.networkanalysis.EvolvingMultiGraph;
import gr.uoa.di.networkanalysis.EvolvingMultiGraph.SuccessorIterator;
import gr.uoa.di.networkanalysis.InstantComparer;
import gr.uoa.di.networkanalysis.Successor;

public class FlickrTest {

	private static String path = System.getProperty("user.dir");
	private static InstantComparer ic = new InstantComparer() {
		
		@Override
		public long instantsDifference(Instant i1, Instant i2) {
			return Duration.between(i1, i2).toSeconds();
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
