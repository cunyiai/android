package com.brzhang.gemma_ai.`data`.db

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.brzhang.gemma_ai.`data`.entity.ChatHistory
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ChatHistoryDao_Impl(
  __db: RoomDatabase,
) : ChatHistoryDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfChatHistory: EntityInsertAdapter<ChatHistory>
  init {
    this.__db = __db
    this.__insertAdapterOfChatHistory = object : EntityInsertAdapter<ChatHistory>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `chat_history` (`id`,`userId`,`timestamp`,`role`,`content`,`inputType`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ChatHistory) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.userId)
        statement.bindLong(3, entity.timestamp)
        statement.bindText(4, entity.role)
        statement.bindText(5, entity.content)
        statement.bindText(6, entity.inputType)
      }
    }
  }

  public override suspend fun insert(chat: ChatHistory): Long = performSuspending(__db, false, true)
      { _connection ->
    val _result: Long = __insertAdapterOfChatHistory.insertAndReturnId(_connection, chat)
    _result
  }

  public override fun getAllHistory(userId: String): Flow<List<ChatHistory>> {
    val _sql: String = "SELECT * FROM chat_history WHERE userId = ? ORDER BY timestamp ASC"
    return createFlow(__db, false, arrayOf("chat_history")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfRole: Int = getColumnIndexOrThrow(_stmt, "role")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfInputType: Int = getColumnIndexOrThrow(_stmt, "inputType")
        val _result: MutableList<ChatHistory> = mutableListOf()
        while (_stmt.step()) {
          val _item: ChatHistory
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpRole: String
          _tmpRole = _stmt.getText(_columnIndexOfRole)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpInputType: String
          _tmpInputType = _stmt.getText(_columnIndexOfInputType)
          _item = ChatHistory(_tmpId,_tmpUserId,_tmpTimestamp,_tmpRole,_tmpContent,_tmpInputType)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getRecent(userId: String, limit: Int): List<ChatHistory> {
    val _sql: String = "SELECT * FROM chat_history WHERE userId = ? ORDER BY timestamp DESC LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfRole: Int = getColumnIndexOrThrow(_stmt, "role")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfInputType: Int = getColumnIndexOrThrow(_stmt, "inputType")
        val _result: MutableList<ChatHistory> = mutableListOf()
        while (_stmt.step()) {
          val _item: ChatHistory
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpRole: String
          _tmpRole = _stmt.getText(_columnIndexOfRole)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpInputType: String
          _tmpInputType = _stmt.getText(_columnIndexOfInputType)
          _item = ChatHistory(_tmpId,_tmpUserId,_tmpTimestamp,_tmpRole,_tmpContent,_tmpInputType)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearAll(userId: String) {
    val _sql: String = "DELETE FROM chat_history WHERE userId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
