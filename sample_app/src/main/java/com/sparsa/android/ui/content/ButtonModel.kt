package com.sparsa.android.ui.content

enum class SparsaButton(val title: String) {
    AuthUser("Recover Digital Address"),
    RegUser("Import Digital Address"),
    ProofProcess("Proof Process"),
    DeleteDevice("Delete Device"),
    GetCredentials("Get Credentials"),
    GetCredentialDetails("Get Credential Details"),
    GetDevices("Get Devices"),
    GetDeviceDetails("Get Device Details"),
    GetLanguage("Get Language"),
    SetLanguage("Set Language"),
    SendRecoveryEmail("Send Recovery Email"),
    SetRecoveryEmail("Set Recovery Email"),
    DeviceBootstrappingVerification("Device Bootstrapping Verification"),
    CheckBootstrappingStatus("Check Bootstrapping Status"),
    StartCredentialVerificationProcess("Start Credential Verification"),
    AcceptProof("Accept Proof"),
    RejectProof("Reject Proof");
}

data class SparsaButtonItem(
    val item: SparsaButton,
    var disabled: Boolean = true,
    var hidden: Boolean = false,
    val action: suspend () -> Unit
)

data class ButtonsGroup(
    val name: String,
    val buttons: List<SparsaButtonItem>
)
