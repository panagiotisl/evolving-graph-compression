package gr.uoa.di.networkanalysis.graphtesttime;

import java.util.PrimitiveIterator.OfInt;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.NoSuchElementException;
import java.util.Random;

import org.junit.Test;

import gr.uoa.di.networkanalysis.EvolvingMultiGraph;
import gr.uoa.di.networkanalysis.EvolvingMultiGraph.SuccessorIterator;

public class TestTime {

	private static final Random random = new Random();
	
	private static final int aggregation = 1;
	private static int k = 2;

	//Flickr
//	private static int firstLabel = 1;
//	private static int lastLabel = 2_302_925;
//	private static final String graphFile = "out.flickr-growth-sorted.gz";
//	private static final String basename = "flickr";
//	private static boolean headers = true;
//	private static String sampleFile = "flickr-sample.txt";
	
	//Wiki
//	private static int firstLabel = 1;
//	private static int lastLabel = 3819691;
//	private static final String graphFile = "out.edit-enwiki.sorted.gz";
//	private static final String basename = "wiki";
//	private static boolean headers = true;
//	private static String sampleFile = "wiki-sample.txt";
	
	//Yahoo
//	private static int firstLabel = 1;
//	private static int lastLabel = 40616537;
//	private static final String graphFile = "yahoo-G5-sorted.tsv.gz";
//	private static final String basename = "yahoo";
//	private static boolean headers = false;
//	private static String sampleFile = "flickr-sample-head.txt";
//	private static String sampleFile = "flickr-sample-tail.txt";
	
	//cbtComm
	private static int firstLabel = 0;
	private static int lastLabel = 9999;
	private static final String graphFile = "cbtComm-sorted.txt.gz";
	private static final String basename = "cbtComm";
	private static boolean headers = false;
	private static String sampleFile = "cbtComm-sample.txt";
	
	//cbtPow
//	private static int firstLabel = 0;
//	private static int lastLabel = 999997;
//	private static final String graphFile = "cbtPow-sorted.txt.gz";
//	private static final String basename = "cbtPow";
//	private static boolean headers = false;
//	private static String sampleFile = "cbtPow-sample.txt";

	@Test
	public void computeFullRetrievalOfNeighborsForRandomNodesTime() throws Exception {

		ClassLoader classLoader = getClass().getClassLoader();
		String graphFileResourcePath = classLoader.getResource(graphFile).getPath();
		
		EvolvingMultiGraph emg = new EvolvingMultiGraph(
				graphFileResourcePath,
				headers,
				k,
				basename,
				aggregation
		);
		
		emg.load();

		OfInt it = random.ints(1,lastLabel+1).iterator();
		
		long numIter = 100_000_000;
		long totalSum = 0;
		for(int i = 0; i < numIter; i++) {
			System.out.println(i);
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
		
		System.out.println("Total time: "+totalSum);
	}
	
	@Test
	public void testIsNeighborFromSample() throws Exception {
		
		ClassLoader classLoader = getClass().getClassLoader();
		String graphFileResourcePath = classLoader.getResource(graphFile).getPath();
		
		EvolvingMultiGraph emg = new EvolvingMultiGraph(
				graphFileResourcePath,
				headers,
				k,
				basename,
				aggregation
		);
		
		emg.load();

		String sampleFileResourcePath = classLoader.getResource(sampleFile).getPath();

		BufferedReader reader = new BufferedReader(new FileReader(sampleFileResourcePath));
		String line = null;
		int[] from = new int[1000];
		int[] to = new int[1000];
		int pos = 0;
		while((line = reader.readLine()) != null) {
			String[] splits = line.split("\\s");
			from[pos] = Integer.parseInt(splits[0]);
			to[pos] = Integer.parseInt(splits[1]);
			pos++;
		}
		reader.close();
		
		long trueCounter = 0;
		long totalSum = 0;
		long trueSum = 0;

		for(int i = 0; i < 100; i++) {
			for(int j = 0; j < from.length; j++) {
				int n1 = from[j];
				int n2 = to[j];
				long tic = System.nanoTime();
				boolean b = emg.isNeighbor(n1,  n2);
				long toc = System.nanoTime();
				
				totalSum += toc-tic;
				
				if(b) {
					trueSum += toc-tic;
					trueCounter++;
				}
			}
		}
		

		System.out.println("Total time: "+totalSum);
		System.out.println("True time: "+trueSum);
		System.out.println("True counter: "+trueCounter);
	}
	
	@Test
	public void testIsNeighborNoRange() throws Exception {
		
		ClassLoader classLoader = getClass().getClassLoader();
		String graphFileResourcePath = classLoader.getResource(graphFile).getPath();
		
		EvolvingMultiGraph emg = new EvolvingMultiGraph(
				graphFileResourcePath,
				headers,
				k,
				basename,
				aggregation
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

		ClassLoader classLoader = getClass().getClassLoader();
		String graphFileResourcePath = classLoader.getResource(graphFile).getPath();		
		
		EvolvingMultiGraph emg = new EvolvingMultiGraph(
				graphFileResourcePath,
				headers,
				k,
				basename,
				aggregation
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
