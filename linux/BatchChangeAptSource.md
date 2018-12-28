在国内使用ubuntu的过程中由于网络的问题可能会有些包下载速度很慢，但是阿里云为我们提供了仓库服务，我们可以将Ubuntu的默认的软件源修改为阿里云的就可以了明显的提高软件的下载速度了。

### 单机修改

我们先来看下修改一台机器的源。一台机器操作比较简单，也用不着使用脚本去操作。
1. 备份原来的源文件（PS:保险起见，怕改糟了。）
    ```bash
    cp /etc/apt/sources.list /etc/apt/sources.list.bak
    ```

2. 获取系统内核版本信息
    ```bash
    lsb_release -c | grep -o "\s.*"
    ```
    这里我的机器的版本为`bionic`

3. 修改/etc/apt/sources.list  
    ***注意将`$SYS_VERSION`替换为第二步中获取的版本信息。***这点非常重要，否则会失败。
    
    ```bash
    deb http://mirrors.aliyun.com/ubuntu/ $SYS_VERSION main restricted universe multiverse
    deb-src http://mirrors.aliyun.com/ubuntu/ $SYS_VERSION main restricted universe multiverse
    deb http://mirrors.aliyun.com/ubuntu/ $SYS_VERSION-security main restricted universe multiverse
    deb-src http://mirrors.aliyun.com/ubuntu/ $SYS_VERSION-security main restricted universe multiverse
    deb http://mirrors.aliyun.com/ubuntu/ $SYS_VERSION-updates main restricted universe multiverse
    deb-src http://mirrors.aliyun.com/ubuntu/ $SYS_VERSION-updates main restricted universe multiverse
    deb http://mirrors.aliyun.com/ubuntu/ $SYS_VERSION-backports main restricted universe multiverse
    deb-src http://mirrors.aliyun.com/ubuntu/ $SYS_VERSION-backports main restricted universe multiverse
    deb http://mirrors.aliyun.com/ubuntu/ $SYS_VERSION-proposed main restricted universe multiverse
    deb-src http://mirrors.aliyun.com/ubuntu/ $SYS_VERSION-proposed main restricted universe multiverse
    ```
4. 更新

    ```bash
    apt-get update
    apt-get upgrade
    ```
    至此，成功。

### 多台机器

当需要修改多台机器的源的时候，上面那种方式就有点麻烦了，其实这些工作都可以通过脚本来做的。[脚本下载](https://g-blog.oss-cn-beijing.aliyuncs.com/%E4%BF%AE%E6%94%B9%E8%BD%AF%E4%BB%B6%E4%BB%93%E5%BA%93.rar)。

1. 配置好hosts，在执行脚本的机器上的hosts文件中添加所有机器名和IP映射关系。
2. 修改脚本文件中`batch_change_apt_source.sh`的机器名称，即`SERVERS`变量。
3. 设置可执行权限
    ```bash
    chmod 777 batch_change_apt_source.sh
    chmod 777 change_apt_source.sh
    ```
4. 执行batch_change_apt_source.sh
    ```bash
    ./batch_change_apt_source.sh
    ```
5. 过程中可能会提示输入机器密码和Y/N