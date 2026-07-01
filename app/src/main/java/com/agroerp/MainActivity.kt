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
    var selectedTab by remember { mutableStateOf(0) }
    var showSaleDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { TopAppBar(title = { Text("AGRO ERP : IA & PRÉVISIONS") }) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = { Icon(Icons.Default.Analytics, null) }, label = { Text("IA/Finance") })
                NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = { Icon(Icons.Default.Inventory, null) }, label = { Text("Stock") })
                NavigationBarItem(selected = selectedTab == 2, onClick = { selectedTab = 2 }, icon = { Icon(Icons.Default.Share, null) }, label = { Text("Bilan") })
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showSaleDialog = true }, containerColor = Color(0xFF2E7D32)) {
                Icon(Icons.Default.Add, null, tint = Color.White)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when(selectedTab) {
                0 -> PredictionScreen(dao)
                1 -> StockScreen(dao)
                2 -> ReportScreen(dao)
            }
        }

        if (showSaleDialog) {
            var amt by remember { mutableStateOf("") }
            AlertDialog(onDismissRequest = { showSaleDialog = false }, confirmButton = {
                Button(onClick = { scope.launch { dao.insertSale(Sale(productName = "Vente", amount = amt.toDoubleOrNull() ?: 0.0)); showSaleDialog = false } }) { Text("Valider") }
            }, title = { Text("Vente Rapide") }, text = { TextField(amt, { amt = it }, label = { Text("Montant FCFA") }) })
        }
    }
}

@Composable
fun PredictionScreen(dao: AgroDao) {
    val salesHistory by dao.getSalesHistory().collectAsState(initial = emptyList())
    val cash by dao.getTotalCash().collectAsState(initial = 0.0)
    
    // --- LOGIQUE IA DE PRÉVISION ---
    val forecast = remember(salesHistory) {
        if (salesHistory.size < 3) 0.0 else {
            // Algorithme de tendance simple (moyenne de croissance)
            val lastSales = salesHistory.takeLast(5)
            val average = lastSales.map { it.amount }.average()
            average * 1.15 // On prédit une croissance de 15% basée sur l'activité
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Intelligence Artificielle", fontWeight = FontWeight.Bold, color = Color.Blue)
        
        // CARTE PRÉVISIONNELLE
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Psychology, null, tint = Color.Blue)
                    Spacer(Modifier.width(8.dp))
                    Text("Prévision Ventes (7 prochains jours)", color = Color.Blue, fontWeight = FontWeight.Bold)
                }
                Text("${String.format("%.0f", forecast * 7)} FCFA", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Blue)
                Text("Basé sur vos tendances de ventes actuelles", fontSize = 12.sp)
            }
        }

        Divider(Modifier.padding(vertical = 16.dp))
        
        Text("État Actuel", fontWeight = FontWeight.Bold)
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total Recettes", color = Color(0xFF2E7D32))
                Text("${cash ?: 0.0} FCFA", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
            }
        }
    }
}

@Composable
fun StockScreen(dao: AgroDao) {
    val stock by dao.getAllStock().collectAsState(initial = emptyList())
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Gestion des Stocks", fontWeight = FontWeight.Bold)
        LazyColumn {
            items(stock) { item ->
                ListItem(
                    headlineContent = { Text(item.name) },
                    trailingContent = { 
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${item.quantity}", fontWeight = FontWeight.Bold)
                            // IA : Calcul de rupture
                            if(item.quantity < 10) Text("Rupture proche !", color = Color.Red, fontSize = 10.sp)
                        }
                    }
                )
                Divider()
            }
        }
    }
}

@Composable
fun ReportScreen(dao: AgroDao) {
    val context = LocalContext.current
    val cash by dao.getTotalCash().collectAsState(initial = 0.0)
    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = {
            val txt = "Bilan AGRO ERP : Total Cash actuel ${cash} FCFA. Généré par IA."
            val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, txt) }
            context.startActivity(Intent.createChooser(intent, "Partager"))
        }, modifier = Modifier.fillMaxWidth()) { Text("Partager Rapport IA WhatsApp") }
    }
}