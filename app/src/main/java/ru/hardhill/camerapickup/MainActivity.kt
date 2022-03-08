package ru.hardhill.camerapickup

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import ru.hardhill.camerapickup.ui.theme.CameraPickUpTheme


class MainActivity : ComponentActivity() {

    var isCameraSelected = false
    var imageUri: Uri? = null
    var bitmap: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CameraPickUpTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    color = MaterialTheme.colors.background
                ) {
                    Scaffold(topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "Capture Image / From gallery",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                )
                            },
                        )
                    }) {
                        TakePictureWidget(bitmap)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    private fun TakePictureWidget(bitmap: Bitmap?) {
        val context = LocalContext.current
        val bottomSheetModalState =
            rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
        val coroutineScope = rememberCoroutineScope()
        val galleryLauncher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
                this.imageUri = uri
                this.bitmap = null
            }
        val cameraLauncher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicturePreview()) { bmp: Bitmap? ->
                this.bitmap = bmp
                this.imageUri = null
            }
        val permissionLauncher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    if (isCameraSelected) {
                        cameraLauncher.launch()
                    } else {
                        galleryLauncher.launch("image/*")
                    }
                    coroutineScope.launch {
                        bottomSheetModalState.hide()
                    }
                } else {
                    Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        ModalBottomSheetLayout(
            sheetContent = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(MaterialTheme.colors.primary.copy(0.08f))
                ) {
                    Column(
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Add Photo!",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(15.dp),
                            color = MaterialTheme.colors.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                        Divider(
                            modifier = Modifier
                                .height(1.dp)
                                .background(color = MaterialTheme.colors.primary)
                        )
                        Text(
                            text = "Take Photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(15.dp)
                                .clickable {
                                    when (PackageManager.PERMISSION_GRANTED) {
                                        ContextCompat.checkSelfPermission(
                                            context,
                                            android.Manifest.permission.CAMERA
                                        ) -> {
                                            cameraLauncher.launch()
                                            coroutineScope.launch {
                                                bottomSheetModalState.hide()
                                            }
                                        }
                                        else -> {
                                            isCameraSelected = true
                                            permissionLauncher.launch(android.Manifest.permission.CAMERA)
                                        }
                                    }
                                },
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                        Divider(
                            modifier = Modifier
                                .height(0.5.dp)
                                .background(color = Color.LightGray)
                        )
                        Text(
                            text = "Choose from gallery",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(15.dp)
                                .clickable {
                                    when (PackageManager.PERMISSION_GRANTED) {
                                        ContextCompat.checkSelfPermission(
                                            context,
                                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                                        ) -> {
                                            galleryLauncher.launch("image/*")
                                            coroutineScope.launch {
                                                bottomSheetModalState.hide()
                                            }
                                        }
                                        else -> {
                                            isCameraSelected = false
                                            permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                                        }
                                    }
                                },
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                        Divider(
                            modifier = Modifier
                                .height(0.5.dp)
                                .background(color = Color.LightGray)
                        )
                        Text(text = "Cancel",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        bottomSheetModalState.hide()
                                    }
                                }
                                .padding(15.dp),
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.SansSerif)

                    }
                }
            },
            sheetState = bottomSheetModalState,
            sheetShape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            modifier = Modifier.background(color = MaterialTheme.colors.background)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (!bottomSheetModalState.isVisible) {
                                bottomSheetModalState.show()
                            } else {
                                bottomSheetModalState.hide()
                            }
                        }
                    },
                    modifier = Modifier.padding(16.dp), shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = "Take picture",
                        modifier = Modifier.padding(8.dp),
                        textAlign = TextAlign.Center,
                        color = Color.White,
                    )
                }
            }
        }
        imageUri?.let {
            if(!isCameraSelected){
                val source = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.createSource(context.contentResolver,it)
                } else {
                    TODO("VERSION.SDK_INT < P")
                }
                this.bitmap = ImageDecoder.decodeBitmap(source)

            }
            this.bitmap?.let{
                bmp ->
                Image(
                    bitmap= bmp.asImageBitmap(),
                    contentDescription = "Image",
                    alignment = Alignment.TopCenter,
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(0.45f).padding(top = 10.dp),
                    contentScale = ContentScale.Fit,
                )
            }
        }
        this.bitmap?.let{
                bmp ->
            Image(
                bitmap= bmp.asImageBitmap(),
                contentDescription = "Image",
                alignment = Alignment.TopCenter,
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.45f).padding(top = 10.dp),
                contentScale = ContentScale.Fit,
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            setContent{
                CameraPickUpTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        color = MaterialTheme.colors.background
                    ) {
                        Scaffold(topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = "Capture Image / From gallery",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                    )
                                },
                            )
                        }) {
                            TakePictureWidget(bitmap)
                        }
                    }
                }
            }
        }
    }
}
