package com.sparsa.android.main.helpers

import com.sparsainc.sdk.sparsa.data.model.external.CredentialItem

data class State(
    var digitalAddress: String = "",
    var qrData: String = "",
    var linkDeviceId: String = "",
    var input: String = "",
    var clientId: String = "",
    var secret: String = ""
)

data class UIState(
    val showInput: Boolean = false,
    val groups: List<ButtonsGroup> = emptyList(),
    val chooserList: List<Pair<String, String>> = emptyList(),
    val selectedItem: String? = null,
    val showBottomSheet: Boolean = false,
    val showFilterSheet: Boolean = false,
    val showAlert: Boolean = false,
    val requesting: Boolean = false,
    val alertMessage: String = "",
    val showConfigureSheet: Boolean = false,
    val showQrScanner: Boolean = false,
    val showBootstrappingSheet: Boolean = false,
    val bootstrappingQrData: String? = null,
    val fetchedCredentialsForFilter: List<CredentialItem> = emptyList(),
    val filterResult: Pair<Set<String>, Set<String>>? = null,
    val submitted: Boolean = false
)