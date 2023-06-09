package com.example.firststepapp.ui.main.test

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.firststepapp.navigation.Screen
import com.example.firststepapp.ui.component.Question
import com.example.firststepapp.ui.theme.FirstStepAppTheme
import com.example.firststepapp.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Test(
    navControl: NavHostController,
    viewModel: MainViewModel,
    token: String
) {
    val context = LocalContext.current

    val quizResponse by viewModel._quizResponse.observeAsState()
    val selectedAnswers = remember { mutableStateMapOf<String, Int?>() }

    LaunchedEffect(Unit) {
        viewModel.getQuiz(context, token)
    }

    Scaffold { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Header()
                LazyColumn(modifier = Modifier.weight(1f)) {
                    quizResponse?.let { response ->
                        items(response.questions.orEmpty()) { question ->
                            question?.let {
                                Question(
                                    question = it,
                                    selectedAnswer = selectedAnswers[question],
                                    onAnswerSelected = { answer ->
                                        selectedAnswers[question] = answer
                                    }
                                )
                            }
                        }
                    }
                }
                ButtonSend(
                    questions = quizResponse?.questions.orEmpty() as List<String>,
                    selectedAnswers = selectedAnswers,
                    viewModel = viewModel,
                    token = token,
                    navControl = navControl
                )
            }
        }
    }
}

@Composable
fun Header() {
    Column(
        modifier = Modifier
            .padding(
                start = 20.dp,
                top = 20.dp,
                bottom = 10.dp,
                end = 20.dp
            )
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Test",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            modifier = Modifier.padding(top = 5.dp),
            text = "Kerjakan dengan sejujurnya, karena jawaban anda dapat mempengaruhi keakuratan hasil. Jika soal tidak muncul, keluar dari halaman ini dan buka halaman ini kembali",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontStyle = FontStyle.Italic
            )
        )
    }
}

@Composable
fun ButtonSend(
    navControl: NavHostController,
    questions: List<String>,
    selectedAnswers: Map<String, Int?>,
    viewModel: MainViewModel,
    token: String,
) {
    val isButtonEnabled = remember(questions) {
        derivedStateOf {
            questions.all { question ->
                selectedAnswers.containsKey(question)
            }
        }
    }
    val showDialog = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(
                start = 20.dp,
                top = 10.dp,
                bottom = 10.dp,
                end = 20.dp
            )
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                showDialog.value = true
            },
            enabled = isButtonEnabled.value,
            modifier = Modifier
                .width(154.dp),
            shape = RoundedCornerShape(size = 10.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 5.dp,
                pressedElevation = 0.dp,
                disabledElevation = 0.dp
            ),
        ) {
            Text(
                text = "Kirim",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    val context = LocalContext.current

    confirmDialog(
        showDialog = showDialog.value,
        onConfirm = {
            val answers = questions.map { question ->
                selectedAnswers[question] ?: 0
            }
            viewModel.predict(context, token, answers)

            if (viewModel.predict(context,token,answers) != null){
                navControl.navigate(Screen.Personality.route) {
                    launchSingleTop = true
                    popUpTo(Screen.Test.route) { inclusive = true }
                }
            }

            showDialog.value = false
        },
        onCancel = {
            showDialog.value = false
        }
    )
}

@Composable
fun confirmDialog(
    showDialog: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onCancel,
            title = { Text(text = "Tunggu dulu...") },
            text = { Text(text = "Apakah Anda yakin ingin mengirimkan jawaban?") },
            confirmButton = {
                Button(
                    onClick = {onConfirm()},
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Text(text = "OK")
                }
            },
            dismissButton = {
                Button(
                    onClick = {onCancel()},
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                ) {
                    Text(text = "Batal")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ButtonPreview(){
    FirstStepAppTheme {
        //ButtonSend()
    }
}