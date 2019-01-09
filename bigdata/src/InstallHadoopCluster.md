  前面我们介绍了[在Ubuntu上安装Hadoop单机版](https://itweknow.cn/detail?id=52)。
但是我们知道，Hadoop在实际工作中都是以集群的形式存在的，毕竟需要处理大量的数据，
单机的处理速度显然不能满足。所以这篇文章我们就来介绍一下如何在Ubuntu上搭建Hadoop
集群。
### 准备阶段

> * 三台在同一局域网内的Linux机器或者虚拟机。
> * 配置好Host文件，让三台机器可以通过主机名进行访问。
> * 三台机器上都安装了JDK。
> * 在其中一台上安装好Hadoop，安装方法可以参考[前面的文章](https://itweknow.cn/detail?id=52)。

### 配置SSH免密登录
你可能很奇怪我要搭建的是Hadoop集群，为啥需要配置SSH免密登录呢。有两点原因：
1. 在启动集群上所有的节点的时候无需重复输入密码。
2. 我们在搭建集群的时候需要将Hadoop的文件夹复制到其他机器上，如果是一两台的话copy起来很快，
   但是如果是几百台呢，所以这个时候如果机器之间可以免密登录的话我们可以很方便的使用脚本完成整个工作。
具体的配置方式可以看下我之前的文章，[SSH免密登录（内含批量配置脚本）](https://itweknow.cn/detail?id=66)

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

### 安装包复制，并设置环境变量
其实在我们搭建好一台机器的Hadoop环境后只需要下面几步就可以完成集群环境的搭建了：
1. 将Hadoop的安装目录拷贝到其他机器上。
2. 在其他机器上配置Hadoop环境变量。
3. 执行source /etc/profile命令，使得环境变量生效。
4. 修改namenode的slave文件，指定datanode是哪几台机器 
前面两步我写好了脚本，直接执行一下就可以了。第3步中说的slave文件在hadoop的安装目录下的etc/hadoop/slave，我这里用的test01和test02两台机器做datanode。
```
test01
test02
```
[脚本地址](https://g-blog.oss-cn-beijing.aliyuncs.com/%E5%A4%8D%E5%88%B6Hadoop.rar)
附上脚本使用说明
> 1. 需要保证脚本文件夹中的两个脚本文件在同一目录下。
> 2. 如果Hadoop的安装目录不为/root/apps/hadoop/hadoop-2.8.5，则需要修改脚本中hadoop的安装位置（两个脚本中都需要修改）。
> 3. 修改脚本中机器名称，SERVERS变量。

### 环境启动

1. 在namenode机器上执行下面的命令

    ```bash
    start-all.sh
    ```
2. 在浏览器中访问`http://{机器IP}:50070，查看到如下结果即代表成功。
![启动结果](https://g-blog.oss-cn-beijing.aliyuncs.com/image/67-01.png)


