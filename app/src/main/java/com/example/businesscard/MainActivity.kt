package com.example.businesscard

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.sqz.businesscard.ui.theme.BusinessCardTheme

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BusinessCardTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF121212),
                ) {
                    BiometricAuthenticationScreen()
                }
            }
        }
        //Analytics Event
        val analytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message", "Integración de Firebase completa")
        analytics.logEvent("InitScreen", bundle)
    }
}

@Composable
fun BiometricAuthenticationScreen() {
    val context = LocalContext.current as FragmentActivity
    val biometricManager = BiometricManager.from(context)
    val canAuthenticateWithBiometrics = when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
        BiometricManager.BIOMETRIC_SUCCESS -> true
        else -> {
            Log.e("TAG", "Device does not support strong biometric authentication")
            false
        }
    }

    var isAuthenticated by remember { mutableStateOf(false) }

    if (isAuthenticated) {
        BusinessCard()
    } else {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF121212),
        ) {
            Column(
                modifier = Modifier
                    .background(Color(0xFF121212))
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (canAuthenticateWithBiometrics) {
                    BiometricButton(context) {
                        isAuthenticated = true
                    }
                } else {
                    Text(text = "Biometric authentication is not available on this device")
                }
            }
        }
    }
}

@Composable
fun BiometricButton(context: FragmentActivity, onAuthenticationSuccess: () -> Unit) {
    val logoAtom = painterResource(R.drawable.logo_uas)
    Column(
        modifier = Modifier
            .background(Color(0xFF121212))
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = logoAtom,
            contentDescription = null,
            contentScale = ContentScale.Inside
        )

        Spacer(Modifier.height(38.dp))

        Button(onClick = {
            authenticateWithBiometric(context, onAuthenticationSuccess)
        }) {
            Text("Autenticar")
        }
    }
}

fun authenticateWithBiometric(context: FragmentActivity, onAuthenticationSuccess: () -> Unit) {
    val executor = context.mainExecutor
    val biometricPrompt = BiometricPrompt(
        context,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onAuthenticationSuccess.invoke()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                Log.e("TAG", "onAuthenticationError")
                // TODO: Handle authentication errors.
            }

            override fun onAuthenticationFailed() {
                Log.e("TAG", "onAuthenticationFailed")
                // TODO: Handle authentication failures.
            }
        })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Biometric Authentication")
        .setDescription("Place your finger on the sensor or look at the front camera to authenticate.")
        .setNegativeButtonText("Cancel")
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        .build()

    biometricPrompt.authenticate(promptInfo)
}

@Composable
fun BusinessCard(modifier: Modifier = Modifier) {
    var isFlipped by remember { mutableStateOf(false) }

    // Configuración de animación para el efecto de abrir una puerta
    val rotationY by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 500), label = ""
    )

    Column(
        modifier = modifier
            .clickable { isFlipped = !isFlipped }
            .padding(16.dp)
            .graphicsLayer(
                rotationY = rotationY
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isFlipped) {
            // Mostrar el frente de la tarjeta
            BusinessCardImage()
            BusinessCardText()
        } else {
            // Mostrar la parte trasera de la tarjeta aquí
            BackSideOfCard(rotationY)
        }
    }
}

@Composable
fun BusinessCardText(modifier: Modifier = Modifier) {
    var isMoreInformation by remember { mutableStateOf(false) }

    Text(
        text = stringResource(R.string.Author),
        fontSize = 35.sp,
        modifier = modifier.padding(top = 20.dp)
    )
    Text(
        text = stringResource(R.string.Title),
        modifier = modifier.padding(top = 20.dp),
        fontWeight = FontWeight.Bold,
        color = Color(0xFFEF763B)
    )

    Spacer(modifier = Modifier.height(58.dp))


    Text(
        text = stringResource(R.string.PhoneNumber),
        modifier = modifier.padding(top = 10.dp),
        fontSize = 20.sp
    )

    Text(
        text = stringResource(R.string.Email),
        modifier = modifier.padding(top = 10.dp),
        fontSize = 20.sp
    )

    Spacer(modifier = Modifier.height(58.dp))

    Button(
        onClick = { isMoreInformation = true },
        modifier = Modifier.wrapContentSize(),
    ) {
        Text("More Information")
    }

    // AlertDialog
    if (isMoreInformation) {
        AlertDialog(
            onDismissRequest = { isMoreInformation = false },
            title = {
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Contenido del Popup, incluida la imagen
                    val socialMediaImage = painterResource(R.drawable.vcard)
                    Image(
                        painter = socialMediaImage,
                        contentDescription = null,
                        contentScale = ContentScale.Inside,
                        modifier = Modifier.size(width = 100.dp, height = 100.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            },
            confirmButton = { },
            dismissButton = { }
        )
    }
}

@Composable
fun BackSideOfCard(rotationY: Float) {
    var isLoyaltyCards by remember { mutableStateOf(false) }
    var isCarrefourDialogVisible by remember { mutableStateOf(false) }
    var isBpDialogVisible by remember { mutableStateOf(false) }
    var isSabadellDialogVisible by remember { mutableStateOf(false) }
    var isCertificadoDialogVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Contenido de la parte trasera de la tarjeta
        Text(
            text = stringResource(R.string.Direction),
            color = Color.White,
            fontSize = 20.sp,
            modifier = Modifier
                .graphicsLayer(
                    rotationY = rotationY
                )
        )
        Text(
            text = stringResource(R.string.MoreDirection),
            color = Color.White,
            fontSize = 20.sp,
            modifier = Modifier
                .graphicsLayer(
                    rotationY = rotationY
                )
        )
        Text(
            text = stringResource(R.string.NIF),
            color = Color.White,
            fontSize = 20.sp,
            modifier = Modifier
                .graphicsLayer(
                    rotationY = rotationY
                )
        )
        Text(
            text = stringResource(R.string.OficcePhone),
            color = Color.White,
            fontSize = 20.sp,
            modifier = Modifier
                .graphicsLayer(
                    rotationY = rotationY
                )
        )
        Spacer(modifier = Modifier.height(58.dp))

        Button(
            onClick = { isLoyaltyCards = true },
            modifier = Modifier.wrapContentSize(),
        ) {
            Text(
                "Tarjetas de Fidelización",
                modifier = Modifier.graphicsLayer(rotationY = rotationY)
            )        }

        // AlertDialog principal
        if (isLoyaltyCards) {
            AlertDialog(
                onDismissRequest = { isLoyaltyCards = false },
                title = {
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Botón Carrefour en el AlertDialog principal
                        Button(
                            onClick = { isCarrefourDialogVisible = true },
                            modifier = Modifier.wrapContentSize(),
                        ) {
                            Text("Carrefour"
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Boton tarjeta BP
                        Button(
                            onClick = { isBpDialogVisible = true },
                            modifier = Modifier.wrapContentSize(),
                        ) {
                            Text("BP", color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        //Boton Sabadell
                        Button(
                            onClick = { isSabadellDialogVisible = true },
                            modifier = Modifier.wrapContentSize(),
                        ) {
                            Text("Sabadell")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        //Boton Certificado Piloto
                        Button(
                            onClick = { isCertificadoDialogVisible = true },
                            modifier = Modifier.wrapContentSize(),
                        ) {
                            Text("Certificado Piloto")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                    }
                },
                confirmButton = { },
                dismissButton = { }
            )
        }

        // AlertDialog para el botón Carrefour
        if (isCarrefourDialogVisible) {
            AlertDialog(
                onDismissRequest = { isCarrefourDialogVisible = false },
                title = {
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Contenido del Popup, incluida la imagen
                        val carrefourImage = painterResource(R.drawable.carrefour)
                        Image(
                            painter = carrefourImage,
                            contentDescription = null,
                            contentScale = ContentScale.Inside,
                            modifier = Modifier.size(width = 100.dp, height = 100.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                },
                confirmButton = { },
                dismissButton = { }
            )
        }

        // AlertDialog para el botón BP
        if (isBpDialogVisible) {
            AlertDialog(
                onDismissRequest = { isBpDialogVisible = false },
                title = {
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Contenido del Popup, incluida la imagen
                        val BPImage = painterResource(R.drawable.vcard)
                        Image(
                            painter = BPImage,
                            contentDescription = null,
                            contentScale = ContentScale.Inside,
                            modifier = Modifier.size(width = 100.dp, height = 100.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                },
                confirmButton = { },
                dismissButton = { }
            )
        }

        // AlertDialog para el botón Sabadell
        if (isSabadellDialogVisible) {
            AlertDialog(
                onDismissRequest = { isSabadellDialogVisible = false },
                title = {
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "4503",
                            fontSize = 30.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                },
                confirmButton = { },
                dismissButton = { }
            )
        }

        // AlertDialog para el botón Certificado Piloto
        if (isCertificadoDialogVisible) {
            AlertDialog(
                onDismissRequest = { isCertificadoDialogVisible = false },
                title = {
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Contenido del Popup, incluida la imagen
                        val certificadoImage = painterResource(R.drawable.certificadopiloto)
                        Image(
                            painter = certificadoImage,
                            contentDescription = null,
                            contentScale = ContentScale.Inside,
                            modifier = Modifier.size(width = 100.dp, height = 100.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                },
                confirmButton = { },
                dismissButton = { }
            )
        }
    }
}

@Composable
fun BusinessCardImage(modifier: Modifier = Modifier) {
    val image = painterResource(R.drawable.logo_uas)
    Image(
        painter = image,
        contentDescription = null,
        contentScale = ContentScale.Inside,
        modifier = modifier.size(width = 150.dp, height = 150.dp)
    )

}

@Preview(showBackground = true)
@Composable
fun BiometricButtonPreview() {
    BusinessCardTheme {
        Surface (
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF121212),
        ) {
            BiometricAuthenticationScreen()
        }
    }
}

//To check design preview in Android Studio
@Preview(showBackground = true)
@Composable
fun BusinessCardPreview() {
    BusinessCardTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF121212),
        ) {
            BusinessCard()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BackSidePreview() {
    BusinessCardTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF121212),
        ) {
            BackSideOfCard(0f)  // Puedes ajustar el ángulo de rotación según sea necesario
        }
    }
}

