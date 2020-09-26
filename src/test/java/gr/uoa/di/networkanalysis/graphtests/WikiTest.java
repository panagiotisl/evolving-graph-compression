package gr.uoa.di.networkanalysis.graphtests;

import java.time.Duration;
import java.time.Instant;

import org.junit.Test;

import gr.uoa.di.networkanalysis.EvolvingMultiGraph;
import gr.uoa.di.networkanalysis.InstantComparer;

public class WikiTest {

	private static String path = System.getProperty("user.dir");
	private static InstantComparer ic = new InstantComparer() {

		@Override
		public long instantsDifference(Instant i1, Instant i2) {
			return Duration.between(i1, i2).toDays();
		}

		@Override
		public long reverse(long previous, long difference) {
			// difference must be a multiple of what was returned in instantsDifference
			long tmp = difference > 0 ? difference : -difference;
			long seconds = Duration.ofDays(tmp).toSeconds();
			return difference > 0 ? previous + seconds : previous - seconds;
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
