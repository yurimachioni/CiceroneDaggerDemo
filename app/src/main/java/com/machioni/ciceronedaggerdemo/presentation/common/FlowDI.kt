package com.machioni.ciceronedaggerdemo.presentation.common

import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import com.machioni.ciceronedaggerdemo.common.di.ApplicationComponent
import com.machioni.ciceronedaggerdemo.common.di.PerFlow
import dagger.Component
import dagger.Module
import dagger.Provides
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.Router

@Module
class FlowModule(val fragmentActivity: FragmentActivity, val fm: FragmentManager, val containerId: Int) {

    @Provides
    @PerFlow
    fun provideCicerone(): Cicerone<Router> = Cicerone.create()

    @Provides
    @PerFlow
    fun provideNavigator(): FlowNavigator = FlowNavigator(fragmentActivity, fm, containerId)

    @Provides
    @PerFlow
    fun provideCustomRouter(cicerone: Cicerone<Router>): Router = cicerone.router

    //This is where we could provide objects to be shared inside a tab, like use cases, datasources and repositories, if it makes sense.
    //They would be lazily initialized with each tab.
}

@PerFlow
@Component(dependencies = [(ApplicationComponent::class)], modules = [(FlowModule::class)])
interface FlowComponent : ApplicationComponent {
    fun inject(tabNavigationFragment: TabNavigationFragment)
    fun provideCicerone(): Cicerone<Router>
    fun router(): Router
}

