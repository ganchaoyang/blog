&nbsp;&nbsp;&nbsp;&nbsp;MapReduce是一种编程模型，"Map（映射）"和"Reduce（归约）"，是它们的主要思想，我们通过Map函数来分布式处理输入数据，然后通过Reduce汇总结果并输出。其实这个概念有点类似于我们Java8中的StreamApi，有兴趣的同学也可以去看看。  
&nbsp;&nbsp;&nbsp;&nbsp;MapReduce任务过程分为两个处理阶段，map阶段和reduce阶段。每个阶段都以键-值对作为输入输出，键和值的类型由我们自己指定。通常情况map的输入内容键是LongWritable类型，为某一行起始位置相对于文件起始位置的偏移量；值是Text类型，为该行的文本内容。  
### 前提条件
> * 一个maven项目。
> * 一台运行着hadoop的linux机器或者虚拟机，当然了hadoop集群也可以，如果你还没有的话可以[戳这里。](https://itweknow.cn/detail?id=54)

&nbsp;&nbsp;&nbsp;&nbsp;我们编写一个MapReduce程序的一般步骤是：（1）map程序。（2）reduce程序。（3）程序驱动。下面我们就根据这个顺序来写一个简单的示例，这个例子是用来统计文件中每个字符出现的次数并输出。
### 项目依赖
我们先来解决一下依赖问题，在`pom.xml`中添加如下内容。

```xml
<dependency>
    <groupId>org.apache.hadoop</groupId>
    <artifactId>hadoop-client</artifactId>
    <version>${hadoop.version}</version>
</dependency>
```
### Map程序
&nbsp;&nbsp;&nbsp;&nbsp;我们继承`Mapper`类并重写了其map方法。Map阶段输入的数据是从hdfs中拿到的原数据，输入的key为某一行起始位置相对于文件起始位置的偏移量，value为该行的文本。​​输出的内容同样也为键-值对，这个时候输出数据的键值对的类型可以自己指定，在本例中key是Text类型的，value是LongWritable类型的。输出的结果将会被发送到reduce函数进一步处理。

```java
public class cn.itweknow.mr.CharCountMapper extends Mapper<LongWritable, Text, Text, LongWritable> {
    @Override
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {
        // 将这一行文本转为字符数组
        char[] chars = value.toString().toCharArray();
        for (char c : chars) {
            // 某个字符出现一次，便输出其出现1次。
            context.write(new Text(c + ""), new LongWritable(1));
        }
    }
}
```

### Reduce程序
&nbsp;&nbsp;&nbsp;&nbsp;我们继承`Reducer`类并重写了其reduce方法。在本例中Reduce阶段的输入是Map阶段的输出，输出的结果可以作为最终的输出结果。相信你也注意到了，reduce方法的第二个参数是一个Iterable，MapReduce会将map阶段中相同字符的输出汇总到一起作为reduce的输入。

```java
public class cn.itweknow.mr.CharCountReducer extends Reducer<Text, LongWritable, Text, LongWritable> {

    @Override
    protected void reduce(Text key, Iterable<LongWritable> values, Context context)
            throws IOException, InterruptedException {
        long count = 0;
        for (LongWritable value : values) {
            count += value.get();
        }
        context.write(key, new LongWritable(count));
    }
}
```

### 驱动程序
&nbsp;&nbsp;&nbsp;&nbsp;到目前为止，我们已经有了map程序和reduce程序，我们还需要一个驱动程序来运行整个作业。可以看到我们在这里初始化了一个Job对象。Job对象指定整个MapReduce作业的执行规范。我们用它来控制整个作业的运作，在这里我们指定了jar包位置还有我们的`Map`程序、`Reduce`程序、`Map`程序的输出类型、整个作业的输出类型还有输入输出文件的地址。

```java
public class cn.itweknow.mr.CharCountDriver {

    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        Job job = Job.getInstance(configuration);
        // Hadoop会自动根据驱动程序的类路径来扫描该作业的Jar包。
        job.setJarByClass(cn.itweknow.mr.CharCountDriver.class);

        // 指定mapper
        job.setMapperClass(CharCountMapper.class);
        // 指定reducer
        job.setReducerClass(CharCountReducer.class);

        // map程序的输出键-值对类型
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LongWritable.class);

        // 输出键-值对类型
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongWritable.class);

        // 输入文件的路径
        FileInputFormat.setInputPaths(job, new Path(args[0]));
        // 输入文件路径
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        boolean res = job.waitForCompletion(true);
        System.exit(res?0:1);
    }
}
```
&nbsp;&nbsp;&nbsp;&nbsp;你会发现我们初始化了一个空的Configuration，但是并没有进行任何的配置，其实当我们将其运行在一个运行着hadoop的机器上时，它会默认使用我们机器上的配置。在后续的文章中我也会写一下如何在程序中进行配置。

### 执行MapReduce作业
1. 打包作业，我们需要将我们的MapReduce程序打成jar包。

    ```java
    mvn package -Dmaven.test.skip=true
    ```
    生成的jar包我们可以在target目录下找到。  
2. 将jar包复制到hadoop机器上。
3. 在HDFS上准备好要统计的文件，我准备的文件在HDFS上的`/mr/input/`目录下，内容如下。
    ```
    hello hadoop hdfs.I am coming.
    ```
4. 执行jar

    ```bash
    hadoop jar mr-test-1.0-SNAPSHOT.jar cn.itweknow.mr.CharCountDriver /mr/input/ /mr/output/out.txt
    ```
5. 查看结果  
    先查看输出目录，结果如下，最终输出的结果就存放在`/mr/output/part-r-00000`文件中。
    ```bash
    root@test:~# hadoop fs -ls /mr/output
    Found 2 items
    -rw-r--r--   1 root supergroup          0 2018-12-24 10:33 /mr/output/_SUCCESS
    -rw-r--r--   1 root supergroup         68 2018-12-24 10:33 /mr/output/part-r-00000
    ```
    查看结果文件的具体内容：
    ```bash
    root@test:~# hadoop fs -cat /mr/output/part-r-00000
     	4
    .	2
    I	1
    a	2
    c	1
    d	2
    e	1
    f	1
    g	1
    h	3
    i	1
    l	2
    m	2
    n	1
    o	4
    p	1
    s	1
    ```
    
最后，送上本文的源码地址，[戳这里哦。](https://github.com/ganchaoyang/blog/tree/master/mr-test)