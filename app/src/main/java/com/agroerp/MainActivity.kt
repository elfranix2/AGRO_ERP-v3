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
    var showStockDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // --- LOGIQUE IA VOCALE ---
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        val spokenText = data?.get(0) ?: ""
        val numberInText = spokenText.filter { it.isDigit() }.toDoubleOrNull()
        if (numberInText != null) {
            scope.launch {
                val isCredit = spokenText.lowercase().contains("crédit") || spokenText.lowercase().contains("dette")
                dao.insertSale(Sale(productName = "Voix: $spokenText", amount = numberInText, isCredit = isCredit))
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("AGRO ERP SMART v4") }) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = { Icon(Icons.Default.Payments, null) }, label = { Text("Finance") })
                NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = { Icon(Icons.Default.Inventory, null) }, label = { Text("Stock") })
                NavigationBarItem(selected = selectedTab == 2, onClick = { selectedTab = 2 }, icon = { Icon(Icons.Default.Description, null) }, label = { Text("Bilan") })
            }
        },
        floatingActionButton = {
            if (selectedTab != 2) {
                Column(horizontalAlignment = Alignment.End) {
                    // BOUTON MICRO IA
                    SmallFloatingActionButton(
                        onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                            }
                            speechLauncher.launch(intent)
                        },
                        containerColor = Color.Blue,
                        contentColor = Color.White
                    ) { Icon(Icons.Default.Mic, null) }
                    
                    Spacer(Modifier.height(8.dp))

                    // BOUTON AJOUT MANUEL
                    FloatingActionButton(onClick = { if(selectedTab == 0) showSaleDialog = true else showStockDialog = true }, containerColor = Color(0xFF2E7D32)) {
                        Icon(Icons.Default.Add, null, tint = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when(selectedTab) {
                0 -> FinanceScreen(dao)
                1 -> StockScreen(dao)
                2 -> ReportScreen(dao)
            }
        }

        // --- DIALOGUES ---
        if (showSaleDialog) {
            var amt by remember { mutableStateOf("") }; var isCr by remember { mutableStateOf(false) }
            AlertDialog(onDismissRequest = { showSaleDialog = false }, confirmButton = {
                Button(onClick = { scope.launch { dao.insertSale(Sale(productName = "Vente", amount = amt.toDoubleOrNull() ?: 0.0, isCredit = isCr)); showSaleDialog = false } }) { Text("Valider") }
            }, title = { Text("Nouvelle Vente") }, text = {
                Column { TextField(amt, { amt = it }, label = { Text("Montant FCFA") })
                    Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(isCr, { isCr = it }); Text("À Crédit ?") } }
            })
        }

        if (showStockDialog) {
            var n by remember { mutableStateOf("") }; var q by remember { mutableStateOf("") }
            AlertDialog(onDismissRequest = { showStockDialog = false }, confirmButton = {
                Button(onClick = { scope.launch { dao.updateStock(StockItem(name = n, quantity = q.toDoubleOrNull() ?: 0.0, unit = "unités")); showStockDialog = false } }) { Text("Ajouter") }
            }, title = { Text("Ajouter Stock") }, text = {
                Column { TextField(n, { n = it }, label = { Text("Nom") }); TextField(q, { q = it }, label = { Text("Quantité") }) }
            })
        }
    }
}

@Composable
fun FinanceScreen(dao: AgroDao) {
    val cash by dao.getTotalCash().collectAsState(initial = 0.0)
    val debt by dao.getTotalCredits().collectAsState(initial = 0.0)
    Column(modifier = Modifier.padding(16.dp)) {
        StatCard("Recettes Cash", "${cash ?: 0.0} FCFA", Color(0xFFE8F5E9), Color(0xFF2E7D32))
        StatCard("Dettes Clients", "${debt ?: 0.0} FCFA", Color(0xFFFDECEA), Color.Red)
        Text("\n🎙️ Astuce IA : Utilisez le micro bleu pour enregistrer une vente à la voix !", fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun StockScreen(dao: AgroDao) {
    val stock by dao.getAllStock().collectAsState(initial = emptyList())
    val low by dao.getLowStock().collectAsState(initial = emptyList())
    Column(modifier = Modifier.padding(16.dp)) {
        if (low.isNotEmpty()) Card(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
            Text("⚠️ IA : Stock critique pour : ${low.joinToString { it.name }}", modifier = Modifier.padding(16.dp), color = Color(0xFFE65100))
        }
        LazyColumn {
            items(stock) { item ->
                ListItem(headlineContent = { Text(item.name) }, trailingContent = { Text("${item.quantity}") })
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
    val count by dao.getSalesCount().collectAsState(initial = 0)

    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("BILAN GÉNÉRAL", fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Spacer(Modifier.height(20.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Nombre de ventes : $count")
                Text("Total Cash : ${cash ?: 0.0} FCFA")
                Text("Total Dettes : ${debt ?: 0.0} FCFA")
                Divider(Modifier.padding(vertical = 8.dp))
                Text("TOTAL GÉNÉRAL : ${(cash ?: 0.0) + (debt ?: 0.0)} FCFA", fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(30.dp))
        Button(
            onClick = {
                val reportText = "📊 *BILAN AGRO ERP*\n\n✅ Ventes Cash: ${cash ?: 0.0} FCFA\n❌ Dettes: ${debt ?: 0.0} FCFA\n📈 *Total: ${(cash ?: 0.0) + (debt ?: 0.0)} FCFA*\n\n_Généré par AGRO ERP Smart Mobile_"
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, reportText)
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Partager le Bilan via :"))
            },
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Icon(Icons.Default.Share, null)
            Spacer(Modifier.width(8.dp))
            Text("PARTAGER LE BILAN WHATSAPP")
        }
    }
}

@Composable
fun StatCard(t: String, v: String, c: Color, tc: Color) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = c)) {
        Column(modifier = Modifier.padding(16.dp)) { Text(t, color = tc); Text(v, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = tc) }
    }
}