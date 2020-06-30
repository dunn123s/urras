package com.chuang.urras.toolskit.third.redis;

import com.chuang.urras.toolskit.basic.CollectionKit;
import com.chuang.urras.toolskit.basic.BasicKit;
import com.chuang.urras.toolskit.basic.cache.HCache;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 *	redis 缓存实现。
 * 由于redis本身便支持 HMap (map里面包含map)的方式来存储数据。
 * 因此Redis的Cached实现更
 */
public class RedisHCached implements HCache {
	public RedisHCached() {

	}
	// -1 - never expireMS
	private long expireMS = -1;
	private RedisTemplate<String, Object> redisTemplate;


    @Override
    public Optional<Boolean> delete(byte[] key) {
        return Optional.of(redisTemplate.delete(new String(key)));
    }

    @Override
    public Optional<Long> delete(byte[]... key) {
        return Optional.of(redisTemplate.delete(
                Arrays.stream(key).map(String::new).collect(Collectors.toList()))
        );
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public Optional<Long> deletePattern(byte[] keyPattern) {
        return Optional.ofNullable(redisTemplate.execute((RedisCallback<Long>) connection -> {
            connection.multi();
            Set<byte[]> keys = connection.keys(keyPattern);
            Long len = connection.del(keys.toArray(new byte[0][]));
            connection.exec();
            return len;
        }));
    }

	@Override
	public Set<byte[]> getKeys(final byte[] pattern) {
        return redisTemplate.execute((RedisCallback<Set<byte[]>>)connection -> CollectionKit.nullToEmpty(connection.keys(pattern)));
	}

    /**
     * 根据 正则表达式，获取所有key的所有 field
     * 注意：redis实现中没有通过 pattern 获取 field 的方法，所以这个实现是通过获取keys后，再遍历keys获取field的，可能性能上有点问题。
     * @param keyPattern key表达式
     * @return 根据正则匹配的所有key 的 field
     */
    @Override
    public Set<Object> getFields(byte[] keyPattern) {
        return redisTemplate.execute((RedisCallback<Set<Object>>)connection -> {
            connection.multi();
            Set<byte[]> keySet = CollectionKit.nullToEmpty(connection.keys(keyPattern));
            Set<Object> result = new HashSet<>();
            for(byte[] b: keySet) {
                Set<byte[]> filedSet = CollectionKit.nullToEmpty(connection.hKeys(b));
                for(byte[] objBytes: filedSet) {
                    Optional<Object> obj = BasicKit.unSerialize(objBytes);
                    result.add(obj.get());
                }
            }

            connection.exec();
            return result;
        });
    }

    /**
     * redis没有直接获取 key 长度的方法，目前只能先获取全部，然后计算长度
     * @param pattern key正则
     */
    @Override
    public int getKeysLen(byte[] pattern) {
        return getKeys(pattern).size();
    }

    @Override
	@SuppressWarnings("ConstantConditions")
	public Optional<Boolean> updateValue(final byte[] key, final byte[] filed, final byte[] value) {
		return Optional.ofNullable(redisTemplate.execute((RedisCallback<Boolean>) connection -> connection.hSet(key, filed, value)));
	}

	@Override
	public Optional<Object> getValue(final byte[] key, final byte[] filed) {
		return redisTemplate.execute((RedisCallback<Optional<Object>>) connection -> {
            byte[] hGet = connection.hGet(key, filed);
            return BasicKit.unSerialize(hGet);
        });
	}
	
	
	@Override
    @SuppressWarnings("ConstantConditions")
	public Optional<Long> deleteValue(final byte[] key, final byte[] filed) {
		return Optional.ofNullable(redisTemplate.execute((RedisCallback<Long>) connection -> connection.hDel(key, filed)));
	}
	
	
	@Override
	public Long getFieldSize(final byte[] key) {
		return redisTemplate.execute((RedisCallback<Long>) connection -> connection.hLen(key));
	}

    @Override
    @SuppressWarnings("ConstantConditions")
    public Optional<Long> deleteField(byte[] key, byte[]... field) {
        return Optional.ofNullable(redisTemplate.execute((RedisCallback<Long>) connection -> connection.hDel(key, field)));
    }


    @Override
	public List<Object> getValuesByKey(final byte[] key) {
		return redisTemplate.execute((RedisCallback<List<Object>>) connection -> CollectionKit.nullToEmpty(connection.hVals(key)).stream().map(bytes -> BasicKit.unSerialize(bytes).get()).collect(Collectors.toList()));
	}

	public RedisTemplate<String, Object> getRedisTemplate() {
		return redisTemplate;
	}

	public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public long getExpireMS() {
		return expireMS;
	}

	public void setExpireMS(int expireMS) {
		this.expireMS = expireMS;
	}






	

}
