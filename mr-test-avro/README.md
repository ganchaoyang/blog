上篇文章我们简要介绍了一下Avro是啥，以及其几种数据类型。那么通过这篇文章我们一起来实践一下Avro在MapReduce中的使用。
### 前提条件

> 一个maven项目
> Hadoop集群，如果你还没有安装的话，[请戳这里](https://itweknow.cn/detail?id=67)，查看之前的文章。
> 

### 说明
本篇文章是一个简单的用例，使用的例子是一个txt文件中存储了大量的学生信息，这些学生有姓名、年龄、爱好和班级信息，我们要做的事情就是通过MapReduce程序找到各个班级年龄最大的学生。

### 项目依赖
我们需要hadoop以及avro相关的包。
```xml
<dependency>
    <groupId>org.apache.hadoop</groupId>
    <artifactId>hadoop-client</artifactId>
    <version>2.8.5</version>
</dependency>

<dependency>
    <groupId>org.apache.avro</groupId>
    <artifactId>avro</artifactId>
    <version>1.8.2</version>
</dependency>

<dependency>
    <groupId>org.apache.avro</groupId>
    <artifactId>avro-mapred</artifactId>
    <version>1.8.2</version>
</dependency>

```

### Avro模式
前面也说到了每个学生有姓名、年龄、爱好、班级四个字段的信息，所以我们定义了如下的Avro模式来描述一个学生。命名为Student.avsc，存放在resources目录下。
```
{
    "type": "record",
    "name": "StudentRecord",
    "doc": "A student",
    "fields": [
        {"name": "name", "type": "string"},
        {"name": "age", "type": "int"},
        {"name": "hobby", "type": "string"},
        {"name": "class", "type": "string"}
    ]
}
```

### Mapper和Reducer

* Mapper  

```java
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
```

上面的代码中你可以看到我们自定义了一个StudentRecordParser的类来解析一行记录，由于篇幅的原因这里就不展示了，你可以在后面提供的源码中找到。其实不难看出，Map程序主要做的事情就是将我们存放在txt中的记录解析成一个个的GenericRecord对戏，然后以班级名称为键，record为值传递给Reducer做进一步处理。

* Reducer

```java
public class StudentAgeMaxReducer extends Reducer<AvroKey<String>, AvroValue<GenericRecord>,
        AvroKey<GenericRecord>, NullWritable> {

    @Override
    protected void reduce(AvroKey<String> key, Iterable<AvroValue<GenericRecord>> values,
                          Context context) throws IOException, InterruptedException {

        GenericRecord max = null;
        for (AvroValue<GenericRecord> value : values) {
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
```

Reducer的逻辑其实也比较简单，就是通过循环比较的方式找到年龄最大的学生。

### 驱动程序

```java
public class StudentAgeMaxDriver {

    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        // 注释1：为了解决在Hadoop集群中运行时我们使用的Avro版本和集群中Avro版本不一致的问题。
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
```
和之前的MapReduce实战中实例比较，我们这里使用AvroJob来配置作业，AvroJob类主要用来给输入、map输出以及最后输出数据指定Avro模式。

### 项目打包
在打包的时候我们需要将依赖也打到jar包中，不然后面在集群中运行的时候会报找不到AvroJob类的错误。可通过在pom.xml中添加如下插件来解决打包的问题。
```xml
<plugin>
    <artifactId>maven-assembly-plugin</artifactId>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>single</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <descriptorRefs>
                    <descriptorRef>jar-with-dependencies</descriptorRef>
        </descriptorRefs>
    </configuration>
</plugin>
```

### 运行

1. 准备输入文件，input.txt。
    ```
    zhangsan	23	music	class1
    lisi	24	pingpong	class2
    wangwu	24	dance	class1
    liuyi	25	music	class1
    chener	25	dance	class2
    zhaoliu	22	dance	class2
    sunqi	22	pingpong	class1
    zhouba	23	music	class2
    wujiu	26	dance	class1
    zhengshi	21	dance	class2
    ```
2. 将输入文件上传到HDFS上
    ```
    hadoop fs -mkdir /input
    hadoop fs -put input.txt /input
    ```
    
3. 将jar拷贝到集群中任意一台Hadoop机器上。
4. 运行下面的命令执行jar包
    ```
    export HADOOP_CLASSPATH=${你的jar包名}
    export HADOOP_USER_CLASSPATH_FIRST=true
    hadoop jar {你的jar包名} {主类路径} /input /output
    ```
5. 将运行结果拷贝到本地
    ```
    hadoop fs -copyToLocal /output/part-r-00000.avro part-r-00000.avro
    ```    

5. 运行结果查看
    ```
    root@test:~# java -jar /root/extra-jar/avro-tools-1.8.2.jar tojson part-r-00000.avro
    {"name":"wujiu","age":26,"hobby":"dance","class":"class1"}
    {"name":"chener","age":25,"hobby":"dance","class":"class2"}
    
    ```
