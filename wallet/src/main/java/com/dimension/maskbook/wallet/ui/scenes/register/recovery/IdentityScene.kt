package com.dimension.maskbook.wallet.ui.scenes.register.recovery

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
fun IdentityScene(
    identity: String,
    onIdentityChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit,
) {
    MaskTheme {
        MaskScaffold(
            topBar = {
                MaskTopAppBar(
                    title = {
                        Text(text = "Identity")
                    },
                    navigationIcon = {
                        MaskBackButton(
                            onBack = onBack,
                        )
                    }
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(ScaffoldPadding),
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = identity,
                    onValueChange = onIdentityChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    placeholder = {
                        Text(text = "Please enter 12 mnemonic words separated by spaces")
                    }
                )
                Spacer(modifier = Modifier.weight(1f))
                PrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onConfirm
                ) {
                    Text(text = "Confirm")
                }
            }
        }
    }
}