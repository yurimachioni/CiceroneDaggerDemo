package com.machioni.ciceronedaggerdemo.presentation.scene.dashboard.scene1

import android.content.Context
import com.machioni.ciceronedaggerdemo.common.di.ActivityContext
import com.machioni.ciceronedaggerdemo.presentation.common.FlowComponent
import com.machioni.ciceronedaggerdemo.common.di.PerScene
import dagger.Component
import dagger.Module
import dagger.Provides

@Module
class ContentModule(private val contentView: ContentView, private val context: Context) {
    @Provides
    @ActivityContext
    @PerScene
    fun provideInnerContext(): Context {
        return context
    }

    @Provides
    @PerScene
    internal fun provideContentView(): ContentView {
        return contentView
    }
}

//every scene of a tab needs the flowComponent (from the parent fragment) to handle the backstack properly
@PerScene
@Component(dependencies = [(FlowComponent::class)], modules = [(ContentModule::class)])
interface ContentComponent {
    fun inject(contentFragment: ContentFragment)
}