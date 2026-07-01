package com.agroerp

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "sales_table")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productName: String,
    val amount: Double,
    val customerName: String = "",
    val isCredit: Boolean = false,
    val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "stock_table")
data class StockItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val quantity: Double,
    val unit: String
)

@Dao
interface AgroDao {
    @Insert suspend fun insertSale(sale: Sale)
    @Query("SELECT * FROM sales_table ORDER BY date DESC") fun getAllSales(): Flow<List<Sale>>
    @Query("SELECT SUM(amount) FROM sales_table WHERE isCredit = 0") fun getTotalCash(): Flow<Double?>
    @Query("SELECT SUM(amount) FROM sales_table WHERE isCredit = 1") fun getTotalCredits(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun updateStock(item: StockItem)
    @Query("SELECT * FROM stock_table") fun getAllStock(): Flow<List<StockItem>>
    @Query("SELECT * FROM stock_table WHERE quantity < 5") fun getLowStock(): Flow<List<StockItem>>
    
    // Pour le bilan
    @Query("SELECT COUNT(*) FROM sales_table") fun getSalesCount(): Flow<Int>
}

@Database(entities = [Sale::class, StockItem::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun agroDao(): AgroDao
    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "agro_database")
                    .fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}