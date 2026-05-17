package com.brzhang.gemma_ai.`data`.db

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.brzhang.gemma_ai.`data`.entity.HealthRecord
import javax.`annotation`.processing.Generated
import kotlin.Float
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
public class HealthRecordDao_Impl(
  __db: RoomDatabase,
) : HealthRecordDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfHealthRecord: EntityInsertAdapter<HealthRecord>
  init {
    this.__db = __db
    this.__insertAdapterOfHealthRecord = object : EntityInsertAdapter<HealthRecord>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `health_record` (`id`,`userId`,`timestamp`,`recordType`,`value`,`secondaryValue`,`unit`,`aiSuggestion`,`dangerLevel`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: HealthRecord) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.userId)
        statement.bindLong(3, entity.timestamp)
        statement.bindText(4, entity.recordType)
        statement.bindDouble(5, entity.value.toDouble())
        val _tmpSecondaryValue: Float? = entity.secondaryValue
        if (_tmpSecondaryValue == null) {
          statement.bindNull(6)
        } else {
          statement.bindDouble(6, _tmpSecondaryValue.toDouble())
        }
        statement.bindText(7, entity.unit)
        statement.bindText(8, entity.aiSuggestion)
        statement.bindLong(9, entity.dangerLevel.toLong())
      }
    }
  }

  public override suspend fun insert(record: HealthRecord): Long = performSuspending(__db, false,
      true) { _connection ->
    val _result: Long = __insertAdapterOfHealthRecord.insertAndReturnId(_connection, record)
    _result
  }

  public override suspend fun getRecent(userId: String, limit: Int): List<HealthRecord> {
    val _sql: String =
        "SELECT * FROM health_record WHERE userId = ? ORDER BY timestamp DESC LIMIT ?"
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
        val _columnIndexOfRecordType: Int = getColumnIndexOrThrow(_stmt, "recordType")
        val _columnIndexOfValue: Int = getColumnIndexOrThrow(_stmt, "value")
        val _columnIndexOfSecondaryValue: Int = getColumnIndexOrThrow(_stmt, "secondaryValue")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfAiSuggestion: Int = getColumnIndexOrThrow(_stmt, "aiSuggestion")
        val _columnIndexOfDangerLevel: Int = getColumnIndexOrThrow(_stmt, "dangerLevel")
        val _result: MutableList<HealthRecord> = mutableListOf()
        while (_stmt.step()) {
          val _item: HealthRecord
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpRecordType: String
          _tmpRecordType = _stmt.getText(_columnIndexOfRecordType)
          val _tmpValue: Float
          _tmpValue = _stmt.getDouble(_columnIndexOfValue).toFloat()
          val _tmpSecondaryValue: Float?
          if (_stmt.isNull(_columnIndexOfSecondaryValue)) {
            _tmpSecondaryValue = null
          } else {
            _tmpSecondaryValue = _stmt.getDouble(_columnIndexOfSecondaryValue).toFloat()
          }
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpAiSuggestion: String
          _tmpAiSuggestion = _stmt.getText(_columnIndexOfAiSuggestion)
          val _tmpDangerLevel: Int
          _tmpDangerLevel = _stmt.getLong(_columnIndexOfDangerLevel).toInt()
          _item =
              HealthRecord(_tmpId,_tmpUserId,_tmpTimestamp,_tmpRecordType,_tmpValue,_tmpSecondaryValue,_tmpUnit,_tmpAiSuggestion,_tmpDangerLevel)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getRecentByType(
    type: String,
    userId: String,
    limit: Int,
  ): List<HealthRecord> {
    val _sql: String =
        "SELECT * FROM health_record WHERE userId = ? AND recordType = ? ORDER BY timestamp DESC LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        _argIndex = 2
        _stmt.bindText(_argIndex, type)
        _argIndex = 3
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfRecordType: Int = getColumnIndexOrThrow(_stmt, "recordType")
        val _columnIndexOfValue: Int = getColumnIndexOrThrow(_stmt, "value")
        val _columnIndexOfSecondaryValue: Int = getColumnIndexOrThrow(_stmt, "secondaryValue")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfAiSuggestion: Int = getColumnIndexOrThrow(_stmt, "aiSuggestion")
        val _columnIndexOfDangerLevel: Int = getColumnIndexOrThrow(_stmt, "dangerLevel")
        val _result: MutableList<HealthRecord> = mutableListOf()
        while (_stmt.step()) {
          val _item: HealthRecord
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpRecordType: String
          _tmpRecordType = _stmt.getText(_columnIndexOfRecordType)
          val _tmpValue: Float
          _tmpValue = _stmt.getDouble(_columnIndexOfValue).toFloat()
          val _tmpSecondaryValue: Float?
          if (_stmt.isNull(_columnIndexOfSecondaryValue)) {
            _tmpSecondaryValue = null
          } else {
            _tmpSecondaryValue = _stmt.getDouble(_columnIndexOfSecondaryValue).toFloat()
          }
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpAiSuggestion: String
          _tmpAiSuggestion = _stmt.getText(_columnIndexOfAiSuggestion)
          val _tmpDangerLevel: Int
          _tmpDangerLevel = _stmt.getLong(_columnIndexOfDangerLevel).toInt()
          _item =
              HealthRecord(_tmpId,_tmpUserId,_tmpTimestamp,_tmpRecordType,_tmpValue,_tmpSecondaryValue,_tmpUnit,_tmpAiSuggestion,_tmpDangerLevel)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByDateRange(
    from: Long,
    to: Long,
    userId: String,
  ): List<HealthRecord> {
    val _sql: String =
        "SELECT * FROM health_record WHERE userId = ? AND timestamp BETWEEN ? AND ? ORDER BY timestamp ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, from)
        _argIndex = 3
        _stmt.bindLong(_argIndex, to)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfRecordType: Int = getColumnIndexOrThrow(_stmt, "recordType")
        val _columnIndexOfValue: Int = getColumnIndexOrThrow(_stmt, "value")
        val _columnIndexOfSecondaryValue: Int = getColumnIndexOrThrow(_stmt, "secondaryValue")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfAiSuggestion: Int = getColumnIndexOrThrow(_stmt, "aiSuggestion")
        val _columnIndexOfDangerLevel: Int = getColumnIndexOrThrow(_stmt, "dangerLevel")
        val _result: MutableList<HealthRecord> = mutableListOf()
        while (_stmt.step()) {
          val _item: HealthRecord
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpRecordType: String
          _tmpRecordType = _stmt.getText(_columnIndexOfRecordType)
          val _tmpValue: Float
          _tmpValue = _stmt.getDouble(_columnIndexOfValue).toFloat()
          val _tmpSecondaryValue: Float?
          if (_stmt.isNull(_columnIndexOfSecondaryValue)) {
            _tmpSecondaryValue = null
          } else {
            _tmpSecondaryValue = _stmt.getDouble(_columnIndexOfSecondaryValue).toFloat()
          }
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpAiSuggestion: String
          _tmpAiSuggestion = _stmt.getText(_columnIndexOfAiSuggestion)
          val _tmpDangerLevel: Int
          _tmpDangerLevel = _stmt.getLong(_columnIndexOfDangerLevel).toInt()
          _item =
              HealthRecord(_tmpId,_tmpUserId,_tmpTimestamp,_tmpRecordType,_tmpValue,_tmpSecondaryValue,_tmpUnit,_tmpAiSuggestion,_tmpDangerLevel)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeAll(userId: String): Flow<List<HealthRecord>> {
    val _sql: String = "SELECT * FROM health_record WHERE userId = ? ORDER BY timestamp DESC"
    return createFlow(__db, false, arrayOf("health_record")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfRecordType: Int = getColumnIndexOrThrow(_stmt, "recordType")
        val _columnIndexOfValue: Int = getColumnIndexOrThrow(_stmt, "value")
        val _columnIndexOfSecondaryValue: Int = getColumnIndexOrThrow(_stmt, "secondaryValue")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfAiSuggestion: Int = getColumnIndexOrThrow(_stmt, "aiSuggestion")
        val _columnIndexOfDangerLevel: Int = getColumnIndexOrThrow(_stmt, "dangerLevel")
        val _result: MutableList<HealthRecord> = mutableListOf()
        while (_stmt.step()) {
          val _item: HealthRecord
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpRecordType: String
          _tmpRecordType = _stmt.getText(_columnIndexOfRecordType)
          val _tmpValue: Float
          _tmpValue = _stmt.getDouble(_columnIndexOfValue).toFloat()
          val _tmpSecondaryValue: Float?
          if (_stmt.isNull(_columnIndexOfSecondaryValue)) {
            _tmpSecondaryValue = null
          } else {
            _tmpSecondaryValue = _stmt.getDouble(_columnIndexOfSecondaryValue).toFloat()
          }
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpAiSuggestion: String
          _tmpAiSuggestion = _stmt.getText(_columnIndexOfAiSuggestion)
          val _tmpDangerLevel: Int
          _tmpDangerLevel = _stmt.getLong(_columnIndexOfDangerLevel).toInt()
          _item =
              HealthRecord(_tmpId,_tmpUserId,_tmpTimestamp,_tmpRecordType,_tmpValue,_tmpSecondaryValue,_tmpUnit,_tmpAiSuggestion,_tmpDangerLevel)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLast3ByType(type: String, userId: String): List<HealthRecord> {
    val _sql: String =
        "SELECT * FROM health_record WHERE userId = ? AND recordType = ? ORDER BY timestamp DESC LIMIT 3"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        _argIndex = 2
        _stmt.bindText(_argIndex, type)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfRecordType: Int = getColumnIndexOrThrow(_stmt, "recordType")
        val _columnIndexOfValue: Int = getColumnIndexOrThrow(_stmt, "value")
        val _columnIndexOfSecondaryValue: Int = getColumnIndexOrThrow(_stmt, "secondaryValue")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfAiSuggestion: Int = getColumnIndexOrThrow(_stmt, "aiSuggestion")
        val _columnIndexOfDangerLevel: Int = getColumnIndexOrThrow(_stmt, "dangerLevel")
        val _result: MutableList<HealthRecord> = mutableListOf()
        while (_stmt.step()) {
          val _item: HealthRecord
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpRecordType: String
          _tmpRecordType = _stmt.getText(_columnIndexOfRecordType)
          val _tmpValue: Float
          _tmpValue = _stmt.getDouble(_columnIndexOfValue).toFloat()
          val _tmpSecondaryValue: Float?
          if (_stmt.isNull(_columnIndexOfSecondaryValue)) {
            _tmpSecondaryValue = null
          } else {
            _tmpSecondaryValue = _stmt.getDouble(_columnIndexOfSecondaryValue).toFloat()
          }
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpAiSuggestion: String
          _tmpAiSuggestion = _stmt.getText(_columnIndexOfAiSuggestion)
          val _tmpDangerLevel: Int
          _tmpDangerLevel = _stmt.getLong(_columnIndexOfDangerLevel).toInt()
          _item =
              HealthRecord(_tmpId,_tmpUserId,_tmpTimestamp,_tmpRecordType,_tmpValue,_tmpSecondaryValue,_tmpUnit,_tmpAiSuggestion,_tmpDangerLevel)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
