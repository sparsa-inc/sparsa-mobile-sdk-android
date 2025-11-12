package com.sparsa.android.ui.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sparsa.android.ui.colors.mainColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ConfigurationInputView(
    viewModel: ContentViewModel,
    onSubmit: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { currentValue ->
            currentValue != SheetValue.Hidden
        }
    )

    val state by viewModel.state.collectAsState()

    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
        dragHandle = {},
        containerColor = Color.White,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = screenHeight * 0.9f)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    onClick = {
                        onDismiss()
                    }
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.Black)
                }
            }

            Text(
                text = "Configure SparsaMobile",
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = state.clientId,
                onValueChange = { viewModel.setState(state.copy(clientId = it)) },
                label = { Text("Client ID") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = mainColor(),
                    focusedLabelColor = mainColor(),
                    cursorColor = mainColor()
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Text
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = state.secret,
                onValueChange = { viewModel.setState(state.copy(secret = it)) },
                label = { Text("Client Secret") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = mainColor(),
                    focusedLabelColor = mainColor(),
                    cursorColor = mainColor()
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Text
                ),
                singleLine = true
            )

            Button(
                onClick = {
                    if (state.clientId.isNotEmpty() && state.secret.isNotEmpty()) {
                        onSubmit(state.clientId, state.secret)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = mainColor()),
                enabled = state.clientId.isNotEmpty() && state.secret.isNotEmpty()
            ) {
                Text("Configure")
            }
        }
    }
}
