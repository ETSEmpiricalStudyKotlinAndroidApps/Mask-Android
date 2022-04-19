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
package com.dimension.maskbook.labs.data

import com.dimension.maskbook.common.ext.decodeJson
import com.dimension.maskbook.common.ext.encodeJson
import com.dimension.maskbook.common.ext.responseSuccess
import com.dimension.maskbook.common.route.Navigator
import com.dimension.maskbook.extension.export.ExtensionServices
import com.dimension.maskbook.labs.model.SendMethodRequest
import com.dimension.maskbook.labs.model.options.RedPacketOptions
import com.dimension.maskbook.labs.route.LabsRoute

class RedPacketMethod(
    private val services: ExtensionServices,
) {
    suspend fun startCollect() {
        services.subscribeCurrentContentJSEvent(notifyRedPacket, claimOrRefundRedPacket).collect { message ->
            when (message.method) {
                notifyRedPacket -> {
                    message.responseSuccess(true)
                }
                claimOrRefundRedPacket -> {
                    val options = message.params?.decodeJson<RedPacketOptions>() ?: return@collect
                    val requestRaw = SendMethodRequest(
                        id = message.id,
                        jsonrpc = message.jsonrpc,
                        method = message.method,
                    ).encodeJson()
                    Navigator.navigate(LabsRoute.RedPacket.LuckyDrop(options.encodeJson(), requestRaw))
                    // response in LuckDropViewModel
                }
            }
        }
    }

    companion object {
        private const val notifyRedPacket = "notifyRedpacket"
        private const val claimOrRefundRedPacket = "claimOrRefundRedpacket"
    }
}
