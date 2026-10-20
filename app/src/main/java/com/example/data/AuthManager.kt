package com.example.data

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.model.AuthUser
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class AuthManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("auth_user_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow(loadSavedUser())
    val currentUser: StateFlow<AuthUser> = _currentUser.asStateFlow()

    private fun loadSavedUser(): AuthUser {
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        if (!isLoggedIn) {
            return AuthUser(isLoggedIn = false)
        }
        return AuthUser(
            id = prefs.getString("user_id", "").orEmpty(),
            name = prefs.getString("user_name", "Usuário").orEmpty(),
            email = prefs.getString("user_email", "").orEmpty(),
            photoUrl = prefs.getString("user_photo_url", "").orEmpty(),
            idToken = prefs.getString("user_id_token", "").orEmpty(),
            isLoggedIn = true,
            isGuest = prefs.getBoolean("is_guest", false)
        )
    }

    fun saveUser(user: AuthUser) {
        prefs.edit()
            .putBoolean("is_logged_in", user.isLoggedIn)
            .putBoolean("is_guest", user.isGuest)
            .putString("user_id", user.id)
            .putString("user_name", user.name)
            .putString("user_email", user.email)
            .putString("user_photo_url", user.photoUrl)
            .putString("user_id_token", user.idToken)
            .apply()
        _currentUser.value = user
    }

    fun logout() {
        prefs.edit().clear().apply()
        _currentUser.value = AuthUser(isLoggedIn = false)
    }

    fun loginAsGuest() {
        val guest = AuthUser(
            id = "guest_${System.currentTimeMillis()}",
            name = "Visitante",
            email = "visitante@app.local",
            photoUrl = "",
            isLoggedIn = true,
            isGuest = true
        )
        saveUser(guest)
    }

    fun loginWithSimulatedGoogle(name: String, email: String, photoUrl: String = "") {
        val user = AuthUser(
            id = "google_${email.hashCode()}",
            name = name,
            email = email,
            photoUrl = photoUrl,
            idToken = "simulated_google_token_${System.currentTimeMillis()}",
            isLoggedIn = true,
            isGuest = false
        )
        saveUser(user)
    }

    suspend fun signInWithGoogleCredentialManager(activityContext: Context): Result<AuthUser> = withContext(Dispatchers.IO) {
        val credentialManager = CredentialManager.create(activityContext)

        // Web Client ID placeholder or standard Google option
        val googleIdOption = com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption.Builder(
            "default-web-client-id.apps.googleusercontent.com"
        ).build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val response: GetCredentialResponse = credentialManager.getCredential(
                context = activityContext,
                request = request
            )

            val credential = response.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val user = AuthUser(
                        id = googleIdTokenCredential.id,
                        name = googleIdTokenCredential.displayName ?: googleIdTokenCredential.givenName ?: "Motorista",
                        email = googleIdTokenCredential.id,
                        photoUrl = googleIdTokenCredential.profilePictureUri?.toString().orEmpty(),
                        idToken = googleIdTokenCredential.idToken,
                        isLoggedIn = true,
                        isGuest = false
                    )
                    withContext(Dispatchers.Main) {
                        saveUser(user)
                    }
                    return@withContext Result.success(user)
                } catch (e: GoogleIdTokenParsingException) {
                    Log.e("AuthManager", "Erro ao fazer parse do Google ID Token", e)
                    return@withContext Result.failure(e)
                }
            } else {
                return@withContext Result.failure(Exception("Tipo de credencial não esperado: ${credential.type}"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d("AuthManager", "Login Google cancelado pelo usuário")
            return@withContext Result.failure(Exception("Login cancelado pelo usuário."))
        } catch (e: NoCredentialException) {
            Log.w("AuthManager", "Nenhuma conta Google encontrada no aparelho", e)
            return@withContext Result.failure(
                NoCredentialException("Nenhuma conta Google configurada ou serviço Google Play indisponível no emulador.")
            )
        } catch (e: GetCredentialException) {
            Log.w("AuthManager", "Falha no Credential Manager Google", e)
            return@withContext Result.failure(e)
        } catch (e: Exception) {
            Log.e("AuthManager", "Erro geral no login Google", e)
            return@withContext Result.failure(e)
        }
    }
}
