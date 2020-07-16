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
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import org.junit.Assert;
import org.junit.Test;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.sux4j.util.EliasFanoMonotoneLongBigList;
import it.unimi.dsi.webgraph.ArcListASCIIGraph;
import it.unimi.dsi.webgraph.BVGraph;

public class EvolvingGraphTest {

	public int writeTimestampsToFile(int[] neighbors, int outDegree, Map<Integer,Long> currentNeighborsWithTimestamps, BufferedWriter writer) throws IOException {

		// Returns the number of characters appended to the file
		int ret = 0;
		for(int i=0; i < outDegree; i++) {
    		String tmp = Long.toString(currentNeighborsWithTimestamps.get(neighbors[i]));
			writer.append(tmp);
    		if(i != outDegree-1) {
    			writer.append(" ");
    		}
    		else {
    			writer.append("\n");
    		}
    		ret += tmp.length() + 1;
    	}
		
		return ret;
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
        writer = new BufferedWriter(new FileWriter("timestamps.txt"));
        // Maintain an index of positions in the file for each node -> timestamps line
        // The number of nodes is known beforehand, set initial capacity accordingly
        IntArrayList offsetsIndex= new IntArrayList();
        // Start reading the file
        int currentNode = 1;
        int currentOffset = 0;
        Map<Integer,Long> currentNeighborsWithTimestamps = new HashMap<Integer,Long>(); // neighbor -> timestamp
        while ((line = buffered.readLine()) != null) {
            String[] tokens = line.split("\\s+");
            int node = Integer.parseInt(tokens[0]);
            int neighbor = Integer.parseInt(tokens[1]);
            long timestamp = Long.parseLong(tokens[3]);

            int previous = currentNode;
            
            // If you find a new currentNode in the file, write the results you have so far about the current node.
            if(node != currentNode) {
            	// First get the sequence of neighbors from BVGraph
            	int[] neighbors = bvgraph.successorArray(currentNode);
            	int outDegree = bvgraph.outdegree(currentNode); // successorArray might return extra results, only the [0,outDegree) range is valid
            	offsetsIndex.add(currentOffset);
            	int charactersWritten = writeTimestampsToFile(neighbors, outDegree, currentNeighborsWithTimestamps, writer);
            	currentOffset += charactersWritten;
            	
            	// Prepare the variables for the next currentNode
            	currentNode = node;
            	currentNeighborsWithTimestamps = new HashMap<Integer,Long>();
            	currentNeighborsWithTimestamps.put(neighbor, timestamp);
            	
            }
            else {
            	currentNeighborsWithTimestamps.put(neighbor, timestamp);
            }
            
            // If at least one node was skipped, add that many empty lines to the file and update the index accordingly
            if(node > previous + 1) {
            	for(int i = 0; i < node - previous - 1; i++) {
            		offsetsIndex.add(currentOffset);
            		writer.append("\n");
            		currentOffset += 1;
            	}
            }
        }
        // Write the last node. It was not written because no change in node != currentNode was detected
        int[] neighbors = bvgraph.successorArray(currentNode);
    	int outDegree = bvgraph.outdegree(currentNode);
    	offsetsIndex.add(currentOffset);
        int charactersWritten = writeTimestampsToFile(neighbors, outDegree, currentNeighborsWithTimestamps, writer);
        currentOffset += charactersWritten;
        
        writer.close();
        buffered.close();
        
        System.out.println(String.format("Index size: %d", offsetsIndex.size()));
        System.out.println(offsetsIndex.getInt(offsetsIndex.size() - 1));

        // Perform compression of the index using EliasFano
        EliasFanoMonotoneLongBigList efmlbl = new EliasFanoMonotoneLongBigList(offsetsIndex);
        System.out.println(efmlbl.numBits());
        Assert.assertTrue(efmlbl.size64() < 4 * 8 * offsetsIndex.size());
        Assert.assertEquals(offsetsIndex.size(), efmlbl.size64());
	}
}
