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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;

import it.unimi.dsi.bits.Fast;
import it.unimi.dsi.fastutil.io.BinIO;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.io.InputBitStream;
import it.unimi.dsi.io.OutputBitStream;
import it.unimi.dsi.sux4j.util.EliasFanoMonotoneLongBigList;
import it.unimi.dsi.webgraph.ArcListASCIIGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;

public class EvolvingMultiGraph {

	protected String graphFile;
	protected boolean headers;
	protected int zetaK;
	protected String basename;
	protected TimestampComparer timestampComparer;
	
	protected BVMultiGraph graph;
	protected EliasFanoMonotoneLongBigList efindex;
	protected byte[] timestamps;
	protected long minTimestamp;
	
	public EvolvingMultiGraph(String graphFile, boolean headers, int zetaK, String basename, TimestampComparer timestampComparer) {
		super();
		this.graphFile = graphFile;
		this.headers = headers;
		this.zetaK = zetaK;
		this.basename = basename;
		this.timestampComparer = timestampComparer;
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
	
	protected long writeTimestampsToFile(ArrayList<Long> currentNeighborsTimestamps, OutputBitStream obs, long minTimestamp) throws IOException {

		// Returns the number of bits appended to the file
		
		// The first timestamp is written with respect to difference
		// in days from the minimum timestamp in the file
		// The rest are written with respect to difference from the previous in the row
		long ret = 0;
		
		long previousNeighborTimestamp = minTimestamp;
		
		for(Long seconds: currentNeighborsTimestamps) {
			long periodsBetween = timestampComparer.timestampsDifference(previousNeighborTimestamp, seconds);
			periodsBetween = Fast.int2nat(periodsBetween);
			previousNeighborTimestamp = seconds;
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
        long minTimestamp = timestampComparer.aggregateMinTimestamp(findMinimumTimestamp());
        
        fileStream = new FileInputStream(graphFile);
        gzipStream = new GZIPInputStream(fileStream);
        decoder = new InputStreamReader(gzipStream, "UTF-8");
        buffered = new BufferedReader(decoder);
        
        if(headers) {
        	// Skip the header of the file
        	buffered.readLine();
        }
        // The file we will write the results to 
        final OutputBitStream obs = new OutputBitStream(new FileOutputStream(basename+".timestamps"), 1024 * 1024);
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
            	currentOffset += writeTimestampsToFile(currentNeighborsTimestamps, obs, minTimestamp);
            	
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
    	currentOffset += writeTimestampsToFile(currentNeighborsTimestamps, obs, minTimestamp);
        
        obs.close();
        buffered.close();

        // Perform compression of the index using EliasFano
        EliasFanoMonotoneLongBigList efmlbl = new EliasFanoMonotoneLongBigList(offsetsIndex);
        FileOutputStream fos = new FileOutputStream(basename+".efindex");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(efmlbl);
        oos.close();
        fos.close();
	}
	
	public void load() throws Exception {
		// Graph
		graph = BVMultiGraph.load(basename);
		// EliasFano index
		FileInputStream fis = new FileInputStream(basename+".efindex");
		ObjectInputStream ois = new ObjectInputStream(fis);
		efindex = (EliasFanoMonotoneLongBigList) ois.readObject();
		ois.close();
		// Timestamps
		fis = new FileInputStream(basename+".timestamps");
		if(fis.getChannel().size() <= Integer.MAX_VALUE) {
			timestamps = new byte[(int) fis.getChannel().size()];
			BinIO.loadBytes(fis, timestamps);
			fis.close();
		}
		InputBitStream ibs = new InputBitStream(timestamps);
		minTimestamp = ibs.readLong(64);
		ibs.close();
	}

	public SuccessorIterator successors(int node) throws Exception {
		return new SuccessorIterator(node);
	}
	
	public class SuccessorIterator implements Iterator<Successor> {

		LazyIntIterator neighborsIterator;
		InputBitStream ibs;
		long previous;
		
		public SuccessorIterator(int node) throws Exception {
			neighborsIterator = graph.successors(node);
			ibs = new InputBitStream(timestamps);
			ibs.position(efindex.getLong(node-1));
			previous = minTimestamp;
		}
	
		@Override
		public boolean hasNext() throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}

		@Override
		public Successor next() throws NoSuchElementException {
			int neighbor = neighborsIterator.nextInt();
			if(neighbor == -1) {
				throw new NoSuchElementException();
			}
			long t;
			try {
				t = Fast.nat2int(ibs.readLongZeta(zetaK));
				t = timestampComparer.reverse(previous, t);
				previous = t;
			}
			catch(IOException e) {
				throw new NoSuchElementException(e.toString());
			}
			return new Successor(neighbor, t);
		}
	}
	
	public String getGraphFile() {
		return graphFile;
	}

	public boolean isHeaders() {
		return headers;
	}

	public int getZetaK() {
		return zetaK;
	}

	public String getBasename() {
		return basename;
	}

	public TimestampComparer getTimestampComparer() {
		return timestampComparer;
	}

	public BVMultiGraph getGraph() {
		return graph;
	}

	public EliasFanoMonotoneLongBigList getEfindex() {
		return efindex;
	}

	public byte[] getTimestamps() {
		return timestamps;
	}

	public long getMinTimestamp() {
		return minTimestamp;
	}
}
