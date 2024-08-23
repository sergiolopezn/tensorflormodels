package com.example.tensorflowmodels.ui.features

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.tensorflowmodels.R
import com.example.tensorflowmodels.ui.common.UiState
import com.example.tensorflowmodels.ui.custom.CustomLoading

@Composable
fun FaceRoute(viewModel: FaceViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    FaceScreen(
        state = state,
        onAddImageEvent = {
            viewModel.runModels(it)
        },
        onRemoveImageEvent = {
            viewModel.removeImage()
        }
    )
}

@Composable
fun FaceScreen(
    state: FaceState,
    onAddImageEvent: (Uri) -> Unit,
    onRemoveImageEvent: () -> Unit,
) {
    when (state.state) {
        is UiState.Error -> {
            state.state.error?.printStackTrace()
        }
        UiState.Loading -> {
            CustomLoading()
        }
        UiState.Success -> {
            FaceContent(
                image = state.image,
                listFruit = state.listFruit,
                listFlower = state.listFlower,
                listFace = state.listFace,
                listAuto = state.listAuto,
                onAddImageEvent = onAddImageEvent,
                onRemoveImageEvent = onRemoveImageEvent
            )
        }
    }
}

@Composable
fun FaceContent(
    onAddImageEvent: (Uri) -> Unit,
    onRemoveImageEvent: () -> Unit,
    image: Uri? = null,
    listFruit: List<String>,
    listFlower: List<String>,
    listFace: List<String>,
    listAuto: List<String>,
) {
    var showGalleryBottomSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(20.dp)
                .align(Alignment.CenterHorizontally)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(18.dp)
                ),
        ) {
            if (image == null) {
                Image(
                    painterResource(R.drawable.add_image),
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.Center)
                        .clickable {
                            showGalleryBottomSheet = true
                        }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = MaterialTheme.colorScheme.secondary,
                            shape = RoundedCornerShape(18.dp)
                        ),
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(image),
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .fillMaxSize()
                    )
                    Image(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clickable {
                                onRemoveImageEvent()
                            }
                            .padding(10.dp),
                        imageVector = Icons.Default.Close,
                        colorFilter = ColorFilter.tint(Color.Red),
                        contentDescription = null
                    )
                }
            }
        }
        TabsModel(listFruit, listFlower, listFace, listAuto)
    }
    if (showGalleryBottomSheet) {
        GalleryBottomSheet(
            onDismissRequest = {
                showGalleryBottomSheet = false
            },
            onDismissSheet = { uri ->
                onAddImageEvent(uri)
                showGalleryBottomSheet = false
            },
        )
    }
}

@Composable
private fun TabsModel(
    listFruit: List<String>,
    listFlower: List<String>,
    listFace: List<String>,
    listAuto: List<String>,
) {
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Fruits", "Flowers", "Auto part")
    Column(modifier = Modifier.fillMaxWidth()) {
        TabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(text = { Text(title) },
                    selected = tabIndex == index,
                    onClick = { tabIndex = index }
                )
            }
        }
        when (tabIndex) {
            0 -> ConfidenceList(listFruit)
            1 -> ConfidenceList(listFlower)
            2 -> ConfidenceList(listAuto)
        }
    }

}

@Composable
fun ConfidenceList(confidenceList: List<String>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        items(confidenceList) { item ->
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = item,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge
                )
                HorizontalDivider(modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun FaceScreenPreview() {
    FaceContent(
        onAddImageEvent = {},
        onRemoveImageEvent = {},
        listFace = emptyList(),
        listAuto = emptyList(),
        listFlower = emptyList(),
        listFruit = listOf(
            "Apple: 65.78",
            "Kiwi: 65.78",
            "Watermelon: 65.78",
            "Blueberry: 65.78"
        )
    )
}