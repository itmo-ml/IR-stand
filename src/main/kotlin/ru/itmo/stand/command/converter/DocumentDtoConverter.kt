package ru.itmo.stand.command.converter

import picocli.CommandLine.ITypeConverter
import ru.itmo.stand.dto.DocumentDto

class DocumentDtoConverter : ITypeConverter<DocumentDto> {

    override fun convert(value: String): DocumentDto {
        return DocumentDto(value)
    }
}
