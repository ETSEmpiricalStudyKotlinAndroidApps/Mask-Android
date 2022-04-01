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
package com.dimension.maskbook.persona.viewmodel

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.net.Uri
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dimension.maskbook.common.ext.asStateIn
import com.dimension.maskbook.persona.BuildConfig
import com.dimension.maskbook.persona.datasource.DbPersonaDataSource
import com.dimension.maskbook.persona.repository.IPersonaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

class DownloadQrCodeViewModel(
    private val personaRepository: IPersonaRepository,
    private val dataSource: DbPersonaDataSource,
) : ViewModel() {
    enum class DownloadState {
        Idle,
        Pending,
        Success,
        Failed
    }
    private val _state = MutableStateFlow(DownloadState.Idle)
    val state = _state.asStateIn(viewModelScope)

    val personaQrCode = personaRepository.currentPersona.mapNotNull {
        it?.let { persona ->
            dataSource.getPersonaQrCode(persona.identifier)
        }
    }.asStateIn(viewModelScope, null)

    fun pickFile() {
        _state.value = DownloadState.Pending
    }

    fun save(uri: Uri, context: Context, pdfContent: View, height: Int, width: Int) {
        _state.value = DownloadState.Pending
        viewModelScope.launch(Dispatchers.IO) {
            try {
                personaQrCode.value?.let {
                    // generate PDF
                    val pdfDocument = PdfDocument()
                    val myPageInfo = PageInfo.Builder(width, height, 1).create()
                    val myPage = pdfDocument.startPage(myPageInfo)
                    pdfContent.draw(myPage.canvas)
                    pdfDocument.finishPage(myPage)
                    context.contentResolver.openOutputStream(uri)?.use {
                        pdfDocument.writeTo(it)
                    }
                    pdfDocument.close()
                    _state.value = DownloadState.Success
                } ?: kotlin.run {
                    _state.value = DownloadState.Failed
                }
            } catch (e: Throwable) {
                if (BuildConfig.DEBUG) e.printStackTrace()
                _state.value = DownloadState.Failed
            }
        }
    }
}
