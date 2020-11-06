package gr.uoa.di.networkanalysis.llp;

import org.junit.Assert;
import org.junit.Test;

import gr.uoa.di.networkanalysis.BVMultiGraph;
import gr.uoa.di.networkanalysis.EvolvingMultiGraph;
import it.unimi.dsi.webgraph.NodeIterator;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public class TestLLP {

    // Flickr
//	private static final String graphFile =  "out.flickr-growth.sorted.gz";
//	private static final String basename =  "flickr";
//	private static final boolean headers = false;
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
    public void testLLP() throws Exception {
    	
    	ClassLoader classLoader = getClass().getClassLoader();
		String graphFileResourcePath = classLoader.getResource(graphFile).getPath();
    	
		// Store the graph without duplicate edges
		EvolvingMultiGraph.storeWithoutDuplicates(graphFileResourcePath, basename, headers);
		
		BVMultiGraph bvgraph = BVMultiGraph.load(basename);
		
//		double[] gammas = new double[50];
//		double value = .01;
//		double step = .02;
//		for(int i = 0; i < gammas.length; i++) {
//			gammas[i] = value;
//			value += step;
//		}
		double[] gammas = new double[] {0.0000000001};
		
		int[] map = EvolvingMultiGraph.applyLLP(graphFileResourcePath, basename, headers, bvgraph, gammas);
		
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(basename+".mapserialize"));
        out.writeObject(map);
        out.flush();
        out.close();
		
    }
    
    @Test
    public void testValidLLP() throws Exception {
    	ObjectInputStream in = new ObjectInputStream(new FileInputStream(basename+".mapserialize"));
        int[] map = (int[]) in.readObject();
        in.close();
        
        BVMultiGraph graph1 = BVMultiGraph.load(basename);
        BVMultiGraph graph2 = BVMultiGraph.load(basename+".llp");
        
        Assert.assertEquals("num nodes not the same", graph1.numNodes(), graph2.numNodes());
        Assert.assertEquals("num arcs not the same", graph1.numArcs(), graph2.numArcs());
        
        ClassLoader classLoader = getClass().getClassLoader();
		String graphFileResourcePath = classLoader.getResource(graphFile).getPath();
		
        FileInputStream fileStream = new FileInputStream(graphFileResourcePath);
        GZIPInputStream gzipStream = new GZIPInputStream(fileStream);
        InputStreamReader decoder = new InputStreamReader(gzipStream, "UTF-8");
        BufferedReader buffered = new BufferedReader(decoder);
        String line;
        if(headers)
        	buffered.readLine();
        
        int previous = -1;
        while((line = buffered.readLine()) != null) {
        	
	    	String[] tokens = line.split("\\s+");
            int node = Integer.parseInt(tokens[0]);
            if(node == previous) continue;
            previous = node;

	    	int[] successors1 = graph1.successorArray(node);
	    	int[] successors2 = graph2.successorArray(map[node]);
	    	int outdegree1 = graph1.outdegree(node);
	    	int outdegree2 = graph2.outdegree(map[node]);
	    	Set<Integer> set1 = null;
	    	Set<Integer> set2 = null;
	        try {
	        	Assert.assertEquals("Unequals lengths", outdegree1, outdegree2);
	        	set1 = new HashSet<>();
	        	set2 = new HashSet<>();
	        	for(int i = 0; i < outdegree1; i++) {
	        		set1.add(map[successors1[i]]);
	        	}
	        	for(int i = 0; i < outdegree2; i++) {
	        		set2.add(successors2[i]);
	        	}
	        	if(!set1.equals(set2)) {
	        		throw new AssertionError("sets not equal");
	        	}
        	}
        	catch(AssertionError e) {
        		System.out.println("------------");
        		System.out.println("Assertion Error:");
        		System.out.println(e);
        		System.out.println("Node: "+node);
        		System.out.println("Mapped node: "+map[node]);
        		System.out.println("Outdegree 1:"+outdegree1);
        		System.out.println("Outdegree 2:"+outdegree2);
        		if(e.getMessage().equals("sets not equal")) {
        			System.out.println("Set 1 size: "+set1.size());
        			System.out.println("Set 2 size: "+set2.size());
        		}
        		System.out.println("------------");
        	}
        }

    }
}
