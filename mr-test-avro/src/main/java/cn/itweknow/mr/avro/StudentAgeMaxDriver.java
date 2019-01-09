package cn.itweknow.mr.avro;

import org.apache.avro.Schema;
import org.apache.avro.mapreduce.AvroJob;
import org.apache.avro.mapreduce.AvroKeyOutputFormat;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.InputStream;

/**
 * @author ganchaoyang
 * @date 2019/1/7 19:19
 * @description
 */
public class StudentAgeMaxDriver {

    public static void main(String[] args) throws Exception {


        Configuration configuration = new Configuration();
        configuration.setBoolean(Job.MAPREDUCE_JOB_USER_CLASSPATH_FIRST, true);
        Job job = Job.getInstance(configuration);

        job.setJarByClass(StudentAgeMaxDriver.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        AvroJob.setMapOutputKeySchema(job, Schema.create(Schema.Type.STRING));
        AvroJob.setMapOutputValueSchema(job, SchemaUtil.STUDENT_SCHEMA.getSchema());
        AvroJob.setOutputKeySchema(job, SchemaUtil.STUDENT_SCHEMA.getSchema());

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(AvroKeyOutputFormat.class);

        job.setMapperClass(StudentAgeMaxMapper.class);
        job.setReducerClass(StudentAgeMaxReducer.class);

        System.exit(job.waitForCompletion(true)?0:1);


    }

}
