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
package com.dimension.maskbook.persona.repository

import android.net.Uri
import com.dimension.maskbook.common.ext.toSite
import com.dimension.maskbook.extension.export.ExtensionServices
import com.dimension.maskbook.persona.data.JSMethod
import com.dimension.maskbook.persona.datasource.DbPersonaDataSource
import com.dimension.maskbook.persona.datasource.DbProfileDataSource
import com.dimension.maskbook.persona.datasource.DbRelationDataSource
import com.dimension.maskbook.persona.export.error.PersonaAlreadyExitsError
import com.dimension.maskbook.persona.export.model.ConnectAccountData
import com.dimension.maskbook.persona.export.model.PersonaData
import com.dimension.maskbook.persona.export.model.PlatformType
import com.dimension.maskbook.persona.export.model.SocialData
import com.dimension.maskbook.persona.model.ContactData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class PersonaRepository(
    private val scope: CoroutineScope,
    private val jsMethod: JSMethod,
    private val extensionServices: ExtensionServices,
    private val preferenceRepository: IPreferenceRepository,
    private val personaDataSource: DbPersonaDataSource,
    private val profileDataSource: DbProfileDataSource,
    private val relationDataSource: DbRelationDataSource,
) : IPersonaRepository,
    ISocialsRepository,
    IContactsRepository {

    private var connectingJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    override val currentPersona: Flow<PersonaData?>
        get() = preferenceRepository.currentPersonaIdentifier.flatMapLatest {
            personaDataSource.getPersonaFlow(it)
        }

    override val personaList: Flow<List<PersonaData>>
        get() = personaDataSource.getPersonaListFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    override val socials: Flow<List<SocialData>>
        get() = preferenceRepository.currentPersonaIdentifier.flatMapLatest {
            profileDataSource.getSocialListFlow(it)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val contacts: Flow<List<ContactData>>
        get() = preferenceRepository.currentPersonaIdentifier.flatMapLatest {
            relationDataSource.getContactListFlow(it)
        }

    override suspend fun hasPersona(): Boolean {
        return !personaDataSource.isEmpty()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun beginConnectingProcess(
        personaId: String,
        platformType: PlatformType,
        onDone: (ConnectAccountData) -> Unit,
    ) {
        connectingJob?.cancel()
        extensionServices.setSite(platformType.toSite())

        connectingJob = preferenceRepository.lastDetectProfileIdentifier
            .filterNot { it.isEmpty() }
            .filterNot { personaDataSource.hasConnected(it) }
            .flatMapLatest { profileDataSource.getSocialFlow(it) }
            .filterNotNull()
            .flowOn(Dispatchers.IO)
            .onEach {
                onDone.invoke(ConnectAccountData(personaId, it))
                connectingJob?.cancel()
            }
            .flowOn(Dispatchers.Main)
            .launchIn(scope)
    }

    override fun init() {
        scope.launch {
            if (personaDataSource.isEmpty()) {
                return@launch
            }

            val identifier = preferenceRepository.currentPersonaIdentifier.firstOrNull()
            if (!identifier.isNullOrEmpty() && personaDataSource.contains(identifier)) {
                return@launch
            }

            val newCurrentPersona = personaDataSource.getPersonaFirst()
            setCurrentPersona(newCurrentPersona?.identifier.orEmpty())
        }
    }

    override fun saveEmailForCurrentPersona(value: String) {
        scope.launch {
            currentPersona.firstOrNull()?.let { personaData ->
                personaDataSource.updateEmail(personaData.identifier, value)
            }
        }
    }

    override fun savePhoneForCurrentPersona(value: String) {
        scope.launch {
            currentPersona.firstOrNull()?.let { personaData ->
                personaDataSource.updatePhone(personaData.identifier, value)
            }
        }
    }

    override fun setCurrentPersona(id: String) {
        scope.launch {
            preferenceRepository.setCurrentPersonaIdentifier(id)
            jsMethod.setCurrentPersonaIdentifier(id)
        }
    }

    override fun logout() {
        scope.launch {
            val deletePersona = currentPersona.firstOrNull() ?: return@launch

            personaDataSource.deletePersona(deletePersona.identifier)
            val newCurrentPersona = personaDataSource.getPersonaFirst()
            setCurrentPersona(newCurrentPersona?.identifier.orEmpty())

            jsMethod.removePersona(deletePersona.identifier)
        }
    }

    override fun updatePersona(id: String, nickname: String) {
        scope.launch {
            personaDataSource.updateNickName(id, nickname)
            jsMethod.updatePersonaInfo(id, nickname)
        }
    }

    override fun updateCurrentPersona(nickname: String) {
        scope.launch {
            val id = currentPersona.firstOrNull()?.identifier ?: return@launch
            personaDataSource.updateNickName(id, nickname)
            jsMethod.updatePersonaInfo(id, nickname)
        }
    }

    override fun connectProfile(personaId: String, profileId: String) {
        scope.launch {
            jsMethod.connectProfile(personaId, profileId)
        }
    }

    override fun disconnectProfile(personaId: String, profileId: String) {
        scope.launch {
            jsMethod.disconnectProfile(profileId)
        }
    }

    override suspend fun createPersonaFromMnemonic(value: List<String>, name: String) {
        withContext(scope.coroutineContext) {
            val mnemonic = value.joinToString(" ")
            if (personaDataSource.containsMnemonic(mnemonic)) {
                throw PersonaAlreadyExitsError()
            }
            jsMethod.createPersonaByMnemonic(mnemonic, name, "")
        }
    }

    override suspend fun createPersonaFromPrivateKey(value: String) {
        withContext(scope.coroutineContext) {
            jsMethod.restoreFromPrivateKey(privateKey = value, nickname = "persona1")
        }
    }

    override suspend fun backupPrivateKey(id: String): String {
        return jsMethod.backupPrivateKey(id) ?: ""
    }

    override fun setPlatform(platformType: PlatformType) {
        extensionServices.setSite(platformType.toSite())
    }

    override fun setAvatarForCurrentPersona(avatar: Uri?) {
        scope.launch {
            currentPersona.firstOrNull()?.let { personaData ->
                personaDataSource.updateAvatar(personaData.identifier, avatar?.toString())
            }
        }
    }
}
