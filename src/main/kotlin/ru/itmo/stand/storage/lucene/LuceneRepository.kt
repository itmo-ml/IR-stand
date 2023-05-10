package ru.itmo.stand.storage.lucene

import io.github.oshai.KotlinLogging
import jakarta.annotation.PreDestroy
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.FSDirectory
import java.nio.file.Paths

abstract class LuceneRepository {
    abstract val indexPath: String
    abstract val writerConfig: IndexWriterConfig
    private val lazyIndexDir: Lazy<FSDirectory> = lazy { FSDirectory.open(Paths.get(indexPath)) }
    private val indexDir: FSDirectory by lazyIndexDir
    protected val writer: IndexWriter by lazy { IndexWriter(indexDir, writerConfig) }
    protected val searcher by lazy { IndexSearcher(DirectoryReader.open(indexDir)) }
    protected val log = KotlinLogging.logger { }

    @PreDestroy
    private fun closeIndex() {
        if (lazyIndexDir.isInitialized()) {
            indexDir.close()
            log.info("Index directory is closed")
        }
    }
}
