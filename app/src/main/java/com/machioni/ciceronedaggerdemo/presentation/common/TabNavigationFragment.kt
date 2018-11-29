package com.machioni.ciceronedaggerdemo.presentation.common

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.machioni.ciceronedaggerdemo.R
import com.machioni.ciceronedaggerdemo.common.MyApplication
import com.machioni.ciceronedaggerdemo.presentation.scene.dashboard.scene1.ContentFragment
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.Router
import javax.inject.Inject

//this is the parent fragment that has the container for the scenes. It is detached from MainActivity
//when the user clicks in another tab, but this only destroy its views. The instance and the state
//are preserved (including the internal cicerone instance, which holds the flow of scenes of this tab)
sealed class TabNavigationFragment : Fragment(), BackButtonListener {

    @Inject
    lateinit var cicerone: Cicerone<Router>

    @Inject
    lateinit var navigator: FlowNavigator

    abstract val navigationKey: String

    //all the sceneComponents will depend on this component. it allows the same instance of cicerone
    //to be injected in all the scenes of a flow, which allows a different backstack per tab.
    //notice we need to pass the fragmentManager and the inner containerId to the module, which will
    //create the navigator for this flow.
    val component: FlowComponent? by lazy {
        context?.let {
            DaggerFlowComponent.builder()
                    .applicationComponent(MyApplication.daggerComponent)
                    .flowModule(FlowModule(activity as FragmentActivity, childFragmentManager, R.id.sceneContainer))
                    .build()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        component?.inject(this)

        return inflater.inflate(R.layout.fragment_tab_navigation, container, false)
                ?: super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        cicerone.navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        cicerone.navigatorHolder.removeNavigator()
        super.onPause()
    }

    //sends backPressed events from Main Activity to child fragments
    override fun onBackPressed(): Boolean {
        return if(isAdded) {
            val childFragment = childFragmentManager.findFragmentById(R.id.sceneContainer)
            childFragment != null && childFragment is BackButtonListener && childFragment.onBackPressed()
        } else false
    }
}

//these subclasses are optional. they allow for easier management of each tab, defining each one`s
//first child fragment on possibly other unique behaviours per tab
class HomeTabFragment : TabNavigationFragment() {
    override val navigationKey: String = NavigationKeys.HOME_TAB_FRAGMENT

    companion object {
        fun newInstance(bundle: Bundle? = null) = HomeTabFragment().apply { arguments = bundle }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (childFragmentManager.findFragmentById(R.id.sceneContainer) == null) {
            cicerone.router.replaceScreen(NavigationKeys.CONTENT_FRAGMENT, 1)
        }
    }
}

class DashboardTabFragment : TabNavigationFragment() {
    override val navigationKey: String = NavigationKeys.DASHBOARD_TAB_FRAGMENT

    companion object {
        fun newInstance(bundle: Bundle? = null) = DashboardTabFragment().apply { arguments = bundle }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (childFragmentManager.findFragmentById(R.id.sceneContainer) == null)
            cicerone.router.replaceScreen(NavigationKeys.CONTENT_FRAGMENT, 1)
    }
}

class NotificationsTabFragment : TabNavigationFragment() {
    override val navigationKey: String = NavigationKeys.NOTIFICATIONS_TAB_FRAGMENT

    companion object {
        fun newInstance(bundle: Bundle? = null) = NotificationsTabFragment().apply { arguments = bundle }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (childFragmentManager.findFragmentById(R.id.sceneContainer) == null)
            cicerone.router.replaceScreen(NavigationKeys.CONTENT_FRAGMENT, 1)
    }
}
