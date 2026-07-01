package com.agroerp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF2E7D32))) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var sector by remember { mutableStateOf("") }
                    if (sector == "") {
                        SectorScreen { sector = it }
                    } else {
                        DashboardScreen(sector)
                    }
                }
            }
        }
    }
}

@Composable
fun SectorScreen(onSelect: (String) -> Unit) {
    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("AGRO ERP", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
        Text("Choisissez votre activité")
        Spacer(modifier = Modifier.height(30.dp))
        listOf("Yaourt de Soja", "Boulangerie", "Jus de fruits").forEach {
            Button(onClick = { onSelect(it) }, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Text(it)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(sector: String) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(sector) }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Chiffre d'Affaires", color = Color(0xFF2E7D32))
                    Text("150 000 FCFA", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                }
            }
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFDECEA))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Dettes Clients", color = Color.Red)
                    Text("45 000 FCFA", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text(" NOUVELLE VENTE")
            }
        }
    }
}