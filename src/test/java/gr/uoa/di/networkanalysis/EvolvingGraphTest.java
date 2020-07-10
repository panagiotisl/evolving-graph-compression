package gr.uoa.di.networkanalysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.junit.Test;

import it.unimi.dsi.webgraph.ArcListASCIIGraph;
import it.unimi.dsi.webgraph.BVGraph;

public class EvolvingGraphTest {

	public void writeTimestampsToFile(int currentNode, int[] neighbors, int outDegree, Map<Integer,Long> currentNeighborsWithTimestamps, BufferedWriter writer) throws IOException{
		// TODO Remove currentNode, it is not needed, I just added it for testing
		writer.append(Integer.toString(currentNode));
    	writer.append(' ');
    	for(int i=0; i < outDegree; i++) {
    		writer.append(Long.toString(currentNeighborsWithTimestamps.get(neighbors[i])));
    		if(i != outDegree-1) {
    			writer.append(' ');
    		}
    		else {
    			writer.newLine();
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
        
        System.out.println("Here comes my code to ruin the day!");
        
        fileStream = new FileInputStream("out.flickr-growth.sorted.gz");
        gzipStream = new GZIPInputStream(fileStream);
        decoder = new InputStreamReader(gzipStream, "UTF-8");
        buffered = new BufferedReader(decoder);
        
        // Skip the header of the file
        buffered.readLine();
        // The file we will write the results to
        writer = new BufferedWriter(new FileWriter("timestamps.txt"));
        // Start reading the file
        int currentNode = 1;
        Map<Integer,Long> currentNeighborsWithTimestamps = new HashMap<Integer,Long>(); // neighbor -> timestamp
        while ((line = buffered.readLine()) != null) {
            String[] tokens = line.split("\\s+");
            int node = Integer.parseInt(tokens[0]);
            int neighbor = Integer.parseInt(tokens[1]);
            long timestamp =  Long.parseLong(tokens[3]);
            if(node != currentNode) {
            	// If you find a new currentNode in the file, write the results you have so far about the current node.
            	// First get the sequence of neighbors from BVGraph
            	int[] neighbors = bvgraph.successorArray(currentNode);
            	int outDegree = bvgraph.outdegree(currentNode); // successorArray might return extra results, only the [0,outDegree) range is valid
            	writeTimestampsToFile(currentNode, neighbors, outDegree, currentNeighborsWithTimestamps, writer);
            	
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
        writeTimestampsToFile(currentNode, neighbors, outDegree, currentNeighborsWithTimestamps, writer);
        
        writer.close();
        buffered.close();
	}
}
