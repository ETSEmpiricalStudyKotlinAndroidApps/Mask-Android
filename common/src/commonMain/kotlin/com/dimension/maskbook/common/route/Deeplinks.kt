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
package com.dimension.maskbook.common.route

import com.dimension.maskbook.common.routeProcessor.annotations.Route

@Suppress("CONST_VAL_WITHOUT_INITIALIZER")
@Route(
    schema = "maskwallet",
)
expect object Deeplinks {
    object Main {
        object Home {
            operator fun invoke(initialRoute: String): String
        }
    }
    object Setting {
        const val SetupPasswordDialog: String
        object BackupData {
            const val BackupSelection: String
        }
    }
    object Persona {
        const val Recovery: String
        object BackUpPassword {
            operator fun invoke(target: String): String
        }
        object Register {
            const val WelcomeCreatePersona: String
            const val CreatePersona: String
        }
    }
    object Wallet {
        const val SwitchWallet: String
        object SendTokenConfirm {
            operator fun invoke(dataRaw: String, ignorePaymentPassword: Boolean): String
        }
        object WalletConnect {
            object Connect {
                operator fun invoke(uriBase64: String): String
            }
        }
    }
    object WebContent {
        operator fun invoke(site: String?): String
    }
    object Labs {
        const val Transak: String
    }

    const val Scan: String
}
