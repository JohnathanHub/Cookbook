package com.example.Cookbook.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ItemDao {

    @Insert
    fun insert(item: ItemEntity)

    @Update
    fun update(item: ItemEntity)

    @Query("SELECT * FROM items ORDER BY id DESC")
    fun getAll(): List<ItemEntity>

    @Query("SELECT * FROM items WHERE id = :id")
    fun getById(id: Int): ItemEntity?

    @Query("DELETE FROM items")
    fun deleteAll()

    @Query("DELETE FROM items WHERE id = :id")
    fun deleteById(id: Int): Int
}