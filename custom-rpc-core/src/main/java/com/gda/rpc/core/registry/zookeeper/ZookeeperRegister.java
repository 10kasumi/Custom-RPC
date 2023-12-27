package com.gda.rpc.core.registry.zookeeper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.gda.rpc.core.common.event.RpcEvent;
import com.gda.rpc.core.common.event.RpcListenerLoader;
import com.gda.rpc.core.common.event.RpcNodeUpdateEvent;
import com.gda.rpc.core.common.event.RpcUpdateEvent;
import com.gda.rpc.core.common.event.data.ProviderNodeInfo;
import com.gda.rpc.core.common.event.data.URLChangeWrapper;
import com.gda.rpc.core.common.utils.CommonUtil;
import com.gda.rpc.core.registry.AbstractRegister;
import com.gda.rpc.core.registry.RegistryService;
import com.gda.rpc.core.registry.URL;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gda.rpc.core.common.cache.CommonClientCache.CLIENT_CONFIG;
import static com.gda.rpc.core.common.cache.CommonServerCache.SERVER_CONFIG;

/**
 * 对zookeeper完成服务注册、服务订阅、服务下线等操作
 */
public class ZookeeperRegister extends AbstractRegister implements RegistryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperRegister.class);

    private final AbstractZookeeperClient zkClient;

    private final String ROOT = "/hb-rpc";

    public ZookeeperRegister(){
        String registryAddr = CLIENT_CONFIG != null ? CLIENT_CONFIG.getRegisterAddr() : SERVER_CONFIG.getRegisterAddr();
        this.zkClient = new CuratorZookeeperClient(registryAddr);
    }

    private String getProviderPath(URL url) {
        return ROOT + "/" + url.getServiceName() + "/provider/" + url.getParameters().get("host") + ":"
                + url.getParameters().get("port");
    }

    private String getConsumerPath(URL url) {
        return ROOT + "/" + url.getServiceName() + "/consumer/" + url.getApplicationName() + ":"
                + url.getParameters().get("host") + ":";
    }

    @Override
    public List<String> getProviderIps(String serviceName) {
        return this.zkClient.getChildrenData(ROOT + "/" + serviceName + "/provider");
    }

    @Override
    public Map<String, String> getServiceWeightMap(String serviceName) {
        List<String> nodeDataList = this.zkClient.getChildrenData(ROOT + "/" + serviceName + "/provider");
        HashMap<String, String> result = new HashMap<>(16);
        for (String ipHost : nodeDataList) {
            String childData = this.zkClient.getNodeData(ROOT + "/" + serviceName + "/provider/" + ipHost);
            result.put(ipHost, childData);
        }
        return result;
    }



    @Override
    public void doBeforeSubscribe(URL url) {

    }

    @Override
    public void doAfterSubscribe(URL url) {
        String newServerNodePath = ROOT + "/" + url.getServiceName() + "/provider";
        watchChildNodeData(newServerNodePath);
        String providerIpStrJson = url.getParameters().get("providerIps");
        List<String> providerIpList = JSON.parseObject(providerIpStrJson, new TypeReference<List<String>>(){});
        for (String providerIP : providerIpList) {
            this.watchNodeDataChange(newServerNodePath + "/" + providerIP);
        }
    }

    @Override
    public void register(URL url) {
        if(!zkClient.existNode(ROOT)){
            zkClient.createPersistentData(ROOT, "");
        }
        String urlStr = URL.buildProviderUrlStr(url);
        if(zkClient.existNode(getProviderPath(url))){
            zkClient.deleteNode(getProviderPath(url));
        }
        zkClient.createTemporaryData(getProviderPath(url), urlStr);
        super.register(url);
    }

    @Override
    public void unRegister(URL url) {
        zkClient.deleteNode(getProviderPath(url));
        super.unRegister(url);
    }

    @Override
    public void subscribe(URL url) {
        if(!zkClient.existNode(ROOT)){
            zkClient.createPersistentData(ROOT, "");
        }
        String urlStr = URL.buildConsumerUrlStr(url);
        if (zkClient.existNode(getConsumerPath(url))) {
            zkClient.deleteNode(getConsumerPath(url));
        }
        zkClient.createTemporarySeqData(getConsumerPath(url), urlStr);
        super.subscribe(url);
    }

    @Override
    public void doUnSubscribe(URL url) {
        zkClient.deleteNode(getConsumerPath(url));
        super.doUnSubscribe(url);
    }

    /**
     * 监听服务子节点数据变化
     * @param newServerNodePath
     */
    private void watchChildNodeData(String newServerNodePath) {
        zkClient.watchChildNodeData(newServerNodePath, new Watcher(){

            @Override
            public void process(WatchedEvent watchedEvent) {
                String path = watchedEvent.getPath();
                LOGGER.info("[watchChildNodeData] 监听到zk节点下的" + path + "节点数据发生变化");
                List<String> childrenDataList = zkClient.getChildrenData(path);
                URLChangeWrapper urlChangeWrapper = new URLChangeWrapper();
                Map<String, String> nodeDetailInfoMap = new HashMap<>();
                for(String providerAddress : childrenDataList){
                    String nodeDetailInfo = zkClient.getNodeData(path + "/" + providerAddress);
                    nodeDetailInfoMap.put(providerAddress, nodeDetailInfo);
                }
                urlChangeWrapper.setNodeDataUrl(nodeDetailInfoMap);
                urlChangeWrapper.setProviderUrl(childrenDataList);
                urlChangeWrapper.setServiceName(path.split("/")[2]);
                RpcEvent rpcEvent = new RpcUpdateEvent(urlChangeWrapper);
                RpcListenerLoader.sendEvent(rpcEvent);

                //收到回调后再注册一次监听
                watchChildNodeData(path);
            }
        });
    }

    /**
     * 订阅节点内部数据变化
     * @param serverNodePath
     */
    public void watchNodeDataChange(String serverNodePath){
        zkClient.watchNodeData(serverNodePath, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                String path = watchedEvent.getPath();
                LOGGER.info("[watchNodeDataChange]收到子节点" + path + "数据变化");
                String nodeData = zkClient.getNodeData(path);
                if(CommonUtil.isEmpty(nodeData)){
                    LOGGER.error("{} node data is null", path);
                } else {
                    ProviderNodeInfo providerNodeINfo = URL.buildUrlFromUrlStr(nodeData);
                    RpcEvent rpcEvent = new RpcNodeUpdateEvent(providerNodeINfo);
                    RpcListenerLoader.sendEvent(rpcEvent);
                }
                watchNodeDataChange(serverNodePath);
            }
        });
    }
}
