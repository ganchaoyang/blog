package cn.itweknow.mr.avro;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.mapred.AvroKey;
import org.apache.avro.mapred.AvroValue;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * @author ganchaoyang
 * @date 2019/1/7 15:55
 * @description
 */
public class StudentAgeMaxReducer extends Reducer<AvroKey<String>, AvroValue<GenericRecord>,
        AvroKey<GenericRecord>, NullWritable> {

    @Override
    protected void reduce(AvroKey<String> key, Iterable<AvroValue<GenericRecord>> values,
                          Context context) throws IOException, InterruptedException {

        GenericRecord max = null;
        for (AvroValue<GenericRecord> value : values) {
            System.out.println(key.datum());
            GenericRecord record = value.datum();
            if (max == null || ((Integer)max.get("age") <
                    (Integer) record.get("age"))) {
                max = new GenericData.Record(SchemaUtil.STUDENT_SCHEMA.getSchema());
                max.put("name", record.get("name"));
                max.put("age", record.get("age"));
                max.put("hobby", record.get("hobby"));
                max.put("class", record.get("class"));
            }
        }
        context.write(new AvroKey<>(max), NullWritable.get());
    }
}
