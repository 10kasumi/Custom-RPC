package com.gda.rpc.core.serialize.kryo;

import com.gda.rpc.core.serialize.SerializeFactory;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class KryoSerializeFactory implements SerializeFactory {

    private static final ThreadLocal<Kryo> kryos = ThreadLocal.withInitial(() -> new Kryo());

    @Override
    public <T> byte[] serialize(T t) {
        Output output = null;
        try{
            Kryo kryo = kryos.get();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            output = new Output(os);
            kryo.writeClassAndObject(output, t);
            return output.toBytes();
        } catch (Exception e){
            throw new RuntimeException(e);
        } finally {
            if(output != null){
                output.close();
            }
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        Input input = null;
        try{
            Kryo kryo = kryos.get();
            ByteArrayInputStream is = new ByteArrayInputStream(data);
            input = new Input(is);
            return (T)kryo.readClassAndObject(input);
        } catch(Exception e){
            throw new RuntimeException(e);
        } finally {
            if(input != null){
                input.close();
            }
        }
    }
}
