package com.calluscompany.ocrapp.ui.card

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.calluscompany.ocrapp.R
import com.calluscompany.ocrapp.databinding.ActivityBusinessCardBinding
import com.calluscompany.ocrapp.model.BusinessCard
import com.calluscompany.ocrapp.repository.BusinessCardRepository
import com.calluscompany.ocrapp.ui.base.BaseActivity
import com.calluscompany.ocrapp.utils.NetworkUtils
import com.calluscompany.ocrapp.utils.ToastUtils
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BusinessCardActivity : BaseActivity<ActivityBusinessCardBinding, BusinessCardViewModel>() {

    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>

    private var currentPhotoPath: String? = null

    private var photoUriForCamera: Uri? = null

    private var imageBitmap: Bitmap? = null

    private var isImageInvalid: Boolean? = false

    override fun createViewModel(): BusinessCardViewModel? {
        val businessCardRepository = BusinessCardRepository()
        val factory = BusinessCardViewModelFactory(businessCardRepository)
        return ViewModelProvider(this, factory)[BusinessCardViewModel::class.java]
    }

    override fun createViewBinding(layoutInflater: LayoutInflater?): ActivityBusinessCardBinding? {
        return layoutInflater?.let { ActivityBusinessCardBinding.inflate(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resources.getColor(R.color.purple_200, theme)
        registerForActivityResult()
        setupObservers()
        setupListeners()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ToastUtils.showLongMessage(getString(R.string.message_permission_granted), this)
            } else {
                ToastUtils.showLongMessage(getString(R.string.message_permission_denied), this)
            }
        }
    }

    private fun registerForActivityResult() {
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                if (photoUriForCamera != null && currentPhotoPath != null) {
                    imageBitmap = BitmapFactory.decodeFile(currentPhotoPath)
                    handleSuccessfulImageCapture(imageBitmap)
                    imageBitmap?.let { viewModel?.processCaptureImage(this,it) }
                } else {
                    ToastUtils.showLongMessage(getString(R.string.failed_to_get_photo_uri_or_path_after_capture), this)
                }
            } else {
                if (currentPhotoPath != null) {
                    val photoFile = File(currentPhotoPath!!)
                    if (photoFile.exists()) {
                        photoFile.delete()
                    }
                }
                ToastUtils.showLongMessage(getString(R.string.camera_capture_canceled_or_failed), this)
            }
            photoUriForCamera = null
        }
    }

    private fun setupObservers() {
        viewModel?.businessCard?.observe(this, Observer { card ->
            if (card != null) {
                updateUI(card)
            }
        })

        viewModel?.imageBitmap?.observe(this) { bitmap ->
            bitmap?.let {
                binding!!.shapeableImageView.setImageBitmap(it)
            }
        }

        viewModel?.uiState?.observe(this) { state ->
            when (state) {
                is BusinessCardViewModel.UiState.Loading -> showLoading()
                is BusinessCardViewModel.UiState.Success -> {
                    isImageInvalid = false
                    if (!state.isProcessCaptureImage) {
                        clearData()
                    }
                    ToastUtils.showLongMessage(state.message, this)
                    hideLoading()
                }
                is BusinessCardViewModel.UiState.Error -> {
                    showError(state.message)
                    isImageInvalid = state.isImageInvalid
                }
                BusinessCardViewModel.UiState.Idle -> {
                    hideLoading()
                    hideError()
                }
                null -> {
                    hideLoading()
                    hideError()
                }
            }
        }
    }

    private fun hideError() {
        binding?.errorTextView?.visibility = View.GONE
        binding?.errorTextView?.text = ""
    }

    private fun showError(message: String?){
        hideLoading()
        binding?.errorTextView?.text = message?: getString(R.string.unknown_error_occurred)
        binding?.errorTextView?.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding!!.progressIndicator.hide()
    }

    private fun showLoading() {
        hideError()
        binding?.progressIndicator?.visibility = View.VISIBLE
        binding!!.progressIndicator.show()
    }

    private fun handleSuccessfulImageCapture(currentImageBitmap: Bitmap?) {
        viewModel?.userSelectedImage(currentImageBitmap)
    }

    private fun updateUI(card: BusinessCard) {
        binding!!.nameEditText.setText(card.name)
        binding!!.emailEditText.setText(card.email)
        binding!!.phoneEditText.setText(card.phone)
        binding!!.companyNameEditText.setText(card.company)
        binding!!.jobTitleEditText.setText(card.jobTitle)
        binding!!.addressEditText.setText(card.address)
        binding!!.websiteEditText.setText(card.website)
    }

    private fun saveBusinessCard() {
        val name = binding!!.nameEditText.text.toString()
        val email = binding!!.emailEditText.text.toString()
        val phone = binding!!.phoneEditText.text.toString()
        val company = binding!!.companyNameEditText.text.toString()
        val jobTitle = binding!!.jobTitleEditText.text.toString()
        val address = binding!!.addressEditText.text.toString()
        val website = binding!!.websiteEditText.text.toString()

        val cardToSave = BusinessCard(
            name = name,
            email = email,
            phone = phone,
            company = company,
            jobTitle = jobTitle,
            address = address,
            website = website,
        )

        if (NetworkUtils.isInternetAvailable(this)) {
            if (imageBitmap == null){
                ToastUtils.showLongMessage(getString(R.string.please_capture_an_image), this)
            } else if(isImageInvalid == true){
                ToastUtils.showLongMessage(getString(R.string.image_is_invalid), this)
            } else if (cardToSave.isFieldsEmpty()) {
                ToastUtils.showLongMessage(getString(R.string.please_fill_at_least_one_field), this)
            } else {
                viewModel?.saveBusinessCardToRealDB(this, cardToSave)
            }
        } else {
            ToastUtils.showLongMessage(getString(R.string.no_internet_connection), this)
        }
    }

    private fun clearData(){
       binding!!.nameEditText.text?.clear()
       binding!!.emailEditText.text?.clear()
       binding!!.phoneEditText.text?.clear()
       binding!!.companyNameEditText.text?.clear()
       binding!!.jobTitleEditText.text?.clear()
       binding!!.addressEditText.text?.clear()
       binding!!.websiteEditText.text?.clear()
       binding!!.shapeableImageView.setImageResource(R.drawable.ic_image)
       currentPhotoPath = null
       photoUriForCamera = null
    }

    private fun setupListeners() {
        binding!!.captureButton.setOnClickListener {
            startCamera()
        }

        binding!!.saveButton.setOnClickListener {
            saveBusinessCard()
        }
    }

    private fun startCamera() {
        if (ContextCompat.checkSelfPermission(this
                , Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
        } else {
            launchCameraIntent()
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf<String?>(Manifest.permission.CAMERA),
            REQUEST_CODE_CAMERA_PERMISSION
        )
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun launchCameraIntent() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            ToastUtils.showLongMessage(getString(R.string.error_creating_image_file)+" ${ex.message}", this)
            null
        }

        photoFile?.also { photoUriForCamera = FileProvider.getUriForFile(this,
                "${applicationContext.packageName}.provider",
                it)
            photoUriForCamera?.let { uri ->
                takePictureLauncher.launch(uri)
            } ?: run {
                ToastUtils.showLongMessage(getString(R.string.failed_to_create_uri_for_camera), this)
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_CAMERA_PERMISSION = 1001
    }
}