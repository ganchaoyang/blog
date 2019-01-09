为了后面能够更加熟悉的保障Hadoop集群的平稳运行，我们需要深入的了解NameNode、SecondaryNameNode（也称辅助NameNode）以及datanode等HDFS组件在磁盘上的目录结构以及运行的原理。这篇文章我们就一起来看一下NameNode的目录结构。
### namenode的目录结构一览

namenode的工作目录就是我门指定的hadoop工作目录（由hadoop.tmp.dir配置项指定，配置在core-site.xml文件内）下的dfs/name目录。

```bash
root@test:~/hadoop/tmp/dfs# tree name
name
├── current
│   ├── edits_0000000000000000001-0000000000000000002
│   ├── edits_0000000000000000003-0000000000000000003
│   ├── edits_0000000000000000004-0000000000000000005
│   ├── edits_0000000000000000006-0000000000000000006
│   ├── edits_0000000000000000007-0000000000000000008
│   ├── edits_0000000000000000009-0000000000000000010
│   ├── edits_0000000000000000011-0000000000000000012
│   ├── edits_0000000000000000013-0000000000000000014
│   ├── edits_0000000000000000015-0000000000000000016
│   ├── edits_0000000000000000017-0000000000000000018
│   ├── edits_0000000000000000019-0000000000000000020
│   ├── edits_inprogress_0000000000000000021
│   ├── fsimage_0000000000000000000
│   ├── fsimage_0000000000000000000.md5
│   ├── fsimage_0000000000000000020
│   ├── fsimage_0000000000000000020.md5
│   ├── seen_txid
│   └── VERSION
└── in_use.lock

```

* edits_0000xxxx -> 编辑日志文件
* edits_inprogress_000xxx -> 当前打开可写的编辑日志
* fsimage_000xxx -> 文件系统镜像文件
* VERSION -> HDFS版本信息的描述文件
* in_use.lock -> 一个锁文件，namenode使用该文件为存储目录枷锁，避免其它namenode实例同时使用同一个存储目录的情况。

### VERSION

`VERSION`文件中包含正在运行的HDFS的版本信息，一般情况下应该包含下面的内容：

```bash
#Wed Jan 02 07:08:58 UTC 2019
namespaceID=1447158958
clusterID=CID-67c5cf48-73da-4469-9c90-2759b2e0481c
cTime=1544608355749
storageType=NAME_NODE
blockpoolID=BP-1627059714-192.168.142.9-1544608355749
layoutVersion=-63
```

* `layoutVersion`是一个负整数，描述HDFS持久性数据结构（也称之为布局）的版本，但是这个与Hadoop发布包的版本号无关。
* `namespaceID`是文件系统命名空间的唯一标识，是在namenode首次格式化的时候创建的。
* `clusterID`是将HDFS集群作为一个整体赋予的唯一标识符，对于联邦HDFS分厂中药，因为在联邦HDFS机制下一个集群由多个命名空间组成，每个命名空间由一个namenode管理。
* `blockpoolID`是数据块池的唯一标识符，数据块池中包含了由一个namenode管理的命名空间中的所有文件
* `cTime`属性标记了namenode存储系统的创建时间。对于刚刚格式化的文件系统这个属性值为0，在文件系统升级之后这个值就会更新到新的时间戳。
* `storageType`说明该存储目录包含的是namenode的数据结构。

### 编辑日志和文件系统镜像

* 编辑日志

当我们操作HDFS中的文件时，这些操作首先会被写入到编辑日志中，然后相关的文件数据也会被更新。编辑日志文件在概念上是单个实体，但是它其实是存储在磁盘上的多个文件上的，我们看到了很多的edits_000xxx就是编辑日志。但是任何一个时刻都只有一个编辑日志文件处于打开可写的状态（edits_inprogress_000xxx）。
其实这个有点类似日志滚动的概念。

* 文件系统镜像
每个fsimage_000xxx文件都是HDFS文件系统的一个镜像（也称之为文件系统元数据的一个完整的永久性检查点），镜像文件的大小一般都比较大，我们对HDFS中文件的操作并不会直接记录到镜像文件中，而是写入到编辑日志中，在namenode启动的时候会首先载入最近的一个镜像文件，然后再读取编辑日志中的改变，这样我们就可以将namenode恢复到上一次正常工作时的状态了。

* 编辑日志合并到文件系统镜像

编辑日志不会是无限的增长的，集群中的SecondaryNameNode会定期为namenode内存中的文件系统元数据创建系统镜像，具体的创建过程参照下图。

![编辑日志合并过程](https://g-blog.oss-cn-beijing.aliyuncs.com/image/69-01.png)

1. SecondaryNameNode请求NameNode停止使用当前打开的edits文件（即edits_inprogress_000xxx文件），并重新打开一个新的编辑日志文件以记录新的操作。
2. SecondaryNameNode从NameNode中获取最近的fsimage和edits文件，使用HTTP GET方式获取。
3. SecondaryNameNode将fsimage载入内存，然后逐一执行edits文件中记录的操作，然后创建一个新的镜像文件。
4. SecondaryNameNode将合并后的镜像文件发送到NameNode（HTTP PUT）,NameNode将其保存为一个临时文件。
5. NameNode重新命名该临时的镜像文件，此为最新的镜像文件。

edits日志文件合并的触发条件受两个配置项的控制，dfs.namenode.checkpoint.period（单位为秒），这个配置项是从时间维度上的控制，默认情况下是每隔1个小时触发一次合并。
第二个配置项是dfs.namenode.checkpoint.txns，这个配置是从编辑日志大大小维度上进行控制的，默认是如果从上一个检查点开始编辑日志已经达到了100万个事务就合并。检查编辑日志大小的频率默认是1分钟检查一次，可由dfs.namenode.checkpoint.check.period（单位为秒）配置项来改变。

### SecondaryNameNode目录结构
SecondaryNameNode的目录结构和NameNode的目录结构一样。为啥要这么设计呢？主要的优点在于当NameNode发生故障的时候，我们可以从SecondaryNameNode恢复数据。恢复的方式有两种：
1. 将相关的目录复制到NameNode的目录中，然后重启NameNode。
2. 使用-importCheckpoint选项启动NameNode，但是这种方式只有在NameNode的目录中没有元数据的时候才会有用。

### DataNode的目录结构
```bash
data/
├── current
│   ├── BP-1627059714-192.168.142.9-1544608355749
│   │   ├── current
│   │   │   ├── dfsUsed
│   │   │   ├── finalized
│   │   │   │   └── subdir0
│   │   │   │       └── subdir0
│   │   │   │           ├── blk_1073741825
│   │   │   │           ├── blk_1073741825_1001.meta
│   │   │   │           ├── blk_1073741831
│   │   │   │           ├── blk_1073741831_1010.meta
│   │   │   │           ├── blk_1073741832
│   │   │   │           ├── blk_1073741832_1011.meta
│   │   │   │           ├── blk_1073741833
│   │   │   │           ├── blk_1073741833_1012.meta
│   │   │   │           ├── blk_1073741834
│   │   │   │           ├── blk_1073741834_1013.meta
│   │   │   │           ├── blk_1073741842
│   │   │   │           ├── blk_1073741842_1021.meta
│   │   │   │           ├── blk_1073741843
│   │   │   │           └── blk_1073741843_1022.meta
│   │   │   ├── rbw
│   │   │   └── VERSION
│   │   ├── scanner.cursor
│   │   └── tmp
│   └── VERSION
└── in_use.lock

```
我们的数据实际上都是存储在以blk_为前缀的文件中，文件名包含了该文件存储的块的原始字节数，每个块还带有一个相关联的以.meta为后缀的文件，这个文件包括头部（含版本和类型信息）和该区块各区段的一系列的校验和。
当目录中数据块的数量到达一定的规模的时候（可通过dfs.datanode.numblocks属性设置，默认为64）就会创建一个子目录。