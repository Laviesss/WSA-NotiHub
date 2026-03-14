package com.laviesss.wsanotihub.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [NotificationEntity::class], version = 1, exportSchema = false)
abstract class NotiHubDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: NotiHubDatabase? = null

        fun getDatabase(context: Context): NotiHubDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotiHubDatabase::class.java,
                    "notihub_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
