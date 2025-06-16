package com.example.Cookbook.repository

import com.example.Cookbook.data.ItemDao
import com.example.Cookbook.data.ItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ItemRepository(private val itemDao: ItemDao) {

    suspend fun insertItem(item: ItemEntity) {
        withContext(Dispatchers.IO) {
            itemDao.insert(item)
        }
    }

    suspend fun updateItem(item: ItemEntity) {
        withContext(Dispatchers.IO) {
            itemDao.update(item)
        }
    }

    suspend fun getAllItems(): List<ItemEntity> {
        return withContext(Dispatchers.IO) {
            itemDao.getAll()
        }
    }

    suspend fun getItemById(id: Int): ItemEntity? {
        return withContext(Dispatchers.IO) {
            itemDao.getById(id)
        }
    }

    suspend fun deleteAllItems() {
        withContext(Dispatchers.IO) {
            itemDao.deleteAll()
        }
    }

    suspend fun deleteItemById(id: Int): Int {
        return withContext(Dispatchers.IO) {
            itemDao.deleteById(id)
        }
    }
}