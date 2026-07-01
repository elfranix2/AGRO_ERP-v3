package com.agroerp

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- TABLE DES VENTES ---
@Entity(tableName = "sales_table")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productName: String,
    val amount: Double,
    val customerName: String = "",
    val isCredit: Boolean = false,
    val date: Long = System.currentTimeMillis()
)

// --- TABLE DU STOCK ---
@Entity(tableName = "stock_table")
data class StockItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val quantity: Double,
    val unit: String // kg, litres, pots, etc.
)

@Dao
interface AgroDao {
    // Ventes
    @Insert suspend fun insertSale(sale: Sale)
    @Query("SELECT SUM(amount) FROM sales_table WHERE isCredit = 0") fun getTotalCash(): Flow<Double?>
    @Query("SELECT SUM(amount) FROM sales_table WHERE isCredit = 1") fun getTotalCredits(): Flow<Double?>

    // Stock
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun updateStock(item: StockItem)
    @Query("SELECT * FROM stock_table") fun getAllStock(): Flow<List<StockItem>>
    @Query("SELECT * FROM stock_table WHERE quantity < 5") fun getLowStock(): Flow<List<StockItem>>
}

@Database(entities = [Sale::class, StockItem::class], version = 2) // Version passée à 2
abstract class AppDatabase : RoomDatabase() {
    abstract fun agroDao(): AgroDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, "agro_database"
                )
                .fallbackToDestructiveMigration() // Évite les crashs lors des mises à jour
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}