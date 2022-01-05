package com.dimension.maskbook.wallet.viewmodel.persona.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dimension.maskbook.wallet.ext.asStateIn
import com.dimension.maskbook.wallet.repository.IPersonaRepository
import kotlinx.coroutines.flow.combine

class FacebookSocialViewModel(
    repository: IPersonaRepository,
) : ViewModel() {
    val items =
        repository.facebook
            .combine(
                repository.currentPersona.asStateIn(
                    viewModelScope,
                    null
                )
            ) { tw, p -> tw.filter { it.personaId == p?.id } }
            .asStateIn(viewModelScope, emptyList())
}