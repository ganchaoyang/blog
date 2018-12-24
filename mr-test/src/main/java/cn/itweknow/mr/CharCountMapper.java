package cn.itweknow.mr;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * @author ganchaoyang
 * @date 2018/12/24 15:31
 * @description
 */
public class CharCountMapper extends Mapper<LongWritable, Text, Text, LongWritable> {
    @Override
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {
        char[] chars = value.toString().toCharArray();
        for (char c : chars) {
            context.write(new Text(c + ""), new LongWritable(1));
        }
    }
}
