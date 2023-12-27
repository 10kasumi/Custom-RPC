package com.gda.rpc.core.router;

import com.gda.rpc.core.common.ChannelFutureWrapper;
import com.gda.rpc.core.registry.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.gda.rpc.core.common.cache.CommonClientCache.*;

/**
 * 岁己筛选
 */
public class RandomRouterImpl implements Router{
    @Override
    public void refreshRouterArr(Selector selector) {
        //获取服务提供者的数目
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(selector.getProviderServiceName());
        ChannelFutureWrapper[] arr = new ChannelFutureWrapper[channelFutureWrappers.size()];
        Integer[] result = createRandomIndex(arr.length);
        for(int i = 0; i < result.length; i++){
            arr[i] = channelFutureWrappers.get(result[i]);
        }
        SERVICE_ROUTER_MAP.put(selector.getProviderServiceName(), arr);
        URL url = new URL();
        url.setServiceName(selector.getProviderServiceName());
        ROUTER.updateWight(url);
    }

    @Override
    public ChannelFutureWrapper select(ChannelFutureWrapper[] channelFutureWrappers) {
        return CHANNEL_FUTURE_POLLING_REF.getChannelFutureWrapper(channelFutureWrappers);
    }

    @Override
    public void updateWight(URL url) {
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(url.getServiceName());
        Integer[] weightArr = createWeightArr(channelFutureWrappers);
        Integer[] finalArr = createRandomArr(weightArr);
        ChannelFutureWrapper[] finalChannelFutureWrappers = new ChannelFutureWrapper[finalArr.length];
        for(int i = 0; i < finalArr.length; i++){
            finalChannelFutureWrappers[i] = channelFutureWrappers.get(finalArr[i]);
        }
        SERVICE_ROUTER_MAP.put(url.getServiceName(), finalChannelFutureWrappers);
    }

    private static Integer[] createWeightArr(List<ChannelFutureWrapper> channelFutureWrappers){
        List<Integer> weightArr = new ArrayList<>();
        for(int k = 0; k < channelFutureWrappers.size(); k++){
            Integer weight = channelFutureWrappers.get(k).getWeight();
            int c = weight / 100;
            for(int i = 0; i < c; i++){
                weightArr.add(k);
            }
        }
        return weightArr.toArray(new Integer[0]);
    }

    private static Integer[] createRandomArr(Integer[] arr){
        int total = arr.length;
        Random random = new Random();
        for(int i = 0; i < total; i++){
            int j = random.nextInt(total);
            if (i == j) continue;
            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
        return arr;
    }

    private Integer[] createRandomIndex(int len){
        Random random = new Random();
        ArrayList<Integer> list = new ArrayList<>(len);
        int index = 0;
        while(index < len){
            int num = random.nextInt(len);
            if(!list.contains(num)){
                list.add(index++, num);
            }
        }
        return list.toArray(new Integer[0]);
    }
}
