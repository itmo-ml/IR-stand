package ru.itmo.stand.util

import java.io.File
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

fun File.createPath(): File = this.apply { parentFile.mkdirs() }

fun walkDirectory(dirPath: Path): List<Path> {
    val paths = mutableListOf<Path>()
    Files.walkFileTree(
        dirPath,
        object : SimpleFileVisitor<Path>() {
            override fun visitFile(path: Path, attrs: BasicFileAttributes): FileVisitResult {
                paths.add(path)
                return FileVisitResult.CONTINUE
            }
        }
    )
    return paths
}
