package cn.itweknow.springbootswagger.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * @author ganchaoyang
 * @date 2018/12/19 10:29
 * @description
 */
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


    public Integer getId() {
        return id;
    }

    public User setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public User setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }
}
