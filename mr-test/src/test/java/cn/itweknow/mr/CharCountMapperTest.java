package cn.itweknow.mr;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
// import org.apache.hadoop.mrunit.MapDriver;
// https://blog.csdn.net/Xiblade/article/details/80559683
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.junit.Test;

import java.io.IOException;

/**
 * @author ganchaoyang
 * @date 2018/12/25 13:52
 * @description
 */
public class CharCountMapperTest {

    @Test
    public void mapperTest() throws IOException {
        Text value = new Text("hi hadoop");
        new MapDriver<LongWritable, Text, Text, LongWritable>()
                .withMapper(new CharCountMapper())
                .withInput(new LongWritable(0), value)
                .withOutput(new Text("h"), new LongWritable(1))
                .withOutput(new Text("i"), new LongWritable(1))
                .withOutput(new Text(" "), new LongWritable(1))
                .withOutput(new Text("h"), new LongWritable(1))
                .withOutput(new Text("a"), new LongWritable(1))
                .withOutput(new Text("d"), new LongWritable(1))
                .withOutput(new Text("o"), new LongWritable(1))
                .withOutput(new Text("o"), new LongWritable(1))
                .withOutput(new Text("p"), new LongWritable(1))
                .runTest();
    }
}
