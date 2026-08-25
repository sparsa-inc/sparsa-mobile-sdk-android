package com.sparsa.android.main

import android.content.Context
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import com.sparsa.android.main.helpers.State
import com.sparsa.android.main.helpers.UIState
import com.sparsa.android.main.helpers.buttonGroups
import com.sparsa.android.main.helpers.execute
import com.sparsa.android.main.helpers.scanQR
import com.sparsa.android.main.helpers.selectCredential
import com.sparsa.android.main.helpers.updateButtonStates
import com.sparsa.android.main.helpers.waitForConfigInput
import com.sparsa.android.main.helpers.waitForUserInput
import com.sparsa.android.main.helpers.waitForUserSelection
import com.sparsainc.sdk.sparsa.Sparsa
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class ContentViewModel : ViewModel() {

    // MARK: - Properties
    private val baseURL = "BASE_URL"
    internal val gson = GsonBuilder().setPrettyPrinting().create()
    internal var onStateChangeCallback: ((String) -> Unit)? = null
    private var activityRef: WeakReference<AppCompatActivity>? = null

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()
    internal val stateFlow: MutableStateFlow<State> get() = _state

    private val _uiState = MutableStateFlow(UIState())
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()
    internal val uiStateFlow: MutableStateFlow<UIState> get() = _uiState

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (_uiState.value.showQrScanner) hideQr()
        }
    }

    init {
        _uiState.value = _uiState.value.copy(groups = buttonGroups)
        updateButtonStates()
    }

    // MARK: - Configuration
    suspend fun configure(activity: AppCompatActivity, state: State, onStateChange: (String) -> Unit) {
        activityRef = WeakReference(activity)
        _state.value = state
        onStateChangeCallback = onStateChange
        activity.getSharedPreferences("sample.app", Context.MODE_PRIVATE)
        activity.onBackPressedDispatcher.addCallback(onBackPressedCallback)
        updateButtonStates()

        execute {
            val (clientId, clientSecret) = waitForConfigInput()
            Sparsa.configure(
                activity = activity,
                url = baseURL,
                clientId = clientId,
                clientSecret = clientSecret,
                onDelete = { clearState() }
            )
            "Success! Sparsa configured"
        }
    }

    fun reconfigure() {
        viewModelScope.launch {
            execute {
                val activity = activityRef?.get() ?: throw IllegalStateException("Activity not available")
                val (clientId, clientSecret) = waitForConfigInput()
                Sparsa.configure(
                    activity = activity,
                    url = baseURL,
                    clientId = clientId,
                    clientSecret = clientSecret,
                    onDelete = { clearState() }
                )
                "Success! Sparsa configured"
            }
        }
    }

    // MARK: - Authentication
    suspend fun authenticateUser() = execute {
        val result = Sparsa.recoverDigitalAddress(state.value.qrData)
        updateState { copy(digitalAddress = result.digitalAddress, linkDeviceId = result.linkDeviceId) }
        updateButtonStates()
        "Success! Digital Address imported"
    }

    suspend fun registerUser() = execute {
        val result = Sparsa.importDigitalAddress(state.value.qrData)
        updateState { copy(digitalAddress = result.digitalAddress, linkDeviceId = result.linkDeviceId) }
        updateButtonStates()
        "Success! Digital Address imported"
    }

    suspend fun updateDigitalAddress() = execute {
        showInput()

        val da = Sparsa.getDigitalAddress()
        this.updateState {
            copy(digitalAddress = da, input = da)
        }
        val res = Sparsa.updateDigitalAddress(waitForUserInput())
        this.updateState { copy(digitalAddress = res) }
        "Success! New digital address is: $res"
    }

    suspend fun getDigitalAddress() = execute {
        val da = Sparsa.getDigitalAddress()
        this.updateState { copy(digitalAddress = da) }
        "Success! Current digital address is: $da"
    }

    suspend fun deviceBootstrappingVerification() = execute {
        try {
            val result = Sparsa.deviceBootstrappingVerification { bootstrappingData ->
                viewModelScope.launch(Dispatchers.Main) { showBootstrappingSheet(gson.toJson(bootstrappingData)) }
            }
            updateState { copy(digitalAddress = result.digitalAddress, linkDeviceId = result.linkDeviceId) }
            hideBootstrappingSheet()
            updateButtonStates()
            return@execute "Success! Digital Address: ${result.digitalAddress} imported"
        } catch (e: Exception) {
            hideBootstrappingSheet()
            return@execute e.message
        }
    }

    // MARK: - Credentials
    suspend fun proofProcess() = execute {
        Sparsa.proofProcess(scanQR())
        "Success! Process finished"
    }

    suspend fun getCredentials() = execute { selectCredential()?.let { gson.toJson(it) } ?: "" }

    suspend fun getCredentialDetails() = execute {
        selectCredential()?.identifier?.let {
            gson.toJson(Sparsa.getCredentialDetails(it))
        } ?: ""
    }

    // MARK: - Devices
    suspend fun getDevices() = execute {
        val devices = Sparsa.getDevices()
        val currentDeviceId = state.value.linkDeviceId
        showBottomSheet(devices.map { device ->
            val label = if (device.identifier == currentDeviceId) {
                "${device.name} (this device) \nCreated At: ${device.createdDate}"
            } else {
                "${device.name} \nCreated At: ${device.createdDate}"
            }
            device.identifier to label
        })
        waitForUserSelection()?.let { gson.toJson(devices.first { d -> d.identifier == it }) } ?: ""
    }

    suspend fun deleteDevice() = execute {
        val devices = Sparsa.getDevices()
        val currentDeviceId = state.value.linkDeviceId
        showBottomSheet(devices.map { device ->
            val label = if (device.identifier == currentDeviceId) {
                "${device.name} (this device)"
            } else {
                device.name
            }
            device.identifier to label
        })
        waitForUserSelection()?.let { selectedId ->
            if (Sparsa.deleteDevice(selectedId)) clearState()
            "Success! Device deleted"
        } ?: ""
    }

    // MARK: - Email
    suspend fun sendRecoveryEmail() = execute {
        showInput()
        Sparsa.sendRecoveryEmail(waitForUserInput())
        "Success! Email sent"
    }

    suspend fun setRecoveryEmail() = execute {
        showInput()
        Sparsa.setRecoveryEmail(waitForUserInput())
        "Success! Email changed"
    }

    // MARK: - Language
    suspend fun setLanguage() = execute {
        showBottomSheet(listOf("ja" to "Japan", "en" to "English"))
        waitForUserSelection()?.let { Sparsa.setLanguage(it); "Language set to: $it" } ?: ""
    }

    suspend fun getLanguage() = execute { Sparsa.getLanguage() }

    // MARK: - State Update Helpers
    internal fun updateState(block: State.() -> State) { _state.value = _state.value.block() }
    internal fun updateUIState(block: UIState.() -> UIState) { _uiState.value = _uiState.value.block() }

    // MARK: - UI Actions
    fun showBottomSheet(items: List<Pair<String, String>>) = updateUIState { copy(chooserList = items, showBottomSheet = true, selectedItem = null) }
    fun hideBottomSheet() = updateUIState { copy(chooserList = emptyList(), selectedItem = null, showBottomSheet = false, requesting = false) }
    fun selectItem(item: String) { updateUIState { copy(selectedItem = item, showBottomSheet = false, chooserList = emptyList(), requesting = false) } }

    fun showConfigBottomSheet() = updateUIState { copy(showConfigureSheet = true, submitted = false) }
    fun hideConfigBottomSheet() = updateUIState { copy(showConfigureSheet = false, submitted = false) }
    fun submitConfiguration(clientId: String, clientSecret: String) {
        updateState { copy(clientId = clientId, secret = clientSecret) }
        updateUIState { copy(submitted = true, showConfigureSheet = false) }
    }

    fun hideFilterSheet() = updateUIState { copy(showFilterSheet = false, selectedItem = null) }
    fun applyFilter(statuses: Set<String>, schemaIds: Set<String>) { updateUIState { copy(filterResult = Pair(statuses, schemaIds)) }; hideFilterSheet() }

    fun showAlertMessage(message: String) = updateUIState { copy(alertMessage = message, showAlert = true) }
    fun hideAlert() = updateUIState { copy(showAlert = false) }

    fun showQr() { onBackPressedCallback.isEnabled = true; updateUIState { copy(showQrScanner = true) } }
    fun hideQr() { onBackPressedCallback.isEnabled = false; updateUIState { copy(showQrScanner = false) } }
    fun scanQRResult(result: Result<String>) {
        hideQr()
        result.fold(
            onSuccess = {
                updateState { copy(qrData = it) }
                updateButtonStates()
            },
            onFailure = { showAlertMessage(it.message ?: "Something went wrong") }
        )
    }

    fun showInput() = updateUIState { copy(showInput = true) }
    fun hideInput() = updateUIState { copy(showInput = false) }
    fun setInput(value: String) = updateState { copy(input = value) }
    fun setClientId(value: String) = updateState { copy(clientId = value) }
    fun setSecret(value: String) = updateState { copy(secret = value) }

    fun showBootstrappingSheet(qrData: String) = updateUIState { copy(bootstrappingQrData = qrData, showBootstrappingSheet = true) }
    fun hideBootstrappingSheet() = updateUIState { copy(showBootstrappingSheet = false, bootstrappingQrData = null, requesting = false) }
    fun onBootstrappingTimeout() { hideBootstrappingSheet(); showAlertMessage("Bootstrapping timed out") }
    fun onBootstrappingCancelled() { hideBootstrappingSheet(); showAlertMessage("Bootstrapping cancelled") }

    fun clearState() = updateState { State(clientId = clientId, secret = secret) }

    override fun onCleared() {
        super.onCleared()
        activityRef?.clear()
        activityRef = null
        onStateChangeCallback = null
    }
}
