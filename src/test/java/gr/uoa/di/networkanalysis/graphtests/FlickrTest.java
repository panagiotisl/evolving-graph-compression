package gr.uoa.di.networkanalysis.graphtests;

import java.time.Duration;
import java.time.Instant;

import org.junit.Test;

import gr.uoa.di.networkanalysis.EvolvingMultiGraph;
import gr.uoa.di.networkanalysis.InstantComparer;

public class FlickrTest {

	private static String path = System.getProperty("user.dir");
	
	@Test
	public void testStore() throws Exception {
		EvolvingMultiGraph emg = new EvolvingMultiGraph(
				"out.flickr-growth-sorted.gz",
				true,
				2,
				"flickr",
				new InstantComparer() {
					
					@Override
					public long instantsDifference(Instant i1, Instant i2) {
						return Duration.between(i1, i2).toDays();
					}
				});

		emg.store();
	}
}
