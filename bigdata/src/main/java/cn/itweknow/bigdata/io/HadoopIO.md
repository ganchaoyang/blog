### HDFS的数据完整性
HDFS会对写入的所有数据计算校验和，并在读取数据时验证校验和。我们在写入数据的时候最后一个处理的datanode会去验证校验和，如果错误的话会抛出IOException的子异常。且每个datanode都会记录每次验证校验和的日志，这样可以方便我们排查错误发生的时间。datanode还会主动的定期验证存储在该datanode上所有数据的校验和，当发现错误时会向namenode汇报并进行数据修复。
### 压缩
#### codec
* CompressionCodec接口  
CodepressionCodec里有两个方法，分别是createOutputStream(OutputStream out)和createInputStream(InputStream in)，前者是对写入输出数据流的数据进行压缩，而后者则是对输入数据流的数据进行解压缩。​一个CodepressionCodec的实现对应着一种压缩-解压缩算法。  

| 压缩格式 | 后缀 | 对应的CompressionCodec实现 | 是否可切分 |   
| ------ | ------ | ------ | ------ |  
| DEFALATE | .deflate | org.apache.hadoop.io.compress​.DefaultCodec  | 否 |   
|  gzip | .gz | org.apache.hadoop.io.compress​.GzipCodec | 否 |   
| bzip2 | .bz2 | org.apache.hadoop.io.compress​.BZip2Codec | 是 |   
|  LZ4  | .lz4 | org.apache.hadoop.io.compress​.Lz4Codec  | 否 |   
| Snappy | .snappy | org.apache.hadoop.io.compress​.SnappyCodec​​​​ | 否 |  
#### 通过CompressionCodecFactory推断CompressionCodec
CompressionCodecFactory.getCodec()方法可以根据文件的扩展名来推断要使用那种CompressionCodec的实现。

```java
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
```
代码中包含的某些变量的定义我没有粘过来，如果需要测试的话可以[戳这里]()查看完整的代码。
#### 压缩和输入分片
DEFALATE,gzip,bzip2,LZO,LZ4,Snappy几种压缩格式中，只有bzip2是支持切分的。对于大文件来说最好不要使用不支持切分整个文件的压缩格式，因为这么做会失去数据的本地特性，大量的网络传输进而造成MapReduce应用效率低下。​
#### 在MapReduce中使用压缩
&nbsp();&nbsp();如果MapReduce程序的输入文件是压缩的，那么MapReduce程序会使用上面通过CompressionCodecFactory推断CompressionCodec的方式推断出对应的codec，然后再读取文件的时候自动解压缩文件。
&nbsp();&nbsp();如果我们想压缩MapReduce作业的输入数据的话，则还需要进行相应的配置，具体如何配置我会在后面的文章中进行说明，期待吧。​
### 序列化
序列化指将结构化的对象转化为字节流以便在网络上传输或写到磁盘存储的过程。与之对应的反序列化就是将字节流转换成结构化对象的过程。
### Writeable接口
Hadoop使用的是自己的序列化格式Writable，紧凑且速度快，但是对Java之外的其他语言并不是很友好。Hadoop本身提供了很多Writable类，Java的基本类型都有被封装，正对String的Text，BytesWritable，NullWritable，ArrayWritable等等。
* 实现定制的Writeable  
&nbsp();&nbsp();这里我们定制一个UserWriteable对象，包含了name和address两个属性。

```java
public class UserWriteable implements Writable {

    private String name;

    private String address;

    public String getName() {
        return name;
    }

    public UserWriteable setName(String name) {
        this.name = name;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public UserWriteable setAddress(String address) {
        this.address = address;
        return this;
    }

    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeChars(name);
        dataOutput.writeChars(address);
    }

    public void readFields(DataInput dataInput) throws IOException {
        // 先进先出原则，与write方法对应
        name = dataInput.readLine();
        address = dataInput.readLine();
    }
}
```
* write
    * 将对象的属性写入二进制流（DataOutput）。
* readFields
    * 从二进制流（DataInput）中还原对象属性。
* 为什么不用Java的序列化机制
    * 该机制与语言密切相关。
    * 太复杂。

