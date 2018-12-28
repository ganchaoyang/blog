  前面我们介绍了[在Ubuntu上安装Hadoop单机版](https://itweknow.cn/detail?id=52)。
但是我们知道，Hadoop在实际工作中都是以集群的形式存在的，毕竟需要处理大量的数据，
单机的处理速度显然不能满足。所以这篇文章我们就来介绍一下如何在Ubuntu上搭建Hadoop
集群。
### 准备阶段

> * 三台在同一局域网内的Linux机器或者虚拟机
> * 配置好Host文件，让三台机器可以通过主机名进行访问。
> * 三台机器上都安装了JDK。
> * 在其中一台上安装好Hadoop，安装方法可以参考[前面的文章](https://itweknow.cn/detail?id=52)。

### 配置SSH免密登录
你可能很奇怪我要搭建的是Hadoop集群，为啥需要配置SSH免密登录呢。有两点原因：
1. 在启动集群上所有的节点的时候无需重复输入密码。
2. 我们在搭建集群的时候需要将Hadoop的文件夹复制到其他机器上，如果是一两台的话copy起来很快，
   但是如果是几百台呢，所以这个时候如果机器之间可以免密登录的话我们可以很方便的使用脚本完成整个工作。

### 修改单机版的配置文件

1. 修改`core-site.xml`，只需要修改`fs.defaultFS`配置项即可，`hadoop.tmp.dir`不需要修改。

```xml
<property>
    <name>fs.defaultFS</name>
    <!-- 修改为namenode的地址，改成主机名 -->
    <value>hdfs://test:9000</value>
</property>
```

2. 修改``hdfs-site.xml`，主要是修改HDFS存储文件副本的数量，之前单机版的时候设置为1，现在改为2（这个配置项默认的配置为2）。

```xml
<!-- 指定HDFS副本的数量 -->
<property>
    <name>dfs.replication</name>
    <value>2</value>
</property>

```


