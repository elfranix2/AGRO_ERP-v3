package com.agroerp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AgroTheme {
                MainNavigation()
            }
        }
    }
}

@Composable
fun MainNavigation() {
    var selectedSector by remember { mutableStateOf("") }
    
    if (selectedSector == "") {
        SectorSelectionScreen(onSectorSelected = { selectedSector = it })
    } else {
        ERPDashboard(selectedSector)
    }
}

@Composable
fun SectorSelectionScreen(onSectorSelected: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("AGRO ERP", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
        Text("Sélectionnez votre activité", fontSize = 16.sp, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        val sectors = listOf("Yaourt de Soja", "Boulangerie", "Jus de fruits", "Confitures", "Épices")
        sectors.forEach { sector ->
            Button(
                onClick = { onSectorSelected(sector) },
                modifier = Modifier.fillMaxWidth().height(60.dp).padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text(sector, fontSize = 18.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ERPDashboard(sector: String) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("ERP - $sector", fontWeight = FontWeight.Bold) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {}, containerColor = Color(0xFF2E7D32)) {
                Icon(Icons.Default.Mic, contentDescription = "Vocal", tint = Color.White)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
            
            // --- CARTE FINANCE ---
            StatCard(
                title = "Chiffre d'Affaires",
                value = "150 000 FCFA",
                subtitle = "Aujourd'hui",
                icon = Icons.Default.TrendingUp,
                color = Color(0xFFE8F5E9),
                contentColor = Color(0xFF2E7D32)
            )

            // --- CARTE CRÉDITS (DETTES) ---
            StatCard(
                title = "Dettes Clients",
                value = "45 000 FCFA",
                subtitle = "6 clients en attente",
                icon = Icons.Default.Warning,
                color = Color(0xFFFDECEA),
                contentColor = Color(0xFFD32F2F)
            )

            // --- IA RECOMMENDATION ---
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFE65100))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "CONSEIL IA : Le stock de Soja est bas. Prévoyez un achat demain pour éviter une rupture.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Text("Actions Rapides", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickActionButton("Vendre", Icons.Default.AddShoppingCart, Modifier.weight(1f))
                QuickActionButton("Production", Icons.Default.SettingsSuggest, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, subtitle: String, icon: ImageVector, color: Color, contentColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = contentColor, fontSize = 14.sp)
                Text(value, color = contentColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = contentColor.copy(alpha = 0.7f), fontSize = 12.sp)
            }
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(40.dp))
        }
    }
}

@Composable
fun QuickActionButton(text: String, icon: ImageVector, modifier: Modifier) {
    OutlinedButton(
        onClick = {},
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null)
            Text(text)
        }
    }
}

@Composable
fun AgroTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(primary = Color(0xFF2E7D32)),
        content = content
    )
}