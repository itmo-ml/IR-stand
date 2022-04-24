package ru.itmo.stand.cache.repository

import org.springframework.data.repository.CrudRepository
import ru.itmo.stand.cache.model.Student


interface StudentRepository : CrudRepository<Student, String>
