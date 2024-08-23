package com.example.tensorflowmodels.ui.features

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tensorflowmodels.ml.Faces
import com.example.tensorflowmodels.ml.FlowerModel
import com.example.tensorflowmodels.ml.ModelFruits
import com.example.tensorflowmodels.ml.ModelMobilenetv2
import com.example.tensorflowmodels.ui.common.UiState
import com.example.tensorflowmodels.util.toBitmap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject

private const val IMAGE_AUTO = 224
private const val IMAGE_SIZE_FRUIT = 224
private const val IMAGE_SIZE_FACE = 128

@HiltViewModel
class FaceViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(FaceState())
    val state = _uiState.asStateFlow()

    fun runModels(image: Uri) {
        _uiState.value = FaceState(state = UiState.Loading)
        viewModelScope.launch {
            flow {
                val result = ResultList(
                    listFruit = runFruitModel(image),
                    listAuto = runAutoModel(image),
                    listFlower = runFlowerModel(image),
                    listFace = runFaceModel(image),
                )
                emit(result)
            }.collect { result ->
                _uiState.update {
                    it.copy(
                        image = image,
                        listFruit = result.listFruit,
                        listFace = result.listFace,
                        listAuto = result.listAuto,
                        listFlower = result.listFlower,
                        state = UiState.Success
                    )
                }
            }

        }
    }

    fun removeImage() {
        _uiState.update {
            it.copy(
                image = null,
                listFruit = emptyList(),
                listFace = emptyList(),
                listAuto = emptyList(),
                listFlower = emptyList(),
                state = UiState.Success
            )
        }
    }

    private suspend fun runFruitModel(image: Uri): List<String> {
        return withContext(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            val model = ModelFruits.newInstance(context)
            val bitmap = context.toBitmap(image)
            // Creates inputs for reference.
            val inputFeature0 = TensorBuffer.createFixedSize(
                intArrayOf(1, IMAGE_SIZE_FRUIT, IMAGE_SIZE_FRUIT, 3),
                DataType.FLOAT32
            )
            val byteBuffer = preprocessImage(bitmap, IMAGE_SIZE_FRUIT)
            inputFeature0.loadBuffer(byteBuffer)

            // Runs model inference and gets result.
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer

            // Get confidence
            val confidences = outputFeature0.floatArray

            val confidenceList = confidences.mapIndexed { index, confidence ->
                Pair(listFruits[index], confidence)
            }.sortedByDescending {
                it.second
            }.mapIndexed { index, pair ->
                String.format("%s %.2f%%", pair.first, (pair.second * 100))
            }

            // Releases model resources if no longer used.
            model.close()
            return@withContext confidenceList
        }
    }

    private suspend fun runAutoModel(image: Uri): List<String> {
        return withContext(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            val model = ModelMobilenetv2.newInstance(context)
            val bitmap = context.toBitmap(image)
            // Creates inputs for reference.
            val inputFeature0 =
                TensorBuffer.createFixedSize(
                    intArrayOf(1, IMAGE_AUTO, IMAGE_AUTO, 3),
                    DataType.FLOAT32
                )

            val byteBuffer = preprocessImage(bitmap, IMAGE_AUTO)
            inputFeature0.loadBuffer(byteBuffer)

            // Runs model inference and gets result.
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer

            // Get confidence
            val confidences = outputFeature0.floatArray

            val confidenceList = confidences.mapIndexed { index, confidence ->
                Pair(listAuto[index], confidence)
            }.sortedByDescending {
                it.second
            }.mapIndexed { index, pair ->
                String.format("%s %.2f%%", pair.first, (pair.second * 100))
            }

            // Releases model resources if no longer used.
            model.close()
            return@withContext confidenceList
        }
    }

    private suspend fun runFlowerModel(image: Uri): List<String> {
        return withContext(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            val model = FlowerModel.newInstance(context)
            val bitmap = context.toBitmap(image)

            // Creates inputs for reference.
            val tensorImage = TensorImage.fromBitmap(bitmap)

            // Runs model inference and gets result.
            val outputs = model.process(tensorImage)
            val probability = outputs.probabilityAsCategoryList

            val confidenceList = probability.sortedByDescending {
                it.score
            }.mapIndexed { index, category ->
                String.format("%s %.2f%%", category.label, (category.score * 100))
            }

            // Releases model resources if no longer used.
            model.close()
            return@withContext confidenceList
        }
    }

    private suspend fun runFaceModel(image: Uri): List<String> {
        return withContext(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            val model = Faces.newInstance(context)
            val bitmap = context.toBitmap(image)

            // Creates inputs for reference.
            val inputFeature0 = TensorBuffer.createFixedSize(
                intArrayOf(1, IMAGE_SIZE_FACE, IMAGE_SIZE_FACE, 3),
                DataType.FLOAT32
            )
            val byteBuffer = preprocessImage(bitmap, IMAGE_SIZE_FACE)
            inputFeature0.loadBuffer(byteBuffer)
            inputFeature0.loadBuffer(byteBuffer)

            // Runs model inference and gets result.
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            val outputFeature1 = outputs.outputFeature1AsTensorBuffer

            val confidences0 = outputFeature0.floatArray
            val confidences1 = outputFeature1.floatArray

            val confidenceList0 = confidences0.mapIndexed { index, confidence ->
                Pair("index $index", confidence)
            }

            val confidenceList1 = confidences1.mapIndexed { index, confidence ->
                Pair("index $index", confidence)
            }

            val listFace = buildList {
                println("confidenceList0 ${confidenceList0.size}")
                println("confidenceList1 ${confidenceList1.size}")
                addAll(confidenceList0)
                addAll(confidenceList1)
            }.sortedByDescending {
                it.second
            }.mapIndexed { index, pair ->
                String.format("%s %.2f%%", pair.first, pair.second)
            }
            println("listFace ${listFace.size}")
            // Releases model resources if no longer used.
            model.close()
            return@withContext listFace
        }
    }

    private fun preprocessImage(bitmap: Bitmap, imageSize: Int): ByteBuffer {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, false)

        val inputImageBuffer = ByteBuffer.allocateDirect(1 * imageSize * imageSize * 3 * 4).apply {
            order(ByteOrder.nativeOrder())
        }

        // Preprocess the image data from 0-255 int to 0.0-1.0 float
        for (y in 0 until imageSize) {
            for (x in 0 until imageSize) {
                val pixelValue = scaledBitmap.getPixel(x, y)
                inputImageBuffer.putFloat(((pixelValue shr 16 and 0xFF) / 255.0f))
                inputImageBuffer.putFloat(((pixelValue shr 8 and 0xFF) / 255.0f))
                inputImageBuffer.putFloat(((pixelValue and 0xFF) / 255.0f))
            }
        }
        return inputImageBuffer
    }
}

data class FaceState(
    val image: Uri? = null,
    val listFruit: List<String> = emptyList(),
    val listFlower: List<String> = emptyList(),
    val listFace: List<String> = emptyList(),
    val listAuto: List<String> = emptyList(),
    val state: UiState = UiState.Success
)

data class ResultList(
    val listFruit: List<String> = emptyList(),
    val listFlower: List<String> = emptyList(),
    val listFace: List<String> = emptyList(),
    val listAuto: List<String> = emptyList(),
)

private val listFruits = listOf(
    "apple",
    "banana",
    "beetroot",
    "bell pepper",
    "cabbage",
    "capsicum",
    "carrot",
    "cauliflower",
    "chilli pepper",
    "corn",
    "cucumber",
    "eggplant",
    "garlic",
    "ginger",
    "grapes",
    "jalepeno",
    "kiwi",
    "lemon",
    "lettuce",
    "mango",
    "onion",
    "orange",
    "paprika",
    "pear",
    "peas",
    "pineapple",
    "pomegranate",
    "potato",
    "raddish",
    "soy beans",
    "spinach",
    "sweetcorn",
    "sweetpotato",
    "tomato",
    "turnip",
    "watermelon"
)

private val listAuto = listOf(
    "Air compressor",
    "Alternator",
    "Batteries",
    "Brake Rotor",
    "Brake pad",
    "Distributor",
    "Fuel Injector",
    "Fuse box",
    "Ignition coil",
    "Muffler",
    "Oil Filter",
    "Piston",
    "Spark plug",
    "Transmission",
    "Windshield wiper blades"
)