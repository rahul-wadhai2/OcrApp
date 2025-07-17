package com.syncwos.ocrapp.ui.card

import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.syncwos.ocrapp.R
import com.syncwos.ocrapp.model.BusinessCard
import com.syncwos.ocrapp.repository.BusinessCardRepository
import com.syncwos.ocrapp.ui.base.BaseViewModel
import kotlinx.coroutines.launch
import java.io.IOException

class BusinessCardViewModel(private val repository: BusinessCardRepository) : BaseViewModel() {

    private val _businessCard = MutableLiveData<BusinessCard>()
    val businessCard: LiveData<BusinessCard> get() = _businessCard

    private val _uiState = MutableLiveData<UiState>(UiState.Idle)
    val uiState: LiveData<UiState> get() = _uiState

    private val _imageBitmap = MutableLiveData<Bitmap?>()
    val imageBitmap: LiveData<Bitmap?> get() = _imageBitmap

    fun setImageBitmap(bitmap: Bitmap?) {
        _imageBitmap.value = bitmap
    }

    fun userSelectedImage(bitmap: Bitmap?) {
        if (bitmap != null) {
            setImageBitmap(bitmap)
        }
    }

    fun processCaptureImage(context: Context, bitmap: Bitmap) {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val textResult = repository.recognizeTextFromImage(bitmap)
                textResult.onSuccess { text ->
                    if (text.isNotBlank()) {
                        val parsedCard = repository.parseBusinessCard(text)
                        _businessCard.value = parsedCard
                        _uiState.value = UiState.Success(getString(context
                        ,R.string.successfully_extracted_text_from_that_image), true)
                    } else {
                        _uiState.value = UiState.Error(getString(context
                            ,R.string.no_text_found_in_the_image), true)
                    }
                }.onFailure {
                    _uiState.value = UiState.Error(it.message ?: getString(context
                        ,R.string.error_processing_image), true)
                }
            } catch (e: IOException) {
                _uiState.value = UiState.Error(e.message ?: getString(context,
                    R.string.io_error), true)
            }
        }
    }

    fun saveBusinessCardToRealDB(context: Context, newCard: BusinessCard) {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val cardId = repository.saveBusinessCardToRealtimeDB(newCard)
                if (cardId.isNotBlank()) {
                    _uiState.value = UiState.Success(
                        getString(
                            context, R.string.business_card_uploaded_with_id
                        ) + cardId
                    )
                } else {
                    _uiState.value = UiState.Error(
                        getString(
                            context, R.string.error_uploading_business_card
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    e.message ?: getString(
                        context, R.string.error_uploading_business_card
                    )
                )
            }
        }
    }

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val message: String? = null
                           ,val isProcessCaptureImage: Boolean = false) : UiState()
        data class Error(val message: String, val isImageInvalid: Boolean = false) : UiState()
    }
}
