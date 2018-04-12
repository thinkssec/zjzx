package com.common.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.*;

/**
 * Created by Administrator on 2018/1/17.
 */
@Component
public class CacheManage implements CacheDao {

    @Autowired
    private JedisPool jedisPool;

    @Override
    public void set(String key, Object value) {
        Jedis jedis = this.getJedis();

        if (value instanceof String) {
            jedis.set(key, (String) value);
        } else {
            jedis.set(key.getBytes(), serialize(value));
        }

        this.releaseJedis(jedis);
    }


    @Override
    public void set(String key, Object value, Integer seconds) {
        Jedis jedis = this.getJedis();
        if (value instanceof String)
            jedis.set(key, (String) value);
        else
            jedis.set(key.getBytes(), serialize(value));
        jedis.expire(key, seconds);
        this.releaseJedis(jedis);
    }

    @Override
    public Object get(String key) {
        Jedis jedis = this.getJedis();
        Object result = unserizlize(jedis.get(key.getBytes()));
        if (result == null)
            result = jedis.get(key);
        this.releaseJedis(jedis);
        return result;
    }

    @Override
    public void del(String key) {
        Jedis jedis = this.getJedis();
        jedis.del(key);
        jedis.del(key.getBytes());
        this.releaseJedis(jedis);
    }

    @Override
    public void expire(String key, Integer seconds) {
        Jedis jedis = this.getJedis();
        jedis.expire(key, seconds);
        jedis.expire(key.getBytes(), seconds);
        this.releaseJedis(jedis);
    }

    @Override
    public Long incr(String key) {
        Jedis jedis = this.getJedis();
        Long count = jedis.incr(key);
        this.releaseJedis(jedis);
        return count;
    }

    /**
     * 获取Jedis连接
     *
     * @return Jedis连接
     */
    public Jedis getJedis() {
        return this.jedisPool.getResource();
    }

    /**
     * 释放Jedis连接
     *
     * @param jedis jedis连接
     */
    public void releaseJedis(Jedis jedis) {
        jedis.close();
    }

    public static byte[] serialize(Object obj) {
        ByteArrayOutputStream byteOut = null;
        ObjectOutputStream ObjOut = null;
        try {
            byteOut = new ByteArrayOutputStream();
            ObjOut = new ObjectOutputStream(byteOut);
            ObjOut.writeObject(obj);
            ObjOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != ObjOut) {
                    ObjOut.close();
                }
            } catch (IOException e) {
                ObjOut = null;
            }
        }
        return byteOut.toByteArray();
    }

    public static Object unserizlize(byte[] byt) {
        if (byt == null) return null;
        ObjectInputStream ObjIn = null;
        Object retVal = null;
        try {
            ByteArrayInputStream byteIn = new ByteArrayInputStream(byt);
            ObjIn = new ObjectInputStream(byteIn);
            retVal = ObjIn.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != ObjIn) {
                    ObjIn.close();
                }
            } catch (IOException e) {
                ObjIn = null;
            }
        }
        return retVal;
    }
}
