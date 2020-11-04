package gr.uoa.di.networkanalysis.llp;

import org.junit.Test;

import gr.uoa.di.networkanalysis.BVMultiGraph;
import gr.uoa.di.networkanalysis.EvolvingMultiGraph;
import it.unimi.dsi.webgraph.NodeIterator;

import java.io.*;
import java.util.Arrays;

public class TestLLP {

    // Flickr
//	private static final String graphFile =  "out.flickr-growth.sorted.gz";
//	private static final String basename =  "flickr";
//	private static final boolean headers = true;
//	private static final int k = 2;
//	private static int aggregation = 24*60*60;

    // Wiki
//	private static final String graphFile =  "out.edit-enwiki.sorted.gz";
//	private static final String basename =  "wiki";
//	private static final boolean headers = false;
//	private static final int k = 2;
//	private static int aggregation = 60*60;

    // Yahoo
	private static final String graphFile =  "yahoo-G5-sorted.tsv.gz";
	private static final String basename =  "yahoo";
	private static final boolean headers = false;
	private static final int k = 2;
	private static int aggregation = 15*60;

    // cbtComm
//    private static final String graphFile =  "cbtComm-sorted.txt.gz";
//    private static final String basename =  "cbtComm";
//    private static final boolean headers = false;
//    private static final int k = 2;
//    private static int aggregation = 1;

    // cbtPow
//	private static final String graphFile =  "cbtPow-sorted.txt.gz";
//	private static final String basename =  "cbtPow";
//	private static final boolean headers = false;
//	private static final int k = 2;
//	private static int aggregation = 1;

    @Test
    public void testLLP() throws FileNotFoundException, IOException {

    	ClassLoader classLoader = getClass().getClassLoader();
		String graphFileResourcePath = classLoader.getResource(graphFile).getPath();
    	
//		EvolvingMultiGraph emg = new EvolvingMultiGraph(graphFileResourcePath, headers, k, basename, aggregation);
//		emg.storeBVMultiGraph();
		
		BVMultiGraph bvgraph = BVMultiGraph.load(basename);
		
		double[] gammas = new double[1];
		double value = .05;
		double step = .1;
		for(int i = 0; i < gammas.length; i++) {
			gammas[i] = value;
			value += step;
		}
		
        String llpFile = EvolvingMultiGraph.applyLLP(graphFileResourcePath, basename, true, bvgraph, gammas);
        System.out.println(Arrays.toString(gammas));
    }
    
//    @Test
    public void test() throws Exception {
    	BVMultiGraph bvgraph = BVMultiGraph.load(basename);

    	for(int node = 0; node <= 3819691; node++) {
    		int[] successors = bvgraph.successorArray(node);
    		if(bvgraph.outdegree(node) < 1) continue;
    		if(Arrays.binarySearch(successors, 0, bvgraph.outdegree(node)-1, -1) >= 0) {
    			System.out.println(node);
    		}
    	}
    }
}
