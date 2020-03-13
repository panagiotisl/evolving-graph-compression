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
import java.util.zip.GZIPInputStream;

import org.junit.Test;

import com.google.common.io.Files;

import it.unimi.dsi.webgraph.ArcListASCIIGraph;
import it.unimi.dsi.webgraph.BVGraph;

public class WebGraphSizeTest {

	@Test
	public void testWebGraphSize() throws IOException {
		
		InputStream fileStream = new FileInputStream("src/main/resources/flickr-growth/out.flickr-growth.sorted.gz");
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
	}
	
}
