package com.gda.rpc.interfaces;

import java.util.List;

public interface DataService {

    String sendData(String body);

    List<String> getList();

    void testError();

    String testErrorV2();
}
