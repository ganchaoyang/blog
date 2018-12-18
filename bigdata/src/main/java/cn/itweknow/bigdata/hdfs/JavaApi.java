package cn.itweknow.bigdata.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;

/**
 * @author ganchaoyang
 * @date 2018/12/18 11:15
 * @description
 */
public class JavaApi {

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
     * 读取hdfs中文件的内容。
     * @throws IOException
     */
    @Test
    public void read() throws IOException {
        // 文件地址。
        URI uri = URI.create("/test/api/test.txt");
        // 用于接收读取的数据流。
        FSDataInputStream in = null;
        try {
            in = fs.open(new Path(uri));
            // 最后的一个boolean类型的参数是指是否在调用结束后关闭流，我们这里选择在finally里面手动关闭。
            IOUtils.copyBytes(in, System.out, 4096, false);
            in.seek(0);
            IOUtils.copyBytes(in, System.out, 4096, false);
        } finally {
            IOUtils.closeStream(in);
        }
    }

    /**
     * 创建目录
     * @throws IOException
     */
    @Test
    public void mkdir() throws IOException {
        fs.mkdirs(new Path("/test/api"));
    }

    /**
     * 查看文件系统目录文件信息
     * @throws IOException
     */
    @Test
    public void ls() throws IOException {
        FileStatus[] fileStatuses = fs.listStatus(new Path("/"));
        if (null == fileStatuses || fileStatuses.length == 0) {
            return;
        }
        for (FileStatus fileStatus : fileStatuses) {
            System.out.println(fileStatus.getPath() + "   " + fileStatus.getPermission());
        }
    }

    /**
     * 删除目录或者文件
     * @throws IOException
     */
    @Test
    public void delete() throws IOException {
        fs.delete(new Path("/test/api"), false);
    }

    /**
     * 删除非空目录
     * @throws IOException
     */
    @Test
    public void deleteNonEmptyDir() throws IOException {
        fs.delete(new Path("/test"), true);
    }

    /**
     * 创建文件
     * @throws IOException
     */
    @Test
    public void create() throws IOException {
        FSDataOutputStream out = null;
        try {
            out = fs.create(new Path("/test/api/test.txt"));
            out.writeChars("hello hdfs.");
        } finally {
            IOUtils.closeStream(out);
        }
    }

    /**
     * 写入文件
     * @throws IOException
     */
    @Test
    public void append() throws IOException {
        FSDataOutputStream out = null;
        try {
            out = fs.append(new Path("/test/api/test.txt"));
            out.writeChars("hello hdfs2.");
            out.flush();
        } finally {
            IOUtils.closeStream(out);
        }
    }

    /**
     * 从本地复制文件到HDFS
     * @throws IOException
     */
    @Test
    public void copyFromLocal() throws IOException {
        fs.copyFromLocalFile(new Path("d:/local.txt"), new Path("/test/api"));
    }

    /**
     * 从HDFS上下载文件到本地
     * @throws IOException
     */
    @Test
    public void copyToLocal() throws IOException {
        fs.copyToLocalFile(new Path("/test/api/local.txt"), new Path("E:/"));
    }

}
