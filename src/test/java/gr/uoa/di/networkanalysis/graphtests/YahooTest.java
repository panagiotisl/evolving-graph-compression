package gr.uoa.di.networkanalysis.graphtests;

import java.time.Duration;
import java.time.Instant;

import org.junit.Test;

import gr.uoa.di.networkanalysis.EvolvingMultiGraph;
import gr.uoa.di.networkanalysis.InstantComparer;

public class YahooTest {

	private static String path = System.getProperty("user.dir");

	@Test
	public void test() throws Exception {
		EvolvingMultiGraph emg = new EvolvingMultiGraph(
				path + "\\yahoo-G5-sorted.tsv.gz",
				true,
				2,
				path + "\\yahoo",
				path + "\\yahoo-timestamps",
				path + "\\yahoo-index", 
				new InstantComparer() {

					@Override
					public long instantsDifference(Instant i1, Instant i2) {
						return Duration.between(i1, i2).toMinutes()/15;
					}
				});

		emg.store();
	}
}
