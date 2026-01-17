package com.example.claudeapp.ui.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.claudeapp.R
import com.example.claudeapp.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    onNavigateToMain: () -> Unit,
    onRequestPermissions: ((Boolean) -> Unit) -> Unit = {},
    permissionsGranted: Boolean = false
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    var hasNavigated by remember { mutableStateOf(false) }
    
    LaunchedEffect(key1 = Unit) {
        authViewModel.checkAuthState()
        delay(2000) // Show for 2 seconds
        
        if (hasNavigated) return@LaunchedEffect
        
        val currentAuthState = authViewModel.uiState.value
        hasNavigated = true
        if (currentAuthState.isSignedIn) {
            onNavigateToMain()
        } else {
            onSplashFinished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        // Center Content: Logo
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.civicwatch_full_logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(200.dp)
            )
        }
        
        // Bottom Content: "by HEMANTH"
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = "by\nHEMANTH",
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
