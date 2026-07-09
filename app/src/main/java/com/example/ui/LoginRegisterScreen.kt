package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.BoardViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoginRegisterScreen(viewModel: BoardViewModel) {
    val isLoginActive by viewModel.isLoginScreenActive.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val focusManager = LocalFocusManager.current

    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme

    // Outer layout: Center container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Notion-style Container: High-contrast borders, clean lines
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 450.dp, max = 650.dp)
                .widthIn(max = 850.dp)
                .border(1.dp, colorScheme.outline, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.background
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            // AnimatedContent to swap left and right panels or slide content
            // We can check screen width to decide split-pane layout or single-column stack
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isWideScreen = maxWidth > 600.dp

                if (isWideScreen) {
                    // Split screen: Card on one side, Form on the other. They swap sides!
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Left side (either Form or Visual Panel)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .animateContentSize(animationSpec = tween(500))
                        ) {
                            if (isLoginActive) {
                                // Login Form
                                LoginForm(
                                    email = emailInput,
                                    password = passwordInput,
                                    onEmailChange = { emailInput = it },
                                    onPasswordChange = { passwordInput = it },
                                    isPasswordVisible = isPasswordVisible,
                                    onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
                                    onSubmit = {
                                        viewModel.login(emailInput, "")
                                    },
                                    onSwitchToRegister = {
                                        viewModel.isLoginScreenActive.value = false
                                        viewModel.authError.value = null
                                    },
                                    authError = authError,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                // Visual Panel on the Left for Register Mode
                                VisualPanel(
                                    title = "Junte-se ao Espaço",
                                    subtitle = "Crie sua conta para planejar metas e gerenciar tarefas no estilo Notion.",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(colorScheme.surface)
                                        .border(
                                            width = 0.dp,
                                            color = Color.Transparent,
                                            shape = RoundedCornerShape(0.dp)
                                        )
                                )
                            }
                        }

                        // Divider line
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(1.dp)
                                .background(colorScheme.outline)
                        )

                        // Right side (either Form or Visual Panel)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .animateContentSize(animationSpec = tween(500))
                        ) {
                            if (isLoginActive) {
                                // Visual Panel on the Right for Login Mode
                                VisualPanel(
                                    title = "Seu Espaço de Metas",
                                    subtitle = "Escreva, planeje, organize. Suas metas diárias e tarefas integradas em um único painel minimalista.",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(colorScheme.surface)
                                )
                            } else {
                                // Register Form
                                RegisterForm(
                                    name = nameInput,
                                    email = emailInput,
                                    password = passwordInput,
                                    onNameChange = { nameInput = it },
                                    onEmailChange = { emailInput = it },
                                    onPasswordChange = { passwordInput = it },
                                    isPasswordVisible = isPasswordVisible,
                                    onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
                                    onSubmit = {
                                        viewModel.register(emailInput, nameInput)
                                    },
                                    onSwitchToLogin = {
                                        viewModel.isLoginScreenActive.value = true
                                        viewModel.authError.value = null
                                    },
                                    authError = authError,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                } else {
                    // Mobile Screen: Just transition between form screens
                    AnimatedContent(
                        targetState = isLoginActive,
                        transitionSpec = {
                            if (targetState) {
                                (slideInHorizontally(animationSpec = tween(300)) { -it } + fadeIn()).togetherWith(
                                    slideOutHorizontally(animationSpec = tween(300)) { it } + fadeOut())
                            } else {
                                (slideInHorizontally(animationSpec = tween(300)) { it } + fadeIn()).togetherWith(
                                    slideOutHorizontally(animationSpec = tween(300)) { -it } + fadeOut())
                            }
                        },
                        label = "auth_panel_transition"
                    ) { activeLogin ->
                        if (activeLogin) {
                            LoginForm(
                                email = emailInput,
                                password = passwordInput,
                                onEmailChange = { emailInput = it },
                                onPasswordChange = { passwordInput = it },
                                isPasswordVisible = isPasswordVisible,
                                onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
                                onSubmit = {
                                    viewModel.login(emailInput, "")
                                },
                                onSwitchToRegister = {
                                    viewModel.isLoginScreenActive.value = false
                                    viewModel.authError.value = null
                                },
                                authError = authError,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            RegisterForm(
                                name = nameInput,
                                email = emailInput,
                                password = passwordInput,
                                onNameChange = { nameInput = it },
                                onEmailChange = { emailInput = it },
                                onPasswordChange = { passwordInput = it },
                                isPasswordVisible = isPasswordVisible,
                                onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
                                onSubmit = {
                                    viewModel.register(emailInput, nameInput)
                                },
                                onSwitchToLogin = {
                                    viewModel.isLoginScreenActive.value = true
                                    viewModel.authError.value = null
                                },
                                authError = authError,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoginForm(
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    onSubmit: () -> Unit,
    onSwitchToRegister: () -> Unit,
    authError: String?,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mini Logo
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ViewAgenda,
                contentDescription = "Notion Logo",
                tint = colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Notion Board",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = colorScheme.primary
            )
        }

        Text(
            text = "Entrar no Espaço",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Acesse suas tarefas e metas",
            fontSize = 14.sp,
            color = colorScheme.secondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
            textAlign = TextAlign.Center
        )

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("E-mail") },
            placeholder = { Text("seu-email@exemplo.com") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_email_input"),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password Field (Visual detail only, since we login directly with email)
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Senha") },
            placeholder = { Text("Sua senha secreta") },
            singleLine = true,
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_password_input"),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onSubmit() }
            ),
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            shape = RoundedCornerShape(12.dp)
        )

        if (authError != null) {
            Text(
                text = authError,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("login_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Entrar",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Não tem conta? ",
                fontSize = 13.sp,
                color = colorScheme.secondary
            )
            Text(
                text = "Registrar-se",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary,
                modifier = Modifier
                    .clickable { onSwitchToRegister() }
                    .testTag("switch_to_register")
            )
        }
    }
}

@Composable
fun RegisterForm(
    name: String,
    email: String,
    password: String,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    onSubmit: () -> Unit,
    onSwitchToLogin: () -> Unit,
    authError: String?,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mini Logo
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ViewAgenda,
                contentDescription = "Notion Logo",
                tint = colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Notion Board",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = colorScheme.primary
            )
        }

        Text(
            text = "Criar Nova Conta",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Comece sua jornada produtiva",
            fontSize = 14.sp,
            color = colorScheme.secondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
            textAlign = TextAlign.Center
        )

        // Name Field
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Nome Completo") },
            placeholder = { Text("Seu nome") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_name_input"),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("E-mail") },
            placeholder = { Text("seu-email@exemplo.com") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_email_input"),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Senha") },
            placeholder = { Text("Mínimo 6 caracteres") },
            singleLine = true,
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_password_input"),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onSubmit() }
            ),
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            shape = RoundedCornerShape(12.dp)
        )

        if (authError != null) {
            Text(
                text = authError,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("register_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Registrar-se",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Já tem conta? ",
                fontSize = 13.sp,
                color = colorScheme.secondary
            )
            Text(
                text = "Fazer Login",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary,
                modifier = Modifier
                    .clickable { onSwitchToLogin() }
                    .testTag("switch_to_login")
            )
        }
    }
}

@Composable
fun VisualPanel(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        // Minimalist quote bracket decoration
        Text(
            text = "“",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 60.sp,
            color = colorScheme.primary.copy(alpha = 0.2f),
            modifier = Modifier.height(40.dp)
        )

        Text(
            text = title,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = colorScheme.primary,
            lineHeight = 36.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Text(
            text = subtitle,
            fontSize = 15.sp,
            color = colorScheme.secondary,
            lineHeight = 22.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Minimalist decorative list representation (Notion grid look)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colorScheme.outline, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(120.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(colorScheme.primary)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(80.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(colorScheme.outline)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(100.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(colorScheme.outline)
                )
            }
        }
    }
}
