package gr.uoa.di.networkanalysis.llp;

import it.unimi.dsi.law.graph.LayeredLabelPropagation;
import it.unimi.dsi.webgraph.ArcListASCIIGraph;
import it.unimi.dsi.webgraph.BVGraph;
import org.junit.Test;

import java.io.*;
import java.util.zip.GZIPInputStream;

public class TestLLP {

    @Test
    public void computeLLPPermutation() throws FileNotFoundException, IOException {

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

        LayeredLabelPropagation llp = new LayeredLabelPropagation(bvgraph, 23);

        int[] map = llp.computePermutation(new double[]{.05,.1,.15,0.2}, null);

        File file = new File(basename + ".llp.txt");
        // if file doesnt exists, then create it
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);

        fileStream = new FileInputStream("out.flickr-growth.sorted.gz");
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
