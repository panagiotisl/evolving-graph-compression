package gr.uoa.di.networkanalysis.graphtests;

import java.time.Duration;
import java.time.Instant;

import org.junit.Test;

import gr.uoa.di.networkanalysis.EvolvingMultiGraph;
import gr.uoa.di.networkanalysis.InstantComparer;

public class WikiTest {

	private static String path = System.getProperty("user.dir");

	@Test
	public void test() throws Exception {
		EvolvingMultiGraph emg = new EvolvingMultiGraph(
				path + "\\out.edit-enwiki.sorted.gz",
				true,
				2,
				path + "\\wiki",
				path + "\\wiki-timestamps",
				path + "\\wiki-index", 
				new InstantComparer() {

					@Override
					public long instantsDifference(Instant i1, Instant i2) {
						return Duration.between(i1, i2).toMinutes()/15;
					}
				});

		emg.store();
	}
}
