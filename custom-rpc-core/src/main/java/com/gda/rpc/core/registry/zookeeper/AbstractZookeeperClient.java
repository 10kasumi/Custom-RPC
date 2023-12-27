package com.gda.rpc.core.registry.zookeeper;

import org.apache.zookeeper.Watcher;

import java.util.List;

public abstract class AbstractZookeeperClient {

    //注册地址 ip:port
    private String zkAddress;

    private int baseSleepTimes;

    private int maxRetries;

    public AbstractZookeeperClient(String zkAddress){
        this.zkAddress = zkAddress;
        this.baseSleepTimes = 1000;
        this.maxRetries = 3;
    }

    public AbstractZookeeperClient(String zkAddress, Integer baseSleepTimes, Integer maxRetriesTimes){
        this.zkAddress = zkAddress;
        if(baseSleepTimes == null){
            this.baseSleepTimes = 1000;
        } else {
            this.baseSleepTimes = baseSleepTimes;
        }
        if(maxRetriesTimes == null){
            this.maxRetries = 3;
        } else {
            this.maxRetries = maxRetriesTimes;
        }
    }

    public String getZkAddress() {
        return zkAddress;
    }

    public void setZkAddress(String zkAddress) {
        this.zkAddress = zkAddress;
    }

    public int getBaseSleepTimes() {
        return baseSleepTimes;
    }

    public void setBaseSleepTimes(int baseSleepTimes) {
        this.baseSleepTimes = baseSleepTimes;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public abstract void updateNodeData(String address, String data);

    public abstract Object getClient();

    /**
     * 拉取节点的数据
     * @param path
     * @return
     */
    public abstract String getNodeData(String path);

    /**
     * 拉取目录下的子节点数据
     * @param path
     * @return
     */
    public abstract List<String> getChildrenData(String path);

    public abstract void createPersistentData(String address, String data);

    /**
     * 创建有序且持久类节点数据信息
     * @param address
     * @param data
     */
    public abstract void createPersistentSeqData(String address, String data);

    /**
     * 创建有序临时类型的数据信息
     * @param address
     * @param data
     */
    public abstract void createTemporarySeqData(String address, String data);


    public abstract void createTemporaryData(String address, String data);

    public abstract void setTemporaryData(String address, String data);

    public abstract void destroy();

    /**
     * 展示节点下的数据
     * @param address
     * @return
     */
    public abstract List<String> listNode(String address);

    /**
     * 删除节点下的数据
     * @param address
     * @return
     */
    public abstract boolean deleteNode(String address);

    public abstract boolean existNode(String address);

    /**
     * 监听path路径下某个节点的数据变化
     * @param path
     * @param watcher
     */
    public abstract void watchNodeData(String path, Watcher watcher);

    /**
     * 监听子节点下的数据变化
     * @param path
     * @param watcher
     */
    public abstract void watchChildNodeData(String path, Watcher watcher);
}
