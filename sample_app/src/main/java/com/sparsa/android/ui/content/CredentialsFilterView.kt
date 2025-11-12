package com.sparsa.android.ui.content

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.sparsa.android.ui.colors.mainColor

/**
 * A Compose view for filtering credentials by status and schema type.
 *
 * @param credentials List of available credentials to extract filter options from
 * @param onDismiss Callback invoked when the filter view is dismissed
 * @param onApply Callback invoked when filters are applied, receives selected statuses and schema identifiers
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialsFilterView(
    credentials: List<data.model.external.Credential>,
    onDismiss: () -> Unit,
    onApply: (statuses: Set<String>, schemaIds: Set<String>) -> Unit
) {
    val all = "All"
    val statuses = listOf("All", "Active", "Revoked")
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    var selectedStatuses by remember { mutableStateOf(setOf("All")) }
    var selectedSchemaIds by remember { mutableStateOf(setOf("All")) }

    // Extract unique schema options from credentials
    val schemaOptions = remember(credentials) {
        val options = mutableListOf("All")
        options.addAll(credentials.map { it.schema }.distinct().sorted())
        options
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { currentValue ->
            currentValue != SheetValue.Hidden
        }
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = {},
        containerColor = Color.White,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = screenHeight * 0.9f)
        ) {
            // Header with close button
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    onClick = onDismiss
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.Black)
                }

                Text(
                    text = "Filter Credentials",
                    color = mainColor(),
                    fontSize = TextUnit(20f, TextUnitType.Sp),
                    fontWeight = FontWeight.Bold
                )

                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    onClick = {
                        val filteredStatuses = selectedStatuses.map { it.lowercase() }.toSet()
                        val filteredSchemaIds = credentials
                            .filter { selectedSchemaIds.contains(it.schema) }
                            .map { it.schemaIdentifier }
                            .toSet()
                        onApply(filteredStatuses, filteredSchemaIds)
                    }
                ) {
                    Icon(Icons.Default.Check, null, tint = mainColor())
                }
            }

            Divider(color = Color.LightGray, thickness = 1.dp)

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Status filter section
                item {
                    Text(
                        text = "Status",
                        color = mainColor(),
                        fontSize = TextUnit(18f, TextUnitType.Sp),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(statuses) { status ->
                    FilterCheckboxItem(
                        label = status,
                        isChecked = selectedStatuses.contains(status),
                        onCheckedChange = { checked ->
                            selectedStatuses = when {
                                checked && status == all -> {
                                    setOf(all)
                                }
                                checked && selectedStatuses.contains(all) -> {
                                    selectedStatuses - all + status
                                }
                                checked -> {
                                    selectedStatuses + status
                                }
                                !checked && status == all -> {
                                    selectedStatuses - all
                                }
                                else -> {
                                    val newSet = selectedStatuses - status
                                    if (newSet.isEmpty()) setOf(all) else newSet
                                }
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Type filter section
                item {
                    Text(
                        text = "Type",
                        color = mainColor(),
                        fontSize = TextUnit(18f, TextUnitType.Sp),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(schemaOptions) { schema ->
                    FilterCheckboxItem(
                        label = schema,
                        isChecked = selectedSchemaIds.contains(schema),
                        onCheckedChange = { checked ->
                            selectedSchemaIds = when {
                                checked && schema == all -> {
                                    setOf(all)
                                }
                                checked && selectedSchemaIds.contains(all) -> {
                                    selectedSchemaIds - all + schema
                                }
                                checked -> {
                                    selectedSchemaIds + schema
                                }
                                !checked && schema == all -> {
                                    selectedSchemaIds - all
                                }
                                else -> {
                                    val newSet = selectedSchemaIds - schema
                                    if (newSet.isEmpty()) setOf(all) else newSet
                                }
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun FilterCheckboxItem(
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) },
        border = BorderStroke(1.dp, if (isChecked) mainColor() else Color.LightGray),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked) mainColor().copy(alpha = 0.1f) else Color.White,
            contentColor = Color.Black
        ),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = label,
                color = if (isChecked) mainColor() else Color.Black,
                fontSize = TextUnit(16f, TextUnitType.Sp),
            )
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = mainColor(),
                    uncheckedColor = Color.Gray
                )
            )
        }
    }
}
