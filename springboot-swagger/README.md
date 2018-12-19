我们提供Restful接口的时候，API文档是尤为的重要，它承载着对接口的定义，描述等。它还是和API消费方沟通的重要工具。在实际情况中由于接口和文档存放的位置不同，我们很难及时的去维护文档。个人在实际的工作中就遇到过很多接口更新了很久，但是文档却还是老版本的情况，其实在这个时候这份文档就已经失去了它存在的意义。而`Swagger`是目前我见过的最好的API文档生成工具，使用起来也很方便，还可以直接调试我们的API。我们今天就来看下`Swagger2`与`SpringBoot`的结合。
### 准备工作
* 一个SpringBoot项目，可以直接去官网[生成一个demo](https://start.spring.io/)。
* 一个用户类。
```java
package cn.itweknow.springbootswagger.model;

import java.io.Serializable;

/**
 * @author ganchaoyang
 * @date 2018/12/19 10:29
 * @description
 */
public class User implements Serializable {

    private Integer id;

    private String name;

    private String password;

    private String email;
}
```

### 项目依赖
Web Service肯定是一个Web项目，所以我们这里依赖了`spring-boot-starter-web`包，其他两个包就是和`Swagger`相关的包了。
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger2</artifactId>
    <version>2.9.2</version>
</dependency>

<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger-ui</artifactId>
    <version>2.9.2</version>
</dependency>
```

### Swagger配置
Springfox Docket实例为Swagger配置提供了便捷的配置方法以及合理的默认配置。我们将通过创建一个Docket实例来对Swagger进行配置，具体配置如下所示。
```java
@Configuration
@EnableSwagger2
public class SwaggerConfig extends WebMvcConfigurationSupport {

    @Bean
    public Docket productApi() {
        return new Docket(DocumentationType.SWAGGER_2).select()
                // 扫描的包路径
                .apis(RequestHandlerSelectors.basePackage("cn.itweknow.springbootswagger.controller"))
                // 定义要生成文档的Api的url路径规则
                .paths(PathSelectors.any())
                .build()
                // 设置swagger-ui.html页面上的一些元素信息。
                .apiInfo(metaData());
    }

    private ApiInfo metaData() {
        return new ApiInfoBuilder()
                // 标题
                .title("SpringBoot集成Swagger2")
                // 描述
                .description("这是一篇博客演示")
                // 文档版本
                .version("1.0.0")
                .license("Apache License Version 2.0")
                .licenseUrl("https://www.apache.org/licenses/LICENSE-2.0")
                .build();
    }

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
```
上述代码中的addResourceHandlers方法添加了两个资源处理程序，这段代码的主要作用是对Swagger UI的支持。

### API文档
好了，到这一步，我们已经在一个SpringBoot项目中配置好了Swagger。现在，我们就来看一下如何去使用他。首先我们定义了一个`Controller`并提供了两个接口：  
* 通过用户id获取用户  
* 用户登录
```java
@RestController
@RequestMapping("/user")
@Api(description = "用户相关接口")
public class UserController {

    /**
     * 通过id查询用户
     * @return
     */
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    @ApiOperation("根据id获取用户")
    public User getUserById(@ApiParam(value = "用户id") Integer id) {
        return new User();
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ApiOperation("用户登录")
    public User login(@RequestBody User user) {
        return new User();
    }
}
``` 
相信大家都注意到了，这个`Controller`里面多了很多新的注解，比如说`@Api`,`@ApiOperation`等，下面我们就来一一解释一下。
* @Api,这个注解是用在Controller类上面的，可以对Controller做必要的说明。
* @ApiOperation，作用在具体的方法上，其实就是对一个具体的API的描述。
* @ApiParam，对API参数的描述。  
到这里，其实我们的Swagger就已经可以有效果了，让我们将项目运行起来先看看效果。访问http://localhost:8080/swagger-ui.html即可。
![](https://g-blog.oss-cn-beijing.aliyuncs.com/image/56-1.png)
### Model
在上面的图中可以看到在页面的下方有一个Models的标签，那么这个是啥呢。其实这个就是我们API中出现的一些对象的文档，我们也可以通过注解来对这些对象中的字段做一些说明，以方便使用者理解。以文章开头提到的`User`类来做一个说明。
```java
@ApiModel("用户实体")
public class User implements Serializable {

    @ApiModelProperty(value = "用户id")
    private Integer id;

    @ApiModelProperty(value = "用户名称", required = true)
    private String name;

    @ApiModelProperty(value = "密码", required = true)
    private String password;

    @ApiModelProperty(value = "用户邮箱")
    private String email;
}
```
我们来看一下`User`类在Swagger上是如何展示的：
![](https://g-blog.oss-cn-beijing.aliyuncs.com/image/56-2.png)
有一个细节，那就是required = true的字段上面被红星修饰，代表了必填项。
### API测试
在`swagger-ui.html`页面上我们可以直接测试API，如下图所示，点击`Try it out`，然后填写参数，并点击`Execute`即可进行调用。
![](https://g-blog.oss-cn-beijing.aliyuncs.com/image/56-3.png)  
好了，对于Swagger的介绍就到这里了，最后奉上本文的源码地址，[请戳这里]()。