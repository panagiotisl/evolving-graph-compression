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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import java.time.temporal.ChronoUnit;

import org.junit.Assert;
import org.junit.Test;

import it.unimi.dsi.bits.Fast;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.sux4j.util.EliasFanoMonotoneLongBigList;
import it.unimi.dsi.webgraph.ArcListASCIIGraph;
import it.unimi.dsi.webgraph.BVGraph;

public class EvolvingGraphTest {

	public long findMinimumTimestamp() throws IOException {
		//InputStream fileStream = new FileInputStream("out.flickr-growth.sorted.gz");
		InputStream fileStream = new FileInputStream("out.edit-enwiki.gz");
        InputStream gzipStream = new GZIPInputStream(fileStream);
        Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
        BufferedReader buffered = new BufferedReader(decoder);
        
        // Skip the header of the file
        buffered.readLine();
        
        Long minTimestamp = null;
        String line;
        while ((line = buffered.readLine()) != null) {
            String[] tokens = line.split("\\s+");
            long timestamp = Long.parseLong(tokens[3]);
            if(minTimestamp == null || timestamp < minTimestamp) {
            	minTimestamp = timestamp;
            }
        }
        
        buffered.close();
        
        return minTimestamp;
	}
	
	public LocalDate unixEpochToLocalDate(long seconds) {
		
		LocalDateTime ldt = LocalDateTime.ofEpochSecond(seconds, 0, ZoneOffset.UTC);

		return LocalDate.of(ldt.getYear(), ldt.getMonthValue(), ldt.getDayOfMonth());

	}
	
	public int writeTimestampsToFile(int[] neighbors, int outDegree, Map<Integer,ArrayList<Long>> currentNeighborsWithTimestamps, BufferedWriter writer, LocalDate minLocalDate) throws IOException {

		// Returns the number of characters appended to the file
		
		// The first timestamp is written with respect to difference
		// in days from the minimum timestamp in the file
		int ret = 0;
		
		LocalDate previousNeighborDate = minLocalDate;
		
		for(int i=0; i < outDegree; i++) {
			ArrayList<Long> secondsList = currentNeighborsWithTimestamps.get(neighbors[i]);
			for(Long seconds: secondsList) {
				LocalDate ld = unixEpochToLocalDate(seconds);
				long daysBetween = -1;
				daysBetween = ChronoUnit.DAYS.between(previousNeighborDate, ld);
				previousNeighborDate = ld;
				daysBetween = Fast.int2nat(daysBetween);
	    		String tmp = Long.toString(daysBetween);
				writer.append(tmp);
	    		if(i != outDegree-1) {
	    			writer.append(" ");
	    		}
	    		else {
	    			writer.append("\n");
	    		}
	    		ret += tmp.length() + 1;
			}
    	}
		
		return ret;
	}
	
	@Test
	public void store() throws IOException {
    	
		//InputStream fileStream = new FileInputStream("out.flickr-growth.sorted.gz");
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
        BVMultiGraph.store(inputGraph, basename);
        BVMultiGraph bvgraph = BVMultiGraph.load(basename);
        System.out.println(bvgraph.numNodes());
        System.out.println(bvgraph.numArcs());
        
        // Find the minimum timestamp in the file
        long minTimestamp = findMinimumTimestamp();
        LocalDate minLocalDate = unixEpochToLocalDate(minTimestamp);
        
        //fileStream = new FileInputStream("out.flickr-growth.sorted.gz");
        fileStream = new FileInputStream("out.flickr-growth.sorted.gz");
        gzipStream = new GZIPInputStream(fileStream);
        decoder = new InputStreamReader(gzipStream, "UTF-8");
        buffered = new BufferedReader(decoder);
        
        // Skip the header of the file
        buffered.readLine();
        // The file we will write the results to 
        writer = new BufferedWriter(new FileWriter("timestamps1.txt"));
        // Maintain an index of positions in the file for each node -> timestamps line
        // The number of nodes is known beforehand, set initial capacity accordingly
        IntArrayList offsetsIndex= new IntArrayList();
        int currentOffset = 0;
        String tmp = Long.toString(minTimestamp);
        currentOffset += tmp.length() + 1;
        writer.write(tmp + "\n");
        // Start reading the file
        int currentNode = 1;
        Map<Integer,ArrayList<Long>> currentNeighborsWithTimestamps = new HashMap<Integer,ArrayList<Long>>(); // neighbor -> timestamp
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
            	int charactersWritten = writeTimestampsToFile(neighbors, outDegree, currentNeighborsWithTimestamps, writer, minLocalDate);
            	currentOffset += charactersWritten;
            	
            	// Prepare the variables for the next currentNode
            	currentNode = node;
            	currentNeighborsWithTimestamps = new HashMap<Integer,ArrayList<Long>>();
            	ArrayList<Long> L = new ArrayList<Long>();
            	L.add(timestamp);
            	currentNeighborsWithTimestamps.put(neighbor, L);
            	
            	// If at least one node was skipped, add that many empty lines to the file and update the index accordingly
                if(node > previous + 1) {
                	for(int i = 0; i < node - previous - 1; i++) {
                		offsetsIndex.add(currentOffset);
                		writer.append("\n");
                		currentOffset += 1;
                	}
                }
            	
            }
            else {
            	if(currentNeighborsWithTimestamps.containsKey(neighbor)) {
            		currentNeighborsWithTimestamps.get(neighbor).add(timestamp);
            	}
            	else {
            		ArrayList<Long> L = new ArrayList<Long>();
                	L.add(timestamp);
                	currentNeighborsWithTimestamps.put(neighbor, L);
            	}
            }
        }
        // Write the last node. It was not written because no change in node != currentNode was detected
        int[] neighbors = bvgraph.successorArray(currentNode);
    	int outDegree = bvgraph.outdegree(currentNode);
    	offsetsIndex.add(currentOffset);
        int charactersWritten = writeTimestampsToFile(neighbors, outDegree, currentNeighborsWithTimestamps, writer, minLocalDate);
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
	
	public void daysBetweenDates() {
		LocalDate d1 = LocalDate.of(2003, 10, 5);
		LocalDate d2 = LocalDate.of(2004, 10, 5);
		long daysBetween = ChronoUnit.DAYS.between(d1, d2);
		System.out.println(daysBetween);
	}
	
	public void daysBetweenDatesAfterEpochToLocalDateConversion() {
		
		long timeInSeconds1 = 1162422000;
		LocalDateTime ldt1 = LocalDateTime.ofEpochSecond(timeInSeconds1, 0, ZoneOffset.UTC);
		System.out.println(ldt1.getYear()+ " " + ldt1.getMonthValue() + " " + ldt1.getDayOfMonth());
		
		long timeInSeconds2 = 1178748000;
		LocalDateTime ldt2 = LocalDateTime.ofEpochSecond(timeInSeconds2, 0, ZoneOffset.UTC);
		System.out.println(ldt2.getYear()+ " " + ldt2.getMonthValue() + " " + ldt2.getDayOfMonth());
		
		
		LocalDate d1 = LocalDate.of(ldt1.getYear(), ldt1.getMonthValue(), ldt1.getDayOfMonth());
		LocalDate d2 = LocalDate.of(ldt2.getYear(), ldt2.getMonthValue(), ldt2.getDayOfMonth());
		long daysBetween = ChronoUnit.DAYS.between(d1, d2);
		System.out.println(daysBetween);
	}
}
