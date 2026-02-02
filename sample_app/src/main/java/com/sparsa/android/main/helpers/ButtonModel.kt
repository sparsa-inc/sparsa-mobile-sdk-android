package com.sparsa.android.main.helpers

enum class SparsaButton(val title: String) {
    AuthUser("Recover Digital Address"),
    RegUser("Import Digital Address"),
    UpdateDigitalAddress("Update Digital Address"),
    GetDigitalAddress("Get Digital Address"),
    ProofProcess("Proof Process"),
    DeleteDevice("Delete Device"),
    GetCredentials("Get Credentials"),
    GetCredentialDetails("Get Credential Details"),
    GetDevices("Get Devices"),
    GetLanguage("Get Language"),
    SetLanguage("Set Language"),
    SendRecoveryEmail("Send Recovery Email"),
    SetRecoveryEmail("Set Recovery Email"),
    DeviceBootstrappingVerification("Use Registered Device"),
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
