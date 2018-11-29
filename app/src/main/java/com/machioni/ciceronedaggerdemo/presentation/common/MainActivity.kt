package com.machioni.ciceronedaggerdemo.presentation.common

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.machioni.ciceronedaggerdemo.R
import com.machioni.ciceronedaggerdemo.R.id.bottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.commands.Back
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Replace
import ru.terrakok.cicerone.commands.SystemMessage

class MainActivity : AppCompatActivity() {

    //if the activity is killed by the OS, the fragments added to fragmentManager are automatically recreated, but the variables in the activity are lost.
    //to work arount that, we first try to initalize them with an already existing fragment from fragmentManager. If it doesn`t exist yet, only then we instantiate.
    private val homeTabFragment: HomeTabFragment by lazy {
        supportFragmentManager.findFragmentByTag(NavigationKeys.HOME_TAB_FRAGMENT) as? HomeTabFragment ?: HomeTabFragment.newInstance()
    }
    private val dashboardTabFragment: DashboardTabFragment by lazy {
        supportFragmentManager.findFragmentByTag(NavigationKeys.DASHBOARD_TAB_FRAGMENT) as? DashboardTabFragment ?: DashboardTabFragment.newInstance()
    }
    private val notificationsTabFragment: NotificationsTabFragment by lazy {
        supportFragmentManager.findFragmentByTag(NavigationKeys.NOTIFICATIONS_TAB_FRAGMENT) as? NotificationsTabFragment ?: NotificationsTabFragment.newInstance()
    }

    //will point to the fragment that is currently visible
    private var currentTab: String = NavigationKeys.HOME_TAB_FRAGMENT
    val currentTabKey = "CURRENT_TAB"

    //using cicerone to change tabs is optional. we could change fragments directly without going through
    //the router, but this may protect against lifecycle problems
    private val localCicerone = Cicerone.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupNavigationBar()

        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.navigation_home
        } else {
            savedInstanceState.getString(currentTabKey)?.let{ currentTab = it }
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putString(currentTabKey, currentTab)
    }

    private fun setupNavigationBar() {
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> localCicerone.router.replaceScreen(NavigationKeys.HOME_TAB_FRAGMENT) //envia comandos para o navigator
                R.id.navigation_dashboard -> localCicerone.router.replaceScreen(NavigationKeys.DASHBOARD_TAB_FRAGMENT)
                R.id.navigation_notifications -> localCicerone.router.replaceScreen(NavigationKeys.NOTIFICATIONS_TAB_FRAGMENT)
            }
            true
        }
    }

    //we shouldn`t use the default navigator since it uses replace() to change fragments, and that
    //destroys the previous one. What we want instead is to hide and show so our flow isn`t lost.
    private val navigator = object : Navigator {
        override fun applyCommands(commands: Array<Command>) {
            for (command in commands) applyCommand(command)
        }

        private fun applyCommand(command: Command) {
            when (command) {
                is Back -> finish()
                is SystemMessage -> Toast.makeText(this@MainActivity, command.message, Toast.LENGTH_SHORT).show()
                is Replace -> {
                    when (command.screenKey) {
                        NavigationKeys.HOME_TAB_FRAGMENT -> changeTab(homeTabFragment)
                        NavigationKeys.DASHBOARD_TAB_FRAGMENT -> changeTab(dashboardTabFragment)
                        NavigationKeys.NOTIFICATIONS_TAB_FRAGMENT -> changeTab(notificationsTabFragment)
                    }
                }
            }
        }

        //we could also use attach() and detach() instead of show() and hide().
        private fun changeTab(targetFragment: TabNavigationFragment) {
            with(supportFragmentManager.beginTransaction()) {
                supportFragmentManager.fragments.filter{ it != targetFragment }.forEach {
                    hide(it)
                    it.userVisibleHint = false //since hide doesnt trigger onPause, we use this instead to let the fragment know it is not visible
                }
                targetFragment.let {
                    currentTab = it.navigationKey
                    if (it.isAdded) {
                        show(it)
                    } else add(R.id.flowContainer, it, it.navigationKey)
                    it.userVisibleHint = true
                }
                commit()
            }
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        localCicerone.navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        localCicerone.navigatorHolder.removeNavigator()
        super.onPause()
    }

    override fun onBackPressed() {
        val currentFragment = (supportFragmentManager.findFragmentByTag(currentTab) as TabNavigationFragment)
        if (!currentFragment.onBackPressed())
            finish()
    }
}
