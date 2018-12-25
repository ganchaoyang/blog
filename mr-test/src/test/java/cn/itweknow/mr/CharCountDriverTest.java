package cn.itweknow.mr;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;
import org.junit.Test;

import java.io.IOException;

/**
 * @author ganchaoyang
 * @date 2018/12/25 15:49
 * @description
 */
public class CharCountDriverTest {

    @Test
    public void driverTest() throws IOException {
        String line1 = "hi";
        String line2 = "hello";
        new MapReduceDriver(new CharCountMapper(), new CharCountReducer())
                .withInput(new LongWritable(0), new Text(line1))
                .withInput(new LongWritable(1), new Text(line2))
                .withOutput(new Text("e"), new LongWritable(1))
                .withOutput(new Text("h"), new LongWritable(2))
                .withOutput(new Text("i"), new LongWritable(1))
                .withOutput(new Text("l"), new LongWritable(2))
                .withOutput(new Text("o"), new LongWritable(1))
                .runTest();
    }

}
