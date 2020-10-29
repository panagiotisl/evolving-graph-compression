package gr.uoa.di.networkanalysis.llp;

import org.junit.Test;

import gr.uoa.di.networkanalysis.BVMultiGraph;
import gr.uoa.di.networkanalysis.EvolvingMultiGraph;

import java.io.*;

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
    public void testLLP() throws FileNotFoundException, IOException {

    	ClassLoader classLoader = getClass().getClassLoader();
		String graphFileResourcePath = classLoader.getResource(graphFile).getPath();
    	
		EvolvingMultiGraph emg = new EvolvingMultiGraph(graphFileResourcePath, headers, k, basename, aggregation);
		emg.storeBVMultiGraph();
		
		BVMultiGraph bvgraph = BVMultiGraph.load(basename);
		
        String llpFile = EvolvingMultiGraph.applyLLP(graphFileResourcePath, basename, true, bvgraph, new double[] {.05, .1, .15, .2});
        System.out.println(llpFile);

    }
}
