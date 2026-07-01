package com.agroerp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val db = AppDatabase.getDatabase(context)
            val saleDao = db.saleDao()
            
            MaterialTheme {
                MainApp(saleDao)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(saleDao: SaleDao) {
    val scope = rememberCoroutineScope()
    
    // Lecture des données réelles depuis la base de données
    val totalCash by saleDao.getTotalCashSales().collectAsState(initial = 0.0)
    val totalDebt by saleDao.getTotalCredits().collectAsState(initial = 0.0)
    
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("AGRO ERP - VRAI SUIVI") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = Color(0xFF2E7D32)) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            // AFFICHAGE DU CHIFFRE D'AFFAIRES RÉEL
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Chiffre d'Affaires (Espèces)", color = Color(0xFF2E7D32))
                    Text("${totalCash ?: 0.0} FCFA", fontSize = 28.sp)
                }
            }

            // AFFICHAGE DES CRÉDITS RÉELS
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFDECEA))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Crédits Clients", color = Color.Red)
                    Text("${totalDebt ?: 0.0} FCFA", fontSize = 28.sp, color = Color.Red)
                }
            }
            
            Text("\nAppuyez sur le + pour enregistrer une vente réelle.")
        }

        // FORMULAIRE DE VENTE
        if (showDialog) {
            var amountText by remember { mutableStateOf("") }
            var isCredit by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Enregistrer une vente") },
                text = {
                    Column {
                        TextField(value = amountText, onValueChange = { amountText = it }, label = { Text("Montant en FCFA") })
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Checkbox(checked = isCredit, onCheckedChange = { isCredit = it })
                            Text("Vente à Crédit ?")
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        scope.launch {
                            saleDao.insertSale(Sale(productName = "Vente", amount = amount, isCredit = isCredit))
                            showDialog = false
                        }
                    }) { Text("Valider") }
                }
            )
        }
    }
}