package com.techmomentum.wc2026.ui.navigation

import android.app.Activity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.techmomentum.wc2026.data.remote.InterstitialAdManager
import com.techmomentum.wc2026.data.remote.RewardedAdManager
import com.techmomentum.wc2026.data.repository.AppAuthState
import com.techmomentum.wc2026.data.repository.AuthRepository
import com.techmomentum.wc2026.ui.album.AlbumScreen
import com.techmomentum.wc2026.ui.auth.AuthScreen
import com.techmomentum.wc2026.ui.auth.CompleteProfileScreen
import com.techmomentum.wc2026.ui.leaderboard.LeaderboardScreen
import com.techmomentum.wc2026.ui.bootstrap.BootstrapState
import com.techmomentum.wc2026.ui.bootstrap.SessionBootstrapViewModel
import com.techmomentum.wc2026.ui.components.ErrorState
import com.techmomentum.wc2026.ui.components.LoadingScreen
import com.techmomentum.wc2026.ui.components.dismissKeyboardOnTap
import com.techmomentum.wc2026.ui.components.rememberDismissKeyboardAction
import androidx.hilt.navigation.compose.hiltViewModel
import com.techmomentum.wc2026.ui.home.HomeScreen
import com.techmomentum.wc2026.ui.pack.PackOpeningScreen
import com.techmomentum.wc2026.ui.profile.ProfileScreen
import com.techmomentum.wc2026.ui.settings.SettingsScreen
import com.techmomentum.wc2026.ui.slot.SlotMachineScreen
import com.techmomentum.wc2026.ui.sticker.StickerDetailScreen
import com.techmomentum.wc2026.ui.layout.AlbumPageScreen
import com.techmomentum.wc2026.ui.layout.LocalMainScaffoldPadding
import com.techmomentum.wc2026.ui.team.TeamAlbumScreen

@Composable
fun AppNavigation(
    authRepository: AuthRepository,
    rewardedAdManager: RewardedAdManager,
    interstitialAdManager: InterstitialAdManager,
    pendingNavRoute: String? = null,
    onPendingNavRouteConsumed: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val authState by authRepository.authState().collectAsState(initial = AppAuthState.Unauthenticated)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val context = LocalContext.current
    val activity = context as? Activity
    val dismissKeyboard = rememberDismissKeyboardAction()
    val isLoggedIn = authState is AppAuthState.SignedIn
    val showBottomBar = currentRoute in Routes.bottomBarDestinations

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) interstitialAdManager.load()
    }

    LaunchedEffect(isLoggedIn, pendingNavRoute) {
        val route = pendingNavRoute ?: return@LaunchedEffect
        if (!isLoggedIn) return@LaunchedEffect
        if (route !in Routes.bottomBarDestinations && route != Routes.PACK_OPEN && route != Routes.SETTINGS) {
            onPendingNavRouteConsumed()
            return@LaunchedEffect
        }
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
        onPendingNavRouteConsumed()
    }

    LaunchedEffect(isLoggedIn, currentRoute) {
        if (!isLoggedIn && currentRoute != Routes.AUTH && currentRoute != Routes.LOADING) {
            navController.navigate(Routes.LOADING) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    val navigateToTab: (String) -> Unit = { route ->
        if (route != currentRoute) {
            dismissKeyboard()
            val navigate = {
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            if (activity != null) {
                interstitialAdManager.tryShowOnNavigation(activity, navigate)
            } else {
                navigate()
            }
        }
    }

    MainScaffold(
        modifier = modifier,
        currentRoute = currentRoute,
        showBottomBar = showBottomBar && isLoggedIn,
        onNavigate = navigateToTab,
    ) { padding ->
        CompositionLocalProvider(LocalMainScaffoldPadding provides padding) {
            NavHost(
                navController = navController,
                startDestination = Routes.LOADING,
                modifier = Modifier.fillMaxSize().dismissKeyboardOnTap(),
            ) {
            composable(Routes.LOADING) {
                val bootstrapVm: SessionBootstrapViewModel = hiltViewModel()
                val bootstrapState by bootstrapVm.state.collectAsState()
                when (bootstrapState) {
                    BootstrapState.Loading -> AlbumPageScreen { LoadingScreen() }
                    is BootstrapState.Error -> {
                        val msg = (bootstrapState as BootstrapState.Error).message
                        AlbumPageScreen {
                            ErrorState(
                                message = msg,
                                onRetry = { bootstrapVm.retry() },
                            )
                        }
                    }
                    is BootstrapState.Ready -> {
                        val destination = (bootstrapState as BootstrapState.Ready).destinationRoute
                        androidx.compose.runtime.LaunchedEffect(destination) {
                            navController.navigate(destination) {
                                popUpTo(Routes.LOADING) { inclusive = true }
                            }
                        }
                        AlbumPageScreen { LoadingScreen() }
                    }
                }
            }
            composable(Routes.AUTH) {
                AuthScreen(
                    onAuthenticated = { route ->
                        navController.navigate(route) {
                            popUpTo(Routes.AUTH) { inclusive = true }
                        }
                    },
                )
            }
            composable(Routes.COMPLETE_PROFILE) {
                CompleteProfileScreen(
                    onContinue = { route ->
                        navController.navigate(route) {
                            popUpTo(Routes.COMPLETE_PROFILE) { inclusive = true }
                        }
                    },
                )
            }
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenPack = { navController.navigate(Routes.PACK_OPEN) },
                    onSettings = { navController.navigate(Routes.SETTINGS) },
                    onNavigateToSlots = { navigateToTab(Routes.SLOT) },
                    rewardedAdManager = rewardedAdManager,
                )
            }
            composable(Routes.ALBUM) {
                AlbumScreen(onTeamClick = { navController.navigate(Routes.team(it)) })
            }
            composable(Routes.LEADERBOARD) {
                LeaderboardScreen()
            }
            composable(Routes.SLOT) {
                SlotMachineScreen(rewardedAdManager = rewardedAdManager)
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onSettings = { navController.navigate(Routes.SETTINGS) },
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = Routes.TEAM,
                arguments = listOf(navArgument("teamId") { type = NavType.StringType }),
            ) {
                TeamAlbumScreen(
                    onStickerClick = { navController.navigate(Routes.sticker(it)) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = Routes.STICKER,
                arguments = listOf(navArgument("stickerId") { type = NavType.StringType }),
            ) {
                StickerDetailScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.PACK_OPEN) {
                PackOpeningScreen(
                    interstitialAdManager = interstitialAdManager,
                    onDone = { navController.popBackStack() },
                )
            }
            }
        }
    }
}
