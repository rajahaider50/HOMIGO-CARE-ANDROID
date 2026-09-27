package pk.homigocare.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val PrimaryTeal = Color(0xFF1B6B63)
private val Orange = Color(0xFFF5823A)

class MainActivity : ComponentActivity() { override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { HomigoTheme { WelcomeScreen() } } } }

@Composable fun HomigoTheme(content: @Composable () -> Unit) { MaterialTheme(colorScheme = lightColorScheme(primary=PrimaryTeal, secondary=Orange), content=content) }

@Composable fun WelcomeScreen() { Surface(Modifier.fillMaxSize(), color=Color.White) { Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment=Alignment.CenterHorizontally, verticalArrangement=Arrangement.Center) { Text("HomigoCare", style=MaterialTheme.typography.headlineLarge, color=PrimaryTeal); Spacer(Modifier.height(12.dp)); Text("Trusted care, delivered to your home.", color=Color(0xFF333333)); Spacer(Modifier.height(32.dp)); Button(onClick={}, colors=ButtonDefaults.buttonColors(containerColor=PrimaryTeal), modifier=Modifier.fillMaxWidth()) { Text("Get started") } } } }
