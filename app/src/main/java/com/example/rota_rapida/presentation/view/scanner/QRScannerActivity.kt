package com.example.rota_rapida.presentation.view.scanner

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class QRScannerActivity : ComponentActivity() {

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

        setContent {
            QRScannerScreen(
                onQrCodeDetected = { qrCode ->
                    // Retorna o resultado e fecha a activity
                    val resultIntent = Intent().apply {
                        // Mantém chaves antigas e nova para compatibilidade
                        putExtra("qr_address", qrCode)
                        putExtra("qr_original", qrCode)
                        putExtra("qr_text", qrCode)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                },
                onClose = {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScannerScreen(
    onQrCodeDetected: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isScanning by remember { mutableStateOf(true) }
    var flash by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Launcher de permissão de câmera
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            hasCameraPermission = isGranted
        }
    )

    // Solicita permissão se não tiver
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escanear QR Code") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, "Fechar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.7f),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (hasCameraPermission) {
                // Preview da câmera
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    onQrCodeDetected = { qr ->
                        if (isScanning) {
                            isScanning = false

                            // Vibração curta para feedback
                            val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                val vm = context.getSystemService(VibratorManager::class.java)
                                vm?.defaultVibrator
                            } else {
                                @Suppress("DEPRECATION")
                                context.getSystemService(Vibrator::class.java)
                            }
                            vibrator?.vibrate(
                                VibrationEffect.createOneShot(
                                    80,
                                    VibrationEffect.DEFAULT_AMPLITUDE
                                )
                            )

                            // Flash verde rápido
                            flash = true
                            scope.launch {
                                delay(120)
                                flash = false
                                // chama callback após o feedback visual
                                onQrCodeDetected(qr)
                            }
                        }
                    }
                )

                // Overlay com quadrado de scanning + flash
                ScanningOverlay(flash = flash)

                // Instruções
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = "Posicione o QR Code dentro do quadrado",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                // Sem permissão
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Permissão de câmera necessária",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    ) {
                        Text("Conceder Permissão")
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onQrCodeDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    // Executor dedicado para análise (fora da UI thread)
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) {
        onDispose {
            analysisExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )

    LaunchedEffect(previewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // Image Analysis para QR Code (resolução alvo e executor dedicado)
            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetResolution(android.util.Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(
                        analysisExecutor,
                        QRCodeAnalyzer(onQrCodeDetected)
                    )
                }

            // Câmera traseira
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
            } catch (e: Exception) {
                Log.e("QRScanner", "Erro ao iniciar câmera", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }
}

@Composable
fun ScanningOverlay(flash: Boolean) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val squareSize = canvasWidth * 0.7f
        val left = (canvasWidth - squareSize) / 2
        val top = (canvasHeight - squareSize) / 2

        // Fundo escurecido levemente
        drawRect(Color.Black.copy(alpha = 0.5f))

        // Quadrado de foco (pisca verde quando 'flash' = true)
        drawRect(
            color = if (flash) Color.Green else Color.White,
            topLeft = androidx.compose.ui.geometry.Offset(left, top),
            size = androidx.compose.ui.geometry.Size(squareSize, squareSize),
            style = Stroke(width = 4f)
        )

        // Cantos em verde (fixos)
        val cornerLength = 40f
        val cornerWidth = 8f

        // Canto superior esquerdo
        drawLine(
            color = Color.Green,
            start = androidx.compose.ui.geometry.Offset(left, top),
            end = androidx.compose.ui.geometry.Offset(left + cornerLength, top),
            strokeWidth = cornerWidth
        )
        drawLine(
            color = Color.Green,
            start = androidx.compose.ui.geometry.Offset(left, top),
            end = androidx.compose.ui.geometry.Offset(left, top + cornerLength),
            strokeWidth = cornerWidth
        )

        // Canto superior direito
        drawLine(
            color = Color.Green,
            start = androidx.compose.ui.geometry.Offset(left + squareSize, top),
            end = androidx.compose.ui.geometry.Offset(left + squareSize - cornerLength, top),
            strokeWidth = cornerWidth
        )
        drawLine(
            color = Color.Green,
            start = androidx.compose.ui.geometry.Offset(left + squareSize, top),
            end = androidx.compose.ui.geometry.Offset(left + squareSize, top + cornerLength),
            strokeWidth = cornerWidth
        )

        // Canto inferior esquerdo
        drawLine(
            color = Color.Green,
            start = androidx.compose.ui.geometry.Offset(left, top + squareSize),
            end = androidx.compose.ui.geometry.Offset(left + cornerLength, top + squareSize),
            strokeWidth = cornerWidth
        )
        drawLine(
            color = Color.Green,
            start = androidx.compose.ui.geometry.Offset(left, top + squareSize),
            end = androidx.compose.ui.geometry.Offset(left, top + squareSize - cornerLength),
            strokeWidth = cornerWidth
        )

        // Canto inferior direito
        drawLine(
            color = Color.Green,
            start = androidx.compose.ui.geometry.Offset(left + squareSize, top + squareSize),
            end = androidx.compose.ui.geometry.Offset(left + squareSize - cornerLength, top + squareSize),
            strokeWidth = cornerWidth
        )
        drawLine(
            color = Color.Green,
            start = androidx.compose.ui.geometry.Offset(left + squareSize, top + squareSize),
            end = androidx.compose.ui.geometry.Offset(left + squareSize, top + squareSize - cornerLength),
            strokeWidth = cornerWidth
        )
    }
}

private class QRCodeAnalyzer(
    private val onQrCodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    // Foca apenas em QR Code (mais rápido e menos falso positivo)
    private val scanner by lazy {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        BarcodeScanning.getClient(options)
    }

    private var isProcessing = false
    private var hasEmittedResult = false

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close(); return
        }
        if (isProcessing || hasEmittedResult) {
            imageProxy.close(); return
        }

        isProcessing = true
        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (!hasEmittedResult) {
                    val text = barcodes.firstOrNull()?.rawValue
                    if (!text.isNullOrBlank()) {
                        hasEmittedResult = true
                        onQrCodeDetected(text)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("QRScanner", "Erro ao processar QR Code", e)
            }
            .addOnCompleteListener {
                isProcessing = false
                imageProxy.close()
            }
    }
}
