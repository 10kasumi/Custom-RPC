package com.gda.rpc.Provider.springboot.service.impl;

import com.gda.rpc.interfaces.UserService;
import com.gda.rpc.spring.starter.common.HbRpcService;

@HbRpcService
public class UserServiceImpl implements UserService {
    @Override
    public void test() {
        System.out.println("UserServiceImpl : test");
    }
}
