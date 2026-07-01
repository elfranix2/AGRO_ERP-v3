package com.agroerp

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// MATIÈRES PREMIÈRES ET PRODUITS FINIS
@Entity(tableName = "stock_table")
data class StockItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val quantity: Double,
    val isFinishedProduct: Boolean = false // false = Matière première, true = Produit fini
)

// RECETTE (Le lien entre les deux)
@Entity(tableName = "recipe_table")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val finalProductName: String,
    val ingredientName: String,
    val quantityNeeded: Double // ex: 0.5kg pour 1 unité
)

@Entity(tableName = "sales_table")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productName: String,
    val amount: Double,
    val isCredit: Boolean = false,
    val date: Long = System.currentTimeMillis()
)

@Dao
interface AgroDao {
    @Query("SELECT * FROM stock_table") fun getAllStock(): Flow<List<StockItem>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun updateStock(item: StockItem)
    @Query("SELECT * FROM stock_table WHERE name = :name LIMIT 1") suspend fun getStockByName(name: String): StockItem?

    @Insert suspend fun insertRecipe(recipe: Recipe)
    @Query("SELECT * FROM recipe_table WHERE finalProductName = :productName") suspend fun getIngredientsFor(productName: String): List<Recipe>

    @Insert suspend fun insertSale(sale: Sale)
    @Query("SELECT SUM(amount) FROM sales_table WHERE isCredit = 0") fun getTotalCash(): Flow<Double?>
    @Query("SELECT SUM(amount) FROM sales_table WHERE isCredit = 1") fun getTotalCredits(): Flow<Double?>
    @Query("SELECT COUNT(*) FROM sales_table") fun getSalesCount(): Flow<Int>
}

@Database(entities = [Sale::class, StockItem::class, Recipe::class], version = 3)
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