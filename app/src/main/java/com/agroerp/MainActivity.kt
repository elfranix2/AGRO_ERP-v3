package com.agroerp

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Mic
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
            val saleDao = db.saleDao()
            MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF2E7D32))) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainApp(saleDao)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(saleDao: SaleDao) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val totalCash by saleDao.getTotalCashSales().collectAsState(initial = 0.0)
    val totalDebt by saleDao.getTotalCredits().collectAsState(initial = 0.0)
    var showDialog by remember { mutableStateOf(false) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        val spokenText = data?.get(0) ?: ""
        val numberInText = spokenText.filter { it.isDigit() }.toDoubleOrNull()
        if (numberInText != null) {
            scope.launch {
                val isCredit = spokenText.lowercase().contains("crédit") || spokenText.lowercase().contains("dette")
                saleDao.insertSale(Sale(productName = "Voix: $spokenText", amount = numberInText, isCredit = isCredit))
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("AGRO ERP SMART") }) },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
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
                ) { Icon(Icons.Default.Mic, contentDescription = null) }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                FloatingActionButton(onClick = { showDialog = true }, containerColor = Color(0xFF2E7D32)) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Chiffre d'Affaires (Espèces)", color = Color(0xFF2E7D32))
                    Text("${totalCash ?: 0.0} FCFA", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
            }
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFDECEA))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Crédits (Dettes)", color = Color.Red)
                    Text("${totalDebt ?: 0.0} FCFA", fontSize = 28.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
            Text("\n🎙️ Appuyez sur le micro bleu et dites :\n'Vendu deux mille' ou 'Crédit cinq cents'")
        }

        if (showDialog) {
            var amountText by remember { mutableStateOf("") }
            var isCredit by remember { mutableStateOf(false) }
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Nouvelle Vente") },
                text = {
                    Column {
                        TextField(value = amountText, onValueChange = { amountText = it }, label = { Text("Montant FCFA") })
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isCredit, onCheckedChange = { isCredit = it })
                            Text("Vente à Crédit ?")
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        scope.launch {
                            saleDao.insertSale(Sale(productName = "Manuel", amount = amount, isCredit = isCredit))
                            showDialog = false
                        }
                    }) { Text("Valider") }
                }
            )
        }
    }
}