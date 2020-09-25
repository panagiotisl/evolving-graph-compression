package gr.uoa.di.networkanalysis.graphtests;

import java.time.Duration;
import java.time.Instant;

import org.junit.Test;

import gr.uoa.di.networkanalysis.EvolvingMultiGraph;
import gr.uoa.di.networkanalysis.InstantComparer;

public class YahooTest {

	private static String path = System.getProperty("user.dir");

	@Test
	public void testStore() throws Exception {
		EvolvingMultiGraph emg = new EvolvingMultiGraph(
				"yahoo-G5-sorted.tsv.gz",
				true,
				2,
				"yahoo",
				new InstantComparer() {

					@Override
					public long instantsDifference(Instant i1, Instant i2) {
						return Duration.between(i1, i2).toMinutes()/15;
					}
				});

		emg.store();
	}
}
