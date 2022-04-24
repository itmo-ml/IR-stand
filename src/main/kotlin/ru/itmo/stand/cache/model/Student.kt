package ru.itmo.stand.cache.model

import java.io.Serializable
import org.springframework.data.annotation.Id

import org.springframework.data.redis.core.RedisHash

@RedisHash("Student")
class Student : Serializable {
    enum class Gender {
        MALE, FEMALE
    }

    @Id
    var id: String? = null
    var name: String? = null
    var gender: Gender? = null
    var grade = 0
}
