package com.machioni.ciceronedaggerdemo.presentation.common

import android.content.Context
import android.content.Intent
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import com.machioni.ciceronedaggerdemo.presentation.scene.dashboard.scene1.ContentFragment
import ru.terrakok.cicerone.android.SupportAppNavigator
import javax.inject.Inject

//Our flowNavigator needs to know how to create our fragments and intents for activities given a string (screenKey).
//the rest will be handled by the SupportAppNavigator. Each tab will have a different instance of FlowNavigator,
//which among other things, holds a stack of fragments
class FlowNavigator @Inject constructor(fragmentActivity: FragmentActivity, fm: FragmentManager, containerId: Int) : SupportAppNavigator(fragmentActivity, fm, containerId) {

    //take extra care with these parameters since data can be null. This will crash in runtime if we
    //don`t use Any?. If this returns null, the super will know that this screenKey shouldn`t be
    //used to create and Activity and will try to create a Fragment instead
    override fun createActivityIntent(context: Context, screenKey: String, data: Any?): Intent? {
        return when (screenKey) {
            //SomeActivity.className -> Intent(context, SomeActivity::class.java)
            //SomeOtherActivity.className -> Intent(context, SomeOtherActivity::class.java)
            else -> null
        }
    }

    override fun createFragment(screenKey: String, data: Any?): Fragment? {
        return when (screenKey) {
            ContentFragment.className -> {
                if (data is Int) {
                    return ContentFragment.newInstance(data)
                } else {
                    throw IllegalArgumentException("Trying to open ContentFragment without providing current depth")
                }
            }

            else -> null
        }
    }
}
