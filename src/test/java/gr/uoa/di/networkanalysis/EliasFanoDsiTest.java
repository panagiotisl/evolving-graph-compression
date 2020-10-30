package gr.uoa.di.networkanalysis;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.sux4j.util.EliasFanoMonotoneLongBigList;

public class EliasFanoDsiTest {

    public void test() throws Exception {
        Random random = new Random(23);
        int number = 0;
        int elements = 100;
        IntArrayList list = new IntArrayList();
        list.add(10);
        list.add(11);
        list.add(11);
        EliasFanoMonotoneLongBigList efmlbl = new EliasFanoMonotoneLongBigList(list);
        FileOutputStream fos = new FileOutputStream("myfile");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(efmlbl);
        oos.close();
        fos.close();
        System.out.println(efmlbl.numBits());
        Assert.assertTrue(efmlbl.size64() < 4 * 8 * elements);
        System.out.println(efmlbl.stream().collect(Collectors.toList()));
        Assert.assertEquals(elements, efmlbl.size64());
    }
    
    @Test
    public void testRead() throws Exception {
    	FileInputStream fis = new FileInputStream("wiki.llp.efindex");
		ObjectInputStream ois = new ObjectInputStream(fis);
		EliasFanoMonotoneLongBigList efindex = (EliasFanoMonotoneLongBigList) ois.readObject();
		ois.close();
		long previous = efindex.getLong(10);
		for(int i = 11; i < efindex.size64(); i++) {
			if(previous == efindex.getLong(i)) {
				System.out.println("SAME " + i);
			}
			previous = efindex.getLong(i);
		}
		
    }

}
