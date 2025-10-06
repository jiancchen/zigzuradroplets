package com.zigzura.droplets.ui.screens.welcome

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WelcomeNavigationScreen(
    onCompleteWelcome: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 5 })
    val scope = rememberCoroutineScope()

    val pages = listOf(
        WelcomePage.Welcome,
        WelcomePage.WhatDropletsDoes,
        WelcomePage.JustTypePrompt,
        WelcomePage.SeeYourApp,
        WelcomePage.AddApiKey
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFD84E),
                        Color(0xFFFFB74D),
                        Color(0xFFFF8A65)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Page indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color.White else Color.White.copy(alpha = 0.5f)
                            )
                    )
                    if (index < pages.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            // Horizontal pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                WelcomePageContent(
                    page = pages[page],
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip button
                TextButton(
                    onClick = { onCompleteWelcome() }
                ) {
                    Text(
                        text = "Skip",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }

                // Next/Get Started button
                Button(
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onCompleteWelcome()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFFFF8A65)
                    ),
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier.height(50.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage == pages.size - 1) "Get Started" else "Next",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun WelcomePageContent(
    page: WelcomePage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = page.emoji,
            fontSize = 80.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = page.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = page.description,
            fontSize = 18.sp,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

sealed class WelcomePage(
    val emoji: String,
    val title: String,
    val description: String
) {
    object Welcome : WelcomePage(
        emoji = "👋",
        title = "Welcome to Droplets",
        description = "Transform your ideas into beautiful apps with the power of AI. Let's get you started on your creative journey."
    )

    object WhatDropletsDoes : WelcomePage(
        emoji = "✨",
        title = "What Droplets Does",
        description = "Droplets converts your text prompts into fully functional web applications. Just describe what you want, and watch it come to life."
    )

    object JustTypePrompt : WelcomePage(
        emoji = "💭",
        title = "Just Type a Prompt",
        description = "Describe your app idea in plain English. For example: 'Create a todo list app' or 'Build a weather dashboard with charts'."
    )

    object SeeYourApp : WelcomePage(
        emoji = "📱",
        title = "See Your App",
        description = "Your apps appear as beautiful 3D cards in your collection. Tap any card to interact with your creation."
    )

    object AddApiKey : WelcomePage(
        emoji = "🔑",
        title = "Add API Key & Choose Model",
        description = "Add your OpenAI API key in Settings and select your preferred model to start generating amazing apps."
    )
}
