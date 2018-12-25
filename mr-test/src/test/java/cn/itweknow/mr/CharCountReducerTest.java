
package cn.itweknow.mr;

import com.sun.org.apache.bcel.internal.generic.NEW;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author ganchaoyang
 * @date 2018/12/25 14:22
 * @description
 */
public class CharCountReducerTest {
    @Test
    public void reducerTest() throws IOException {
        new ReduceDriver<Text, LongWritable, Text, LongWritable>()
                .withReducer(new CharCountReducer())
                .withInput(new Text("h"),
                        Arrays.asList(new LongWritable(1),
                                new LongWritable(1),
                                new LongWritable(1)))
                .withInput(new Text("i"), Arrays.asList(new LongWritable(1)))
                .withOutput(new Text("h"), new LongWritable(3))
                .withOutput(new Text("i"), new LongWritable(1))
                .runTest();
    }
}
