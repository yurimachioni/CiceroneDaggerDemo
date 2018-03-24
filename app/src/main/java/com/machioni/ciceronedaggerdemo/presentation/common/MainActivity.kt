package com.machioni.ciceronedaggerdemo.presentation.common

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.machioni.ciceronedaggerdemo.R
import kotlinx.android.synthetic.main.activity_main.*
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.commands.Back
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Replace
import ru.terrakok.cicerone.commands.SystemMessage

class MainActivity : AppCompatActivity() {

    private val homeTabFragment: HomeTabFragment by lazy { HomeTabFragment.newInstance().apply { addFragment(this) } }
    private val dashboardTabFragment: DashboardTabFragment  by lazy { DashboardTabFragment.newInstance().apply { addFragment(this) } }
    private val notificationsTabFragment: NotificationsTabFragment by lazy { NotificationsTabFragment.newInstance().apply { addFragment(this) } }

    private val FRAGMENT_TAGS = arrayOf(HomeTabFragment.className, DashboardTabFragment.className, NotificationsTabFragment.className)

    //using cicerone to change tabs is optional. we could change fragments directly without going through
    //the router, but it may protect against lifecycle problems
    private val localCicerone = Cicerone.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupNavigationBar()

        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.navigation_home
        }
    }

    private fun setupNavigationBar() {
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> localCicerone.router.replaceScreen(HomeTabFragment.className) //envia comandos para o navigator
                R.id.navigation_dashboard -> localCicerone.router.replaceScreen(DashboardTabFragment.className)
                R.id.navigation_notifications -> localCicerone.router.replaceScreen(NotificationsTabFragment.className)
            }
            true
        }
    }

    //each fragment should be added but not attached initally, so our navigator can deal with them equally
    private fun addFragment(fragment: TabNavigationFragment) {
        supportFragmentManager.beginTransaction()
                .add(R.id.flowContainer, fragment, fragment.javaClass.simpleName)
                .detach(fragment)
                .commitNow()
    }

    //we shouldn`t use the default navigator since it uses replace() to change fragments, and that
    //destroys the previous one. What we want instead is to detach and reattach the same instance
    //so our flow isn`t lost.
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
                        HomeTabFragment.className -> changeTab(homeTabFragment)
                        DashboardTabFragment.className -> changeTab(dashboardTabFragment)
                        NotificationsTabFragment.className -> changeTab(notificationsTabFragment)
                    }
                }
            }
        }

        //we could also use hide() and show() instead of detach() and attach(). it is super fast since
        //it doesnt destroy and create the views, but if each tab has deep flows it makes all of them
        //to be inflated and running all the time, which could use a lot of RAM and make the fragments
        //lifecycle callbacks to be skipped when switching back to a previous tab.
        private fun changeTab(targetFragment: TabNavigationFragment) {
            val fragmentTransaction = supportFragmentManager.beginTransaction()

            FRAGMENT_TAGS.forEach {
                val fragment = supportFragmentManager.findFragmentByTag(it)
                if (fragment != null && !fragment.isDetached && fragment != targetFragment) {
                    fragmentTransaction.detach(fragment)
                }
            }
            fragmentTransaction.attach(targetFragment).commitNow()
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
        val fragment = supportFragmentManager.findFragmentById(R.id.flowContainer)
        if (fragment != null && fragment is BackButtonListener && fragment.onBackPressed()) {
            return
        } else {
            finish()
        }
    }

}
