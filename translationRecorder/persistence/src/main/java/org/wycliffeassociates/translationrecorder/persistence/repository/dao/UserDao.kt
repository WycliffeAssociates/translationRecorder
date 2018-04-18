package org.wycliffeassociates.translationrecorder.persistence.repository.dao

import android.arch.persistence.room.*
import org.wycliffeassociates.translationrecorder.persistence.entity.UserEntity

/**
 * Created by sarabiaj on 3/28/2018.
 */
@Dao
interface UserDao {

    @Insert
    fun insert(user: UserEntity)

    @Insert
    fun insertAll(users: List<UserEntity>)

    @Update
    fun update(user: UserEntity)

    @Delete
    fun delete(user: UserEntity)

    @Query("SELECT * FROM users")
    fun getUsers(): List<UserEntity>
}