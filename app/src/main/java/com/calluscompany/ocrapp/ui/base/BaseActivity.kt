package com.calluscompany.ocrapp.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<Binding : ViewBinding, ViewModel : BaseViewModel>
    : AppCompatActivity() {

    protected var viewModel: ViewModel? = null

    protected var binding: Binding? = null

    protected abstract fun createViewModel(): ViewModel?

    protected abstract fun createViewBinding(layoutInflater: LayoutInflater?): Binding?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = createViewBinding(LayoutInflater.from(this))
        setContentView(binding?.getRoot())
        viewModel = createViewModel()
    }
}