package com.example.android.codelabs.paging.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.android.codelabs.paging.model.Repo


@Database(entities = [Repo::class,RemoteKeys::class], version = 2, exportSchema = false)
abstract class RepoDatabase : RoomDatabase() {

    abstract fun reposDao() : RepoDao
    abstract fun remoteKeysDao() : RemoteKeysDao

    companion object{

        @Volatile
        private var INSTANCE : RepoDatabase? = null

        // Get instance of Database here....
        fun getInstance(mContext : Context) : RepoDatabase = INSTANCE ?: synchronized(this){ INSTANCE ?: buildDatabase(mContext).also { INSTANCE = it } }

        // Create Database Builder here...
        private fun buildDatabase(mContext: Context) =
                Room.databaseBuilder(mContext.applicationContext,RepoDatabase::class.java,"Github.db")
                        .fallbackToDestructiveMigration()
                        .build()
    }
}