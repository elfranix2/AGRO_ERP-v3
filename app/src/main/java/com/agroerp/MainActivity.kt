package com.agroerp

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val db = AppDatabase.getDatabase(context)
            val dao = db.agroDao()
            MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF2E7D32))) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainApp(dao)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(dao: AgroDao) {
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) }
    var showSaleDialog by remember { mutableStateOf(false) }
    var showStockDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("AGRO ERP v3") }) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = { Icon(Icons.Default.Payments, null) }, label = { Text("Finance") })
                NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = { Icon(Icons.Default.Inventory, null) }, label = { Text("Stock") })
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { if(selectedTab == 0) showSaleDialog = true else showStockDialog = true }, containerColor = Color(0xFF2E7D32)) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (selectedTab == 0) FinanceScreen(dao) else StockScreen(dao)
        }

        if (showSaleDialog) {
            var amountText by remember { mutableStateOf("") }
            var isCredit by remember { mutableStateOf(false) }
            AlertDialog(onDismissRequest = { showSaleDialog = false }, confirmButton = {
                Button(onClick = {
                    scope.launch { dao.insertSale(Sale(productName = "Manuel", amount = amountText.toDoubleOrNull() ?: 0.0, isCredit = isCredit)) }
                    showSaleDialog = false
                }) { Text("Valider") }
            }, title = { Text("Nouvelle Vente") }, text = {
                Column {
                    TextField(value = amountText, onValueChange = { amountText = it }, label = { Text("Montant FCFA") })
                    Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = isCredit, onCheckedChange = { isCredit = it }); Text("Crédit ?") }
                }
            })
        }

        if (showStockDialog) {
            var name by remember { mutableStateOf("") }
            var qty by remember { mutableStateOf("") }
            AlertDialog(onDismissRequest = { showStockDialog = false }, confirmButton = {
                Button(onClick = {
                    scope.launch { dao.updateStock(StockItem(name = name, quantity = qty.toDoubleOrNull() ?: 0.0, unit = "unités")) }
                    showStockDialog = false
                }) { Text("Ajouter") }
            }, title = { Text("Ajouter au Stock") }, text = {
                Column {
                    TextField(value = name, onValueChange = { name = it }, label = { Text("Nom (ex: Soja)") })
                    TextField(value = qty, onValueChange = { qty = it }, label = { Text("Quantité") })
                }
            })
        }
    }
}

@Composable
fun FinanceScreen(dao: AgroDao) {
    val cash by dao.getTotalCash().collectAsState(initial = 0.0)
    val debt by dao.getTotalCredits().collectAsState(initial = 0.0)

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Tableau de Bord Financier", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
            Column(modifier = Modifier.padding(16.dp)) { Text("Recettes (Cash)"); Text("${cash ?: 0.0} FCFA", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32)) }
        }
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFDECEA))) {
            Column(modifier = Modifier.padding(16.dp)) { Text("Dettes Clients"); Text("${debt ?: 0.0} FCFA", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Red) }
        }
    }
}

@Composable
fun StockScreen(dao: AgroDao) {
    val stockList by dao.getAllStock().collectAsState(initial = emptyList())
    val lowStock by dao.getLowStock().collectAsState(initial = emptyList())

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Gestion du Stock", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        
        if (lowStock.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE65100))
                    Spacer(Modifier.width(8.dp))
                    Text("IA : Alerte ! Stock de ${lowStock.joinToString { it.name }} bas !", color = Color(0xFFE65100))
                }
            }
        }

        LazyColumn {
            items(stockList) { item ->
                ListItem(
                    headlineContent = { Text(item.name) },
                    trailingContent = { Text("${item.quantity} ${item.unit}", fontWeight = FontWeight.Bold, color = if(item.quantity < 5) Color.Red else Color.Black) },
                    leadingContent = { Icon(Icons.Default.Inventory2, null) }
                )
                Divider() // ICI LE CHANGEMENT : Divider au lieu de HorizontalDivider
            }
        }
    }
}