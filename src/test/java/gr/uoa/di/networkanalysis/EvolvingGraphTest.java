package gr.uoa.di.networkanalysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.junit.Test;

import it.unimi.dsi.webgraph.ArcListASCIIGraph;
import it.unimi.dsi.webgraph.BVGraph;

public class EvolvingGraphTest {

	public void writeTimestampsToFile(int[] neighbors, int outDegree, Map<Integer,Long> currentNeighborsWithTimestamps, RandomAccessFile raf) throws IOException {

		for(int i=0; i < outDegree; i++) {
    		raf.writeBytes(Long.toString(currentNeighborsWithTimestamps.get(neighbors[i])));
    		if(i != outDegree-1) {
    			raf.writeBytes(" ");
    		}
    		else {
    			raf.writeBytes("\n");
    		}
    	}
	}
	
	@Test
	public void store() throws IOException {
    	
        InputStream fileStream = new FileInputStream("out.flickr-growth.sorted.gz");
        InputStream gzipStream = new GZIPInputStream(fileStream);
        Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
        BufferedReader buffered = new BufferedReader(decoder);
        File tempFile = File.createTempFile("temp", ".txt");
        tempFile.deleteOnExit();

        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        String line = buffered.readLine();
        while ((line = buffered.readLine()) != null) {
            String[] splits = line.split("\\s");
            writer.write(String.format("%s\t%s\n", splits[0], splits[1]));
        }
        writer.close();
        buffered.close();

        String basename = "flickr-growth";

        ArcListASCIIGraph inputGraph = new ArcListASCIIGraph(new FileInputStream(tempFile), 0);
        BVGraph.store(inputGraph, basename);
        BVGraph bvgraph = BVGraph.load(basename);
        System.out.println(bvgraph.numNodes());
        System.out.println(bvgraph.numArcs());
                
        fileStream = new FileInputStream("out.flickr-growth.sorted.gz");
        gzipStream = new GZIPInputStream(fileStream);
        decoder = new InputStreamReader(gzipStream, "UTF-8");
        buffered = new BufferedReader(decoder);
        
        // Skip the header of the file
        buffered.readLine();
        // The file we will write the results to
        RandomAccessFile raf = new RandomAccessFile("timestamps.txt", "rw");
        // Maintain an index of positions in the file for each node -> timestamps line
        // The number of nodes is known beforehand, set initial capacity accordingly
        List<Long> offsetsIndex = new ArrayList<Long>(bvgraph.numNodes());
        // Start reading the file
        int currentNode = 1;
        Map<Integer,Long> currentNeighborsWithTimestamps = new HashMap<Integer,Long>(); // neighbor -> timestamp
        while ((line = buffered.readLine()) != null) {
            String[] tokens = line.split("\\s+");
            int node = Integer.parseInt(tokens[0]);
            int neighbor = Integer.parseInt(tokens[1]);
            long timestamp = Long.parseLong(tokens[3]);
            // If at least one node was skipped, add that many empty lines to the file and update the index accordingly
            if(node > currentNode + 1) {
            	for(int i = 0; i < node - currentNode - 1; i++) {
            		offsetsIndex.add(raf.getFilePointer());
            		raf.writeBytes("\n");
            	}
            }

            // If you find a new currentNode in the file, write the results you have so far about the current node.
            if(node != currentNode) {
            	// First get the sequence of neighbors from BVGraph
            	int[] neighbors = bvgraph.successorArray(currentNode);
            	int outDegree = bvgraph.outdegree(currentNode); // successorArray might return extra results, only the [0,outDegree) range is valid
            	offsetsIndex.add(raf.getFilePointer());
            	writeTimestampsToFile(neighbors, outDegree, currentNeighborsWithTimestamps, raf);
            	
            	// Prepare the variables for the next currentNode
            	currentNode = node;
            	currentNeighborsWithTimestamps = new HashMap<Integer,Long>();
            	currentNeighborsWithTimestamps.put(neighbor, timestamp);
            	
            }
            else {
            	currentNeighborsWithTimestamps.put(neighbor, timestamp);
            }
        }
        // Write the last node. It was not written because no change in node != currentNode was detected
        int[] neighbors = bvgraph.successorArray(currentNode);
    	int outDegree = bvgraph.outdegree(currentNode);
    	offsetsIndex.add(raf.getFilePointer());
        writeTimestampsToFile(neighbors, outDegree, currentNeighborsWithTimestamps, raf);
        
        raf.close();
        buffered.close();
        
        System.out.println(String.format("Index size: %d", offsetsIndex.size()));
        // At this point, if raf was not closed, you could seek any node using the offsetsIndex list which holds the offsets
        /* 
         * e.g. To find the timestamps of the node i:
         * 
         * raf.seek(offsetsIndex.get(i));
         * String[] timestamps = raf.readLine().split(" "); returns the timestamps as string tokens 
        	
		*/
	}
}
