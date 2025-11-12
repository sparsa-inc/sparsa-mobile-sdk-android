package com.sparsa.android.ui.content

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import main.SparsaMobile
import java.lang.ref.WeakReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ContentViewModel: ViewModel() {
    data class State(
        var digitalAddress: String = "",
        var qrData: String = "",
        var linkDeviceId: String = "",
        var transactionId: String = "",
        var credentialVerificationStarted: Boolean = false,
        var email: String = "",
        var clientId: String = "",
        var secret: String = ""
    )

    private val baseURL = "https://exchange-api.dev.sparsainc.com"

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private var sharedPreferences: SharedPreferences? = null
    private var activityRef: WeakReference<AppCompatActivity>? = null

    private var onStateChange: ((String) -> Unit)? = null

    val chooserList = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val isBottomSheetVisible = MutableStateFlow(false)
    private val selectedItem = MutableStateFlow<String?>(null)

    val isConfigBottomSheetVisible = MutableStateFlow(false)
    val _submitted = MutableStateFlow(false)
    val submitted = _submitted.asStateFlow()

    fun setSubmitted(value: Boolean) {
        _submitted.value = value
    }

    fun clearState() {
        setState(State(clientId = _state.value.clientId, secret = _state.value.secret))
    }

    private fun showBottomSheet(items: List<Pair<String, String>>) {
        chooserList.value = items
        isBottomSheetVisible.value = true
    }

    fun hideBottomSheet() {
        chooserList.value = emptyList()
        selectedItem.value = null
        isBottomSheetVisible.value = false
        _requesting.value = false
    }

    fun showConfigBottomSheet() {
        isConfigBottomSheetVisible.value = true
    }

    fun hideConfigBottomSheet() {
        _requesting.value = false
        isConfigBottomSheetVisible.value = false
    }

    fun showFilterSheet(credentials: List<data.model.external.Credential>) {
        _fetchedCredentialsForFilter.value = credentials
        _showFilterSheet.value = true
        _requesting.value = false
    }

    fun hideFilterSheet() {
        _showFilterSheet.value = false
    }

    fun applyFilter(statuses: Set<String>, schemaIds: Set<String>) {
        _filterResult.value = Pair(statuses, schemaIds)
        hideFilterSheet()
    }

    fun submitConfiguration(clientId: String, clientSecret: String) {
        _state.value = _state.value.copy(clientId = clientId, secret = clientSecret)
        hideConfigBottomSheet()
    }

    fun selectItem(item: String) {
        selectedItem.value = item
        hideBottomSheet()
    }

    private suspend fun waitForUserSelection(): String? {
        return suspendCancellableCoroutine { continuation ->
            val job = viewModelScope.launch {
                try {
                    val selectedItem = selectedItem.first { !it.isNullOrEmpty() }
                    continuation.resume(selectedItem)
                } catch (e: Exception) {
                    if (continuation.isActive) {
                        continuation.resumeWithException(e)
                    }
                }
            }

            continuation.invokeOnCancellation {
                job.cancel()
            }
        }
    }

    private suspend fun waitForConfigInput(): Pair<String, String> {
        showConfigBottomSheet()
        return suspendCancellableCoroutine { continuation ->
            viewModelScope.launch {
                var isResumed = false
                val job = viewModelScope.launch {
                    isConfigBottomSheetVisible.collect { isVisible ->
                        if (!isVisible && !isResumed) {
                            isResumed = true
                            if (_submitted.value && _state.value.clientId.isNotEmpty() && _state.value.secret.isNotEmpty()) {
                                continuation.resume(Pair(_state.value.clientId, _state.value.secret))
                            } else {
                                continuation.resumeWithException(Exception("Cancelled"))
                            }
                            cancel()
                        }
                    }
                }

                continuation.invokeOnCancellation {
                    job.cancel()
                }
            }
        }
    }

    private suspend fun presentCredentialsFilter(credentials: List<data.model.external.Credential>): Pair<Set<String>, Set<String>>? {
        showFilterSheet(credentials)
        return suspendCancellableCoroutine { continuation ->
            viewModelScope.launch {
                var isResumed = false
                val job = viewModelScope.launch {
                    _showFilterSheet.collect { isVisible ->
                        if (!isVisible && !isResumed) {
                            isResumed = true
                            if (_filterResult.value != null) {
                                continuation.resume(_filterResult.value)
                                _filterResult.value = null
                            } else {
                                continuation.resume(null)
                            }
                            cancel()
                        }
                    }
                }

                continuation.invokeOnCancellation {
                    job.cancel()
                }
            }
        }
    }

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private val _groups = MutableStateFlow<List<ButtonsGroup>>(emptyList())
    val groups: StateFlow<List<ButtonsGroup>> = _groups.asStateFlow()

    private val _showAlert = MutableStateFlow(false)
    val showAlert: StateFlow<Boolean> = _showAlert.asStateFlow()

    private val _showEmailDialog = MutableStateFlow(false)
    val showEmailDialog: StateFlow<Boolean> = _showEmailDialog.asStateFlow()

    private val _requesting = MutableStateFlow(false)
    val requesting: StateFlow<Boolean> = _requesting.asStateFlow()

    private val _showQr = MutableStateFlow(false)
    val showQr: StateFlow<Boolean> = _showQr.asStateFlow()

    private val _alertMessage = MutableStateFlow("")
    val alertMessage: StateFlow<String> = _alertMessage.asStateFlow()

    private val _showFilterSheet = MutableStateFlow(false)
    val showFilterSheet: StateFlow<Boolean> = _showFilterSheet.asStateFlow()

    private val _fetchedCredentialsForFilter = MutableStateFlow<List<data.model.external.Credential>>(emptyList())
    val fetchedCredentialsForFilter: StateFlow<List<data.model.external.Credential>> = _fetchedCredentialsForFilter.asStateFlow()

    private val _filterResult = MutableStateFlow<Pair<Set<String>, Set<String>>?>(null)
    val filterResult: StateFlow<Pair<Set<String>, Set<String>>?> = _filterResult.asStateFlow()

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            hideQr()
        }
    }

    init {
        _groups.value = buttonGroups
        updateButtonStates()
    }

    suspend fun configure(activity: AppCompatActivity, state: State, onStateChange: (String) -> Unit) {
        this.activityRef = WeakReference(activity)
        _state.value = state
        updateButtonStates()
        this.onStateChange = onStateChange
        sharedPreferences = activity.getSharedPreferences("sample.app", Context.MODE_PRIVATE)
        activity.onBackPressedDispatcher.addCallback(onBackPressedCallback)

        execute {
            val (clientId, clientSecret) = waitForConfigInput()
            SparsaMobile.configure(
                activity = activity,
                url = baseURL,
                clientId = clientId,
                clientSecret = clientSecret
            )
            return@execute "SparsaMobile successfully configured!"
        }
    }

    fun reconfigure() {
        viewModelScope.launch {
            execute {
                val activity = activityRef?.get()
                    ?: throw IllegalStateException("Activity not available. It may have been destroyed or configure() was not called.")

                val (clientId, clientSecret) = waitForConfigInput()
                SparsaMobile.configure(
                    activity = activity,
                    url = baseURL,
                    clientId = clientId,
                    clientSecret = clientSecret
                )
                return@execute "SparsaMobile successfully reconfigured!"
            }
        }
    }

    fun setState(state: State) {
        _state.value = state
        updateButtonStates()
    }

    fun showAlertMessage(message: String) {
        _alertMessage.value = message
        _showAlert.value = true
    }

    fun hideAlert() { _showAlert.value = false }

    fun showQr() {
        onBackPressedCallback.isEnabled = true
        _showQr.value = true
    }

    fun hideQr() {
        onBackPressedCallback.isEnabled = false
        _showQr.value = false
        _requesting.value = false
    }

    private suspend fun scanQR(): String {
        _state.value = _state.value.copy(qrData = "")
        showQr()
        return suspendCancellableCoroutine { continuation ->
            var isResumed = false
            val job = viewModelScope.launch {
                _state.collectLatest { state ->
                    if (state.qrData.isNotEmpty() && !isResumed) {
                        isResumed = true
                        continuation.resume(state.qrData)
                        hideQr()
                        cancel()
                    }
                }
            }

            continuation.invokeOnCancellation {
                job.cancel()
            }
        }
    }

    fun scanQRResult(result: (Result<String>)) {
        _showQr.value = false
        onBackPressedCallback.isEnabled = false
        result.fold(
            onSuccess = {
                setState(_state.value.copy(qrData = it))
            },
            onFailure = {
                val message = it.message
                showAlertMessage(message ?: "Something went wrong")
            }
        )
    }

    suspend fun authenticateUser() {
        execute {
            val result = SparsaMobile.authenticateUser(state.value.qrData)
            _state.value = _state.value.copy(digitalAddress = result.digitalAddress, linkDeviceId = result.linkDeviceId)
            setState(_state.value)
            return@execute "Digital Address successfully imported!"
        }
    }

    suspend fun registerUser() {
        execute {
            val result = SparsaMobile.registerUser(state.value.qrData)
            _state.value = _state.value.copy(digitalAddress = result.digitalAddress, linkDeviceId = result.linkDeviceId)
            setState(_state.value)
            return@execute "Digital Address successfully imported!"
        }
    }

    suspend fun proofProcess() {
        execute {
            val qrData = scanQR()
            SparsaMobile.proofProcess(qrData)
            return@execute "Process finished"
        }
    }

    fun showEmailInput() {
        _showEmailDialog.value = true
    }

    fun hideEmailInput() {
        _showEmailDialog.value = false
    }

    fun setEmail(value: String) {
        _state.value  = _state.value.copy(email = value)
    }

    private suspend fun waitForUserInput(): String = suspendCancellableCoroutine { continuation ->
        var isResumed = false
        val job = CoroutineScope(Dispatchers.IO).launch {
            showEmailDialog.collect { isVisible ->
                if (!isVisible && !isResumed) {
                    isResumed = true
                    if (_state.value.email.isNotEmpty()) {
                        continuation.resume(_state.value.email)
                    } else {
                        continuation.resumeWithException(
                            Exception("Action cancelled by user")
                        )
                    }
                    cancel()
                }
            }
        }

        continuation.invokeOnCancellation {
            job.cancel()
        }
    }

    suspend fun sendRecoveryEmail() {
        execute {
            showEmailInput()
            val userEmail = waitForUserInput()
            SparsaMobile.sendRecoveryEmail(userEmail)
            return@execute "Sent successfully"
        }
    }

    suspend fun setRecoveryEmail() {
        execute {
            showEmailInput()
            val userEmail = waitForUserInput()
            SparsaMobile.setRecoveryEmail(userEmail)
            return@execute "Set successfully"
        }
    }

    suspend fun getCredentials() {
        execute {
            var credentials = SparsaMobile.getCredentials()
            val filterResult = presentCredentialsFilter(credentials) ?: return@execute "Failed to get credentials"

            val (statuses, types) = filterResult
            credentials = SparsaMobile.getCredentials(statuses, types)

            if (credentials.isEmpty()) {
                return@execute "No credentials found with statuses: $statuses and types: $types"
            }

            showBottomSheet(credentials
                .filter { it.identifier != null }
                .map { (it.identifier!! to it.schema + (if (it.isRevoked == true) " (Revoked)" else "")) }
            )

            val selectedCredentialIdentifier = waitForUserSelection()

            return@execute if (selectedCredentialIdentifier != null) {
                val selectedCredential = credentials.first { it.identifier == selectedCredentialIdentifier }
                return@execute gson.toJson(selectedCredential)
            } else {
                "Failed to get credentials"
            }
        }
    }

    suspend fun getCredentialDetails() {
        execute {
            var credentials = SparsaMobile.getCredentials()
            val filterResult = presentCredentialsFilter(credentials)

            if (filterResult != null) {
                val (statuses, types) = filterResult
                credentials = SparsaMobile.getCredentials(statuses, types)
            }

            showBottomSheet(credentials
                .filter { it.identifier != null }
                .map { (it.identifier!! to it.schema + (if (it.isRevoked == true) " (Revoked)" else "")) }
            )

            val selectedCredentialIdentifier = waitForUserSelection()

            return@execute if (selectedCredentialIdentifier != null) {
                val selectedCredential = credentials.first { it.identifier == selectedCredentialIdentifier }
                val result = selectedCredential.identifier?.let { identifier -> SparsaMobile.getCredentialDetails(identifier) }
                return@execute gson.toJson(result)
            } else {
                ""
            }
        }
    }

    suspend fun getDevices() {
        getDeviceDetails()
    }

    suspend fun getDeviceDetails() {
        execute {
            val devices = SparsaMobile.getDevices()
            showBottomSheet(devices.map { it.identifier to listOf(it.name, it.createdDate).joinToString(" ")  })

            val selectedDeviceIdentifier = waitForUserSelection()

            return@execute if (selectedDeviceIdentifier != null) {
                val selectedDevice = devices.first { it.identifier == selectedDeviceIdentifier }
                return@execute gson.toJson(selectedDevice)
            } else {
                ""
            }
        }
    }

    suspend fun deleteDevice() {

        execute {
            val devices = SparsaMobile.getDevices()
            showBottomSheet(devices.map { it.identifier to it.name })

            val selectedDeviceId = waitForUserSelection()

            return@execute if (selectedDeviceId != null) {
                val isCurrentDevice = SparsaMobile.deleteDevice(selectedDeviceId)
                if (isCurrentDevice) {
                    setState(State(clientId = _state.value.clientId, secret = _state.value.secret))
                }
                "Successfully deleted."
            } else {
                ""
            }
        }
    }

    suspend fun setLanguage() {
        execute {
            val languages = listOf("ja" to "Japan", "en" to "English")
            showBottomSheet(languages)

            val selectedLanguage = waitForUserSelection()

            return@execute if (selectedLanguage != null) {
                val result = SparsaMobile.setLanguage(selectedLanguage)
                return@execute result
            } else {
                "Retry setting language."
            }
        }
    }

    suspend fun getLanguage() {
        execute {
            SparsaMobile.getLanguage()
        }
    }

    suspend fun deviceBootstrappingVerification() {
        execute {
            val result = SparsaMobile.deviceBootstrappingVerification()
            setState(_state.value.copy(transactionId = result.identifier))
            result.identifier + "\nStatus: " + result.status
        }
    }

    suspend fun checkBootstrappingStatus() {
        execute {
            "Status: " + SparsaMobile.checkBootstrappingStatus(state.value.transactionId).status
        }
    }

    suspend fun startCredentialVerificationProcess() {
        execute {
            val result = SparsaMobile.startCredentialVerificationProcess(state.value.transactionId)
            setState(_state.value.copy(credentialVerificationStarted = true))
            result.identifier + "\nStatus: " + result.status
        }
    }

    suspend fun acceptProof() {
        execute {
            val credentials = SparsaMobile.getCredentials()
            showBottomSheet(credentials
                .filter { it.identifier != null }
                .map { it.identifier!! to it.schema + (if (it.isRevoked == true) " (Revoked)" else "") }
            )

            val selectedDeviceName = waitForUserSelection()

            return@execute if (selectedDeviceName != null) {
                val selectedCredential = credentials.first { it.schema == selectedDeviceName }
                val result = selectedCredential.identifier?.let { identifier -> SparsaMobile.getCredentialDetails(identifier) }
                val acceptResult = result?.identifier?.let { identifier -> SparsaMobile.acceptProof(state.value.transactionId, credentialIdentifier = identifier) }
                setState(_state.value.copy(credentialVerificationStarted = false, transactionId = ""))
                arrayOf(acceptResult?.identifier + " accepted", "\nStatus: " + acceptResult?.status).joinToString(", ")
            } else {
                ""
            }
        }
    }

    suspend fun rejectProof() {
        execute {
            val result = SparsaMobile.rejectProof(state.value.transactionId)
            setState(_state.value.copy(credentialVerificationStarted = false, transactionId = ""))
            arrayOf(result.identifier + " rejected", "Status: " + result.status).joinToString(", ")
        }
    }

    suspend fun execute(block: suspend (ContentViewModel) -> String?) {
        _requesting.value = true
        try {
            val result = block(this)
            if (result != null) {
                showAlertMessage(result)
            }
        } catch (e: Exception) {
            val message = e.localizedMessage
            showAlertMessage(message ?: "Unknown Error")
        } finally {
            _requesting.value = false
        }

        updateButtonStates()
    }

    private fun updateButtonStates() {
        onStateChange?.let { it(Gson().toJson(state.value)) }

        val updatedGroups = _groups.value.map { group ->
            group.copy(
                buttons = group.buttons.map { button ->
                    button.copy(
                        disabled = when (button.item) {
                            SparsaButton.AuthUser, SparsaButton.RegUser
                                -> state.value.qrData.isEmpty() || state.value.digitalAddress.isNotEmpty()

                            SparsaButton.SendRecoveryEmail -> false

                            SparsaButton.StartCredentialVerificationProcess
                                -> state.value.digitalAddress.isEmpty()
                                    || state.value.transactionId.isEmpty()
                                    || state.value.credentialVerificationStarted


                            SparsaButton.GetCredentials, SparsaButton.GetCredentialDetails,
                            SparsaButton.GetDevices, SparsaButton.GetDeviceDetails,
                            SparsaButton.DeleteDevice, SparsaButton.DeviceBootstrappingVerification,
                            SparsaButton.GetLanguage, SparsaButton.SetLanguage, SparsaButton.ProofProcess,
                            SparsaButton.SetRecoveryEmail
                                -> state.value.digitalAddress.isEmpty()

                            SparsaButton.CheckBootstrappingStatus
                                -> state.value.transactionId.isEmpty()

                            SparsaButton.AcceptProof, SparsaButton.RejectProof
                                -> !state.value.credentialVerificationStarted
                        }
                    )
                }
            )
        }

        _groups.value = updatedGroups
    }

    private val buttonGroups: List<ButtonsGroup>
        get() = listOf(
            ButtonsGroup(
                name = "Authentication",
                buttons = listOf(
                    SparsaButtonItem(SparsaButton.AuthUser, action = ::authenticateUser),
                    SparsaButtonItem(SparsaButton.RegUser, action = ::registerUser),
                    SparsaButtonItem(SparsaButton.ProofProcess, action = ::proofProcess),
                )
            ),
            ButtonsGroup(
                name = "Email",
                buttons = listOf(
                    SparsaButtonItem(SparsaButton.SendRecoveryEmail, action = ::sendRecoveryEmail),
                    SparsaButtonItem(SparsaButton.SetRecoveryEmail, action = ::setRecoveryEmail)
                )
            ),
            ButtonsGroup(
                name = "Digital Address Dependent",
                buttons = listOf(
                    SparsaButtonItem(SparsaButton.GetCredentials, action = ::getCredentials),
                    SparsaButtonItem(SparsaButton.GetCredentialDetails, action = ::getCredentialDetails),
                    SparsaButtonItem(SparsaButton.GetDevices, action = ::getDevices),
                    SparsaButtonItem(SparsaButton.GetDeviceDetails, action = ::getDeviceDetails),
                    SparsaButtonItem(SparsaButton.DeleteDevice, action = ::deleteDevice),
                    SparsaButtonItem(SparsaButton.SetLanguage, action = ::setLanguage),
                    SparsaButtonItem(SparsaButton.GetLanguage, action = ::getLanguage)
                )
            ),
            ButtonsGroup(
                name = "Bootstrapping, Credential Verification",
                buttons = listOf(
                    SparsaButtonItem(SparsaButton.DeviceBootstrappingVerification, disabled = false, action = ::deviceBootstrappingVerification),
                    SparsaButtonItem(SparsaButton.CheckBootstrappingStatus, action = ::checkBootstrappingStatus),
                    SparsaButtonItem(SparsaButton.StartCredentialVerificationProcess, action = ::startCredentialVerificationProcess),
                    SparsaButtonItem(SparsaButton.AcceptProof, action = ::acceptProof),
                    SparsaButtonItem(SparsaButton.RejectProof, action = ::rejectProof)
                )
            )
        )

    override fun onCleared() {
        super.onCleared()
        activityRef?.clear()
        activityRef = null
        sharedPreferences = null
        onStateChange = null
    }
}
