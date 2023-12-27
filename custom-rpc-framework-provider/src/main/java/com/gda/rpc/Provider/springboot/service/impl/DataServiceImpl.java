package com.gda.rpc.Provider.springboot.service.impl;

import com.gda.rpc.interfaces.DataService;
import com.gda.rpc.spring.starter.common.HbRpcService;

import java.util.ArrayList;
import java.util.List;


@HbRpcService(serviceToken = "data-token", group = "data-group", limit = 2)
public class DataServiceImpl implements DataService {
    @Override
    public String sendData(String body) {
        System.out.println("这里是服务提供者，body is " + body);
        return "success from server";
    }

    @Override
    public List<String> getList() {
        List<String> arrayList = new ArrayList<>();
        arrayList.add("data1");
        arrayList.add("data2");
        arrayList.add("data3");
        arrayList.add("data4");
        arrayList.add("data5");
        return arrayList;
    }

    @Override
    public void testError() {
        System.out.println(1 / 0);
    }

    @Override
    public String testErrorV2() {
        throw new RuntimeException("测试异常");
    }
}
