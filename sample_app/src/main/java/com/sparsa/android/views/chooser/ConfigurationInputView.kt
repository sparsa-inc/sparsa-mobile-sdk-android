package com.sparsa.android.views.chooser

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sparsa.android.main.helpers.colors.mainColor
import com.sparsa.android.main.ContentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ConfigurationInputView(
    viewModel: ContentViewModel,
    onSubmit: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
        confirmValueChange = { currentValue ->
            currentValue != SheetValue.Hidden
        }
    )

    val state by viewModel.state.collectAsState()

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        dragHandle = null,
        containerColor = Color.White,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    onClick = { onDismiss() }
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.Black)
                }
            }

            Text(
                text = "Configure Sparsa",
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = state.clientId,
                onValueChange = { viewModel.setClientId(it) },
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
                onValueChange = { viewModel.setSecret(it) },
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

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (state.clientId.isNotEmpty() && state.secret.isNotEmpty()) {
                        onSubmit(state.clientId, state.secret)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = mainColor(),
                    disabledContainerColor = mainColor().copy(alpha = 0.5f)
                ),
                enabled = state.clientId.isNotEmpty() && state.secret.isNotEmpty()
            ) {
                Text("Configure", color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
