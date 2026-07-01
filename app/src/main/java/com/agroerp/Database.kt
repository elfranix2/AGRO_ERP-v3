package com.agroerp

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// 1. Définition d'une Vente
@Entity(tableName = "sales_table")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productName: String,
    val amount: Double,
    val customerName: String = "",
    val isCredit: Boolean = false,
    val date: Long = System.currentTimeMillis()
)

// 2. Les ordres pour la base de données (DAO)
@Dao
interface SaleDao {
    @Insert
    suspend fun insertSale(sale: Sale)

    @Query("SELECT * FROM sales_table ORDER BY date DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Query("SELECT SUM(amount) FROM sales_table WHERE isCredit = 0")
    fun getTotalCashSales(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM sales_table WHERE isCredit = 1")
    fun getTotalCredits(): Flow<Double?>
}

// 3. La base de données elle-même
@Database(entities = [Sale::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun saleDao(): SaleDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, "agro_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}