package cn.itweknow.springbootdocker.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ganchaoyang
 * @date 2018/11/20 13:43
 * @description
 */
@RestController
public class HelloController {

    @RequestMapping("/hello")
    public String hello() {
        return "Hello Docker.";
    }
}
