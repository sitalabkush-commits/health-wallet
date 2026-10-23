package com.example.medwallet.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

/** Room database encrypted with SQLCipher. */
@Database(
    entities = [MedicalProfile::class, MedicalDocument::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun documentDao(): DocumentDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context.applicationContext).also { instance = it }
            }

        private fun build(ctx: Context): AppDatabase {
            System.loadLibrary("sqlcipher")
            val factory = SupportOpenHelperFactory(DbKeyProvider.passphrase(ctx))
            return Room.databaseBuilder(ctx, AppDatabase::class.java, "wallet.db")
                .openHelperFactory(factory)
                .build()
        }
    }
}
