package com.dimension.maskbook.wallet.ui.scenes.register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dimension.maskbook.wallet.ui.MaskTheme
import com.dimension.maskbook.wallet.ui.widget.*

@Composable
@Preview
fun WelcomeScene(
    persona: String,
    onPersonaChanged: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    MaskTheme {
        MaskScaffold(
            topBar = {
                MaskTopAppBar(
                    title = {
                        Text(text = "Welcome to Mask Network")
                    },
                    subTitle = {
                        Text(text = "Create personas and connect social accounts")
                    },
                    navigationIcon = {
                        MaskBackButton(onBack = onBack)
                    }
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(ScaffoldPadding)
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(text = "Persona")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = persona,
                    onValueChange = onPersonaChanged,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.weight(1f))
                PrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNext
                ) {
                    Text(text = "Next")
                }
            }
        }
    }
}