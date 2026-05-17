package com.brzhang.gemma_ai.`data`.db

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.brzhang.gemma_ai.`data`.entity.UserProfile
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class UserProfileDao_Impl(
  __db: RoomDatabase,
) : UserProfileDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfUserProfile: EntityInsertAdapter<UserProfile>
  init {
    this.__db = __db
    this.__insertAdapterOfUserProfile = object : EntityInsertAdapter<UserProfile>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `user_profile` (`userId`,`name`,`age`,`gender`,`phone`,`bloodType`,`allergies`,`chronicDiseases`,`chronicConditions`,`currentMedications`,`emergencyContact`,`emergencyContacts`,`emergencyPhone`,`village`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: UserProfile) {
        statement.bindText(1, entity.userId)
        statement.bindText(2, entity.name)
        statement.bindLong(3, entity.age.toLong())
        statement.bindText(4, entity.gender)
        statement.bindText(5, entity.phone)
        statement.bindText(6, entity.bloodType)
        statement.bindText(7, entity.allergies)
        statement.bindText(8, entity.chronicDiseases)
        statement.bindText(9, entity.chronicConditions)
        statement.bindText(10, entity.currentMedications)
        statement.bindText(11, entity.emergencyContact)
        statement.bindText(12, entity.emergencyContacts)
        statement.bindText(13, entity.emergencyPhone)
        statement.bindText(14, entity.village)
        statement.bindLong(15, entity.createdAt)
        statement.bindLong(16, entity.updatedAt)
      }
    }
  }

  public override suspend fun upsert(profile: UserProfile): Unit = performSuspending(__db, false,
      true) { _connection ->
    __insertAdapterOfUserProfile.insert(_connection, profile)
  }

  public override suspend fun getProfile(userId: String): UserProfile? {
    val _sql: String = "SELECT * FROM user_profile WHERE userId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfAge: Int = getColumnIndexOrThrow(_stmt, "age")
        val _columnIndexOfGender: Int = getColumnIndexOrThrow(_stmt, "gender")
        val _columnIndexOfPhone: Int = getColumnIndexOrThrow(_stmt, "phone")
        val _columnIndexOfBloodType: Int = getColumnIndexOrThrow(_stmt, "bloodType")
        val _columnIndexOfAllergies: Int = getColumnIndexOrThrow(_stmt, "allergies")
        val _columnIndexOfChronicDiseases: Int = getColumnIndexOrThrow(_stmt, "chronicDiseases")
        val _columnIndexOfChronicConditions: Int = getColumnIndexOrThrow(_stmt, "chronicConditions")
        val _columnIndexOfCurrentMedications: Int = getColumnIndexOrThrow(_stmt,
            "currentMedications")
        val _columnIndexOfEmergencyContact: Int = getColumnIndexOrThrow(_stmt, "emergencyContact")
        val _columnIndexOfEmergencyContacts: Int = getColumnIndexOrThrow(_stmt, "emergencyContacts")
        val _columnIndexOfEmergencyPhone: Int = getColumnIndexOrThrow(_stmt, "emergencyPhone")
        val _columnIndexOfVillage: Int = getColumnIndexOrThrow(_stmt, "village")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: UserProfile?
        if (_stmt.step()) {
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpAge: Int
          _tmpAge = _stmt.getLong(_columnIndexOfAge).toInt()
          val _tmpGender: String
          _tmpGender = _stmt.getText(_columnIndexOfGender)
          val _tmpPhone: String
          _tmpPhone = _stmt.getText(_columnIndexOfPhone)
          val _tmpBloodType: String
          _tmpBloodType = _stmt.getText(_columnIndexOfBloodType)
          val _tmpAllergies: String
          _tmpAllergies = _stmt.getText(_columnIndexOfAllergies)
          val _tmpChronicDiseases: String
          _tmpChronicDiseases = _stmt.getText(_columnIndexOfChronicDiseases)
          val _tmpChronicConditions: String
          _tmpChronicConditions = _stmt.getText(_columnIndexOfChronicConditions)
          val _tmpCurrentMedications: String
          _tmpCurrentMedications = _stmt.getText(_columnIndexOfCurrentMedications)
          val _tmpEmergencyContact: String
          _tmpEmergencyContact = _stmt.getText(_columnIndexOfEmergencyContact)
          val _tmpEmergencyContacts: String
          _tmpEmergencyContacts = _stmt.getText(_columnIndexOfEmergencyContacts)
          val _tmpEmergencyPhone: String
          _tmpEmergencyPhone = _stmt.getText(_columnIndexOfEmergencyPhone)
          val _tmpVillage: String
          _tmpVillage = _stmt.getText(_columnIndexOfVillage)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _result =
              UserProfile(_tmpUserId,_tmpName,_tmpAge,_tmpGender,_tmpPhone,_tmpBloodType,_tmpAllergies,_tmpChronicDiseases,_tmpChronicConditions,_tmpCurrentMedications,_tmpEmergencyContact,_tmpEmergencyContacts,_tmpEmergencyPhone,_tmpVillage,_tmpCreatedAt,_tmpUpdatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeProfile(userId: String): Flow<UserProfile?> {
    val _sql: String = "SELECT * FROM user_profile WHERE userId = ?"
    return createFlow(__db, false, arrayOf("user_profile")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfAge: Int = getColumnIndexOrThrow(_stmt, "age")
        val _columnIndexOfGender: Int = getColumnIndexOrThrow(_stmt, "gender")
        val _columnIndexOfPhone: Int = getColumnIndexOrThrow(_stmt, "phone")
        val _columnIndexOfBloodType: Int = getColumnIndexOrThrow(_stmt, "bloodType")
        val _columnIndexOfAllergies: Int = getColumnIndexOrThrow(_stmt, "allergies")
        val _columnIndexOfChronicDiseases: Int = getColumnIndexOrThrow(_stmt, "chronicDiseases")
        val _columnIndexOfChronicConditions: Int = getColumnIndexOrThrow(_stmt, "chronicConditions")
        val _columnIndexOfCurrentMedications: Int = getColumnIndexOrThrow(_stmt,
            "currentMedications")
        val _columnIndexOfEmergencyContact: Int = getColumnIndexOrThrow(_stmt, "emergencyContact")
        val _columnIndexOfEmergencyContacts: Int = getColumnIndexOrThrow(_stmt, "emergencyContacts")
        val _columnIndexOfEmergencyPhone: Int = getColumnIndexOrThrow(_stmt, "emergencyPhone")
        val _columnIndexOfVillage: Int = getColumnIndexOrThrow(_stmt, "village")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: UserProfile?
        if (_stmt.step()) {
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpAge: Int
          _tmpAge = _stmt.getLong(_columnIndexOfAge).toInt()
          val _tmpGender: String
          _tmpGender = _stmt.getText(_columnIndexOfGender)
          val _tmpPhone: String
          _tmpPhone = _stmt.getText(_columnIndexOfPhone)
          val _tmpBloodType: String
          _tmpBloodType = _stmt.getText(_columnIndexOfBloodType)
          val _tmpAllergies: String
          _tmpAllergies = _stmt.getText(_columnIndexOfAllergies)
          val _tmpChronicDiseases: String
          _tmpChronicDiseases = _stmt.getText(_columnIndexOfChronicDiseases)
          val _tmpChronicConditions: String
          _tmpChronicConditions = _stmt.getText(_columnIndexOfChronicConditions)
          val _tmpCurrentMedications: String
          _tmpCurrentMedications = _stmt.getText(_columnIndexOfCurrentMedications)
          val _tmpEmergencyContact: String
          _tmpEmergencyContact = _stmt.getText(_columnIndexOfEmergencyContact)
          val _tmpEmergencyContacts: String
          _tmpEmergencyContacts = _stmt.getText(_columnIndexOfEmergencyContacts)
          val _tmpEmergencyPhone: String
          _tmpEmergencyPhone = _stmt.getText(_columnIndexOfEmergencyPhone)
          val _tmpVillage: String
          _tmpVillage = _stmt.getText(_columnIndexOfVillage)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _result =
              UserProfile(_tmpUserId,_tmpName,_tmpAge,_tmpGender,_tmpPhone,_tmpBloodType,_tmpAllergies,_tmpChronicDiseases,_tmpChronicConditions,_tmpCurrentMedications,_tmpEmergencyContact,_tmpEmergencyContacts,_tmpEmergencyPhone,_tmpVillage,_tmpCreatedAt,_tmpUpdatedAt)
        } else {
          _result = null
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
