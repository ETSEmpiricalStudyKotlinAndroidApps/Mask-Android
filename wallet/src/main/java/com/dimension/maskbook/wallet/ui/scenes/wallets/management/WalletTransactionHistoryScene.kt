package com.dimension.maskbook.wallet.ui.scenes.wallets.management

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.dimension.maskbook.wallet.repository.TransactionData
import com.dimension.maskbook.wallet.ui.MaskTheme
import com.dimension.maskbook.wallet.ui.scenes.wallets.token.TransactionHistoryList
import com.dimension.maskbook.wallet.ui.widget.MaskBackButton
import com.dimension.maskbook.wallet.ui.widget.MaskScaffold
import com.dimension.maskbook.wallet.ui.widget.MaskSingleLineTopAppBar

@Composable
fun WalletTransactionHistoryScene(
    onBack: () -> Unit,
    transactions: List<TransactionData>,
    onSpeedUp: (TransactionData) -> Unit,
    onCancel: (TransactionData) -> Unit,
) {
    MaskTheme {
        MaskScaffold(
            topBar = {
                MaskSingleLineTopAppBar(
                    title = {
                        Text(text = "Transaction History")
                    },
                    navigationIcon = {
                        MaskBackButton(
                            onBack = onBack
                        )
                    }
                )
            }
        ) {
            TransactionHistoryList(
                transactions = transactions,
                onSpeedUp = onSpeedUp,
                onCancel = onCancel,
            )
        }
    }
}