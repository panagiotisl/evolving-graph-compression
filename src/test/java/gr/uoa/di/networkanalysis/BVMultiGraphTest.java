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
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

import org.junit.Test;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.webgraph.ArcListASCIIGraph;
import org.junit.Assert;

public class BVMultiGraphTest {

    @Test
    public void testWebGraphSize() throws IOException {

        InputStream fileStream = new FileInputStream("out.test.gz");
        //        InputStream fileStream = new FileInputStream("out.flickr-growth.sorted.gz");
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

        String basename = "test";

        ArcListASCIIGraph inputGraph = new ArcListASCIIGraph(new FileInputStream(tempFile), 0);
        BVMultiGraph.store(inputGraph, basename);
        BVMultiGraph bvgraph = BVMultiGraph.load(basename);

        Assert.assertEquals(52, bvgraph.numNodes());
        Assert.assertEquals(33, bvgraph.numArcs());

        System.out.println(Arrays.toString(bvgraph.successorArray(1)));
        System.out.println(Arrays.toString(bvgraph.successorArray(2)));
        System.out.println(Arrays.toString(bvgraph.successorArray(3)));
        System.out.println(Arrays.toString(bvgraph.successorArray(4)));
        System.out.println(Arrays.toString(bvgraph.successorArray(5)));
    }

    @Test
    public void testIntervalizeMultiples() {

        int[] extras = {4, 4, 4, 6, 7, 8, 8};
        IntArrayList left = new IntArrayList();
        IntArrayList leftExpected = new IntArrayList();
        leftExpected.add(4);
        leftExpected.add(8);
        IntArrayList len = new IntArrayList();
        IntArrayList lenExpected = new IntArrayList();
        lenExpected.add(3);
        lenExpected.add(2);
        IntArrayList residuals = new IntArrayList();
        IntArrayList residualsExpected = new IntArrayList();
        residualsExpected.add(6);
        residualsExpected.add(7);
        int nMultiples = BVMultiGraph.intervalizeMultiples(extras, extras.length, left, len, residuals);

        Assert.assertEquals(2, nMultiples);
        Assert.assertEquals(leftExpected, left);
        Assert.assertEquals(lenExpected, len);
        Assert.assertEquals(residualsExpected, residuals);

    }

}
