package com.calluscompany.ocrapp.model

import com.google.firebase.database.Exclude

data class BusinessCard(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var company: String = "",
    var jobTitle: String = "",
    var address: String = "",
    var website: String = "",
    var timestamp: Long = 0
) {
    @Exclude
    fun isFieldsEmpty(): Boolean {
        return name.isEmpty() && email.isEmpty() && phone.isEmpty() && company.isEmpty() &&
                jobTitle.isEmpty() && address.isEmpty() && website.isEmpty()
    }
}