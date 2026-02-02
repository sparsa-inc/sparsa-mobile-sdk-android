package com.sparsa.android.main.helpers

import androidx.lifecycle.viewModelScope
import com.sparsa.android.main.ContentViewModel
import com.sparsainc.sdk.sparsa.Sparsa
import com.sparsainc.sdk.sparsa.data.model.external.CredentialItem
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// MARK: - Execute Helper
internal suspend fun ContentViewModel.execute(block: suspend () -> String?) {
    updateUIState { copy(requesting = true) }
    try {
        val result = block()
        if (!result.isNullOrEmpty()) {
            showAlertMessage(result)
        }
    } catch (e: Exception) {
        val message = e.localizedMessage ?: "Unknown Error"
        if (message != "Cancelled" && message != "Action cancelled by user") {
            showAlertMessage(message)
        }
    } finally {
        updateUIState { copy(requesting = false) }
    }
    updateButtonStates()
}

// MARK: - Selection Helpers
internal suspend fun ContentViewModel.selectCredential(): CredentialItem? {
    var credentials = Sparsa.getCredentials()
    val filterResult = presentCredentialsFilter(credentials) ?: throw Exception("Cancelled")
    val (statuses, types) = filterResult
    updateUIState { copy(requesting = true) }
    credentials = Sparsa.getCredentials(statuses, types)
    if (credentials.isEmpty()) throw Exception("No credentials found")
    showBottomSheet(credentials.filter { it.identifier != null }.map { credential ->
        val issuer = credential.issuer ?: ""
        val dateOnly = formatDateOnly(credential.issueDate ?: "")
        val revokedLabel = if (credential.isRevoked == true) " (Revoked)" else ""
        credential.identifier!! to "${credential.schema}$revokedLabel\nIssuer: $issuer\nType: ${credential.schema}\nDate: $dateOnly"
    })
    val selectedId = waitForUserSelection() ?: throw Exception("Cancelled")
    return credentials.firstOrNull { it.identifier == selectedId }
}

private fun formatDateOnly(dateString: String): String {
    val parts = dateString.split(" ", "T")
    return parts.firstOrNull() ?: dateString
}

internal suspend fun ContentViewModel.scanQR(): String {
    updateState { copy(qrData = "") }
    showQr()
    return suspendCancellableCoroutine { continuation ->
        var isResumed = false
        val job = viewModelScope.launch {
            stateFlow.collect { state ->
                if (state.qrData.isNotEmpty() && !isResumed) {
                    isResumed = true
                    continuation.resume(state.qrData)
                    hideQr()
                    cancel()
                }
            }
        }
        continuation.invokeOnCancellation { job.cancel() }
    }
}

// MARK: - Wait Helpers
internal suspend fun ContentViewModel.waitForUserSelection(): String? = suspendCancellableCoroutine { continuation ->
    var isResumed = false
    val job = viewModelScope.launch {
        uiStateFlow.collect { ui ->
            if (ui.selectedItem != null && !isResumed) {
                isResumed = true
                continuation.resume(ui.selectedItem)
                cancel()
            } else if (!ui.showBottomSheet && ui.selectedItem == null && !isResumed) {
                isResumed = true
                continuation.resume(null)
                cancel()
            }
        }
    }
    continuation.invokeOnCancellation { job.cancel() }
}

internal suspend fun ContentViewModel.waitForConfigInput(): Pair<String, String> {
    showConfigBottomSheet()
    return suspendCancellableCoroutine { continuation ->
        viewModelScope.launch {
            var isResumed = false
            val job = viewModelScope.launch {
                uiStateFlow.collect { ui ->
                    val state = stateFlow.value
                    if (ui.submitted && !ui.showConfigureSheet && state.clientId.isNotEmpty() && state.secret.isNotEmpty() && !isResumed) {
                        isResumed = true
                        continuation.resume(Pair(state.clientId, state.secret))
                        cancel()
                    } else if (!ui.showConfigureSheet && !ui.submitted && !isResumed) {
                        isResumed = true
                        continuation.resumeWithException(Exception("Cancelled"))
                        cancel()
                    }
                }
            }
            continuation.invokeOnCancellation { job.cancel() }
        }
    }
}

internal suspend fun ContentViewModel.presentCredentialsFilter(credentials: List<CredentialItem>): Pair<Set<String>, Set<String>>? {
    updateUIState { copy(fetchedCredentialsForFilter = credentials, showFilterSheet = true, filterResult = null, requesting = false) }
    return suspendCancellableCoroutine { continuation ->
        viewModelScope.launch {
            var isResumed = false
            val job = viewModelScope.launch {
                uiStateFlow.collect { ui ->
                    if (!ui.showFilterSheet && !isResumed) {
                        isResumed = true
                        continuation.resume(ui.filterResult)
                        cancel()
                    }
                }
            }
            continuation.invokeOnCancellation { job.cancel() }
        }
    }
}

internal suspend fun ContentViewModel.waitForUserInput(): String = suspendCancellableCoroutine { continuation ->
    var isResumed = false
    val job = viewModelScope.launch {
        uiStateFlow.collect { ui ->
            if (!ui.showInput && !isResumed) {
                isResumed = true
                val email = stateFlow.value.input
                if (email.isNotEmpty()) {
                    continuation.resume(email)
                } else {
                    continuation.resumeWithException(Exception("Action cancelled by user"))
                }
                cancel()
            }
        }
    }
    continuation.invokeOnCancellation { job.cancel() }
}

// MARK: - Button Groups
internal val ContentViewModel.buttonGroups: List<ButtonsGroup>
    get() = listOf(
        ButtonsGroup(
            "Authentication", listOf(
                SparsaButtonItem(SparsaButton.AuthUser, action = ::authenticateUser),
                SparsaButtonItem(SparsaButton.RegUser, action = ::registerUser),
                SparsaButtonItem(
                    SparsaButton.DeviceBootstrappingVerification,
                    disabled = false,
                    action = ::deviceBootstrappingVerification
                ),
                SparsaButtonItem(
                    SparsaButton.UpdateDigitalAddress,
                    action = ::updateDigitalAddress
                ),
                SparsaButtonItem(
                    SparsaButton.GetDigitalAddress,
                    action = ::getDigitalAddress
                )
            )
        ),
        ButtonsGroup(
            "Credentials", listOf(
                SparsaButtonItem(SparsaButton.GetCredentials, action = ::getCredentials),
                SparsaButtonItem(SparsaButton.GetCredentialDetails, action = ::getCredentialDetails),
                SparsaButtonItem(SparsaButton.ProofProcess, action = ::proofProcess)
            )
        ),
        ButtonsGroup(
            "Devices", listOf(
                SparsaButtonItem(SparsaButton.GetDevices, action = ::getDevices),
                SparsaButtonItem(SparsaButton.DeleteDevice, action = ::deleteDevice)
            )
        ),
        ButtonsGroup(
            "Email", listOf(
                SparsaButtonItem(SparsaButton.SendRecoveryEmail, action = ::sendRecoveryEmail),
                SparsaButtonItem(SparsaButton.SetRecoveryEmail, action = ::setRecoveryEmail)
            )
        ),
        ButtonsGroup(
            "Languages", listOf(
                SparsaButtonItem(SparsaButton.SetLanguage, action = ::setLanguage),
                SparsaButtonItem(SparsaButton.GetLanguage, action = ::getLanguage)
            )
        )
    )

internal fun ContentViewModel.updateButtonStates() {
    onStateChangeCallback?.invoke(gson.toJson(state.value))
    val state = stateFlow.value
    val updatedGroups = uiState.value.groups.map { group ->
        group.copy(buttons = group.buttons.map { button ->
            button.copy(disabled = when (button.item) {
                SparsaButton.AuthUser, SparsaButton.RegUser ->
                    state.qrData.isEmpty() || state.digitalAddress.isNotEmpty()
                SparsaButton.DeviceBootstrappingVerification ->
                    state.digitalAddress.isNotEmpty()
                SparsaButton.UpdateDigitalAddress, SparsaButton.GetDigitalAddress ->
                    state.digitalAddress.isEmpty()
                SparsaButton.SendRecoveryEmail ->
                    false
                else ->
                    state.digitalAddress.isEmpty()
            })
        })
    }
    updateUIState { copy(groups = updatedGroups) }
}
