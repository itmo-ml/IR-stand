package ru.itmo.stand.cache.repository

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository

@Repository
class TermRepository (
    private val redisTemplate: RedisTemplate<Double, String>
        ){

    private val ops = redisTemplate.opsForValue();

    public fun saveTerm(key: Double, value: String) {

        //TODO: can be optimized for batch insert using lua scripts
        ops.set(key, value);
    }

    public fun getTerm(key: Double): String? {
        return ops.get(key)
    }

}