package gr.uoa.di.networkanalysis;

import java.util.Random;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.sux4j.util.EliasFanoMonotoneLongBigList;

public class EliasFanoTest {

    @Test
    public void test() {
        Random random = new Random(23);
        int number = 0;
        int elements = 100;
        IntArrayList list = new IntArrayList();
        for (int i = 0; i < elements; i++) {
            list.add(number);
            number += random.nextInt(5);
        }
        EliasFanoMonotoneLongBigList efmlbl = new EliasFanoMonotoneLongBigList(list);
        System.out.println(efmlbl.numBits());
        Assert.assertTrue(efmlbl.size64() < 4 * 8 * elements);
        System.out.println(efmlbl.stream().collect(Collectors.toList()));
        Assert.assertEquals(elements, efmlbl.size64());

    }

}
