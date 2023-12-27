package com.gda.rpc.core.common.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

public class CommonUtil {

    public static List<Class<?>> getAllInterfaces(Class<?> targetClass){
        if(targetClass == null){
            throw new IllegalArgumentException("targetClass is null");
        }
        Class<?>[] clazz = targetClass.getInterfaces();
        if(clazz.length == 0){
            return Collections.emptyList();
        }
        List<Class<?>> classes = new ArrayList<>(clazz.length);
        classes.addAll(Arrays.asList(clazz));
        return classes;
    }

    public static String getIpAddress(){
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while(allNetInterfaces.hasMoreElements()){
                NetworkInterface networkInterface = (NetworkInterface) allNetInterfaces.nextElement();
                if(!networkInterface.isLoopback() && !networkInterface.isVirtual() && networkInterface.isUp()){
                    Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                    while(addresses.hasMoreElements()){
                        ip = addresses.nextElement();
                        if(ip instanceof InetAddress){
                            return ip.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            System.out.println("IP地址获取失败" + e.toString());
        }
        return "";
    }

    public static boolean isEmpty(String str){
        return str == null || str.length() == 0;
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isEmptyList(List<?> list){
        return list == null || list.size() == 0;
    }

    public static boolean isNotEmptyList(List<?> list){
        return !isEmptyList(list);
    }
}
