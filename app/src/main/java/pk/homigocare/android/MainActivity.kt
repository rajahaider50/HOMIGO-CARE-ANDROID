package pk.homigocare.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import pk.homigocare.android.auth.AuthRepository
import pk.homigocare.android.auth.AuthScreen
import pk.homigocare.android.auth.EmailNotVerifiedException
import pk.homigocare.android.home.HomeScreen

private val PrimaryTeal = Color(0xFF1B6B63)
private val Orange = Color(0xFFF5823A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val auth = AuthRepository(FirebaseAuth.getInstance())
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = PrimaryTeal, secondary = Orange)) {
                var currentUser by remember { mutableStateOf(auth.currentUser()?.takeIf { it.isEmailVerified }) }
                var pendingVerification by remember { mutableStateOf(auth.currentUser()?.takeIf { !it.isEmailVerified }) }
                var isSignUp by remember { mutableStateOf(false) }
                var name by remember { mutableStateOf("") }
                var email by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }
                var loading by remember { mutableStateOf(false) }
                var message by remember { mutableStateOf<String?>(null) }

                if (currentUser != null) {
                    HomeScreen(name = currentUser?.displayName.orEmpty(), onSignOut = {
                        auth.signOut(); currentUser = null; pendingVerification = null
                        message = null; password = ""; isSignUp = false
                    })
                } else if (pendingVerification != null) {
                    VerificationScreen(
                        account = pendingVerification!!,
                        loading = loading,
                        message = message,
                        onResend = {
                            loading = true; message = null
                            auth.resendVerification { result ->
                                loading = false
                                message = result.fold({ "Verification email sent. Check your inbox and spam folder." }, { it.localizedMessage ?: "Could not send verification email." })
                            }
                        },
                        onRefresh = {
                            loading = true; message = null
                            auth.refreshVerification { result ->
                                loading = false
                                result.fold({ currentUser = it; pendingVerification = null }, { message = it.localizedMessage })
                            }
                        },
                        onSignOut = { auth.signOut(); pendingVerification = null; message = null; isSignUp = false },
                    )
                } else {
                    AuthScreen(
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
                            if (validation != null) { message = validation; return@AuthScreen }
                            loading = true; message = null
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
    androidx.compose.material3.Surface(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
        androidx.compose.foundation.layout.Column(
            modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        ) {
            androidx.compose.material3.Text("Check your email", style = MaterialTheme.typography.headlineMedium, color = PrimaryTeal)
            androidx.compose.foundation.layout.Spacer(androidx.compose.ui.Modifier.height(12.dp))
            androidx.compose.material3.Text("We sent a verification link to ${account.email.orEmpty()}. Verify your email before continuing.")
            if (message != null) {
                androidx.compose.foundation.layout.Spacer(androidx.compose.ui.Modifier.height(12.dp))
                androidx.compose.material3.Text(message, color = MaterialTheme.colorScheme.primary)
            }
            androidx.compose.foundation.layout.Spacer(androidx.compose.ui.Modifier.height(20.dp))
            androidx.compose.material3.Button(onClick = onRefresh, enabled = !loading, modifier = androidx.compose.ui.Modifier.fillMaxWidth()) { androidx.compose.material3.Text("I've verified my email") }
            androidx.compose.material3.TextButton(onClick = onResend, enabled = !loading, modifier = androidx.compose.ui.Modifier.fillMaxWidth()) { androidx.compose.material3.Text("Resend verification email") }
            androidx.compose.material3.TextButton(onClick = onSignOut, enabled = !loading, modifier = androidx.compose.ui.Modifier.fillMaxWidth()) { androidx.compose.material3.Text("Use a different account") }
        }
    }
}
