package gr.uoa.di.networkanalysis.llp;

import it.unimi.dsi.law.graph.LayeredLabelPropagation;
import it.unimi.dsi.webgraph.ArcListASCIIGraph;
import it.unimi.dsi.webgraph.BVGraph;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

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
    public void computeLLPPermutation() throws FileNotFoundException, IOException {

        ClassLoader classLoader = getClass().getClassLoader();
        String graphFileResourcePath = classLoader.getResource(graphFile).getPath();

        InputStream fileStream = new FileInputStream(graphFileResourcePath);
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

        LayeredLabelPropagation llp = new LayeredLabelPropagation(bvgraph, 23);

        int[] map = llp.computePermutation(new double[]{.05,.1,.15,0.2}, null);
        int[] reverse_map = new int[map.length];
        for(int i = 0; i < map.length; i++) {
            reverse_map[map[i]] = i;
        }

        System.out.println(map[100]);
        System.out.println(reverse_map[map[100]]);

        if(true) return;

        File file = new File(basename + ".llp.txt");
        // if file doesnt exists, then create it
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);

        fileStream = new FileInputStream(graphFileResourcePath);
        gzipStream = new GZIPInputStream(fileStream);
        decoder = new InputStreamReader(gzipStream, "UTF-8");
        buffered = new BufferedReader(decoder);

        writer = new BufferedWriter(new FileWriter(tempFile));
        line = buffered.readLine();
        while ((line = buffered.readLine()) != null) {
            String[] splits = line.split("\\s");
            bw.write(
                    map[Integer.parseInt(splits[0])] + "\t" +
                            map[Integer.parseInt(splits[1])] + "\t" +
                            splits[3] + "\t"
                            + splits[4] + "\n");
        }

        bw.close();
        buffered.close();

    }
}
