package gr.uoa.di.networkanalysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.apache.lucene.util.Counter;

import it.unimi.dsi.bits.Fast;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.io.OutputBitStream;
import it.unimi.dsi.sux4j.util.EliasFanoMonotoneLongBigList;
import it.unimi.dsi.webgraph.ArcListASCIIGraph;

public class EvolvingMultiGraph {

	protected String graphFile;
	protected boolean headers;
	protected int zetaK;
	protected String basename;
	protected String timestampsFile;
	protected String eliasFanoFile;
	protected InstantComparer instantComparer;
	
	
	public EvolvingMultiGraph(String graphFile, boolean headers, int zetaK, String basename, String timestampsFile, String eliasFanoFile, InstantComparer instantComparer) {
		super();
		this.graphFile = graphFile;
		this.headers = headers;
		this.zetaK = zetaK;
		this.basename = basename;
		this.timestampsFile = timestampsFile;
		this.eliasFanoFile = eliasFanoFile;
		this.instantComparer = instantComparer;
	}

	protected long findMinimumTimestamp() throws IOException {
		InputStream fileStream = new FileInputStream(graphFile);
        InputStream gzipStream = new GZIPInputStream(fileStream);
        Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
        BufferedReader buffered = new BufferedReader(decoder);
        
        if(headers) {
        	// Skip the header of the file
        	buffered.readLine();
        }
        
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
	
	protected long writeTimestampsToFile(ArrayList<Long> currentNeighborsTimestamps, OutputBitStream obs, Instant minInstant) throws IOException {

		// Returns the number of bits appended to the file
		
		// The first timestamp is written with respect to difference
		// in days from the minimum timestamp in the file
		// The rest are written with respect to difference from the previous in the row
		long ret = 0;
		
		Instant previousNeighborInstant = minInstant;
		
		for(Long seconds: currentNeighborsTimestamps) {
			Instant currentNeighborInstant = Instant.ofEpochSecond(seconds);
			long periodsBetween = instantComparer.instantsDifference(previousNeighborInstant, currentNeighborInstant);
			periodsBetween = Fast.int2nat(periodsBetween);
			previousNeighborInstant = currentNeighborInstant;
			ret += obs.writeLongZeta(periodsBetween, zetaK);
		}
		
		return ret;
	}
	
	public void store() throws IOException {

		InputStream fileStream = new FileInputStream(graphFile);
        InputStream gzipStream = new GZIPInputStream(fileStream);
        Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
        BufferedReader buffered = new BufferedReader(decoder);
        File tempFile = File.createTempFile("temp", ".txt");
        tempFile.deleteOnExit();

        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        String line;
        if(headers) {
        	// Skip the header of the file
        	buffered.readLine();
        }
        while ((line = buffered.readLine()) != null) {
            String[] splits = line.split("\\s");
           	writer.write(String.format("%s\t%s\n", splits[0], splits[1]));
        }
           
        writer.close();
        buffered.close();

        ArcListASCIIGraph inputGraph = new ArcListASCIIGraph(new FileInputStream(tempFile), 0);
        BVMultiGraph.store(inputGraph, basename);
        // This is not needed anymore
        //BVMultiGraph bvgraph = BVMultiGraph.load(basename);
        
        // Find the minimum timestamp in the file
        long minTimestamp = findMinimumTimestamp();
        Instant minInstant = Instant.ofEpochSecond(minTimestamp);
        
        fileStream = new FileInputStream(graphFile);
        gzipStream = new GZIPInputStream(fileStream);
        decoder = new InputStreamReader(gzipStream, "UTF-8");
        buffered = new BufferedReader(decoder);
        
        if(headers) {
        	// Skip the header of the file
        	buffered.readLine();
        }
        // The file we will write the results to 
        final OutputBitStream obs = new OutputBitStream(new FileOutputStream(timestampsFile), 1024 * 1024);
        // Maintain an index of positions in the file for each node -> timestamps line
        // The number of nodes is known beforehand, set initial capacity accordingly
        LongArrayList offsetsIndex= new LongArrayList();
        long currentOffset = 0L;
        currentOffset += obs.writeLong(minTimestamp, 64);
        // Start reading the file
        int currentNode = 1;
        ArrayList<Long> currentNeighborsTimestamps = new ArrayList<Long>();
        while ((line = buffered.readLine()) != null) {
            String[] tokens = line.split("\\s+");
            int node = Integer.parseInt(tokens[0]);
            int neighbor = Integer.parseInt(tokens[1]);
            long timestamp = Long.parseLong(tokens[3]);

            int previous = currentNode;
            
            // If you find a new currentNode in the file, write the results you have so far about the current node.
            if(node != currentNode) {
            	offsetsIndex.add(currentOffset);
            	currentOffset += writeTimestampsToFile(currentNeighborsTimestamps, obs, minInstant);
            	
            	// Prepare the variables for the next currentNode
            	currentNode = node;
            	currentNeighborsTimestamps = new ArrayList<Long>();
            	currentNeighborsTimestamps.add(timestamp);
            	
            	// If at least one node was skipped, add that many empty lines to the file and update the index accordingly
                if(node > previous + 1) {
                	for(int i = 0; i < node - previous - 1; i++) {
                		offsetsIndex.add(currentOffset);
                	}
                }
            	
            }
            else {
            	currentNeighborsTimestamps.add(timestamp);
            }
        }
        // Write the last node. It was not written because no change in node != currentNode was detected
    	offsetsIndex.add(currentOffset);
    	currentOffset += writeTimestampsToFile(currentNeighborsTimestamps, obs, minInstant);
        
        obs.close();
        buffered.close();

        // Perform compression of the index using EliasFano
        EliasFanoMonotoneLongBigList efmlbl = new EliasFanoMonotoneLongBigList(offsetsIndex);
        FileOutputStream fos = new FileOutputStream(eliasFanoFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(efmlbl);
        oos.close();
        fos.close();
	}
}
