package pk.homigocare.android.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pk.homigocare.android.R
import pk.homigocare.android.designsystem.icons.HgIcon
import pk.homigocare.android.designsystem.icons.HgIconImage
import pk.homigocare.android.designsystem.theme.HgColor
import pk.homigocare.android.designsystem.theme.HgRadius

@Composable
fun HomeScreen(name: String, onSignOut: () -> Unit, onEmergencyCall: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Image(
                    painter = painterResource(R.drawable.homigo_lockup_horizontal),
                    contentDescription = "HomiGo Care — Because Care Begins at Home",
                    modifier = Modifier.weight(1f).height(54.dp),
                    contentScale = ContentScale.Fit,
                )
                OutlinedButton(onClick = onSignOut, shape = MaterialTheme.shapes.large) {
                    HgIconImage(HgIcon.LOGOUT, modifier = Modifier.size(18.dp), contentDescription = null)
                    Spacer(Modifier.size(6.dp))
                    Text("Sign out")
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Hello, ${name.ifBlank { "there" }}", style = MaterialTheme.typography.titleMedium, color = HgColor.PrimaryDark)
                Text("Because Care Begins at Home", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(
                modifier = Modifier.fillMaxWidth().background(HgColor.Green50, MaterialTheme.shapes.small).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HgIconImage(HgIcon.SHIELD_CHECK, modifier = Modifier.size(20.dp), contentDescription = null, tint = HgColor.Success)
                Text("Your email address is verified", style = MaterialTheme.typography.bodyMedium, color = HgColor.Green700)
            }
            Text("Care services", style = MaterialTheme.typography.headlineMedium)
            ServiceCard(HgIcon.STETHOSCOPE, "Doctor appointments", "Browse verified doctors and request a visit")
            ServiceCard(HgIcon.HAND_HEART, "Home nursing", "Explore home-care services")
            ServiceCard(HgIcon.PASSPORT, "Health Passport", "Patient-controlled health information")
            EmergencyCard(onEmergencyCall)
            Text(
                "Bookings, health records, provider profiles and SOS reporting are not connected yet. Emergency call opens your phone dialer; no call is placed automatically.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ServiceCard(icon: HgIcon, title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, HgColor.Border, MaterialTheme.shapes.medium),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = HgColor.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(HgColor.Teal50, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                HgIconImage(icon, modifier = Modifier.size(24.dp), contentDescription = null)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Coming in a later app phase", style = MaterialTheme.typography.bodySmall, color = HgColor.Primary)
            }
        }
    }
}

@Composable
private fun EmergencyCard(onEmergencyCall: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HgIconImage(HgIcon.SIREN, modifier = Modifier.size(24.dp), contentDescription = null, tint = HgColor.Error)
                Text("Emergency", style = MaterialTheme.typography.titleMedium, color = HgColor.Red700)
            }
            Text("In a medical emergency, use this to open your dialer for Pakistan emergency service 1122. HomiGo SOS dispatch is not enabled yet.", style = MaterialTheme.typography.bodyMedium)
            Button(
                onClick = onEmergencyCall,
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(containerColor = HgColor.Error, contentColor = HgColor.White),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                HgIconImage(HgIcon.PHONE, modifier = Modifier.size(20.dp), contentDescription = null, tint = HgColor.White)
                Spacer(Modifier.size(8.dp))
                Text("Open dialer · 1122", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
