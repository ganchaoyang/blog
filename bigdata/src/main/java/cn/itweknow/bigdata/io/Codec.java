package cn.itweknow.bigdata.io;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.compress.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * @author ganchaoyang
 * @date 2018/12/21 11:10
 * @description
 */
public class Codec {

    FileSystem fs = null;
    Configuration configuration = null;

    @Before
    public void init() throws Exception {
        configuration = new Configuration();
        configuration.set("fs.defaultFS", "hdfs://192.168.142.9:9000");
        configuration.set("dfs.replication", "1");
        // hdfs也是有一套权限体系的。
        // 这边为了可以在windows环境下测试，设置一下用户。
        fs = FileSystem.get(new URI("hdfs://192.168.142.9:9000"), configuration, "root");
    }

    /**
     * 根据文件名选取codec解压缩文件。
     * @throws IOException
     */
    @Test
    public void fileDecompressor() throws IOException {
        FSDataInputStream in = null;
        try {
            CompressionCodecFactory factory = new CompressionCodecFactory(configuration);
            Path inPath = new Path("/test/codec/test.txt.gz");
            CompressionCodec codec = factory.getCodec(inPath);
            in = fs.open(new Path("/test/codec/test.txt.gz"));
            IOUtils.copyBytes(codec.createInputStream(in), System.out, 4096);
        } finally {
            in.close();
        }
    }


}
