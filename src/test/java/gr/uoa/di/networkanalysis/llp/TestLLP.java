package gr.uoa.di.networkanalysis.llp;

import org.junit.Assert;
import org.junit.Test;

import gr.uoa.di.networkanalysis.BVMultiGraph;
import gr.uoa.di.networkanalysis.EvolvingMultiGraph;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.NodeIterator;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TestLLP {

    // Flickr
	private static final String graphFile =  "out.flickr-growth.sorted.gz";
	private static final String basename =  "flickr";
	private static final boolean headers = true;
	private static final int k = 2;
	private static int aggregation = 24*60*60;

    // Wiki
//	private static final String graphFile =  "out.edit-enwiki.sorted.gz";
//	private static final String basename =  "wiki";
//	private static final boolean headers = true;
//	private static final int k = 2;
//	private static int aggregation = 60*60;

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
    	
    	ClassLoader classLoader = getClass().getClassLoader();
		String graphFileResourcePath = classLoader.getResource(graphFile).getPath();
    	
		EvolvingMultiGraph.storeAsBVGraph(graphFileResourcePath, basename, headers);
		
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
    
    @Test
    public void testValidLLP() throws Exception {
    	ObjectInputStream in = new ObjectInputStream(new FileInputStream("map.ser"));
        int[] map = (int[]) in.readObject();
        in.close();
        
        BVMultiGraph graph1 = BVMultiGraph.load("flickr");
        BVMultiGraph graph2 = BVMultiGraph.load("flickr.llp");
        
        NodeIterator iter = graph1.nodeIterator();
        while(iter.hasNext()) {
        	int node = iter.nextInt();
        	System.out.println("Node: "+node+" mapped node: "+map[node]);
        	int[] successors1 = graph1.successorArray(node);
        	int[] successors2 = graph2.successorArray(map[node]);
        	int outdegree1 = graph1.outdegree(node);
        	int outdegree2 = graph2.outdegree(map[node]);

        		System.out.println(Arrays.toString(Arrays.copyOfRange(successors1, 0, outdegree1)));
        		System.out.println(Arrays.toString(Arrays.copyOfRange(successors2, 0, outdegree2)));
        	Assert.assertEquals("Unequals lengths", outdegree1, outdegree2);
        	Set<Integer> set1 = new HashSet<>();
        	Set<Integer> set2 = new HashSet<>();
        	for(int i = 0; i < outdegree1; i++) {
        		set1.add(map[successors1[i]]);
        	}
        	for(int i = 0; i < outdegree2; i++) {
        		set2.add(successors2[i]);
        	}
        	if(!set1.equals(set2)) {
        		throw new RuntimeException("sets not equal");
        	}
        }

    }
}
