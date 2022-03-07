package ru.itmo.stand.command.converter

import picocli.CommandLine.ITypeConverter
import ru.itmo.stand.dto.DocumentBm25Dto

class DocumentBm25DtoConverter : ITypeConverter<DocumentBm25Dto> {

    override fun convert(value: String): DocumentBm25Dto {
        return DocumentBm25Dto(value)
    }
}
