package gr.uoa.di.networkanalysis.llp;

import org.junit.Test;

import gr.uoa.di.networkanalysis.BVMultiGraph;
import gr.uoa.di.networkanalysis.EvolvingMultiGraph;
import gr.uoa.di.networkanalysis.MyLayeredLabelPropagation;
import it.unimi.dsi.law.graph.LayeredLabelPropagation;
import it.unimi.dsi.webgraph.ArcListASCIIGraph;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.NodeIterator;

import java.io.*;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

public class TestLLP {

    // Flickr
//	private static final String graphFile =  "out.flickr-growth.sorted.gz";
//	private static final String basename =  "flickr";
//	private static final boolean headers = true;
//	private static final int k = 2;
//	private static int aggregation = 24*60*60;

    // Wiki
	private static final String graphFile =  "out.edit-enwiki.sorted.gz";
	private static final String basename =  "wiki";
	private static final boolean headers = true;
	private static final int k = 2;
	private static int aggregation = 60*60;

    // Yahoo
//	private static final String graphFile =  "yahoo-G5-sorted.tsv.gz";
//	private static final String basename =  "yahoo";
//	private static final boolean headers = false;
//	private static final int k = 2;
//	private static int aggregation = 15*60;

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
    public void testLLP() throws Exception {

    	InputStream fileStream;
		InputStream gzipStream;
		Reader decoder;
		BufferedReader buffered;
		String line;
    	
    	ClassLoader classLoader = getClass().getClassLoader();
		String graphFileResourcePath = classLoader.getResource(graphFile).getPath();
    	
		//EvolvingMultiGraph.storeAsBVGraph(graphFileResourcePath, basename, headers);
		
		BVGraph bvgraph = BVGraph.load(basename);
		
		double[] gammas = new double[1];
		double value = .05;
		double step = .1;
		for(int i = 0; i < gammas.length; i++) {
			gammas[i] = value;
			value += step;
		}
		
		String producedFile =  EvolvingMultiGraph.applyLLP(graphFileResourcePath, basename, headers, bvgraph, gammas);
		System.out.println(producedFile);
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
