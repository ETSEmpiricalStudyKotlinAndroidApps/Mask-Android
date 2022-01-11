package com.dimension.maskbook.wallet.ui.scenes.wallets.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dimension.maskbook.wallet.ui.widget.MaskInputField
import com.dimension.maskbook.wallet.ui.widget.MaskModal
import com.dimension.maskbook.wallet.ui.widget.PrimaryButton
import com.dimension.maskbook.wallet.ui.widget.ScaffoldPadding

@Composable
fun SpeedUpOrCancelTransactionModal(
    title: @Composable () -> Unit,
    onConfirm: () -> Unit,
    price: String,
    costFee: String,
    gasLimit: Long,
    onGasLimitChanged: (Long) -> Unit,
    maxPriorityFee: Long,
    onMaxPriorityFeeChanged: (Long) -> Unit,
    maxFee: Long,
    onMaxFeeChanged: (Long) -> Unit,
) {
    MaskModal {
        Column(
            modifier = Modifier.padding(ScaffoldPadding)
        ) {
            title.invoke()
//            Text(
//                text = "Speed Up Transaction",
//                style = MaterialTheme.typography.subtitle1,
//                modifier = Modifier.fillMaxWidth(),
//                textAlign = TextAlign.Center,
//            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = price, style = MaterialTheme.typography.h4)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "cost fee: $costFee",
                style = MaterialTheme.typography.caption.copy(fontSize = 16.sp),
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Gas limit")
            Spacer(modifier = Modifier.height(8.dp))
            MaskInputField(
                value = gasLimit.toString(),
                onValueChange = {
                    onGasLimitChanged.invoke(it.toLong())
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )


            Spacer(modifier = Modifier.height(16.dp))
            Text(text = androidx.compose.ui.res.stringResource(com.dimension.maskbook.wallet.R.string.scene_sendTransaction_gasPrice_maxPriorityFee))
            Spacer(modifier = Modifier.height(8.dp))
            MaskInputField(
                value = maxPriorityFee.toString(),
                onValueChange = {
                    onMaxPriorityFeeChanged.invoke(it.toLong())
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = androidx.compose.ui.res.stringResource(com.dimension.maskbook.wallet.R.string.scene_sendTransaction_gasPrice_maxFee))
            Spacer(modifier = Modifier.height(8.dp))
            MaskInputField(
                value = maxFee.toString(),
                onValueChange = {
                    onMaxFeeChanged.invoke(it.toLong())
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(28.dp))
            PrimaryButton(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = androidx.compose.ui.res.stringResource(com.dimension.maskbook.wallet.R.string.common_controls_confirm))
            }
        }
    }
}
