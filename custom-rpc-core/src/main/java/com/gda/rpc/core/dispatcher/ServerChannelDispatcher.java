package com.gda.rpc.core.dispatcher;

import com.gda.rpc.core.common.RpcInvocation;
import com.gda.rpc.core.common.RpcProtocol;
import com.gda.rpc.core.common.excrption.RpcException;
import com.gda.rpc.core.server.ServerChannelReadData;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.lang.reflect.Method;
import java.util.concurrent.*;

import static com.gda.rpc.core.common.cache.CommonServerCache.*;

public class ServerChannelDispatcher {

    private BlockingQueue<ServerChannelReadData> RPC_DATA_QUEUE;

    private ExecutorService executorService;

    public ServerChannelDispatcher(){

    }

    public void init(int queueSize, int bizThreadNums){
        RPC_DATA_QUEUE = new ArrayBlockingQueue<>(queueSize);
        executorService = new ThreadPoolExecutor(bizThreadNums, bizThreadNums, 3L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(512),
                new ThreadFactoryBuilder().setNameFormat("hb-rpc-pool-%d").build());
    }

    public void add(ServerChannelReadData serverChannelReadData){
        RPC_DATA_QUEUE.add(serverChannelReadData);
    }

    class ServerJobCoreHandler implements Runnable{
        @Override
        public void run() {
            while(true){
                try{
                    ServerChannelReadData serverChannelReadData = RPC_DATA_QUEUE.take();
                    executorService.submit(new Runnable(){
                        @Override
                        public void run() {
                            try{
                                RpcProtocol rpcProtocol = serverChannelReadData.getRpcProtocol();
                                RpcInvocation rpcInvocation = SERVER_SERIALIZE_FACTORY.deserialize(rpcProtocol.getContent(), RpcInvocation.class);
                                //执行过滤
                                try{
                                    SERVER_BEFORE_FILTER_CHAIN.doFilter(rpcInvocation);
                                } catch (Exception e){
                                    if(e instanceof RpcException){
                                        RpcException rpcException = (RpcException) e;
                                        RpcInvocation reqParam = rpcException.getRpcInvocation();
                                        rpcInvocation.setE(rpcException);
                                        byte[] body = SERVER_SERIALIZE_FACTORY.serialize(reqParam);
                                        RpcProtocol respRpcProtocol = new RpcProtocol(body);
                                        serverChannelReadData.getChannelHandlerContext().writeAndFlush(respRpcProtocol);
                                        return;
                                    }
                                }
                                Object aimObject = PROVIDER_CLASS_MAP.get(rpcInvocation.getTargetServiceName());
                                Method[] methods = aimObject.getClass().getDeclaredMethods();
                                Object result = null;
                                for(Method method : methods){
                                    if(method.getName().equals(rpcInvocation.getTargetMethod())){
                                        if(method.getReturnType().equals(Void.TYPE)){
                                            try{
                                                method.invoke(aimObject, rpcInvocation.getArgs());
                                            } catch (Exception e){
                                                rpcInvocation.setE(e);
                                            }
                                        } else {
                                            try{
                                                result = method.invoke(aimObject, rpcInvocation.getArgs());
                                            } catch (Exception e){
                                                rpcInvocation.setE(e);
                                            }
                                        }
                                        break;
                                    }
                                }
                                rpcInvocation.setResponse(result);
                                SERVER_AFTER_FILTER_CHAIN.doFilter(rpcInvocation);
                                RpcProtocol respRpcProtocol = new RpcProtocol(SERVER_SERIALIZE_FACTORY.serialize(rpcInvocation));
                                serverChannelReadData.getChannelHandlerContext().writeAndFlush(respRpcProtocol);
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public void startDataConsume(){
        Thread thread = new Thread(new ServerJobCoreHandler());
        thread.start();
    }
}
