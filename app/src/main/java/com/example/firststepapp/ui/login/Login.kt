package com.example.firststepapp.ui.login

import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import com.example.firststepapp.R
import com.example.firststepapp.navigation.Screen
import com.example.firststepapp.preferences.UserPreferences.Companion.preferenceDefaultValue
import com.example.firststepapp.ui.register.RegisterStatus
import com.example.firststepapp.viewmodel.AuthViewModel

@Composable
fun Login(
    navController: NavHostController,
    onBackPressed: () -> Unit,
    viewModel: AuthViewModel
) {
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    DisposableEffect(backDispatcher) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }
        backDispatcher?.addCallback(callback)
        onDispose {
            callback.remove()
        }
    }

    var isLoading by remember { mutableStateOf(false) }

    viewModel.isLoading.observeAsState().value?.let {
        isLoading = it
    }

    Column(
        modifier = Modifier
            .padding(40.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "logo"
        )
        Text(
            text = "Login",
            modifier = Modifier
                .padding(
                    top = 50.dp,
                    bottom = 50.dp
                ),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        val context = LocalContext.current

        // Email
        BasicTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier
                .padding(bottom = 10.dp)
                .border(
                    2.dp,
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(size = 10.dp)
                ),
            decorationBox = { innerTextField ->
                Box(
                    Modifier
                        .background(Color.Transparent)
                        .padding(10.dp)
                        .fillMaxWidth()
                ) {
                    if (email.isEmpty()) {
                        Text(
                            text = "Email",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    innerTextField()
                }
            },
        )

        // Password
        BasicTextField(
            value = password,
            onValueChange = { password = it },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .padding(bottom = 10.dp)
                .border(
                    2.dp,
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(size = 10.dp)
                ),
            decorationBox = { innerTextField ->
                Box(
                    Modifier
                        .background(Color.Transparent)
                        .padding(10.dp)
                        .fillMaxWidth()
                ) {
                    if (password.isEmpty()) {
                        Text(
                            text = "Password",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    innerTextField()
                }
            },
        )

        var loginStatus by remember { mutableStateOf(LoginStatus.NONE) }
        var loginMessage by remember { mutableStateOf("") }
        var showLoginStatus by remember { mutableStateOf(false) }

        Button(
            onClick = {
                val email = email
                val password = password

                viewModel.login(context, email, password) { success ->
                    if (success) {
                        viewModel.getUserPreferences("Token").observe(context as LifecycleOwner) { token ->
                            Log.e("AutentikasiActivity", "Token : $token")
                            if (token != preferenceDefaultValue) {
                                Log.e("AutentikasiActivity : ", "Menuju ke MainActivity")
                                Toast.makeText(context, "Login berhasil!", Toast.LENGTH_SHORT)
                                    .show()
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                        }
                    } else {
                            loginStatus = LoginStatus.FAILURE
                            loginMessage = "Login gagal, coba lagi"
                            showLoginStatus = true
                    }
                }
            },
            modifier = Modifier
                .padding(top = 40.dp)
                .width(154.dp),
            shape = RoundedCornerShape(size = 10.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 5.dp,
                pressedElevation = 0.dp,
                disabledElevation = 0.dp
            ),
        ) {
            Text(
                text = "Login",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 20.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        if (loginStatus != LoginStatus.NONE) {
            AlertDialog(
                onDismissRequest = {
                    loginStatus = LoginStatus.NONE
                },
                title = {
                    Text(text = "Gagal")
                },
                text = {
                    Text(text = loginMessage)
                },
                confirmButton = {
                    Button(
                        onClick = {
                            loginStatus = LoginStatus.NONE
                        }
                    ) {
                        Text("OK")
                    }
                }
            )
        }

        // Konfirmasi punya akun atau belum
        Row(
            modifier = Modifier
                .padding(top = 10.dp)
        ) {
            Text(
                text = "Belum punya akun?",
                style = MaterialTheme.typography.bodySmall
            )
            ClickableText(
                onClick = {
                    navController.navigate(Screen.Register.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .padding(start = 5.dp),
                text = AnnotatedString("Daftar"),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun LoginPreview(){
//    val navController = rememberNavController()
//    FirstStepAppTheme {
//        Login(navController, onBackPressed = {})
//    }
//}