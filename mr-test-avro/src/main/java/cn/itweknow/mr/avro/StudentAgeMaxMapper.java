package cn.itweknow.mr.avro;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.mapred.AvroKey;
import org.apache.avro.mapred.AvroValue;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * @author ganchaoyang
 * @date 2019/1/7 15:55
 * @description
 */
public class StudentAgeMaxMapper extends Mapper<LongWritable, Text,
        AvroKey<String>, AvroValue<GenericRecord>> {

    private GenericRecord record = new GenericData.Record(SchemaUtil.STUDENT_SCHEMA.getSchema());

    private StudentRecordParser parser = new StudentRecordParser();

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException,
            InterruptedException {
        parser.parse(value);
        if (parser.isValid()) {
            // 数据合法。
            record.put("name", parser.getName());
            record.put("age", parser.getAge());
            record.put("hobby", parser.getHobby());
            record.put("class", parser.getClazz());
            context.write(new AvroKey<>(parser.getClazz()), new AvroValue<>(record));
        }
    }
}
