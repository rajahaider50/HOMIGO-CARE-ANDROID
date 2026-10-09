package pk.homigocare.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import pk.homigocare.android.auth.AuthRepository
import pk.homigocare.android.auth.AuthScreen
import pk.homigocare.android.auth.EmailNotVerifiedException
import pk.homigocare.android.designsystem.HomiGoSplashScreen
import pk.homigocare.android.designsystem.icons.HgIcon
import pk.homigocare.android.designsystem.icons.HgIconImage
import pk.homigocare.android.designsystem.theme.HgColor
import pk.homigocare.android.designsystem.theme.HgRadius
import pk.homigocare.android.designsystem.theme.HomiGoTheme
import pk.homigocare.android.home.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val auth = AuthRepository(FirebaseAuth.getInstance())
        setContent {
            HomiGoTheme {
                var showSplash by remember { mutableStateOf(true) }
                LaunchedEffect(Unit) {
                    delay(1200)
                    showSplash = false
                }
                var currentUser by remember { mutableStateOf(auth.currentUser()?.takeIf { it.isEmailVerified }) }
                var pendingVerification by remember { mutableStateOf(auth.currentUser()?.takeIf { !it.isEmailVerified }) }
                var isSignUp by remember { mutableStateOf(false) }
                var name by remember { mutableStateOf("") }
                var email by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }
                var loading by remember { mutableStateOf(false) }
                var message by remember { mutableStateOf<String?>(null) }

                when {
                    showSplash -> HomiGoSplashScreen()
                    currentUser != null -> HomeScreen(
                        name = currentUser?.displayName.orEmpty(),
                        onSignOut = {
                            auth.signOut()
                            currentUser = null
                            pendingVerification = null
                            message = null
                            password = ""
                            isSignUp = false
                        },
                        onEmergencyCall = {
                            runCatching { startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:1122"))) }
                        },
                    )
                    pendingVerification != null -> VerificationScreen(
                        account = pendingVerification!!,
                        loading = loading,
                        message = message,
                        onResend = {
                            loading = true
                            message = null
                            auth.resendVerification { result ->
                                loading = false
                                message = result.fold(
                                    { "Verification email sent. Check your inbox and spam folder." },
                                    { it.localizedMessage ?: "Could not send verification email." },
                                )
                            }
                        },
                        onRefresh = {
                            loading = true
                            message = null
                            auth.refreshVerification { result ->
                                loading = false
                                result.fold(
                                    { currentUser = it; pendingVerification = null },
                                    { message = it.localizedMessage ?: "Email is not verified yet. Open the email link and try again." },
                                )
                            }
                        },
                        onSignOut = {
                            auth.signOut()
                            pendingVerification = null
                            message = null
                            isSignUp = false
                        },
                    )
                    else -> AuthScreen(
                        isSignUp = isSignUp,
                        name = name,
                        email = email,
                        password = password,
                        loading = loading,
                        message = message,
                        onNameChange = { name = it; message = null },
                        onEmailChange = { email = it; message = null },
                        onPasswordChange = { password = it; message = null },
                        onModeChange = { isSignUp = it; message = null },
                        onSubmit = {
                            val validation = when {
                                isSignUp && name.trim().length < 2 -> "Enter your full name."
                                email.trim().isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "Enter a valid email address."
                                password.length < 8 -> "Use a password with at least 8 characters."
                                else -> null
                            }
                            if (validation != null) {
                                message = validation
                                return@AuthScreen
                            }
                            loading = true
                            message = null
                            val callback: (Result<FirebaseUser>) -> Unit = { result ->
                                loading = false
                                result.fold(
                                    onSuccess = { user ->
                                        password = ""
                                        if (user.isEmailVerified) currentUser = user else pendingVerification = user
                                    },
                                    onFailure = { error ->
                                        val session = auth.currentUser()
                                        when {
                                            error is EmailNotVerifiedException -> { pendingVerification = error.account; message = null }
                                            isSignUp && session?.isEmailVerified == false -> {
                                                pendingVerification = session
                                                message = error.localizedMessage ?: "Your account was created. Use the verification options to continue."
                                            }
                                            else -> message = error.localizedMessage ?: "Something went wrong. Please try again."
                                        }
                                    },
                                )
                            }
                            if (isSignUp) auth.createAccount(name, email, password, callback)
                            else auth.signIn(email, password, callback)
                        },
                    )
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun VerificationScreen(
    account: FirebaseUser,
    loading: Boolean,
    message: String?,
    onResend: () -> Unit,
    onRefresh: () -> Unit,
    onSignOut: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.homigo_lockup_horizontal),
                contentDescription = "HomiGo Care — Because Care Begins at Home",
                modifier = Modifier.fillMaxWidth(0.9f).height(68.dp),
                contentScale = ContentScale.Fit,
            )
            Spacer(Modifier.height(30.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HgIconImage(HgIcon.MESSAGE, modifier = Modifier.size(24.dp), contentDescription = null)
                Text("Check your email", style = MaterialTheme.typography.headlineMedium, color = HgColor.PrimaryDark)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "We sent a verification link to ${account.email.orEmpty()}. Verify your email before continuing.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (message != null) {
                Spacer(Modifier.height(12.dp))
                Text(message, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onRefresh,
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.large,
            ) {
                if (loading) CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                else Text("I've verified my email", style = MaterialTheme.typography.labelLarge)
            }
            TextButton(onClick = onResend, enabled = !loading, modifier = Modifier.fillMaxWidth().height(HgRadius.Button * 2.2f)) {
                Text("Resend verification email")
            }
            OutlinedButton(onClick = onSignOut, enabled = !loading, modifier = Modifier.fillMaxWidth().height(56.dp), shape = MaterialTheme.shapes.large) {
                Text("Use a different account")
            }
        }
    }
}
