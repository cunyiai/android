package com.brzhang.gemma_ai.`data`.db

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _userProfileDao: Lazy<UserProfileDao> = lazy {
    UserProfileDao_Impl(this)
  }

  private val _healthRecordDao: Lazy<HealthRecordDao> = lazy {
    HealthRecordDao_Impl(this)
  }

  private val _chatHistoryDao: Lazy<ChatHistoryDao> = lazy {
    ChatHistoryDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1,
        "1abe40d82923c81b597591ce74b4f213", "bfc8b3779686220c010900839730751c") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `user_profile` (`userId` TEXT NOT NULL, `name` TEXT NOT NULL, `age` INTEGER NOT NULL, `gender` TEXT NOT NULL, `phone` TEXT NOT NULL, `bloodType` TEXT NOT NULL, `allergies` TEXT NOT NULL, `chronicDiseases` TEXT NOT NULL, `chronicConditions` TEXT NOT NULL, `currentMedications` TEXT NOT NULL, `emergencyContact` TEXT NOT NULL, `emergencyContacts` TEXT NOT NULL, `emergencyPhone` TEXT NOT NULL, `village` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`userId`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `health_record` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userId` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `recordType` TEXT NOT NULL, `value` REAL NOT NULL, `secondaryValue` REAL, `unit` TEXT NOT NULL, `aiSuggestion` TEXT NOT NULL, `dangerLevel` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `chat_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userId` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `role` TEXT NOT NULL, `content` TEXT NOT NULL, `inputType` TEXT NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '1abe40d82923c81b597591ce74b4f213')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `user_profile`")
        connection.execSQL("DROP TABLE IF EXISTS `health_record`")
        connection.execSQL("DROP TABLE IF EXISTS `chat_history`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsUserProfile: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsUserProfile.put("userId", TableInfo.Column("userId", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfile.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfile.put("age", TableInfo.Column("age", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfile.put("gender", TableInfo.Column("gender", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfile.put("phone", TableInfo.Column("phone", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfile.put("bloodType", TableInfo.Column("bloodType", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfile.put("allergies", TableInfo.Column("allergies", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfile.put("chronicDiseases", TableInfo.Column("chronicDiseases", "TEXT", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfile.put("chronicConditions", TableInfo.Column("chronicConditions", "TEXT",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfile.put("currentMedications", TableInfo.Column("currentMedications", "TEXT",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfile.put("emergencyContact", TableInfo.Column("emergencyContact", "TEXT",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfile.put("emergencyContacts", TableInfo.Column("emergencyContacts", "TEXT",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfile.put("emergencyPhone", TableInfo.Column("emergencyPhone", "TEXT", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfile.put("village", TableInfo.Column("village", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfile.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfile.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysUserProfile: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesUserProfile: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoUserProfile: TableInfo = TableInfo("user_profile", _columnsUserProfile,
            _foreignKeysUserProfile, _indicesUserProfile)
        val _existingUserProfile: TableInfo = read(connection, "user_profile")
        if (!_infoUserProfile.equals(_existingUserProfile)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |user_profile(com.brzhang.gemma_ai.data.entity.UserProfile).
              | Expected:
              |""".trimMargin() + _infoUserProfile + """
              |
              | Found:
              |""".trimMargin() + _existingUserProfile)
        }
        val _columnsHealthRecord: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsHealthRecord.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthRecord.put("userId", TableInfo.Column("userId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthRecord.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthRecord.put("recordType", TableInfo.Column("recordType", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthRecord.put("value", TableInfo.Column("value", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthRecord.put("secondaryValue", TableInfo.Column("secondaryValue", "REAL", false,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthRecord.put("unit", TableInfo.Column("unit", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthRecord.put("aiSuggestion", TableInfo.Column("aiSuggestion", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthRecord.put("dangerLevel", TableInfo.Column("dangerLevel", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysHealthRecord: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesHealthRecord: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoHealthRecord: TableInfo = TableInfo("health_record", _columnsHealthRecord,
            _foreignKeysHealthRecord, _indicesHealthRecord)
        val _existingHealthRecord: TableInfo = read(connection, "health_record")
        if (!_infoHealthRecord.equals(_existingHealthRecord)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |health_record(com.brzhang.gemma_ai.data.entity.HealthRecord).
              | Expected:
              |""".trimMargin() + _infoHealthRecord + """
              |
              | Found:
              |""".trimMargin() + _existingHealthRecord)
        }
        val _columnsChatHistory: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsChatHistory.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsChatHistory.put("userId", TableInfo.Column("userId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsChatHistory.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsChatHistory.put("role", TableInfo.Column("role", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsChatHistory.put("content", TableInfo.Column("content", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsChatHistory.put("inputType", TableInfo.Column("inputType", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysChatHistory: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesChatHistory: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoChatHistory: TableInfo = TableInfo("chat_history", _columnsChatHistory,
            _foreignKeysChatHistory, _indicesChatHistory)
        val _existingChatHistory: TableInfo = read(connection, "chat_history")
        if (!_infoChatHistory.equals(_existingChatHistory)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |chat_history(com.brzhang.gemma_ai.data.entity.ChatHistory).
              | Expected:
              |""".trimMargin() + _infoChatHistory + """
              |
              | Found:
              |""".trimMargin() + _existingChatHistory)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "user_profile", "health_record",
        "chat_history")
  }

  public override fun clearAllTables() {
    super.performClear(false, "user_profile", "health_record", "chat_history")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(UserProfileDao::class, UserProfileDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(HealthRecordDao::class, HealthRecordDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ChatHistoryDao::class, ChatHistoryDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun userProfileDao(): UserProfileDao = _userProfileDao.value

  public override fun healthRecordDao(): HealthRecordDao = _healthRecordDao.value

  public override fun chatHistoryDao(): ChatHistoryDao = _chatHistoryDao.value
}
