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
    var showProductionDialog by remember { mutableStateOf(false) }
    var showSaleDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("AGRO ERP SMART") }) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = { Icon(Icons.Default.Payments, null) }, label = { Text("Ventes") })
                NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = { Icon(Icons.Default.Factory, null) }, label = { Text("Production") })
                NavigationBarItem(selected = selectedTab == 2, onClick = { selectedTab = 2 }, icon = { Icon(Icons.Default.Description, null) }, label = { Text("Bilan") })
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { if(selectedTab == 0) showSaleDialog = true else showProductionDialog = true }, containerColor = Color(0xFF2E7D32)) {
                Icon(Icons.Default.Add, null, tint = Color.White)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when(selectedTab) {
                0 -> SalesScreen(dao)
                1 -> ProductionScreen(dao)
                2 -> ReportScreen(dao)
            }
        }

        // DIALOGUE PRODUCTION (Le cœur de la recette)
        if (showProductionDialog) {
            var prodName by remember { mutableStateOf("Yaourt Soja") }
            var qty by remember { mutableStateOf("") }
            AlertDialog(onDismissRequest = { showProductionDialog = false }, confirmButton = {
                Button(onClick = {
                    scope.launch {
                        val q = qty.toDoubleOrNull() ?: 0.0
                        // Logique ERP : On augmente le produit fini
                        val current = dao.getStockByName(prodName)
                        dao.updateStock(StockItem(id = current?.id ?: 0, name = prodName, quantity = (current?.quantity ?: 0.0) + q, isFinishedProduct = true))
                        showProductionDialog = false
                    }
                }) { Text("Lancer Production") }
            }, title = { Text("Nouvelle Production") }, text = {
                Column {
                    Text("Produit : $prodName")
                    TextField(qty, { qty = it }, label = { Text("Quantité à produire") })
                }
            })
        }
    }
}

@Composable
fun SalesScreen(dao: AgroDao) {
    val cash by dao.getTotalCash().collectAsState(initial = 0.0)
    val debt by dao.getTotalCredits().collectAsState(initial = 0.0)
    Column(modifier = Modifier.padding(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth().padding(8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
            Column(modifier = Modifier.padding(16.dp)) { Text("Cash: ${cash ?: 0.0} FCFA", fontSize = 24.sp, fontWeight = FontWeight.Bold) }
        }
        Card(modifier = Modifier.fillMaxWidth().padding(8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFDECEA))) {
            Column(modifier = Modifier.padding(16.dp)) { Text("Dettes: ${debt ?: 0.0} FCFA", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Red) }
        }
    }
}

@Composable
fun ProductionScreen(dao: AgroDao) {
    val stock by dao.getAllStock().collectAsState(initial = emptyList())
    Column(modifier = Modifier.padding(16.dp)) {
        Text("État des Stocks (Matières & Produits)", fontWeight = FontWeight.Bold)
        LazyColumn {
            items(stock) { item ->
                ListItem(
                    headlineContent = { Text(item.name) },
                    trailingContent = { Text("${item.quantity}") },
                    supportingContent = { Text(if(item.isFinishedProduct) "Produit Fini" else "Matière Première") }
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
    val debt by dao.getTotalCredits().collectAsState(initial = 0.0)
    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = {
            val txt = "Bilan: Cash ${cash} / Dettes ${debt}"
            val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, txt) }
            context.startActivity(Intent.createChooser(intent, "Partager"))
        }, modifier = Modifier.fillMaxWidth()) { Text("Partager Bilan WhatsApp") }
    }
}