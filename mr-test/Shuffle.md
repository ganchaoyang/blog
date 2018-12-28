### 简介
MapReduce程序会确保每个reduce函数的输入都是按键排序的。系统执行排序以及将map函数的输出传给reduce函数的过程称之为shuffle。整个Shuffle分为Map端和Reduce端，下图是MapReduce的Shuffle的一个整体概览图，大家先看一下整个图，我们后面再做进一步的解释说明。
![MapReduce的Shuffle和排序](https://g-blog.oss-cn-beijing.aliyuncs.com/image/64-01.png)

### Map端  
其实Map函数产生的输出会写到磁盘上而不是HDFS。但是它也不是简简单单的直接写到磁盘，这中间有一个复杂的过程，下面我们就来拆解一下。  
&nbsp;&nbsp;&nbsp;&nbsp;从上面的图可以看到每个Map任务都会有一个缓冲区，这个缓冲区会临时
存储map函数输出的内容，缓冲区的个大小默认是100M，我们可以通过mapreduce.task.io.sort.mb这个配置项配置，
当缓冲区中的内容达到其设定的阈值（阈值的设置值是占整个缓冲区的大小，默认为0.8，我们可以通过mapreduce.map.sort.spill.percent来配置）时就会产生溢出，这个时候会有一个后台线程将缓冲区中的内容分区（根据最终要传给的
Reduce任务分成不同的区，分区的目的是将输出划分到不同的Reducer上去，后面的Reducer就会根据分区来读取自己对应的数据）
然后区内按照key排序，如果我们设置了Combiner（Combiner的本质也是一个Reducer，其目的是对将要写入到磁盘上的文件先进行一次处理，这样，写入到磁盘的数据量就会减少。）
的话，这个时候会运行Combiner函数，最后再写入磁盘。而在这个过程中Map任务还会继续往缓冲区中输出内容，
如果出现缓冲区空间被占满的情况，Map任务就会阻塞直到缓冲区中的内容被全部写到磁盘中为止。  
&nbsp;&nbsp;&nbsp;&nbsp;每次缓冲区溢出时都会新建一个新的溢出文件，这样最后其实是会出现多个溢出文件的，在Map任务结束前这些溢出文件会被合并到一个整的输出文件。

### Reduce端
Reduce端的Shuffle分为三个阶段，复制阶段、合并阶段和Reduce。  
&nbsp;&nbsp;&nbsp;&nbsp;首先是复制阶段，Reduce任务需要集群上若干个map输出作为其输入内容，在每个Map任务完成完成的时候Reduce任务就开复制其输出，
上面也提到过Map任务在写入磁盘前会将输出进行根据Reduce任务进行分区，所以这里Reduce任务在复制的时候只会复制自己的那个分区里的内容。
如果Map的输出非常小，那么Reduce会直接将其复制到内存中，否则会被复制到磁盘。  
&nbsp;&nbsp;&nbsp;&nbsp;合并阶段，因为有很多的Map任务，所以Reduce复制过来的map输出会有很多个，在这个阶段主要就是将这些Map输出合并成为一个文件。  
&nbsp;&nbsp;&nbsp;&nbsp;Reduce阶段，这个阶段主要就是执行我们的Reduce函数的代码了，并产生最终的结果，然后写入到HDFS中。

### 附

在整个Shuffle过程中涉及到很多的参数可以调整，比如说Map输出的时候缓冲区的大小以及其阈值的大小，还有Reduce执行复制阶段的线程数等等。我们在这里罗列一下这些配置供大家参考。

* Map端

| 属性名称 | 类型 | 默认值 | 说明 |   
| ------ | ------ | ------ | ------ |  
| mapreduce.task.io.sort.mb | int | 100  | Map输出时所使用的缓冲区的大小，单位为MB |   
| mapreduce.map.sort.spill.percent | float | 0.80 | 缓冲区的阈值，当缓冲区中内容达到这个阈值时会开始写入磁盘的操作 |   
| mapreduce.task.io.sort.factor | int | 10 | 排序文件时，一次最多合并的流数，一般我们会将这个值提高到100 |   
| mapreduce.map.combine.minspills  | int | 3  | 运行Combiner所需的最少溢出文件数 |   
| mapreduce.map.output.compress | boolean | false​​​​ | 是否压缩map输出 |
| mapreduce.map.output.compress.codec | Class Name | org.apache.hadoop.io.compress.DefaultCodec | 用于Map输出压缩的编解码器 |
| mapreduce.shuffle.max.threads | int | 0 | 每个节点管理器的工作线程数，用于将map输出到reducer。这个是集群范围的设置，不能由单个作业设置。0的话表示使用Netty的默认值，即两倍的cpu数。 |

* Reduce端

| 属性名称 | 类型 | 默认值 | 说明 |   
| ------ | ------ | ------ | ------ |  
| mapreduce.reduce.shuffle.parallelcopies | int | 5  | 用于复制Map输出到Reduce的线程数 |   
| mapreduce.reduce.shuffle.maxfetchfailures | int | 10 | Reducer获取一个Map输出所花的最大时间 |   
| mapreduce.task.io.sort.factor | int | 10 | 合并Map输入的时候，一次最多合并的流的数量 |   
| mapreduce.reduce.shuffle.input.buffer.percent  | float | 0.7  | 在复制阶段，分配给Map输出的缓冲区占堆空间的百分比 |   
| mapreduce.reduce.shuffle.merge.percent | float | 0.66​​​ | Map输出缓冲区的阈值使用比例，用于启动合并输出和磁盘溢出写的过程 |
| mapreduce.reduce.merge.in.mem.threshold | int | 1000 | 启动合并输出和磁盘溢出写过程的Map输出的阈值数。0或更小的数意味着没有阈值限制，溢出写行为由mapreduce.reduce.shuffle.merge.percent控制 |
| mapreduce.reduce.input.buffer.percent | float | 0 | 在reduce过程中，在内存中保存Map输出空间占整个堆空间的比例。Reduce阶段开始时，内存中的Map输出大小不能大于整个值。默认情况下，在Reduce任务开始之前，所有Map输出都合并到磁盘上，以便为Reducer提供尽可能多的内存。然而，如果Reducer需要的内存较少，可以增加增加此值来最小化访问磁盘的次数 |

对与后面四个配置项可能有点难的理解，其实前面也提到过，在Reduce端Shuffle的复制阶段其实先是将map的输出复制到一个缓冲区，然后当达到缓冲区的阈值或者Map输出阈值时会逐步溢出写到磁盘。其中缓冲区的大小就是由mapreduce.reduce.shuffle.input.buffer.percent配置项决定的，而缓冲区的阈值则由mapreduce.reduce.shuffle.merge.percent控制，Map输出阈值则由mapreduce.reduce.merge.in.mem.threshold指定，也就是说会由mapreduce.reduce.shuffle.merge.percent和mapreduce.reduce.merge.in.mem.threshold两个配置项同时决定何时溢出到磁盘。真正的Reduce任务执行时也就是三个过程中的Reduce阶段会逐步从磁盘中读取Map的输出到内存中的一个缓冲区，而最后的一个配置项mapreduce.reduce.input.buffer.percent指定的是这个缓冲区的大小，当我们Reduce程序需要的内存比较少的时候，可以适当调大该值以减少访问磁盘的次数。