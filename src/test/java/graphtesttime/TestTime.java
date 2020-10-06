package graphtesttime;

import java.util.PrimitiveIterator.OfInt;
import java.util.NoSuchElementException;
import java.util.Random;
import org.junit.Test;

import gr.uoa.di.networkanalysis.EvolvingMultiGraph;
import gr.uoa.di.networkanalysis.EvolvingMultiGraph.SuccessorIterator;

public class TestTime {

	private static final Random random = new Random();
	
	private static final int factor = 1;
	private static int k = 2;

	//Flickr
	private static int firstLabel = 1;
	private static int lastLabel = 2_302_925;
	private static final String graphFile = "out.flickr-growth-sorted.gz";
	private static final String basename = "flickr";
	private static boolean headers = true;
	
	//Wiki
//	private static int firstLabel = 1;
//	private static int lastLabel = 2302925;
//	private static final String graphFile = "out.edit-enwiki.sorted.gz";
//	private static final String basename = "wiki";
//	private static boolean headers = true;
	
	//Yahoo
//	private static int firstLabel = 1;
//	private static int lastLabel = 40616537;
//	private static final String graphFile = "yahoo-G5-sorted.tsv.gz";
//	private static final String basename = "yahoo";
//	private static boolean headers = false;
	
//	@Test
	public void computeFullRetrievalOfNeighborsForRandomNodesTime() throws Exception {
		
		EvolvingMultiGraph emg = new EvolvingMultiGraph(
				graphFile,
				headers,
				k,
				basename,
				factor
		);
		
		emg.load();

		OfInt it = random.ints(1,lastLabel+1).iterator();
		
		long numIter = 100_000_000;
		long totalSum = 0;
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
			totalSum += toc-tic;
		}
		long endTotal = System.nanoTime();
		
		System.out.println("Total time: "+totalSum);
	}
	
//	@Test
	public void testIsNeighborNoRange() throws Exception {
		EvolvingMultiGraph emg = new EvolvingMultiGraph(
				graphFile,
				headers,
				k,
				basename,
				factor
		);
		
		emg.load();
		
		long trueCounter = 0;
		long numIter = 100_000_000;
		long totalSum = 0;
		long trueSum = 0;
		for(int i = 0; i < numIter; i++) {
			int n1 = randInRangeInclusive(firstLabel, lastLabel);
			int n2 = randInRangeInclusive(firstLabel, lastLabel);
			long tic = System.nanoTime();
			boolean b = emg.isNeighbor(n1,  n2);
			long toc = System.nanoTime();
			
			totalSum += toc-tic;
			if(b) {
				trueCounter++;
				trueSum += toc-tic;
			}
		}

		System.out.println("Total time: "+totalSum);
		System.out.println("True time: "+trueSum);
		System.out.println("True counter: "+trueCounter);
	}
	
	@Test
	public void testIsNeighborWithRange() throws Exception {
		

		EvolvingMultiGraph emg = new EvolvingMultiGraph(
				graphFile,
				headers,
				k,
				basename,
				factor
		);
		
		emg.load();
		
		long minTimestamp = emg.getMinTimestamp();
		long maxTimestamp = System.currentTimeMillis()/1000;
		
		long trueCounter = 0;
		long numIter = 100_000_000;
		long totalSum = 0;
		long trueSum = 0;
		for(int i = 0; i < numIter; i++) {
			int n1 = randInRangeInclusive(firstLabel, lastLabel);
			int n2 = randInRangeInclusive(firstLabel, lastLabel);
			long t1 = randInRangeInclusive((int) minTimestamp, (int) maxTimestamp);
			long t2 = randInRangeInclusive((int) t1, (int) maxTimestamp);
			long tic = System.nanoTime();
			boolean b = emg.isNeighbor( n1, n2, t1, t2);
			long toc = System.nanoTime();
			
			totalSum += toc-tic;
			if(b) {
				trueCounter++;
				trueSum += toc-tic;
			}
		}

		System.out.println("Total time: "+totalSum);
		System.out.println("True time: "+trueSum);
		System.out.println("True counter: "+trueCounter);
	}
	
	public int randInRangeInclusive(int x, int y) {
		return random.nextInt((y - x) + 1) + x;
	}
}
