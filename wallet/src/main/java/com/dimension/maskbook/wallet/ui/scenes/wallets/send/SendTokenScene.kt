package com.dimension.maskbook.wallet.ui.scenes.wallets.send

import androidx.annotation.DrawableRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.dimension.maskbook.wallet.R
import com.dimension.maskbook.wallet.ext.humanizeDollar
import com.dimension.maskbook.wallet.ext.humanizeToken
import com.dimension.maskbook.wallet.repository.SearchAddressData
import com.dimension.maskbook.wallet.repository.TokenData
import com.dimension.maskbook.wallet.repository.UnlockWays
import com.dimension.maskbook.wallet.repository.WalletTokenData
import com.dimension.maskbook.wallet.ui.MaskTheme
import com.dimension.maskbook.wallet.ui.widget.*
import java.math.BigDecimal

@Composable
fun SendTokenScene(
    onBack: () -> Unit,
    addressData: SearchAddressData,
    onAddContact: () -> Unit,
    tokenData: TokenData,
    walletTokenData: WalletTokenData,
    onSelectToken: () -> Unit,
    amount: String,
    maxAmount: BigDecimal,
    onAmountChanged: (String) -> Unit,
    unlockWays: UnlockWays,
    gasFee: String,
    arrivesIn: String,
    onEditGasFee: () -> Unit,
    onSend: (UnlockWays) -> Unit,
    sendError: String?,
    paymentPassword: String,
    onPaymentPasswordChanged: (String) -> Unit,
    canConfirm: Boolean,
) {
    MaskTheme {
        MaskScaffold(
            topBar = {
                MaskSingleLineTopAppBar(
                    navigationIcon = {
                        MaskBackButton(onBack = onBack)
                    },
                    title = { Text(text = "Send") }
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(ScaffoldPadding)
            ) {
                Text(text = "To")
                Spacer(modifier = Modifier.height(10.dp))
                AddressContent(
                    name = addressData.name ?: addressData.ens ?: addressData.address,
                    isContact = addressData.isContact,
                    onAddContact = onAddContact
                )
                Spacer(modifier = Modifier.height(20.dp))

                TokenContent(
                    logoUri = tokenData.logoURI ?: "",
                    tokenName = tokenData.name,
                    balance = "${walletTokenData.count.humanizeToken()} ${tokenData.symbol} ≈ ${(walletTokenData.count * tokenData.price).humanizeDollar()}",
                    onClick = onSelectToken
                )

                Spacer(modifier = Modifier.height(20.dp))
                AmountContent(
                    amount = amount,
                    onValueChanged = {
                        if (it.toBigDecimalOrNull() != null) {
                            onAmountChanged.invoke(it)
                        }
                    },
                    onMax = { onAmountChanged.invoke(maxAmount.toPlainString()) },
                    error = amount.toBigDecimal() > maxAmount
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (unlockWays == UnlockWays.PASSWORD) {
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

                SendButton(
                    faceId = unlockWays == UnlockWays.FACE_ID,
                    touchId = unlockWays == UnlockWays.TOUCH_ID,
                    onFaceIdSend = { onSend.invoke(UnlockWays.FACE_ID) },
                    onTouchIdSend = { onSend.invoke(UnlockWays.TOUCH_ID) },
                    onSend = { onSend.invoke(UnlockWays.PASSWORD) },
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.weight(1f)
        )
        if (!isContact) {
            IconButton(onClick = onAddContact) {
                Image(
                    painter = painterResource(id = R.drawable.ic_add_user),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun TokenContent(
    logoUri: String,
    tokenName: String,
    balance: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick.invoke() },
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
                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold)
            )
            Text(text = balance)
        }
        Icon(imageVector = Icons.Default.ArrowRight, contentDescription = null)
    }
}

@Composable
private fun AmountContent(
    amount: String,
    onValueChanged: (String) -> Unit,
    onMax: () -> Unit,
    error: Boolean,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Amount")
        Spacer(modifier = Modifier.height(8.dp))
        MaskInputField(
            modifier = Modifier.fillMaxWidth(),
            value = amount,
            onValueChange = onValueChanged,
            trailingIcon = {
                Row {
                    TextButton(
                        onClick = onMax,
                        modifier = Modifier.background(
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small
                        )
                    ) {
                        Text(text = "MAX", color = MaterialTheme.colors.primary)
                    }
                    Spacer(modifier = Modifier.padding(end = 12.dp))
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        if (error) {
            Spacer(modifier = Modifier.padding(end = 8.dp))
            Text(text = "Insufficient amount.", color = MaterialTheme.colors.error)
        }
    }
}

@Composable
private fun PaymentPasswordContent(
    pwd: String,
    onValueChanged: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Payment password")
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
private fun ColumnScope.SendButton(
    faceId: Boolean,
    touchId: Boolean,
    onFaceIdSend: () -> Unit,
    onTouchIdSend: () -> Unit,
    onSend: () -> Unit,
    canConfirm: Boolean,
) {
    Spacer(modifier = Modifier.weight(1f))
    when {
        faceId -> {
            PrimaryButton(modifier = Modifier
                .combinedClickable(
                    onLongClick = onFaceIdSend,
                    onClick = {}
                )
                .fillMaxWidth(), onClick = {}) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_faceid_small),
                    contentDescription = null
                )
                Text(text = "Hold to Send", modifier = Modifier.padding(start = 8.dp))
            }
        }
        touchId -> {
            PrimaryButton(modifier = Modifier
                .combinedClickable(
                    onLongClick = onTouchIdSend,
                    onClick = {}
                )
                .fillMaxWidth(), onClick = {}) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_touchid_small),
                    contentDescription = null
                )
                Text(text = "Hold to Send", modifier = Modifier.padding(start = 8.dp))
            }
        }
        else -> {
            PrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onSend,
                enabled = canConfirm,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_upload_small),
                    contentDescription = null
                )
                Text(text = "Send", modifier = Modifier.padding(start = 8.dp))
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
    SecondaryButton(
        onClick = onClick,
        shape = MaterialTheme.shapes.small.copy(CornerSize(50)),
        modifier = Modifier.clip(MaterialTheme.shapes.small.copy(CornerSize(50)))
    ) {
        if (iconStart) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = MaterialTheme.colors.primary,
                modifier = Modifier
                    .size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
        }
        Text(text = title, color = MaterialTheme.colors.primary)
        if (iconEnd) {
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = MaterialTheme.colors.primary,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

