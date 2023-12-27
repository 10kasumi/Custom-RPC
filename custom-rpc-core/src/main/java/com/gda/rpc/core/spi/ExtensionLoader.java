package com.gda.rpc.core.spi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * spi加载器实现
 */
public class ExtensionLoader {

    public static String EXTENSION_LOADER_DIR_PREFIX = "META-INF/hb-rpc/";

    public static Map<String, LinkedHashMap<String, Class<?>>> EXTENSION_LOADER_CLASS_CACHE = new ConcurrentHashMap<>();

    public void loadExtension(Class<?> clazz) throws IOException, ClassNotFoundException{
        if(clazz == null){
            throw new IllegalArgumentException("class is null");
        }
        String spiFilePath = EXTENSION_LOADER_DIR_PREFIX + clazz.getName();
        ClassLoader classLoader = this.getClass().getClassLoader();
        Enumeration<URL> resources = classLoader.getResources(spiFilePath);
        while(resources.hasMoreElements()){
            URL url = resources.nextElement();
            InputStreamReader inputStreamReader = new InputStreamReader(url.openStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            LinkedHashMap<String, Class<?>> classMap = new LinkedHashMap<>();
            while((line = bufferedReader.readLine()) != null){
                //如果配置中有 #注释，则忽略
                if(line.startsWith("#")){
                    continue;
                }
                String[] lineArr = line.split("=");
                String implClassName = lineArr[0];
                String interfaceName= lineArr[1];
                classMap.put(implClassName, Class.forName(interfaceName));
            }
            //只会触发class文件的加载，不会触发对象的实例化
            if(EXTENSION_LOADER_CLASS_CACHE.containsKey(clazz.getName())){
                EXTENSION_LOADER_CLASS_CACHE.get(clazz.getName()).putAll(classMap);
            } else {
                EXTENSION_LOADER_CLASS_CACHE.put(clazz.getName(), classMap);
            }
        }
    }
}
