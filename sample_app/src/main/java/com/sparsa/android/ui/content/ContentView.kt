package com.sparsa.android.ui.content

import QRScannerView
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.sparsa.android.ui.colors.mainColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

@Composable
internal fun ContentView(viewModel: ContentViewModel) {

    val groups by viewModel.groups.collectAsState()
    val showAlert by viewModel.showAlert.collectAsState()
    val showEmailDialog by viewModel.showEmailDialog.collectAsState()
    val state by viewModel.state.collectAsState()
    val showBottomSheet by viewModel.isBottomSheetVisible.collectAsState()
    val showConfigBottomSheet by viewModel.isConfigBottomSheetVisible.collectAsState()
    val showFilterSheet by viewModel.showFilterSheet.collectAsState()
    val fetchedCredentialsForFilter by viewModel.fetchedCredentialsForFilter.collectAsState()
    val alertMessage by viewModel.alertMessage.collectAsState()
    val requesting by viewModel.requesting.collectAsState()
    val showQrScanner by viewModel.showQr.collectAsState()

    val expandedSections = remember { mutableStateMapOf<String, Boolean>() }
    val grayColor = Color(0xFFF2F3F4)

    if (showAlert) {
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
                        .conditional(alertMessage.startsWith("{")) {
                                Modifier
                                    .horizontalScroll(rememberScrollState())
                            }
                        .verticalScroll(rememberScrollState())
                ) {
                    Column {
                        SelectionContainer {
                            Text(alertMessage)
                        }
                    }
                }
            }
        )
    }


    RecoveryEmailDialog(
        showDialog = showEmailDialog,
        email = state.email,
        onEmailChange = viewModel::setEmail,
        onContinue = { viewModel.hideEmailInput() },
        onCancel = {
            viewModel.setEmail("")
            viewModel.hideEmailInput()
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarColors(
                    Color.White,
                    Color.White,
                    Color.White,
                    Color.Black,
                    Color.White
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
                            onClick = {
                                viewModel.reconfigure()
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reconfigure SDK")
                        }
                        Button(
                            modifier = Modifier.width(30.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = mainColor()),
                            onClick = {
                                viewModel.showQr()
                            }
                        ) {
                            Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "Scan QR")
                        }
                    }
                },
                title = { Text("Sparsa Sample App", maxLines = 1) }
            )
        },
        content = { padding ->

            if (showQrScanner) {
                QRScannerView(
                    onQrCodeScanned = { result ->
                        viewModel.scanQRResult(result)
                    }
                )
            } else {
                Box {
                    if (requesting) {
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
                    Column {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(0.dp),
                            contentPadding = padding,
                            modifier = Modifier
                                .background(grayColor)
                        ) {
                            groups.forEach { group ->
                                val isExpanded = expandedSections.getOrDefault(group.name, true)

                                stickyHeader {
                                    Column {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    expandedSections[group.name] = !isExpanded
                                                }
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
                                            enabled = !button.disabled && !requesting,
                                            elevation = ButtonDefaults.elevatedButtonElevation(
                                                defaultElevation = 5.dp,
                                                disabledElevation = 1.dp
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            border = BorderStroke(
                                                0.5.dp,
                                                if (button.disabled) Color.Gray else mainColor()
                                            ),
                                            colors = ButtonDefaults.buttonColors(
                                                disabledContainerColor = Color.White,
                                                containerColor = Color.White,
                                                disabledContentColor = Color.Gray,
                                                contentColor = mainColor()
                                            )
                                        ) {
                                            Text(
                                                button.item.title,
                                                Modifier.fillMaxWidth(),
                                                textAlign = TextAlign.Left
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Box(modifier = Modifier
                            .background(grayColor)
                            .fillMaxSize())
                    }
                    if (showBottomSheet) {
                        ChooserView(
                            viewModel.chooserList.value,
                            onSelect = { selectedItem ->
                                selectedItem?.let {
                                    viewModel.selectItem(it)
                                } ?: viewModel.hideBottomSheet()
                        })
                    }

                    if (showConfigBottomSheet) {
                        ConfigurationInputView(
                            viewModel = viewModel,
                            onSubmit = { clientId, clientSecret ->
                                viewModel.setSubmitted(true)
                                viewModel.submitConfiguration(clientId, clientSecret)
                            },
                            onDismiss = {
                                viewModel.setSubmitted(false)
                                viewModel.hideConfigBottomSheet()
                            }
                        )
                    }

                    if (showFilterSheet) {
                        CredentialsFilterView(
                            credentials = fetchedCredentialsForFilter,
                            onDismiss = { viewModel.hideFilterSheet() },
                            onApply = { statuses, schemaIds ->
                                viewModel.applyFilter(statuses, schemaIds)
                            }
                        )
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
            title = { Text("Recovery Email", color = mainColor()) },
            text = {
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = mainColor(),
                        focusedLabelColor = mainColor(),
                        cursorColor = mainColor()
                    ),
                    label = { Text("Email") },
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = mainColor()
                    )
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = mainColor()
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
