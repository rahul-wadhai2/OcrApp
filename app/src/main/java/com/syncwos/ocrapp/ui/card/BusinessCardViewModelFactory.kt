package com.syncwos.ocrapp.ui.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.syncwos.ocrapp.repository.BusinessCardRepository

class BusinessCardViewModelFactory(businessCardRepository: BusinessCardRepository?) :
    ViewModelProvider.Factory {

    private val businessCardRepository: BusinessCardRepository?

    init {
        this.businessCardRepository = businessCardRepository
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BusinessCardViewModel::class.java)) {
            return businessCardRepository?.let { BusinessCardViewModel(it) } as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}