在上一篇文章[《Hadoop之MapReduce实战》](https://itweknow.cn/detail?id=61)中，我们已经完成了一个很简单的MapReduce程序，并且成功的在Hadoop集群上执行。下面我们将来简要的介绍一下如何在我们本地调试和测试我们的MapReduce程序。
### MrUnit
MRUnit是Cloudera公司专为Hadoop MapReduce写的单元测试框架，其API非常简洁实用。该框架对不同的测试对象使用不同的Driver，因此分为了：MapDriver、ReduceDriver和MapReduceDriver。
### 项目依赖
在前一篇文章的基础之上我们还需要添加如下依赖：

```xml
<dependency>
    <groupId>org.apache.mrunit</groupId>
    <artifactId>mrunit</artifactId>
    <version>1.1.0</version>
    <classifier>hadoop2</classifier>
    <scope>test</scope>
</dependency>
```
### Mapper测试
测试的方法很简单，其实就是我们模拟一个输入，然后判断输出结果是否符合预期。我们初始化一个MapDriver，然后通过withMapper指定要测试的Mapper，通过withInput和withOutput指定输入和期望输出，最后runTest()来执行测试方法。
```java
public class CharCountMapperTest {

    @Test
    public void mapperTest() throws IOException {
        Text value = new Text("hi hadoop");
        new MapDriver<LongWritable, Text, Text, LongWritable>()
                .withMapper(new CharCountMapper())
                .withInput(new LongWritable(0), value)
                .withOutput(new Text("h"), new LongWritable(1))
                .withOutput(new Text("i"), new LongWritable(1))
                .withOutput(new Text(" "), new LongWritable(1))
                .withOutput(new Text("h"), new LongWritable(1))
                .withOutput(new Text("a"), new LongWritable(1))
                .withOutput(new Text("d"), new LongWritable(1))
                .withOutput(new Text("o"), new LongWritable(1))
                .withOutput(new Text("o"), new LongWritable(1))
                .withOutput(new Text("p"), new LongWritable(1))
                .runTest();
    }
}
```
在写这个MapperTest的时候遇到了一个坑，这里说明一下，就是这里需要注意一下我们的MapDriver是`org.apache.hadoop.mrunit.mapreduce`包下的而不是`org.apache.hadoop.mrunit`包下的，如果导错包的话会编译不通过的。
### Reducer测试
```java
public class CharCountReducerTest {
    @Test
    public void reducerTest() throws IOException {
        new ReduceDriver<Text, LongWritable, Text, LongWritable>()
                .withReducer(new CharCountReducer())
                .withInput(new Text("h"),
                        Arrays.asList(new LongWritable(1),
                                new LongWritable(1),
                                new LongWritable(1)))
                .withInput(new Text("i"), Arrays.asList(new LongWritable(1)))
                .withOutput(new Text("h"), new LongWritable(3))
                .withOutput(new Text("i"), new LongWritable(1))
                .runTest();
    }
}
```
### 驱动程序测试
对与驱动程序我们可以选择使用MapReduceDriver写单元测试来测试，也可以直接执行main方法完成测试。
* MapReduceDriver  
需要指定mapper和reducer，然后输入和预期输出。这里的输出包含多个键值对，有个顺序问题，默认情况下是根据键来自然排序输出的。
```java
@Test
    public void driverTest() throws IOException {
        String line1 = "hi";
        String line2 = "hello";
        new MapReduceDriver(new CharCountMapper(), new CharCountReducer())
                .withInput(new LongWritable(0), new Text(line1))
                .withInput(new LongWritable(1), new Text(line2))
                .withOutput(new Text("e"), new LongWritable(1))
                .withOutput(new Text("h"), new LongWritable(2))
                .withOutput(new Text("i"), new LongWritable(1))
                .withOutput(new Text("l"), new LongWritable(2))
                .withOutput(new Text("o"), new LongWritable(1))
                .runTest();
    }
```
* main方法  
也可以直接运行驱动程序中的main方法，Hadoop有一个本地作业运行库，是为测试而生的，我们可以通过设置`mapreduce.framwork.name`来配置，默认就是local（本地作业运行器）。
