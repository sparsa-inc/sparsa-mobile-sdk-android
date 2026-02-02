package com.sparsa.android.views.chooser

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sparsa.android.main.helpers.colors.mainColor

@Composable
fun Modifier.conditional(
    condition: Boolean,
    ifTrue: @Composable Modifier.() -> Modifier,
): Modifier {
    return if (condition) {
        then(ifTrue(Modifier))
    } else {
        this
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChooserView(
    contentList: List<Pair<String, String>>,
    onSelect: (String?) -> Unit
) {

    val state = rememberModalBottomSheetState(
        confirmValueChange = { currentValue ->
            currentValue != SheetValue.Hidden
        }
    )

    ModalBottomSheet(
        onDismissRequest = {
            onSelect(null)
        },
        dragHandle = {},
        containerColor = Color.White,
        sheetState = state
    ) {


        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    onClick = {
                        onSelect(null)
                    }
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.Gray)
                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                userScrollEnabled = true
            ) {
                items(contentList) {
                    ChooserItemView(
                        item = it,
                        onSelect = { selectedItem ->
                            onSelect(selectedItem)
                        }
                    )
                }
            }
        }
    }
}

@Composable
internal fun ChooserItemView(item: Pair<String, String>, onSelect: (String) -> Unit) {
    val lines = item.second.split("\n")
    val title = lines.firstOrNull() ?: ""
    val details = lines.drop(1)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(item.first) },
        border = BorderStroke(1.dp, mainColor().copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = mainColor().copy(alpha = 0.05f),
            contentColor = Color.Black
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                details.forEach { detail ->
                    Text(
                        text = detail,
                        color = Color.DarkGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = mainColor()
            )
        }
    }
}