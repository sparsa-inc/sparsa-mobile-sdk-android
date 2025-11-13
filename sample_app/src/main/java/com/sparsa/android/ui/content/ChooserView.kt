package com.sparsa.android.ui.content

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.sparsa.android.ui.colors.mainColor

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

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    val state = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
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
                .heightIn(min = 200.dp, max = screenHeight * 0.9f),
        ) {
            Row(
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    onClick = {
                        onSelect(null)
                    }
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.Black)
                }
            }
            LazyColumn(
                Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable {
                onSelect(item.first)
            },
        border = BorderStroke(1.dp, mainColor()),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color.White
        ),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Text(
                item.second,
                color = mainColor(),
                fontSize = TextUnit(20f, TextUnitType.Sp),
            )
            Icon(Icons.Default.ChevronRight, null, tint = mainColor())
        }

    }
}