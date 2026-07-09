package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.viewmodel.TaskViewModel

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LoginRegisterScreen(
    viewModel: TaskViewModel,
    onLoginSuccess: () -> Unit
) {
    var isLoginMode by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isWideScreen) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val illustrationPanel = @Composable {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_login_illustration),
                                contentDescription = "Ilustração Minimalista",
                                modifier = Modifier
                                    .size(320.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Pense, organize e realize.",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif
                                ),
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Seu espaço de trabalho tudo-em-um para metas e tarefas diárias.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.widthIn(max = 280.dp)
                            )
                        }
                    }
                }

                val formPanel = @Composable {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FormContainer(
                            isLoginMode = isLoginMode,
                            username = username,
                            password = password,
                            passwordVisible = passwordVisible,
                            errorMessage = errorMessage,
                            successMessage = successMessage,
                            onUsernameChange = { username = it; errorMessage = null; successMessage = null },
                            onPasswordChange = { password = it; errorMessage = null; successMessage = null },
                            onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                            onSubmit = {
                                if (isLoginMode) {
                                    viewModel.login(username, password,
                                        onLoginSuccess,
                                        { errorMessage = it }
                                    )
                                } else {
                                    viewModel.register(username, password,
                                        {
                                            successMessage = "Conta criada! Entrando..."
                                        },
                                        { errorMessage = it }
                                    )
                                }
                            },
                            onModeSwitch = {
                                isLoginMode = !isLoginMode
                                errorMessage = null
                                successMessage = null
                            }
                        )
                    }
                }

                AnimatedContent(
                    targetState = isLoginMode,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(500)) with fadeOut(animationSpec = tween(500))
                    },
                    modifier = Modifier.fillMaxSize()
                ) { loginState ->
                    Row(modifier = Modifier.fillMaxSize()) {
                        if (loginState) {
                            illustrationPanel()
                            formPanel()
                        } else {
                            formPanel()
                            illustrationPanel()
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Image(
                    painter = painterResource(id = R.drawable.img_login_illustration),
                    contentDescription = "Ilustração Minimalista",
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(24.dp))
                FormContainer(
                    isLoginMode = isLoginMode,
                    username = username,
                    password = password,
                    passwordVisible = passwordVisible,
                    errorMessage = errorMessage,
                    successMessage = successMessage,
                    onUsernameChange = { username = it; errorMessage = null; successMessage = null },
                    onPasswordChange = { password = it; errorMessage = null; successMessage = null },
                    onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                    onSubmit = {
                        if (isLoginMode) {
                            viewModel.login(username, password,
                                onLoginSuccess,
                                { errorMessage = it }
                            )
                        } else {
                            viewModel.register(username, password,
                                {
                                    successMessage = "Conta criada! Entrando..."
                                },
                                { errorMessage = it }
                            )
                        }
                    },
                    onModeSwitch = {
                        isLoginMode = !isLoginMode
                        errorMessage = null
                        successMessage = null
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormContainer(
    isLoginMode: Boolean,
    username: String,
    password: String,
    passwordVisible: Boolean,
    errorMessage: String?,
    successMessage: String?,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onSubmit: () -> Unit,
    onModeSwitch: () -> Unit
) {
    Column(
        modifier = Modifier
            .widthIn(max = 380.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = if (isLoginMode) "Entrar no NotionTask" else "Criar conta NotionTask",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                letterSpacing = (-0.5).sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Text(
            text = if (isLoginMode) "Use suas credenciais para continuar." else "Cadastre-se para organizar suas metas.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f))
                    .border(1.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(4.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        if (successMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = successMessage,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Text(
            text = "Nome de usuário",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            placeholder = { Text("Ex: ramon") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("username_input")
        )

        Text(
            text = "Senha",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = { Text("Sua senha") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Ocultar senha" else "Mostrar senha"
                IconButton(onClick = onPasswordVisibilityToggle) {
                    Icon(imageVector = image, contentDescription = description, tint = MaterialTheme.colorScheme.secondary)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .testTag("password_input")
        )

        Button(
            onClick = onSubmit,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("submit_button")
        ) {
            Text(
                text = if (isLoginMode) "Entrar" else "Registrar",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isLoginMode) "Não tem uma conta? " else "Já possui uma conta? ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = if (isLoginMode) "Criar conta" else "Fazer login",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { onModeSwitch() }
                    .testTag("switch_form_button")
            )
        }
    }
}
