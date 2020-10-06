package graphtesttime;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.PrimitiveIterator.OfInt;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;

import org.junit.Test;

import gr.uoa.di.networkanalysis.EvolvingMultiGraph;
import gr.uoa.di.networkanalysis.TimestampComparer;
import gr.uoa.di.networkanalysis.TimestampComparerAggregator;
import gr.uoa.di.networkanalysis.EvolvingMultiGraph.SuccessorIterator;

public class TestTime {

	private static final int factor = 1;
	private static TimestampComparer ic = new TimestampComparerAggregator(factor);
	
	private static String graphFile = "out.flickr-growth-sorted.gz";
	private static String basename = "flickr";
	private static boolean headers = true;
	private static int k = 2;
	
	//Flickr
	private static int firstLabel = 1;
	private static int lastLabel = 2302925;
	
	//Wiki
//	private static int firstLabel = 1;
//	private static int lastLabel = 2302925;
	
	//Yahoo
//	private static int firstLabel = 1;
//	private static int lastLabel = 40616537;
	
	@Test
	public void computeTime() throws Exception {
		
		EvolvingMultiGraph emg = new EvolvingMultiGraph(
				graphFile,
				headers,
				k,
				basename,
				ic
		);
		
		emg.load();
		Random random = new Random();
		OfInt it = random.ints(1,lastLabel+1).iterator();
		
		long numIter = 100000000;
		long startTotal = System.nanoTime();
		long perNodeTotal = 0;
		for(int i = 0; i < numIter; i++) {
			long tic = System.nanoTime();
			SuccessorIterator si = emg.successors(it.nextInt());
			while(true) {
				try {
					si.next();
				}
				catch(NoSuchElementException e) {
					break;
				}
			}
			long toc = System.nanoTime();
			perNodeTotal += toc-tic;
		}
		long endTotal = System.nanoTime();
		
		System.out.println("Total time: "+(endTotal-startTotal));
		System.out.println("Average per node: "+perNodeTotal/numIter);
	}
}
