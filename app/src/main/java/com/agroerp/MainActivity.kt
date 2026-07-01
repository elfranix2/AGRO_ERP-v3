package com.agroerp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Dashboard()
                }
            }
        }
    }
}

@Composable
fun Dashboard() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("AGRO ERP v2", style = MaterialTheme.typography.headlineLarge, color = Color(0xFF2E7D32))
        Spacer(modifier = Modifier.height(20.dp))
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Ventes (FCFA)")
                Text("250 000", style = MaterialTheme.typography.displaySmall)
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Dettes Clients", color = Color.Red)
                Text("15 000 FCFA", style = MaterialTheme.typography.headlineMedium, color = Color.Red)
            }
        }
        
        Button(onClick = {}, modifier = Modifier.padding(top = 20.dp).fillMaxWidth()) {
            Text("NOUVELLE VENTE")
        }
    }
}
