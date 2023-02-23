package ru.itmo.stand.service.sqlite

import org.springframework.stereotype.Service
import org.sqlite.SQLiteConfig
import ru.itmo.stand.config.StandProperties
import java.sql.Connection.TRANSACTION_READ_UNCOMMITTED
import java.sql.DriverManager
import java.sql.Statement
import javax.annotation.PostConstruct

@Service
class SqliteService(
    private val standProperties: StandProperties,
) {

    private val jdbcUrl: String = "jdbc:sqlite:${standProperties.app.basePath}/indexes/sqlite/index.sqlite3db"

    private val config = SQLiteConfig()
        .also {
            it.setTempStore(org.sqlite.SQLiteConfig.TempStore.MEMORY)
            it.setSynchronous(SQLiteConfig.SynchronousMode.OFF)
            it.setJournalMode(SQLiteConfig.JournalMode.OFF)
            it.setCacheSize(-3_048_576)
            it.setPragma(SQLiteConfig.Pragma.MMAP_SIZE, "3000000")
        }

    private val connection by lazy {

        val conn = DriverManager.getConnection(jdbcUrl, config.toProperties());
        conn.autoCommit = false
        conn
    }

    @PostConstruct
    private fun initialize() {
        val statement = getStatement()
        statement.executeUpdate(
            "CREATE TABLE IF NOT EXISTS $DB_NAME " +
                    "($TOKEN_KEY_FIELD VARCHAR($MAX_TOKEN_LENGTH), $CONTENT_FIELD TEXT, $DOC_ID_FIELD TEXT ) "
        )
        connection.commit()
    }

    fun completeIndexing() {
        val statement = getStatement()
        statement.executeUpdate("CREATE INDEX IF NOT EXISTS $INDEX_NAME ON $DB_NAME ($TOKEN_KEY_FIELD)")

        connection.commit()
    }


    fun saveInBatch(documents: List<SqliteDocument>) {
        val statement = connection.prepareStatement(
            "INSERT INTO $DB_NAME ($TOKEN_KEY_FIELD, $CONTENT_FIELD, $DOC_ID_FIELD) values (?, ?, ?);"
        )

        for (doc in documents) {
            statement.setString(1, doc.groupKey)
            statement.setString(2, doc.content)
            statement.setString(3, doc.documentId)
            statement.addBatch()
        }
        statement.executeBatch();

        connection.commit()
    }


    private fun getStatement(): Statement {
        val statement: Statement = connection.createStatement()
        statement.queryTimeout = TIMEOUT
        return statement
    }

    companion object {
        const val DB_NAME = "tokens"
        const val INDEX_NAME = "token_index"
        const val TOKEN_KEY_FIELD = "token"
        const val CONTENT_FIELD = "content"
        const val DOC_ID_FIELD = "docId"
        const val TIMEOUT = 600

        //google states that the longest english word has 29 chars, so I made it 30
        const val MAX_TOKEN_LENGTH = 30

    }
}