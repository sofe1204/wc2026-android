package com.techmomentum.wc2026.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.techmomentum.wc2026.data.remote.RewardedAdManager
import com.techmomentum.wc2026.data.repository.AppAuthState
import com.techmomentum.wc2026.data.repository.AuthRepository
import com.techmomentum.wc2026.ui.album.AlbumScreen
import com.techmomentum.wc2026.ui.auth.AuthScreen
import com.techmomentum.wc2026.ui.auth.CompleteProfileScreen
import com.techmomentum.wc2026.ui.auth.VerifyEmailScreen
import com.techmomentum.wc2026.ui.leaderboard.LeaderboardScreen
import com.techmomentum.wc2026.ui.bootstrap.BootstrapState
import com.techmomentum.wc2026.ui.bootstrap.SessionBootstrapViewModel
import com.techmomentum.wc2026.ui.components.ErrorState
import com.techmomentum.wc2026.ui.components.LoadingScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.techmomentum.wc2026.ui.home.HomeScreen
import com.techmomentum.wc2026.ui.pack.PackOpeningScreen
import com.techmomentum.wc2026.ui.profile.ProfileScreen
import com.techmomentum.wc2026.ui.settings.SettingsScreen
import com.techmomentum.wc2026.ui.slot.SlotMachineScreen
import com.techmomentum.wc2026.ui.sticker.StickerDetailScreen
import com.techmomentum.wc2026.ui.team.TeamAlbumScreen

@Composable
fun AppNavigation(
    authRepository: AuthRepository,
    rewardedAdManager: RewardedAdManager,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val authState by authRepository.authState().collectAsState(initial = AppAuthState.Unauthenticated)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isLoggedIn = authState is AppAuthState.SignedIn || authState is AppAuthState.Guest
    val showBottomBar = currentRoute in Routes.bottomBarDestinations

    if (!isLoggedIn && currentRoute != Routes.AUTH && currentRoute != Routes.LOADING) {
        androidx.compose.runtime.LaunchedEffect(Unit) {
            navController.navigate(Routes.AUTH) { popUpTo(0) { inclusive = true } }
        }
    }

    MainScaffold(
        modifier = modifier,
        currentRoute = currentRoute,
        showBottomBar = showBottomBar && isLoggedIn,
        onNavigate = { route ->
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.LOADING,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.LOADING) {
                val bootstrapVm: SessionBootstrapViewModel = hiltViewModel()
                val bootstrapState by bootstrapVm.state.collectAsState()
                when (bootstrapState) {
                    BootstrapState.Loading -> LoadingScreen()
                    is BootstrapState.Error -> {
                        val msg = (bootstrapState as BootstrapState.Error).message
                        ErrorState(
                            message = msg,
                            onRetry = { bootstrapVm.retry() },
                        )
                    }
                    is BootstrapState.Ready -> {
                        val destination = (bootstrapState as BootstrapState.Ready).destinationRoute
                        androidx.compose.runtime.LaunchedEffect(destination) {
                            navController.navigate(destination) {
                                popUpTo(Routes.LOADING) { inclusive = true }
                            }
                        }
                        LoadingScreen()
                    }
                }
            }
            composable(Routes.AUTH) {
                AuthScreen(
                    isGuestMode = authState is AppAuthState.Guest,
                    onAuthenticated = { route ->
                        navController.navigate(route) {
                            popUpTo(Routes.AUTH) { inclusive = true }
                        }
                    },
                )
            }
            composable(Routes.VERIFY_EMAIL) {
                VerifyEmailScreen(
                    onContinue = { route ->
                        navController.navigate(route) {
                            popUpTo(Routes.VERIFY_EMAIL) { inclusive = true }
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
                    rewardedAdManager = rewardedAdManager,
                    isGuest = authState is AppAuthState.Guest,
                )
            }
            composable(Routes.ALBUM) {
                AlbumScreen(onTeamClick = { navController.navigate(Routes.team(it)) })
            }
            composable(Routes.LEADERBOARD) {
                LeaderboardScreen(isGuest = authState is AppAuthState.Guest)
            }
            composable(Routes.SLOT) {
                SlotMachineScreen(rewardedAdManager = rewardedAdManager)
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    isGuest = authState is AppAuthState.Guest,
                    onSignedOut = {
                        navController.navigate(Routes.AUTH) { popUpTo(0) { inclusive = true } }
                    },
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
                PackOpeningScreen(onDone = { navController.popBackStack() })
            }
        }
    }
}
