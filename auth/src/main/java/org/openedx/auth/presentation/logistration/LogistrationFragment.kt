package org.openedx.auth.presentation.logistration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.openedx.auth.presentation.llavemx.LlaveMxCallbackActivity
import org.openedx.core.ApiConstants
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.auth.presentation.ui.OnboardingScreen
import org.openedx.foundation.utils.UrlUtils

class LogistrationFragment : Fragment() {

    private val viewModel: LogistrationViewModel by viewModel {
        parametersOf(arguments?.getString(ARG_COURSE_ID, "") ?: "")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

        // Cuando LlaveMxCallbackActivity devuelve un resultado, navegar a SignInFragment
        // que ya tiene toda la lógica para procesar el PKCE code exchange.
        LlaveMxCallbackActivity.authResult.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                viewModel.navigateToSignIn(parentFragmentManager)
            }
        }

        setContent {
            OpenEdXTheme {
                OnboardingScreen(
                    onCreateAccount = {
                        if (viewModel.isBrowserRegistrationEnabled) {
                            UrlUtils.openInBrowser(
                                activity = context,
                                apiHostUrl = viewModel.apiHostUrl,
                                url = ApiConstants.URL_REGISTER_BROWSER,
                            )
                        } else {
                            viewModel.navigateToSignUp(parentFragmentManager)
                        }
                    },
                    onSignIn = {
                        if (viewModel.isBrowserLoginEnabled) {
                            viewModel.signInBrowser(requireActivity())
                        } else {
                            viewModel.navigateToSignIn(parentFragmentManager)
                        }
                    },
                    onSignInWithCredentials = { login, password ->
                        viewModel.navigateToSignIn(parentFragmentManager)
                    },
                    onLlaveMxSignIn = {
                        org.openedx.auth.presentation.llavemx.LlaveMxAuthManager(requireContext())
                            .startAuthorizationFlow(requireContext())
                    },
                    onSearchClick = {
                        viewModel.navigateToDiscovery(parentFragmentManager, "")
                    },
                )
            }
        }
    }

    companion object {
        private const val ARG_COURSE_ID = "courseId"
        fun newInstance(courseId: String?): LogistrationFragment {
            val fragment = LogistrationFragment()
            fragment.arguments = bundleOf(
                ARG_COURSE_ID to courseId
            )
            return fragment
        }
    }
}

