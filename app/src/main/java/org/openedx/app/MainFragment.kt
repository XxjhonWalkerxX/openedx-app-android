package org.openedx.app

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.openedx.app.databinding.FragmentMainBinding
import org.openedx.app.deeplink.HomeTab
import org.openedx.core.AppUpdateState
import org.openedx.core.AppUpdateState.wasUpgradeDialogClosed
import org.openedx.core.adapter.NavigationFragmentAdapter
import org.openedx.core.presentation.dialog.appupgrade.AppUpgradeDialogFragment
import org.openedx.core.presentation.global.appupgrade.AppUpgradeRecommendedBox
import org.openedx.core.presentation.global.appupgrade.UpgradeRequiredFragment
import org.openedx.core.presentation.global.viewBinding
import org.openedx.core.system.notifier.app.AppUpgradeEvent
import org.openedx.core.ui.brand.components.BrandTabBar
import org.openedx.core.ui.brand.components.BrandTabItem
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.discovery.presentation.DiscoveryRouter
import org.openedx.downloads.presentation.download.DownloadsFragment
import org.openedx.learn.presentation.LearnFragment
import org.openedx.learn.presentation.LearnTab
import org.openedx.profile.presentation.profile.ProfileFragment

class MainFragment : Fragment(R.layout.fragment_main) {

    private val binding by viewBinding(FragmentMainBinding::bind)
    private val viewModel by viewModel<MainViewModel>()
    private val router by inject<DiscoveryRouter>()

    private val tabIds = mutableListOf<HomeTab>()
    private val selectedIndex = MutableStateFlow(0)
    private val tabBarEnabled = MutableStateFlow(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(viewModel)
        setFragmentResultListener(UpgradeRequiredFragment.REQUEST_KEY) { _, _ ->
            val profileIdx = tabIds.indexOf(HomeTab.PROFILE)
            if (profileIdx >= 0) {
                selectedIndex.value = profileIdx
                binding.viewPager.setCurrentItem(profileIdx, false)
            }
            viewModel.enableBottomBar(false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleArguments()
        setupTabs()
        setupBottomPopup()
        observeViewModel()
    }

    private fun handleArguments() {
        requireArguments().apply {
            getString(ARG_COURSE_ID).takeIf { it.isNullOrBlank().not() }?.let { courseId ->
                val infoType = getString(ARG_INFO_TYPE)
                if (viewModel.isDiscoveryTypeWebView && infoType != null) {
                    router.navigateToCourseInfo(parentFragmentManager, courseId, infoType)
                } else {
                    router.navigateToCourseDetail(parentFragmentManager, courseId)
                }
                putString(ARG_COURSE_ID, "")
                putString(ARG_INFO_TYPE, "")
            }
        }
    }

    private fun setupTabs() {
        val openTabArg = requireArguments().getString(ARG_OPEN_TAB, HomeTab.LEARN.name)
        val (ids, factories) = createTabList(openTabArg)
        tabIds.clear()
        tabIds.addAll(ids)
        selectedIndex.value = ids.indexOf(initialHomeTab(openTabArg)).coerceAtLeast(0)

        initViewPager(factories)
        binding.viewPager.setCurrentItem(selectedIndex.value, false)

        binding.brandTabBar.setContent {
            OpenEdXTheme {
                val current by selectedIndex.collectAsState()
                val enabled by tabBarEnabled.collectAsState()
                MainTabBarHost(
                    tabIds = tabIds,
                    selectedIndex = current,
                    enabled = enabled,
                    onSelect = { newIndex ->
                        if (!tabBarEnabled.value) return@MainTabBarHost
                        when (tabIds[newIndex]) {
                            HomeTab.LEARN, HomeTab.PROGRAMS -> viewModel.logLearnTabClickedEvent()
                            HomeTab.DISCOVER -> viewModel.logDiscoveryTabClickedEvent()
                            HomeTab.DOWNLOADS -> viewModel.logDownloadsTabClickedEvent()
                            HomeTab.PROFILE -> viewModel.logProfileTabClickedEvent()
                        }
                        selectedIndex.value = newIndex
                        binding.viewPager.setCurrentItem(newIndex, false)
                    },
                )
            }
        }

        requireArguments().remove(ARG_OPEN_TAB)
    }

    private fun createTabList(
        openTabArg: String,
    ): Pair<List<HomeTab>, List<() -> Fragment>> {
        val learnFactory: () -> Fragment = {
            LearnFragment.newInstance(
                openTab = if (openTabArg == HomeTab.PROGRAMS.name) {
                    LearnTab.PROGRAMS.name
                } else {
                    LearnTab.COURSES.name
                },
            )
        }
        val ids = mutableListOf<HomeTab>()
        val factories = mutableListOf<() -> Fragment>()
        ids.add(HomeTab.LEARN); factories.add(learnFactory)
        ids.add(HomeTab.DISCOVER); factories.add { viewModel.getDiscoveryFragment }
        if (viewModel.isDownloadsFragmentEnabled) {
            ids.add(HomeTab.DOWNLOADS); factories.add { DownloadsFragment() }
        }
        ids.add(HomeTab.PROFILE); factories.add { ProfileFragment() }
        return ids to factories
    }

    private fun initialHomeTab(openTabArg: String): HomeTab = when (openTabArg) {
        HomeTab.PROGRAMS.name, HomeTab.LEARN.name -> HomeTab.LEARN
        HomeTab.DISCOVER.name -> HomeTab.DISCOVER
        HomeTab.DOWNLOADS.name ->
            if (viewModel.isDownloadsFragmentEnabled) HomeTab.DOWNLOADS else HomeTab.LEARN
        HomeTab.PROFILE.name -> HomeTab.PROFILE
        else -> HomeTab.LEARN
    }

    private fun observeViewModel() {
        viewModel.isBottomBarEnabled.observe(viewLifecycleOwner) { isEnabled ->
            tabBarEnabled.value = isEnabled
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.navigateToDiscovery.collect { shouldNavigate ->
                if (shouldNavigate) {
                    val idx = tabIds.indexOf(HomeTab.DISCOVER)
                    if (idx >= 0) {
                        selectedIndex.value = idx
                        binding.viewPager.setCurrentItem(idx, false)
                    }
                }
            }
        }
    }

    private fun initViewPager(factories: List<() -> Fragment>) {
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.viewPager.offscreenPageLimit = factories.size
        binding.viewPager.adapter = NavigationFragmentAdapter(this).apply {
            factories.forEach { factory ->
                addFragment { factory() }
            }
        }
        binding.viewPager.isUserInputEnabled = false
    }

    private fun setupBottomPopup() {
        binding.composeBottomPopup.setContent {
            val appUpgradeEvent by viewModel.appUpgradeEvent.observeAsState()
            val wasUpgradeDialogClosed by remember { wasUpgradeDialogClosed }
            val appUpgradeParameters = AppUpdateState.AppUpgradeParameters(
                appUpgradeEvent = appUpgradeEvent,
                wasUpgradeDialogClosed = wasUpgradeDialogClosed,
                appUpgradeRecommendedDialog = {
                    val dialog = AppUpgradeDialogFragment.newInstance()
                    dialog.show(
                        requireActivity().supportFragmentManager,
                        AppUpgradeDialogFragment::class.simpleName,
                    )
                },
                onAppUpgradeRecommendedBoxClick = {
                    AppUpdateState.openPlayMarket(requireContext())
                },
                onAppUpgradeRequired = {
                    router.navigateToUpgradeRequired(
                        requireActivity().supportFragmentManager,
                    )
                },
            )
            when (appUpgradeParameters.appUpgradeEvent) {
                is AppUpgradeEvent.UpgradeRecommendedEvent -> {
                    if (appUpgradeParameters.wasUpgradeDialogClosed) {
                        AppUpgradeRecommendedBox(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = appUpgradeParameters.onAppUpgradeRecommendedBoxClick,
                        )
                    } else {
                        if (!AppUpdateState.wasUpdateDialogDisplayed) {
                            AppUpdateState.wasUpdateDialogDisplayed = true
                            appUpgradeParameters.appUpgradeRecommendedDialog()
                        }
                    }
                }

                is AppUpgradeEvent.UpgradeRequiredEvent -> {
                    if (!AppUpdateState.wasUpdateDialogDisplayed) {
                        AppUpdateState.wasUpdateDialogDisplayed = true
                        appUpgradeParameters.onAppUpgradeRequired()
                    }
                }

                else -> {}
            }
        }
    }

    companion object {
        private const val ARG_COURSE_ID = "courseId"
        private const val ARG_INFO_TYPE = "info_type"
        private const val ARG_OPEN_TAB = "open_tab"

        fun newInstance(
            courseId: String? = null,
            infoType: String? = null,
            openTab: String = HomeTab.LEARN.name,
        ): MainFragment {
            val fragment = MainFragment()
            fragment.arguments = bundleOf(
                ARG_COURSE_ID to courseId,
                ARG_INFO_TYPE to infoType,
                ARG_OPEN_TAB to openTab,
            )
            return fragment
        }
    }
}

@Composable
private fun MainTabBarHost(
    tabIds: List<HomeTab>,
    selectedIndex: Int,
    enabled: Boolean,
    onSelect: (Int) -> Unit,
) {
    val items = tabIds.map { it.toBrandTabItem() }
    BrandTabBar(
        items = items,
        selectedIndex = selectedIndex,
        onSelect = { idx -> if (enabled) onSelect(idx) },
    )
}

private fun HomeTab.toBrandTabItem(): BrandTabItem = when (this) {
    HomeTab.LEARN, HomeTab.PROGRAMS -> BrandTabItem("learn", "Aprende", Icons.Outlined.School)
    HomeTab.DISCOVER -> BrandTabItem("discover", "Descubre", Icons.Outlined.Explore)
    HomeTab.DOWNLOADS -> BrandTabItem("downloads", "Descargas", Icons.Outlined.Download)
    HomeTab.PROFILE -> BrandTabItem("profile", "Perfil", Icons.Outlined.Person)
}
