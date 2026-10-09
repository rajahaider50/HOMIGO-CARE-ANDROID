package pk.homigocare.android.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

/** Firebase is the identity provider; profile/domain writes remain server-owned. */
class AuthRepository(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {
    fun currentUser(): FirebaseUser? = auth.currentUser

    fun signIn(email: String, password: String, onResult: (Result<FirebaseUser>) -> Unit) {
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                val user = task.result?.user
                if (!task.isSuccessful || user == null) {
                    onResult(Result.failure(task.exception ?: IllegalStateException("Sign-in failed.")))
                    return@addOnCompleteListener
                }
                user.reload().addOnCompleteListener { reload ->
                    if (!reload.isSuccessful) {
                        onResult(Result.failure(reload.exception ?: IllegalStateException("Could not refresh account status.")))
                    } else if (user.isEmailVerified) {
                        onResult(Result.success(user))
                    } else {
                        onResult(Result.failure(EmailNotVerifiedException(user)))
                    }
                }
            }
    }

    fun createAccount(name: String, email: String, password: String, onResult: (Result<FirebaseUser>) -> Unit) {
        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                val user = task.result?.user
                if (!task.isSuccessful || user == null) {
                    onResult(Result.failure(task.exception ?: IllegalStateException("Account creation failed.")))
                    return@addOnCompleteListener
                }
                val profile = UserProfileChangeRequest.Builder().setDisplayName(name.trim()).build()
                user.updateProfile(profile).continueWithTask { update ->
                    if (!update.isSuccessful) throw (update.exception ?: IllegalStateException("Could not save the account name."))
                    user.sendEmailVerification()
                }.addOnCompleteListener { verification ->
                    if (verification.isSuccessful) onResult(Result.success(user))
                    else onResult(Result.failure(verification.exception ?: IllegalStateException("Verification email could not be sent.")))
                }
            }
    }

    fun resendVerification(onResult: (Result<Unit>) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onResult(Result.failure(IllegalStateException("Your session has expired. Please sign in again.")))
            return
        }
        user.sendEmailVerification().addOnCompleteListener { task ->
            if (task.isSuccessful) onResult(Result.success(Unit))
            else onResult(Result.failure(task.exception ?: IllegalStateException("Verification email could not be sent.")))
        }
    }

    fun refreshVerification(onResult: (Result<FirebaseUser>) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onResult(Result.failure(IllegalStateException("Your session has expired. Please sign in again.")))
            return
        }
        user.reload().addOnCompleteListener { task ->
            val refreshed = auth.currentUser
            if (!task.isSuccessful) onResult(Result.failure(task.exception ?: IllegalStateException("Could not refresh account status.")))
            else if (refreshed?.isEmailVerified == true) onResult(Result.success(refreshed))
            else onResult(Result.failure(EmailNotVerifiedException(refreshed ?: user)))
        }
    }

    fun signOut() = auth.signOut()
}

class EmailNotVerifiedException(val account: FirebaseUser) : Exception("Please verify your email address before continuing.")
