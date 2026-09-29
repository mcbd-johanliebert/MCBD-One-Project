package com.mcbdone.app.data.remote

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.mcbdone.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoogleAuthManager(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)

    fun getLegacyGoogleSignInClient(activity: Activity): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(activity, gso)
    }

    suspend fun signInWithGoogle(activity: Activity): Result<String> = withContext(Dispatchers.IO) {
        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response: GetCredentialResponse = credentialManager.getCredential(
                request = request,
                context = activity
            )

            val credential = response.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                Log.d("GoogleAuthManager", "Google Sign-In successful: $idToken")
                Result.success(idToken)
            } else {
                Log.w("GoogleAuthManager", "Unexpected credential type: ${credential.type}")
                Result.failure(Exception("Unsupported credential type"))
            }
        } catch (e: GetCredentialException) {
            Log.e("GoogleAuthManager", "Credential Manager error: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("GoogleAuthManager", "Sign in general error: ${e.message}", e)
            Result.failure(e)
        }
    }
}
