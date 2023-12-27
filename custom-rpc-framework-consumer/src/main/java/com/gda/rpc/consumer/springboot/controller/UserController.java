package com.gda.rpc.consumer.springboot.controller;

import com.gda.rpc.interfaces.DataService;
import com.gda.rpc.interfaces.UserService;
import com.gda.rpc.spring.starter.common.HbRpcReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @HbRpcReference
    private UserService userService;

    @HbRpcReference(group = "data-group", serviceToken = "data-token")
    private DataService dataService;

    @GetMapping(value = "/test")
    public void test(){
        userService.test();
    }

    @GetMapping("/send/{msg}")
    public String testMaxData(@PathVariable("msg") String msg){
        return dataService.sendData(msg);
    }

    @GetMapping("/list")
    public List<String> getOrderNo(){
        return dataService.getList();
    }
}
