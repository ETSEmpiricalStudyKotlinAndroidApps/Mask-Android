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
package com.dimension.maskbook.wallet.ui.scenes.wallets.send

import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.dimension.maskbook.common.bigDecimal.BigDecimal
import com.dimension.maskbook.common.ext.humanizeDollar
import com.dimension.maskbook.common.ext.humanizeToken
import com.dimension.maskbook.common.ext.ifNullOrEmpty
import com.dimension.maskbook.common.ui.theme.moreColor
import com.dimension.maskbook.common.ui.widget.MaskCard
import com.dimension.maskbook.common.ui.widget.MaskDecimalInputField
import com.dimension.maskbook.common.ui.widget.MaskPasswordInputField
import com.dimension.maskbook.common.ui.widget.MaskScaffold
import com.dimension.maskbook.common.ui.widget.MaskScene
import com.dimension.maskbook.common.ui.widget.MaskSingleLineTopAppBar
import com.dimension.maskbook.common.ui.widget.ScaffoldPadding
import com.dimension.maskbook.common.ui.widget.button.MaskBackButton
import com.dimension.maskbook.common.ui.widget.button.MaskIconButton
import com.dimension.maskbook.common.ui.widget.button.MaskTextButton
import com.dimension.maskbook.common.ui.widget.button.MaskTransparentButton
import com.dimension.maskbook.common.ui.widget.button.PrimaryButton
import com.dimension.maskbook.wallet.R
import com.dimension.maskbook.wallet.export.model.TradableData
import com.dimension.maskbook.wallet.export.model.WalletCollectibleData
import com.dimension.maskbook.wallet.export.model.WalletTokenData
import com.dimension.maskbook.wallet.repository.SearchAddressData
import com.dimension.maskbook.wallet.repository.UnlockType
import com.dimension.maskbook.wallet.ui.widget.CollectibleCard

@Composable
fun TransferDetailScene(
    onBack: () -> Unit,
    addressData: SearchAddressData?,
    onAddContact: () -> Unit,
    data: TradableData?,
    balance: BigDecimal,
    onSelectToken: () -> Unit,
    amount: String,
    maxAmount: String,
    onAmountChanged: (String) -> Unit,
    unlockType: UnlockType,
    gasFee: String,
    arrivesIn: String,
    onEditGasFee: () -> Unit,
    onSend: (UnlockType) -> Unit,
    sendError: String?,
    paymentPassword: String,
    onPaymentPasswordChanged: (String) -> Unit,
    canConfirm: Boolean,
    isEnoughForGas: Boolean,
) {
    MaskScene {
        MaskScaffold(
            topBar = {
                MaskSingleLineTopAppBar(
                    navigationIcon = {
                        MaskBackButton(onBack = onBack)
                    },
                    title = { Text(text = stringResource(R.string.scene_wallet_balance_btn_Send)) }
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(ScaffoldPadding)
            ) {
                // in order to display NFT
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(R.string.scene_sendTransaction_send_Label_To))
                    Spacer(modifier = Modifier.height(10.dp))
                    if (addressData != null) {
                        AddressContent(
                            name = addressData.name
                                .ifNullOrEmpty { addressData.ens }
                                .ifNullOrEmpty { addressData.address },
                            isContact = addressData.isContact,
                            onAddContact = onAddContact
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    when (data) {
                        is WalletTokenData -> TokenContent(
                            logoUri = data.tokenData.logoURI ?: "",
                            tokenName = data.tokenData.name,
                            balance = "${balance.humanizeToken()} ${data.tokenData.symbol} ≈ ${(balance * data.tokenData.price).humanizeDollar()}",
                            onClick = onSelectToken
                        )
                        is WalletCollectibleData -> CollectibleContent(
                            logoUri = data.icon,
                            name = data.name,
                            collectionName = data.collection.name,
                            onClick = onSelectToken
                        )
                        null -> TokenContent(
                            onClick = onSelectToken
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    when (data) {
                        is WalletCollectibleData -> CollectibleDisplayContent(data = data)
                        else -> AmountContent(
                            amount = amount,
                            onValueChanged = onAmountChanged,
                            onMax = { onAmountChanged.invoke(maxAmount) },
                            error = when {
                                !isEnoughForGas -> {
                                    stringResource(R.string.scene_sendTransaction_send_not_enough_gas)
                                }
                                amount.toBigDecimalOrNull() ?: BigDecimal.ZERO > maxAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO -> {
                                    stringResource(R.string.scene_sendTransaction_send_amount_error)
                                }
                                else -> ""
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (unlockType == UnlockType.PASSWORD) {
                        PaymentPasswordContent(
                            pwd = paymentPassword,
                            onValueChanged = { onPaymentPasswordChanged.invoke(it) },
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (!sendError.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.padding(end = 8.dp))
                        Text(text = sendError, color = MaterialTheme.colors.error)
                    }

                    GasFeeContent(
                        fee = gasFee,
                        arrivesTimes = arrivesIn,
                        onChangeGasFee = onEditGasFee
                    )
                }

                SendButton(
                    unlockType = unlockType,
                    onSend = onSend,
                    canConfirm = canConfirm,
                )
            }
        }
    }
}

@Composable
private fun AddressContent(
    name: String,
    isContact: Boolean,
    onAddContact: () -> Unit,
) {
    MaskCard(elevation = 0.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )
            if (!isContact) {
                MaskIconButton(onClick = onAddContact) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_add_user),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TokenContent(
    logoUri: String = "",
    tokenName: String = "",
    balance: String = "",
    onClick: () -> Unit
) {
    MaskCard(
        onClick = onClick,
        elevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberImagePainter(logoUri),
                contentDescription = null,
                modifier = Modifier.size(38.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tokenName,
                    style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold),
                )
                Text(text = balance, style = MaterialTheme.typography.body2)
            }
            Icon(imageVector = Icons.Default.ArrowRight, contentDescription = null)
        }
    }
}

@Composable
private fun CollectibleContent(
    logoUri: String,
    name: String,
    collectionName: String,
    onClick: () -> Unit
) {
    MaskTransparentButton(onClick = onClick) {
        Image(
            painter = rememberImagePainter(logoUri),
            contentDescription = null,
            modifier = Modifier.size(38.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold)
            )
            Text(text = collectionName)
        }
        Icon(imageVector = Icons.Default.ArrowRight, contentDescription = null)
    }
}

@Composable
private fun CollectibleDisplayContent(
    data: WalletCollectibleData
) {
    CollectibleCard(
        modifier = Modifier.clip(RoundedCornerShape(12.dp)),
        data = data
    )
}

@Composable
private fun AmountContent(
    amount: String,
    onValueChanged: (String) -> Unit,
    onMax: () -> Unit,
    error: String,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.scene_sendTransaction_send_label_Amount),
            style = MaterialTheme.typography.h5.copy(color = MaterialTheme.colors.onSurface)
        )
        Spacer(modifier = Modifier.height(8.dp))
        MaskDecimalInputField(
            modifier = Modifier.fillMaxWidth(),
            decimalValue = amount.toBigDecimal(),
            onValueChange = { onValueChanged(it.toString()) },
            trailingIcon = {
                Row {
                    MaskTextButton(
                        onClick = onMax,
                        modifier = Modifier.background(
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small
                        ),
                        minWidth = 42.dp,
                        minHeight = 24.dp
                    ) {
                        Text(
                            text = stringResource(R.string.scene_sendTransaction_send_btn_max),
                            color = MaterialTheme.colors.primary,
                            style = MaterialTheme.typography.body2
                        )
                    }
                    Spacer(modifier = Modifier.padding(end = 12.dp))
                }
            },
        )
        if (error.isNotEmpty()) {
            Spacer(modifier = Modifier.padding(end = 8.dp))
            Text(
                text = error,
                color = MaterialTheme.colors.error
            )
        }
    }
}

@Composable
private fun PaymentPasswordContent(
    pwd: String,
    onValueChanged: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.scene_setting_general_setup_payment_password),
            style = MaterialTheme.typography.h5.copy(color = MaterialTheme.colors.onSurface)
        )
        Spacer(modifier = Modifier.height(8.dp))
        MaskPasswordInputField(
            value = pwd,
            onValueChange = onValueChanged,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun GasFeeContent(
    fee: String,
    arrivesTimes: String,
    onChangeGasFee: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        RoundButton(
            onClick = onChangeGasFee,
            title = "Fee:$fee",
            iconStart = false,
            iconEnd = true,
            icon = R.drawable.ic_arrow_right
        )
        RoundButton(
            onClick = onChangeGasFee,
            title = "Arrives in ~ $arrivesTimes",
            iconStart = true,
            iconEnd = false,
            icon = R.drawable.ic_time_circle_border
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SendButton(
    unlockType: UnlockType,
    onSend: (UnlockType) -> Unit,
    canConfirm: Boolean,
) {
    when (unlockType) {
        UnlockType.BIOMETRIC -> {
            // TODO Biometrics Replace UI
            PrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onSend.invoke(unlockType) }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_faceid_small),
                    contentDescription = null
                )
                Text(
                    text = stringResource(R.string.scene_wallet_balance_btn_Send),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        UnlockType.PASSWORD -> {
            PrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onSend.invoke(unlockType) },
                enabled = canConfirm,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_upload_small),
                    contentDescription = null
                )
                Text(
                    text = stringResource(R.string.scene_wallet_balance_btn_Send),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun RoundButton(
    onClick: () -> Unit,
    title: String,
    @DrawableRes icon: Int,
    iconStart: Boolean = true,
    iconEnd: Boolean = false
) {
    MaskTextButton(
        onClick = onClick,
        shape = MaterialTheme.shapes.small.copy(CornerSize(13.dp)),
        modifier = Modifier.clip(MaterialTheme.shapes.small.copy(CornerSize(13.dp))),
        minHeight = 28.dp,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.moreColor.caption,
        )
    ) {
        if (iconStart) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = MaterialTheme.typography.h6.color,
                modifier = Modifier
                    .size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
        }
        Text(text = title, style = MaterialTheme.typography.h6)
        if (iconEnd) {
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = MaterialTheme.typography.h6.color,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}
