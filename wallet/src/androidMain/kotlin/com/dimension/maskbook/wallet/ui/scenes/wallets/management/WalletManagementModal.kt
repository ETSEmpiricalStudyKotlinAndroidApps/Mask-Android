/*
 *  Mask-Android
 *
 *  Copyright (C) 2022  DimensionDev and Contributors
 *
 *  This file is part of Mask-Android.
 *
 *  Mask-Android is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Mask-Android is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with Mask-Android.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.dimension.maskbook.wallet.ui.scenes.wallets.management

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dimension.maskbook.common.ui.widget.MaskModal
import com.dimension.maskbook.common.ui.widget.MiddleEllipsisText
import com.dimension.maskbook.common.ui.widget.button.MaskListItemButton
import com.dimension.maskbook.wallet.R
import com.dimension.maskbook.wallet.export.model.WalletData

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WalletManagementModal(
    walletData: WalletData?,
    onRename: () -> Unit,
    onBackup: () -> Unit,
    onTransactionHistory: () -> Unit,
    onDelete: () -> Unit,
    onDisconnect: () -> Unit,
) {
    MaskModal(
        title = {
            MiddleEllipsisText(
                text = walletData?.address.orEmpty(),
                modifier = Modifier.fillMaxWidth(0.5f)
            )
        }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MaskListItemButton(
                onClick = onRename,
                icon = R.drawable.ic_rename_wallet,
                text = {
                    Text(text = stringResource(R.string.scene_wallet_edit_item_rename))
                },
                trailing = {
                    Text(text = walletData?.name.orEmpty())
                }
            )
            if (walletData != null && !walletData.fromWalletConnect) {
                MaskListItemButton(
                    onClick = onBackup,
                    icon = R.drawable.ic_back_up,
                ) {
                    Text(text = stringResource(R.string.scene_personas_action_backup))
                }
            }
            MaskListItemButton(
                onClick = onTransactionHistory,
                icon = R.drawable.ic_transaction_history,
            ) {
                Text(text = stringResource(R.string.scene_wallet_detail_wallet_items_history))
            }
            if (walletData != null && walletData.fromWalletConnect) {
                MaskListItemButton(
                    onClick = onDisconnect,
                    icon = R.drawable.ic_disconnect,
                ) {
                    Text(
                        text = stringResource(R.string.scene_wallet_connect_disconnect),
                        color = Color.Red,
                    )
                }
            } else {
                MaskListItemButton(
                    onClick = onDelete,
                    icon = R.drawable.ic_delete_wallet,
                ) {
                    Text(
                        text = stringResource(R.string.scene_wallet_edit_item_delete),
                        color = Color.Red,
                    )
                }
            }
        }
    }
}
