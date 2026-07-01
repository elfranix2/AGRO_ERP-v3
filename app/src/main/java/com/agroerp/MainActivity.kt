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
                Surface(modifier = Modifier.fillMaxSize()) { MainApp(dao) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(dao: AgroDao) {
    var selectedTab by remember { mutableStateOf(0) }
    var showSaleDialog by remember { mutableStateOf(false) }
    var showProdDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // IA VOCALE
    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val text = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
        val amount = text.filter { it.isDigit() }.toDoubleOrNull()
        if (amount != null) {
            scope.launch { dao.insertSale(Sale(productName = "Vocal: $text", amount = amount, isCredit = text.contains("crédit"))) }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("MAMY SOJA - ERP SMART") }) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = { Icon(Icons.Default.Analytics, null) }, label = { Text("Finance") })
                NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = { Icon(Icons.Default.Factory, null) }, label = { Text("Production") })
                NavigationBarItem(selected = selectedTab == 2, onClick = { selectedTab = 2 }, icon = { Icon(Icons.Default.Share, null) }, label = { Text("Bilan") })
            }
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                SmallFloatingActionButton(onClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply { 
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    }
                    speechLauncher.launch(intent)
                }, containerColor = Color.Blue) { Icon(Icons.Default.Mic, null, tint = Color.White) }
                Spacer(Modifier.height(8.dp))
                FloatingActionButton(onClick = { if(selectedTab == 0) showSaleDialog = true else showProdDialog = true }, containerColor = Color(0xFF2E7D32)) {
                    Icon(Icons.Default.Add, null, tint = Color.White)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when(selectedTab) {
                0 -> FinanceWithPrediction(dao)
                1 -> StockAndProduction(dao)
                2 -> ReportAndShare(dao)
            }
        }

        if (showSaleDialog) {
            var amt by remember { mutableStateOf("") }; var isCr by remember { mutableStateOf(false) }
            AlertDialog(onDismissRequest = { showSaleDialog = false }, confirmButton = {
                Button(onClick = { scope.launch { dao.insertSale(Sale(productName = "Vente", amount = amt.toDoubleOrNull() ?: 0.0, isCredit = isCr)); showSaleDialog = false } }) { Text("Valider") }
            }, title = { Text("Vente") }, text = { Column { TextField(amt, { amt = it }, label = { Text("Montant") }); Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(isCr, { isCr = it }); Text("Crédit") } } })
        }

        if (showProdDialog) {
            var n by remember { mutableStateOf("") }; var q by remember { mutableStateOf("") }
            AlertDialog(onDismissRequest = { showProdDialog = false }, confirmButton = {
                Button(onClick = { scope.launch { 
                    val prodQty = q.toDoubleOrNull() ?: 0.0
                    // Transformation : On ajoute au stock de produit fini
                    dao.updateStock(StockItem(name = n, quantity = prodQty, isFinishedProduct = true))
                    showProdDialog = false 
                } }) { Text("Produire") }
            }, title = { Text("Nouvelle Transformation") }, text = { Column { TextField(n, { n = it }, label = { Text("Nom Produit (ex: Yaourt)") }); TextField(q, { q = it }, label = { Text("Quantité") }) } })
        }
    }
}

@Composable
fun FinanceWithPrediction(dao: AgroDao) {
    val cash by dao.getTotalCash().collectAsState(initial = 0.0)
    val debt by dao.getTotalCredits().collectAsState(initial = 0.0)
    val history by dao.getSalesHistory().collectAsState(initial = emptyList())
    val prediction = if(history.size >= 3) (history.map { it.amount }.average() * 7) else 0.0

    Column(modifier = Modifier.padding(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("PRÉVISION IA (7 JOURS)", color = Color.Blue, fontWeight = FontWeight.Bold)
                Text("${String.format("%.0f", prediction)} FCFA", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Blue)
            }
        }
        Spacer(Modifier.height(10.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
            Column(modifier = Modifier.padding(16.dp)) { Text("Recettes Cash"); Text("${cash ?: 0.0} FCFA", fontSize = 24.sp, fontWeight = FontWeight.Bold) }
        }
        Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFDECEA))) {
            Column(modifier = Modifier.padding(16.dp)) { Text("Dettes Clients", color = Color.Red); Text("${debt ?: 0.0} FCFA", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Red) }
        }
    }
}

@Composable
fun StockAndProduction(dao: AgroDao) {
    val stock by dao.getAllStock().collectAsState(initial = emptyList())
    val low by dao.getLowStock().collectAsState(initial = emptyList())
    Column(modifier = Modifier.padding(16.dp)) {
        if (low.isNotEmpty()) Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
            Text("⚠️ ALERTE IA STOCK : ${low.joinToString { it.name }}", modifier = Modifier.padding(16.dp), color = Color(0xFFE65100))
        }
        LazyColumn {
            items(stock) { item ->
                ListItem(headlineContent = { Text(item.name) }, trailingContent = { Text("${item.quantity}") }, supportingContent = { Text(if(item.isFinishedProduct) "Produit Fini" else "Matière") })
                Divider()
            }
        }
    }
}

@Composable
fun ReportAndShare(dao: AgroDao) {
    val context = LocalContext.current
    val cash by dao.getTotalCash().collectAsState(initial = 0.0)
    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = {
            val txt = "BILAN AGRO ERP\nTotal: ${cash ?: 0.0} FCFA"
            val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, txt) }
            context.startActivity(intent)
        }, modifier = Modifier.fillMaxWidth().height(60.dp)) { Icon(Icons.Default.Share, null); Text(" PARTAGER WHATSAPP") }
    }
}