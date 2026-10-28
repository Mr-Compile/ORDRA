package com.ordra.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ordra.R
import com.ordra.core.ui.ai.CompanionAvatar
import com.ordra.core.ui.ai.CompanionState
import com.ordra.core.ui.ai.CompanionVariant
import kotlinx.coroutines.launch

/** First-run onboarding: 3 concise pages + skip. No account creation. */
@Composable
fun OnboardingScreen(onFinish: (groqApiKey: String) -> Unit) {
    var apiKey by remember { mutableStateOf("") }
    val pages = listOf(
        Triple(CompanionState.GREETING, R.string.onboard_1_title, R.string.onboard_1_body),
        Triple(CompanionState.DELIVERY, R.string.onboard_2_title, R.string.onboard_2_body),
        Triple(CompanionState.IDLE, R.string.onboard_3_title, R.string.onboard_3_body),
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { onFinish(apiKey) }) {
                Text(stringResource(R.string.onboard_skip))
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { page ->
            val (companionState, titleRes, bodyRes) = pages[page]
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CompanionAvatar(
                    state = companionState,
                    variant = CompanionVariant.ONBOARDING,
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    stringResource(titleRes),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(bodyRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                if (page == pages.lastIndex) {
                    Spacer(Modifier.height(20.dp))
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.onboard_api_key)) },
                        supportingText = { Text(stringResource(R.string.onboard_api_key_hint)) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                    )
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(pages.size) { index ->
                Text(
                    if (index == pagerState.currentPage) "●" else "○",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
        Button(
            onClick = {
                if (pagerState.currentPage < pages.lastIndex) {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                } else {
                    onFinish(apiKey)
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                stringResource(
                    if (pagerState.currentPage < pages.lastIndex) {
                        R.string.onboard_next
                    } else {
                        R.string.onboard_start
                    },
                ),
            )
        }
    }
}
