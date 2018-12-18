### 准备工作
> * 一个maven项目  
> * 一台运行着hadoop的linux机器或者虚拟机，当然了hadoop集群也可以，如果你还没有的话可以[戳这里。](https://itweknow.cn/detail?id=54)

### 项目依赖
调用HDFS提供的Java API我们需要依赖`hadoop-common`和`hadoop-hdfs`两个包，`junit`是用方便我们测试使用的。
```xml
<dependency>
    <groupId>org.apache.hadoop</groupId>
    <artifactId>hadoop-common</artifactId>
    <version>2.8.5</version>
</dependency>

<dependency>
    <groupId>org.apache.hadoop</groupId>
    <artifactId>hadoop-hdfs</artifactId>
    <version>2.8.5</version>
</dependency>

<dependency>
   <groupId>junit</groupId>
   <artifactId>junit</artifactId>
   <version>4.12</version>
</dependency>

```
### Java API
为了方便起见，我这里使用了`Junit`来进行测试，我们主要是通过`org.apache.hadoop.fs.FileSystem`这个类来操作HDFS,我们可以将获取`FileSystem`的代码放到`@Before`里面。
```java
FileSystem fs = null;
Configuration configuration = null;

@Before
public void init() throws Exception {
    configuration = new Configuration();
    configuration.set("fs.defaultFS", "hdfs://192.168.142.9:9000");
    // hdfs也是有一套权限体系的。
    // 这边为了可以在windows环境下测试，设置一下用户。
    fs = FileSystem.get(new URI("hdfs://192.168.142.9:9000"), configuration, "root");
}
```
* 读取HDFS中文件的内容  
`FileSystem.open()`方法可以获取指定文件的输入流，然后我们将它copy到`System.out`输出到控制台。
```java
@Test
public void read() throws IOException {
    // 文件地址。
    URI uri = URI.create("/test/test.txt");
    // 用于接收读取的数据流。
    FSDataInputStream in = null;
    try {
        in = fs.open(new Path(uri));
        // 最后的一个boolean类型的参数是指是否在调用结束后关闭流，我们这里选择在finally里面手动关闭。
        IOUtils.copyBytes(in, System.out, 4096, false);
    } finally {
        IOUtils.closeStream(in);
    }
}
```
不出意外的话，你可以在控制台看到你指定文件的内容。在这一步我遇到一个问题，就是无法直接在windows下操作HDFS,具体的解决方法可以参照[这篇文章](https://blog.csdn.net/sunshine920103/article/details/52431138)。`FSDataInputStream.seek()`方法可以实现从文件输入流的任意一个绝对位置读取文件内容，比如我们可以在上面代码中添加如下的内容来实现在控制台重复打印文件内容。
```java
in.seek(0);
IOUtils.copyBytes(in, System.out, 4096, false);
```
* 创建目录
```java
@Test
public void mkdir() throws IOException {
    fs.mkdirs(new Path("/test/api"));
}
```
* 查询文件系统  
这里引入一个类`FileStatus`,这个类封装了HDFS中文件和目录的元数据，包括文件长度、块大小、复本、修改时间、所有者以及权限信息。`FileSystem`里面提供的`listStatus`方法可以获取一个目录下的所有目录或者文件的`FileStatus`,但是它不会递归获取下级目录的内容，这里可以开发你的想象自己实现一下（Tips：`fileStatus.isDirectory()`可以判断这个fileStatus是否是一个文件夹）。
```java
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
```
* 删除文件或目录  
`FileSystem`提供了`delete()`方法来删除文件或者目录。查看源码发现其定义为：
```java
public abstract boolean delete(Path var1, boolean var2) throws IOException;
```
我们可以看到这个方法有两个参数，第一个参数很好理解，就是我们要删除的目录或者文件的地址。那么第二个Boolean类型的参数呢，如果删除的是文件或者空目录这个参数实际上是会被忽略的，如果删除的是非空目录，只有在这个参数值为`true`的时候才会成功删除。
```java
@Test
public void delete() throws IOException {
    fs.delete(new Path("/test/api"), false);
}
@Test
public void deleteNonEmptyDir() throws IOException {
    fs.delete(new Path("/test"), true);
}
```
* 创建写入文件  
我们通过``FileSystem.create()`方法来创建一个文件，这个方法会顺带着创建补存在的父级目录，如果不需要这个的话，最好是在创建之前调用`exists()`方法来判断一下，如果父级目录不存在直接报错即可。
```java
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
```
文件创建好后，可以通过`append()`方法在文件末尾添加内容。
```java
@Test
public void append() throws IOException {
    FSDataOutputStream out = null;
    try {
        out = fs.append(new Path("/test/api/test.txt"));
        out.writeChars("hello hdfs.");
    } finally {
        out.close();
    }
}
```
* 从本地上传文件到HDFS
```java
@Test
public void copyFromLocal() throws IOException {
    fs.copyFromLocalFile(new Path("d:/local.txt"), new Path("/test/api"));
}
```
* 从HDFS上下载文件
```java
@Test
public void copyToLocal() throws IOException {
    fs.copyToLocalFile(new Path("/test/api/local.txt"), new Path("E:/"));
}
```
通常情况下这些API基本上够我们使用了，如果后续我有使用到其他的API会及时的更新到本篇文章中。本文的源码：



