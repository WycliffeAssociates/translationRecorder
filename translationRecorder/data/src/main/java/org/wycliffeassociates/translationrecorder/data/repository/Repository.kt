package org.wycliffeassociates.translationrecorder.data.repository

/**
 * Created by sarabiaj on 4/24/2018.
 */
interface Repository<T> {
    fun getById(id: Int): T
    fun getAll(): List<T>
    fun insert(type: T): Long
    fun insertAll(types: List<T>): List<Long>
    fun update(type: T)
    fun delete(type: T)
}