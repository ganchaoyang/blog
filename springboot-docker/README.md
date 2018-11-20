微服务现在在互联网公司可谓非常流行了，之前找工作的的时候很多HR电话约面试的时候都会问对微服务是否有过接触。而微服务和Docker可以非常完美的结合，更加方便的实现微服务架构的落地。作为微服务中的代表SpringBoot框架，今天我们就来了解一下如何在Docker容器中运行一个SpringBoot应用。
### 创建SpringBoot程序
在这篇文章中我们将在Docker容器中运行一个简单的SpringBoot的Web应用，下面是初始时刻的`pom.xml`中的内容。
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>cn.itweknow</groupId>
    <artifactId>springboot-docker</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>springboot-docker</name>
    <description>Demo project for Spring Boot</description>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.0.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```
添加一个`HelloController.java`
```java
@RestController
public class HelloController {

    @RequestMapping("/hello")
    public String hello() {
        return "Hello Docker.";
    }
}
```
好了，到现在为止我们一个简单的web应用就已经建好了，可以在本地运行起来并看下效果，确保程序正确。
### 配置docker
docker提供了maven构建的插件`docker-maven-plugin`,我们只需要在我们的`pom.xml`中添加这个插件，然后做相关的一些简单的配置就OK了。
```xml
<plugin>
    <groupId>com.spotify</groupId>
    <artifactId>docker-maven-plugin</artifactId>
    <version>1.2.0</version>
    <configuration>
        <!-- 这里是最终生成的docker镜像名称 -->
        <imageName>itweknow/${project.artifactId}</imageName>
        <!-- 基础镜像，运行一个springboot应用只需要基础的java环境就行 -->
        <baseImage>java:8</baseImage>
        <!-- docker启动的时候执行的命令 -->
        <entryPoint>["java", "-jar", "/${project.build.finalName}.jar"]</entryPoint>
        <resources>
        <resource>
            <targetPath>/</targetPath>
            <directory>${project.build.directory}</directory>
            <include>${project.build.finalName}.jar</include>
            </resource>
        </resources>
    </configuration>
</plugin>
```
没错就这么简单，到目前我们整个项目已经搭建好了，剩下的工作就是讲项目copy到我们linux环境下生成docker镜像然后运行起来了。
### 构建镜像
copy项目到linux下后，进入到项目目录下，顺序执行下面的命令，就可以生成一个docker镜像了。
```bash
mvn clean
# -Dmaven.test.skip=true 是跳过测试代码
mvn package -Dmaven.test.skip=true
mvn docker:build
```
当然你也可以三条命令一起执行
```bash
mvn clean package docker:build -Dmaven.test.skip=true
```
然后在执行`docker images`查看系统中的docker镜像，看是否生成成功。
```bash
REPOSITORY                                           TAG                 IMAGE ID            CREATED             SIZE
itweknow/springboot-docker                           latest              f03b689cfc33        10 seconds ago      660MB
```
### 运行docker容器
```bash
# -d 是指定后台运行
# --name是指定容器名称
# -p 8080:8080 是指将容器的8080端口映射给宿主机的8080端口 格式为：主机(宿主)端口:容器端口
docker run -d --name test -p 8080:8080 itweknow/springboot-docker
```
执行`docker ps`查看一下正在运行的容器
```bash
CONTAINER ID        IMAGE                        COMMAND                  CREATED             STATUS              PORTS                    NAMES
652fd3ccac89        itweknow/springboot-docker   "java -jar /springbo…"   3 seconds ago       Up 2 seconds        0.0.0.0:8080->8080/tcp   test
```
我们的项目已经成功运行在了docker容器中了，我们可以访问一下`http://虚拟机IP:8080/hello`测试一下了。  
项目地址： https://github.com/ganchaoyang/blog/tree/master/springboot-docker