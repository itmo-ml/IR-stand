package ru.itmo.stand.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.itmo.stand.service.DocumentService

@Configuration
class DocumentConfig {

    @Bean
    fun documentServicesByMethod(documentServices: List<DocumentService>): Map<Method, DocumentService> {
        return documentServices.associateBy { it.method }
    }

}
