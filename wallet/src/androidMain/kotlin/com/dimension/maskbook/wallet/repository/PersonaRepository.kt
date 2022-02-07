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
package com.dimension.maskbook.wallet.repository

import androidx.lifecycle.LiveData
import com.dimension.maskbook.wallet.R
import kotlinx.coroutines.flow.Flow

data class SocialData(
    val id: String,
    val name: String,
    val avatar: String,
    val personaId: String? = null,
    val network: Network,
)

enum class RedirectTarget {
    Gecko,
    Setup,
}

interface IPersonaRepository {
    val twitter: Flow<List<SocialData>>
    val facebook: Flow<List<SocialData>>
    val persona: Flow<List<PersonaData>>
    val currentPersona: Flow<PersonaData?>
    val redirect: LiveData<RedirectTarget?>
    fun beginConnectingProcess(
        personaId: String,
        platformType: PlatformType,
    )

    fun finishConnectingProcess(
        userName: String,
        platformType: PlatformType,
    )

    fun cancelConnectingProcess()
    fun setCurrentPersona(id: String)
    fun generateNewMnemonic(): List<String>
    fun logout()
    fun updatePersona(id: String, value: String)
    fun connectTwitter(personaId: String, userName: String)
    fun connectFacebook(personaId: String, userName: String)
    fun disconnectTwitter(personaId: String, socialId: String)
    fun disconnectFacebook(personaId: String, socialId: String)
    fun createPersonaFromMnemonic(value: List<String>, name: String)
    fun createPersonaFromPrivateKey(value: String)
    fun updateCurrentPersona(value: String)
    suspend fun backupPrivateKey(id: String): String
    fun init()
}

val Network.icon: Int
    get() = when (this) {
        Network.Twitter -> R.drawable.twitter
        Network.Facebook -> R.drawable.facebook
        Network.Instagram -> R.drawable.instagram
        Network.Minds -> R.drawable.ic_persona_empty_mind
    }

val Network.title: String
    get() = when (this) {
        Network.Twitter -> "Twitter"
        Network.Facebook -> "Facebook"
        Network.Instagram -> "Instagram"
        Network.Minds -> "Minds"
    }

val Network.platform: PlatformType?
    get() = when (this) {
        Network.Twitter -> PlatformType.Twitter
        Network.Facebook -> PlatformType.Facebook
        Network.Instagram -> null
        Network.Minds -> null
    }
