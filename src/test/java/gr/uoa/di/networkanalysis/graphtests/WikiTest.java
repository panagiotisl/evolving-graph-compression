package gr.uoa.di.networkanalysis.graphtests;

import java.time.Duration;
import java.time.Instant;

import org.junit.Test;

import gr.uoa.di.networkanalysis.EvolvingMultiGraph;
import gr.uoa.di.networkanalysis.TimestampComparer;

public class WikiTest {

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

	@Test
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
}
