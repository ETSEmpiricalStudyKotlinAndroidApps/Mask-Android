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
package com.dimension.maskbook.persona.ui.scenes

import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dimension.maskbook.common.ext.encodeBase64
import com.dimension.maskbook.common.ext.navigateUri
import com.dimension.maskbook.common.route.Deeplinks
import com.dimension.maskbook.common.route.navigationComposeAnimComposable
import com.dimension.maskbook.common.route.navigationComposeAnimComposablePackage
import com.dimension.maskbook.common.routeProcessor.annotations.Back
import com.dimension.maskbook.common.routeProcessor.annotations.NavGraphDestination
import com.dimension.maskbook.common.ui.theme.moreColor
import com.dimension.maskbook.common.ui.widget.MaskCard
import com.dimension.maskbook.common.ui.widget.MaskScaffold
import com.dimension.maskbook.common.ui.widget.MaskScene
import com.dimension.maskbook.common.ui.widget.MaskSingleLineTopAppBar
import com.dimension.maskbook.common.ui.widget.ScaffoldPadding
import com.dimension.maskbook.common.ui.widget.button.MaskBackButton
import com.dimension.maskbook.persona.R
import com.dimension.maskbook.persona.route.PersonaRoute
import com.dimension.maskbook.persona.viewmodel.DownloadQrCodeViewModel
import com.dimension.maskbook.persona.viewmodel.PersonaMenuViewModel
import com.dimension.maskbook.setting.export.model.BackupActions
import org.koin.androidx.compose.getViewModel

@NavGraphDestination(
    route = PersonaRoute.PersonaMenu,
    packageName = navigationComposeAnimComposablePackage,
    functionName = navigationComposeAnimComposable,
)
@Composable
fun PersonaMenuScene(
    navController: NavController,
    @Back onBack: () -> Unit,
) {
    val viewModel = getViewModel<PersonaMenuViewModel>()

    val currentPersona by viewModel.currentPersona.collectAsState()
    val backupPassword by viewModel.backupPassword.collectAsState()

    MaskScene {
        MaskScaffold(
            topBar = {
                MaskSingleLineTopAppBar(
                    navigationIcon = {
                        MaskBackButton(onBack = onBack)
                    },
                    title = {
                        Text(text = currentPersona?.name ?: "")
                    }
                )
            }
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.subtitle1
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(ScaffoldPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    MenuItem(
                        onClick = {
                            navController.navigate(PersonaRoute.SwitchPersona)
                        },
                        icon = R.drawable.ic_profile,
                        title = stringResource(R.string.scene_personas_action_change_add_persona)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    MenuItem(
                        onClick = {
                            currentPersona?.let {
                                navController.navigate(PersonaRoute.RenamePersona(it.identifier))
                            }
                        },
                        icon = R.drawable.ic_edit,
                        title = stringResource(R.string.scene_personas_action_rename)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    MenuItem(
                        onClick = {
                            if (backupPassword.isEmpty()) {
                                navController.navigateUri(Uri.parse(Deeplinks.Setting.SetupPasswordDialog(BackupActions.ExportPrivateKey.name)))
                            } else {
                                navController.navigate(Uri.parse(Deeplinks.Persona.BackUpPassword(PersonaRoute.ExportPrivateKey)))
                            }
                        },
                        icon = R.drawable.ic_password,
                        title = stringResource(R.string.scene_persona_export_private_key_title)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    MenuItem(
                        onClick = {
                            if (backupPassword.isEmpty()) {
                                navController.navigateUri(Uri.parse(Deeplinks.Setting.SetupPasswordDialog(BackupActions.DownloadQrCode.name)))
                            } else {
                                currentPersona?.let {
                                    navController.navigate(
                                        Uri.parse(
                                            Deeplinks.Persona.BackUpPassword(
                                                PersonaRoute.DownloadQrCode(
                                                    idType = DownloadQrCodeViewModel.IdType.ID.name,
                                                    idBase64 = it.identifier.encodeBase64(Base64.NO_WRAP)
                                                )
                                            )
                                        )
                                    )
                                }
                            }
                        },
                        icon = R.drawable.ic_download,
                        title = stringResource(R.string.scene_persona_download_qr_code_title)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    MenuItem(
                        onClick = {
                            if (backupPassword.isEmpty()) {
                                navController.navigateUri(Uri.parse(Deeplinks.Setting.SetupPasswordDialog(BackupActions.BackUp.name)))
                            } else {
                                navController.navigateUri(Uri.parse(Deeplinks.Setting.BackupData.BackupSelection))
                            }
                        },
                        icon = R.drawable.ic_paper_plus,
                        title = stringResource(R.string.common_controls_back_up)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    MaskCard(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 0.dp,
                        onClick = {
                            if (backupPassword.isEmpty()) {
                                navController.navigate(PersonaRoute.Logout)
                            } else {
                                navController.navigate(PersonaRoute.LogoutBeforeCheck)
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                painterResource(id = R.drawable.ic_logout),
                                contentDescription = null,
                                tint = Color.Red,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(R.string.scene_setting_profile_log_out), color = Color.Red, modifier = Modifier.weight(1f))
                            Icon(
                                painterResource(id = R.drawable.ic_arrow_right),
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(6.dp, 11.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuItem(
    onClick: () -> Unit,
    icon: Int,
    title: String,
) {
    MaskCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 0.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, style = MaterialTheme.typography.h5, modifier = Modifier.weight(1f))
            Icon(
                painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = null,
                tint = MaterialTheme.moreColor.onCaption,
                modifier = Modifier.size(6.dp, 11.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}
