package pk.homigocare.android.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(
    isSignUp: Boolean,
    name: String,
    email: String,
    password: String,
    loading: Boolean,
    message: String?,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onModeChange: (Boolean) -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text("HomiGo Care", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Trusted care, delivered to your home.", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(28.dp))
            Text(if (isSignUp) "Create your patient account" else "Sign in to continue", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(20.dp))
            if (isSignUp) {
                OutlinedTextField(value = name, onValueChange = onNameChange, label = { Text("Full name") }, singleLine = true, enabled = !loading, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
            }
            OutlinedTextField(value = email, onValueChange = onEmailChange, label = { Text("Email address") }, singleLine = true, enabled = !loading, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = password, onValueChange = onPasswordChange, label = { Text("Password (at least 8 characters)") }, singleLine = true, enabled = !loading, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            if (message != null) {
                Spacer(Modifier.height(10.dp))
                Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onSubmit,
                enabled = !loading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth().height(54.dp),
            ) {
                if (loading) CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.height(22.dp))
                else Text(if (isSignUp) "Create account" else "Sign in")
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { onModeChange(!isSignUp) }, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
                Text(if (isSignUp) "Already registered? Sign in" else "New to HomiGo Care? Create an account")
            }
        }
    }
}
