package cn.itweknow.mr.avro;

import org.apache.avro.Schema;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author ganchaoyang
 * @date 2019/1/7 16:40
 * @description
 */
public enum  SchemaUtil {

    // 学生Avro模式
    STUDENT_SCHEMA("Student.avsc");

    private Schema schema;

    public Schema getSchema() {
        return schema;
    }


    SchemaUtil(String avscPath) {
        try {
            this.schema = new Schema.Parser().parse(
                    this.getClass().getClassLoader()
                            .getResourceAsStream(avscPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
