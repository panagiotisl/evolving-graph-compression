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
import it.unimi.dsi.law.graph.LayeredLabelPropagation;
import it.unimi.dsi.sux4j.util.EliasFanoMonotoneLongBigList;
import it.unimi.dsi.webgraph.ArcListASCIIGraph;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;

public class EvolvingMultiGraph {

	protected String graphFile;
	protected boolean headers;
	protected int zetaK;
	protected String basename;
	protected long aggregationFactor;
	
	protected BVMultiGraph graph;
	protected EliasFanoMonotoneLongBigList efindex;
	protected byte[] timestamps;
	protected long minTimestamp;
	
	public EvolvingMultiGraph(String graphFile, boolean headers, int zetaK, String basename, long aggregationFactor) {
		super();
		this.graphFile = graphFile;
		this.headers = headers;
		this.zetaK = zetaK;
		this.basename = basename;
		this.aggregationFactor = aggregationFactor;
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
		long ret = 0;	
		long previousNeighborTimestamp = minTimestamp;
		
		for(Long seconds: currentNeighborsTimestamps) {
			long periodsBetween = TimestampComparerAggregator.timestampsDifference(previousNeighborTimestamp, seconds, aggregationFactor);
			periodsBetween = Fast.int2nat(periodsBetween);
			previousNeighborTimestamp = seconds;
			ret += obs.writeLongZeta(periodsBetween, zetaK);
		}
		
		return ret;
	}
	
	public void store() throws IOException {
		storeBVMultiGraph();
		storeTimestampsAndIndex();
	}
	
	public void storeBVMultiGraph() throws IOException {
		
		InputStream fileStream = new FileInputStream(graphFile);
        InputStream gzipStream = new GZIPInputStream(fileStream);
        Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
        BufferedReader buffered = new BufferedReader(decoder);
        File tempFile = File.createTempFile("temp", ".txt");
        tempFile.deleteOnExit();

        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        String line;
        if(headers) {
        	buffered.readLine();
        }
        
        // While generating the tmp file for the arc list, find the minimum timestamp of the file
        Long tmpMinTimestamp = null;
        while ((line = buffered.readLine()) != null) {
            String[] tokens = line.split("\\s");
           	writer.write(String.format("%s\t%s\n", tokens[0], tokens[1]));
           	long timestamp = Long.parseLong(tokens[3]);
            if(tmpMinTimestamp == null || timestamp < tmpMinTimestamp) {
            	tmpMinTimestamp = timestamp;
            }
        }
        
        minTimestamp = tmpMinTimestamp;
           
        writer.close();
        buffered.close();

        ArcListASCIIGraph inputGraph = new ArcListASCIIGraph(new FileInputStream(tempFile), 0);
        BVMultiGraph.store(inputGraph, basename);
	}
	
	public void storeTimestampsAndIndex() throws IOException {
		
		// Aggregate the minimum timestamp of the ile
        minTimestamp = TimestampComparerAggregator.aggregateMinTimestamp(minTimestamp, aggregationFactor);
        
        InputStream fileStream = new FileInputStream(graphFile);
        GZIPInputStream gzipStream = new GZIPInputStream(fileStream);
        InputStreamReader decoder = new InputStreamReader(gzipStream, "UTF-8");
        BufferedReader buffered = new BufferedReader(decoder);
        
        if(headers) {
        	buffered.readLine();
        }
        // The file we will write the results to 
        final OutputBitStream obs = new OutputBitStream(new FileOutputStream(basename+".timestamps"), 1024 * 1024);
        LongArrayList offsetsIndex= new LongArrayList();
        long currentOffset = obs.writeLong(minTimestamp, 64);

        int currentNode = 0;
        ArrayList<Long> currentNeighborsTimestamps = new ArrayList<>();
        String line;
        
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
            	
            	// If at least one node was skipped, add the current offset to the index for each node in-between
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
	
	public boolean isNeighbor(int node, int neighbor) {
		LazyIntIterator it = graph.successors(node);
		int n = -1;
		while((n = it.nextInt()) != -1) {
			return true;
			//			if(n == neighbor) return true;
//			else if(n > neighbor) return false;
		}
		
		return false;
	}
	
	public boolean isNeighbor(int node, int neighbor, long t1, long t2) throws Exception {
		LazyIntIterator it = graph.successors(node);
		int n = -1, from = -1, to = -1, pos = 0;
		long t;
		// Find  the starting position
		while((n = it.nextInt()) != -1) {
			if(n == neighbor) {
				from = pos++;
				break;
			}
		}
		// Return false if it was not found at least once
		if(from == -1) {
			return false;
		}
		// Find the ending position
		while((n = it.nextInt()) == neighbor) {
			pos++;
		}
		to = pos-1;
		
		InputBitStream ibs = new InputBitStream(timestamps);
		ibs.position(efindex.getLong(node));
		// Skip everything up to from
		long previous = minTimestamp;
		for(int i =0; i < from; i++) {
			t = Fast.nat2int(ibs.readLongZeta(zetaK));
			t = TimestampComparerAggregator.reverse(previous, t, aggregationFactor);
			previous = t;
		}
		// Scan all the timestamps in the range from->to
		for(int i = from; i <= to; i++) {
			t = Fast.nat2int(ibs.readLongZeta(zetaK));
			t = TimestampComparerAggregator.reverse(previous, t, aggregationFactor);
			if(t1 <= t && t <= t2) return true;
			previous = t;
		}
		
		ibs.close();
		
		return false;
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
			ibs.position(efindex.getLong(node));
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
				t = TimestampComparerAggregator.reverse(previous, t, aggregationFactor);
				previous = t;
			}
			catch(IOException e) {
				throw new NoSuchElementException(e.toString());
			}
			return new Successor(neighbor, t);
		}
	}
	
	// Stores a multigraph as a BVGraph without repetition of edges
	// Serves as an in-between step for extracting an LLP mapping for multigraphs
	public static void storeAsBVGraph(String multigraphFile, String basename, boolean headers) throws IOException {
		
		InputStream fileStream = new FileInputStream(multigraphFile);
        InputStream gzipStream = new GZIPInputStream(fileStream);
        Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
        BufferedReader buffered = new BufferedReader(decoder);
        File tempFile = File.createTempFile("temp", ".txt");
        tempFile.deleteOnExit();

        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        String line;
        if(headers) {
        	buffered.readLine();
        }
        long previousNode = -1;
        long previousNeighbor = -1;
        
        if(headers)
        	buffered.readLine();
        
        while ((line = buffered.readLine()) != null) {
            String[] tokens = line.split("\\s");
            long currentNode = Long.parseLong(tokens[0]);
            long currentNeighbor = Long.parseLong(tokens[1]);
            
           	if(!(currentNode == previousNode && currentNeighbor == previousNeighbor)) {
           		writer.write(String.format("%s\t%s\n", tokens[0], tokens[1]));
           		previousNode = currentNode;
           		previousNeighbor = currentNeighbor;
           	}
        }
        
          
        writer.close();
        buffered.close();

        ArcListASCIIGraph inputGraph = new ArcListASCIIGraph(new FileInputStream(tempFile), 0);
        BVGraph.store(inputGraph, basename);
	}
	
	public static int[] applyLLP(String graphFile, String basename, boolean headers, BVGraph bvgraph, double[] gammas) throws Exception {

		InputStream fileStream;
		InputStream gzipStream;
		Reader decoder;
		BufferedReader buffered;
		String line;
		
        LayeredLabelPropagation llp = new LayeredLabelPropagation(bvgraph, 23);

        int[] map = llp.computePermutation(gammas, null);

        
        File tempFile = new File(basename+".llp.txt");

        BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));

        fileStream = new FileInputStream(graphFile);
        gzipStream = new GZIPInputStream(fileStream);
        decoder = new InputStreamReader(gzipStream, "UTF-8");
        buffered = new BufferedReader(decoder);

        if(headers)
        	line = buffered.readLine();
        
        while ((line = buffered.readLine()) != null) {
            String[] splits = line.split("\\s+");
            int node1 = Integer.parseInt(splits[0]);
            int node2 = Integer.parseInt(splits[1]);
            if(node1 == 371) {
            	System.out.println("Mapping:");
            	System.out.println(node1+" "+node2);
            	System.out.println(map[node1]+" "+map[node2]);
            }
            bw.write(
                    map[Integer.parseInt(splits[0])] + "\t" +
                            map[Integer.parseInt(splits[1])] + "\t" +
                            splits[2] + "\t"
                            + splits[3] + "\n");
        }

        bw.close();
        buffered.close();
        
        int exitCode = new ProcessBuilder("/bin/sh", "-c", "gzip "+tempFile.getAbsolutePath()).start().waitFor();
        if(exitCode != 0) {
        	throw new Exception("could not zip mapped file. exit code: "+exitCode);
        }
        
        String sortThenZip = "zcat "+tempFile.getAbsolutePath()+".gz|sort -k1,1n -k2,2n "+"| gzip > "+basename+".llp.sorted.txt.gz";
        exitCode = new ProcessBuilder("/bin/sh", "-c", sortThenZip).start().waitFor();
        if(exitCode != 0) {
        	throw new Exception("could not sort and then zip the mapped zipped file. exit code: "+exitCode);
        }
        
        exitCode = new ProcessBuilder("/bin/sh", "-c", "rm -f "+basename+".llp.txt.gz").start().waitFor();
        if(exitCode != 0) {
        	throw new Exception("could not delete non sorted llp file. exit code: "+exitCode);
        }
        
        return map;
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
