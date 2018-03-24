package com.machioni.ciceronedaggerdemo.presentation.scene.dashboard.scene1

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.machioni.ciceronedaggerdemo.R
import com.machioni.ciceronedaggerdemo.presentation.common.BackButtonListener
import com.machioni.ciceronedaggerdemo.presentation.common.TabNavigationFragment
import kotlinx.android.synthetic.main.fragment_content.*
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.Router
import javax.inject.Inject

//this is a standard content fragment, which will be inflated inside a TabNavigationFragment.
class ContentFragment : Fragment(), ContentView, BackButtonListener {

    @Inject
    lateinit var contentPresenter: ContentPresenter

    @Inject
    lateinit var cicerone: Cicerone<Router>

    var depth: Int = 0

    //creates the SceneComponent using the FlowComponent from the parent fragment(TabNavigationFragment)
    //this is what provides a shared instance of cicerone to the fragments inside a tab. This also
    //allows for a fragment from another tab to be instantiated at the top of this flow`s stack with
    //proper back navigation if needed
    val component: ContentComponent? by lazy {
        context?.let {
            DaggerContentComponent.builder()
                .flowComponent( (parentFragment as TabNavigationFragment).component )
                .contentModule(ContentModule(this, it))
            .build()
        }
    }

    companion object {
        fun newInstance(depth: Int?): ContentFragment = ContentFragment().apply { depth?.let{this.depth = it }}
        val className : String = ContentFragment::class.java.simpleName
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        component?.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View {
        return inflater.inflate(R.layout.fragment_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button.text = "we need to go deeper ($depth)"
        button.setOnClickListener { contentPresenter.onButtonClicked() }
    }

    override fun navigateDeeper() {
        cicerone.router.navigateTo(ContentFragment.className, depth + 1)
    }

    override fun onBackPressed(): Boolean {
        Toast.makeText(context, "backPressed $depth", Toast.LENGTH_SHORT).show()
        cicerone.router.exit()

        return true
    }
}
