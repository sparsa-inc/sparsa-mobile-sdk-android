package com.sparsa.android.main

import android.annotation.SuppressLint
import android.content.ClipData
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.sparsa.android.views.bootstrapping.BootstrappingView
import com.sparsa.android.views.chooser.ChooserView
import com.sparsa.android.views.chooser.CredentialsFilterView
import com.sparsa.android.views.chooser.conditional
import com.sparsa.android.main.helpers.colors.mainColor
import com.sparsa.android.views.qrScanner.QRScannerView
import com.sparsa.android.views.chooser.ConfigurationInputView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun ContentView(viewModel: ContentViewModel) {

    val uiState by viewModel.uiState.collectAsState()
    val state by viewModel.state.collectAsState()

    val expandedSections = remember { mutableStateMapOf<String, Boolean>() }
    val grayColor = Color(0xFFF2F3F4)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    if (uiState.showAlert) {
        AlertDialog(
            containerColor = Color.White,
            titleContentColor = mainColor(),
            onDismissRequest = { viewModel.hideAlert() },
            confirmButton = {
                TextButton(onClick = { viewModel.hideAlert() }) {
                    Text("OK", color = mainColor())
                }
            },
            title = { Text("Alert") },
            text = {
                Box(
                    modifier = Modifier
                        .conditional(uiState.alertMessage.startsWith("{")) {
                            Modifier.horizontalScroll(rememberScrollState())
                        }
                        .verticalScroll(rememberScrollState())
                ) {
                    Column {
                        SelectionContainer {
                            Text(uiState.alertMessage)
                        }
                    }
                }
            }
        )
    }

    RecoveryEmailDialog(
        showDialog = uiState.showInput,
        email = state.input,
        onEmailChange = viewModel::setInput,
        onContinue = { viewModel.hideInput() },
        onCancel = {
            viewModel.setInput("")
            viewModel.hideInput()
        }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarColors(
                    Color.White,
                    Color.White,
                    Color.White,
                    Color.Black,
                    Color.White,
                    Color.Transparent
                ),
                actions = {
                    Row(
                        modifier = Modifier.padding(end = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        Button(
                            modifier = Modifier.width(30.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = mainColor()),
                            onClick = { viewModel.reconfigure() }
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reconfigure SDK")
                        }
                        Button(
                            modifier = Modifier.width(30.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = mainColor()),
                            onClick = { viewModel.showQr() }
                        ) {
                            Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "Scan QR")
                        }
                    }
                },
                title = { Text("Sparsa Sample App", maxLines = 1) }
            )
        },
        content = { padding ->
            if (uiState.showQrScanner) {
                QRScannerView(onQrCodeScanned = { result -> viewModel.scanQRResult(result) })
            } else {
                Box {
                    if (uiState.requesting) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.6f))
                                .fillMaxSize()
                                .zIndex(2f)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = { }
                                ),
                        ) {
                            CircularProgressIndicator(color = Color.White)
                            Spacer(Modifier.height(10.dp))
                            Text("Requesting...", color = Color.White)
                        }
                    }
                    val clipboardManager = LocalClipboard.current

                    Column {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(0.dp),
                            contentPadding = padding,
                            modifier = Modifier.background(grayColor)
                        ) {
                            if (state.digitalAddress.isNotEmpty()) {
                                item {
                                    DigitalAddressCard(
                                        digitalAddress = state.digitalAddress,
                                        onCopy = {
                                            CoroutineScope(Dispatchers.IO).launch {
                                                clipboardManager.setClipEntry(ClipEntry(ClipData.newPlainText("da", state.digitalAddress)))
                                            }
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = "Digital address copied",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                            uiState.groups.forEach { group ->
                                val isExpanded = expandedSections.getOrDefault(group.name, true)

                                stickyHeader {
                                    Column {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { expandedSections[group.name] = !isExpanded }
                                                .background(Color.White)
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = group.name,
                                                modifier = Modifier.padding(16.dp),
                                                color = mainColor()
                                            )
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = "Expand/Collapse",
                                                tint = mainColor()
                                            )
                                        }
                                        HorizontalDivider(thickness = 0.5.dp, color = mainColor())
                                    }
                                }
                                if (isExpanded) {
                                    items(group.buttons.filter { !it.hidden }) { button ->
                                        Button(
                                            onClick = { CoroutineScope(Dispatchers.IO).launch { button.action() } },
                                            enabled = !button.disabled && !uiState.requesting,
                                            elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 5.dp, disabledElevation = 1.dp),
                                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                                            border = BorderStroke(0.5.dp, if (button.disabled) Color.Gray else mainColor()),
                                            colors = ButtonDefaults.buttonColors(
                                                disabledContainerColor = Color.White,
                                                containerColor = Color.White,
                                                disabledContentColor = Color.Gray,
                                                contentColor = mainColor()
                                            )
                                        ) {
                                            Text(button.item.title, Modifier.fillMaxWidth(), textAlign = TextAlign.Left)
                                        }
                                    }
                                }
                            }
                        }
                        Box(modifier = Modifier.background(grayColor).fillMaxSize())
                    }

                    if (uiState.showBottomSheet) {
                        ChooserView(
                            uiState.chooserList,
                            onSelect = { selectedItem ->
                                selectedItem?.let {
                                    viewModel.selectItem(it) } ?: viewModel.hideBottomSheet()
                            }
                        )
                    }

                    if (uiState.showConfigureSheet) {
                        ConfigurationInputView(
                            viewModel = viewModel,
                            onSubmit = { clientId, clientSecret ->
                                viewModel.submitConfiguration(clientId, clientSecret)
                            },
                            onDismiss = {
                                viewModel.hideConfigBottomSheet()
                            }
                        )
                    }

                    if (uiState.showFilterSheet) {
                        CredentialsFilterView(
                            credentials = uiState.fetchedCredentialsForFilter,
                            onDismiss = { viewModel.hideFilterSheet() },
                            onApply = { statuses, schemaIds -> viewModel.applyFilter(statuses, schemaIds) }
                        )
                    }

                    if (uiState.showBootstrappingSheet && uiState.bootstrappingQrData != null) {
                        ModalBottomSheet(
                            onDismissRequest = { viewModel.onBootstrappingCancelled() },
                            containerColor = Color.White
                        ) {
                            BootstrappingView(
                                qrData = uiState.bootstrappingQrData!!,
                                onTimeout = { viewModel.onBootstrappingTimeout() }
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
internal fun RecoveryEmailDialog(
    showDialog: Boolean,
    email: String,
    onEmailChange: (String) -> Unit,
    onContinue: () -> Unit,
    onCancel: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onCancel,
            containerColor = Color.White,
            textContentColor = mainColor(),
            title = { Text("Input Field", color = mainColor()) },
            text = {
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = mainColor(),
                        focusedLabelColor = mainColor(),
                        cursorColor = mainColor()
                    ),
                    label = { Text("Input") },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Email
                    ),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onContinue,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = mainColor())
                ) { Text("Continue") }
            },
            dismissButton = {
                TextButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = mainColor())
                ) { Text("Cancel") }
            }
        )
    }
}

@Composable
internal fun DigitalAddressCard(
    digitalAddress: String,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, mainColor())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Digital Address",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = digitalAddress,
                    fontSize = 16.sp,
                    color = mainColor()
                )
            }
            IconButton(onClick = onCopy) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = mainColor()
                )
            }
        }
    }
}
