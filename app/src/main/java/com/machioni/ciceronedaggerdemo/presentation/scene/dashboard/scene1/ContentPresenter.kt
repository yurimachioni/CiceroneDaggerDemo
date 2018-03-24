package com.machioni.ciceronedaggerdemo.presentation.scene.dashboard.scene1

import javax.inject.Inject

class ContentPresenter @Inject constructor(private val contentView: ContentView){

    fun onButtonClicked(){
        contentView.navigateDeeper()
    }
}