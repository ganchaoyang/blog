import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;

/**
 * @author ganchaoyang
 * @date 2019/1/4 15:55
 * @description
 */
public class AvroTest {

    @Test
    public void write() throws IOException {
        Schema.Parser parser = new Schema.Parser();
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("User.avsc");
        Schema schema = parser.parse(in);
        GenericRecord record = new GenericData.Record(schema);
        record.put("name", "ganchaoyang");
        record.put("age", 23);

        File file = new File("result.avro");
        DatumWriter<GenericRecord> writer = new GenericDatumWriter<GenericRecord>(schema);
        try(DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<GenericRecord>(writer)) {
            dataFileWriter.create(schema, file);
            dataFileWriter.append(record);
        }
    }


    @Test
    public void read() throws IOException {
        File file = new File("result.avro");
        DatumReader<GenericRecord> reader = new GenericDatumReader<>();
        try (DataFileReader<GenericRecord> dataFileReader = new DataFileReader<GenericRecord>(file, reader)) {
            GenericRecord record;
            while (dataFileReader.hasNext()) {
                record = dataFileReader.next();
                Assert.assertEquals("ganchaoyang", record.get("name").toString());
                Assert.assertEquals(23, record.get("age"));
            }
        }
    }
}
