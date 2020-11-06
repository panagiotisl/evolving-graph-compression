package gr.uoa.di.networkanalysis.llp;

import org.apache.lucene.search.similarities.IBSimilarity;
import org.junit.Assert;
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

	
//    @Test
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
		
//		double[] gammas = new double[50];
//		double value = .01;
//		double step = .02;
//		for(int i = 0; i < gammas.length; i++) {
//			gammas[i] = value;
//			value += step;
//		}
		double[] gammas = new double[] {0.0000000001};
		
		int[] map = EvolvingMultiGraph.applyLLP(graphFileResourcePath, basename, headers, bvgraph, gammas);
		
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("map.ser"));
        out.writeObject(map);
        out.flush();
        out.close();
		
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
    
    @Test
    public void testValidLLP() throws Exception {
    	ObjectInputStream in = new ObjectInputStream(new FileInputStream("map.ser"));
        int[] map = (int[]) in.readObject();
        in.close();
        
        System.out.println(map.length);
        
        BVMultiGraph graph1 = BVMultiGraph.load("wiki");
        BVMultiGraph graph2 = BVMultiGraph.load("wiki.llp");
        
        NodeIterator iter = graph1.nodeIterator();
        while(iter.hasNext()) {
        	int node = iter.nextInt();
        	System.out.println("Checking node: "+node);
        	System.out.println("Mapped to: "+map[node]);
        	int[] successors1 = iter.successorArray();
        	int[] successors2 = graph2.successorArray(map[node]);
        	Assert.assertEquals("Unequals lengths", successors1.length, successors2.length);
        	for(int i = 0; i < successors2.length; i++) {
        		Assert.assertEquals("Mapping error", successors2[i], map[successors1[i]]);
        	}
        }

    }
}
