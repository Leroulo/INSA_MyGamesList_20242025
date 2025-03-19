package com.insa.mygamelist

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.insa.mygamelist.MainActivity.Screen

@Composable
fun AppNavHost(
    navController: NavHostController,
    favoriteGames: MutableState<MutableSet<Int>>,
    searchQuery: String,
    isSearching: Boolean,
    onSearchUpdate: (String, Boolean) -> Unit
) {
    NavHost(navController = navController, startDestination = Screen.Home.route) { //les paramètres sont le navController ainsi que l'écran qui doit être affiché en premier
        composable(Screen.Home.route) { //créé la route pour le HomeScreen
            HomeScreen(
                onNavigateToDetails = { gameId, gameName, gameCover, gameGenres, gamePlatformes, gameResume ->
                    navController.navigate(Screen.Details.createRoute(gameId, gameName, gameCover, gameGenres, gamePlatformes, gameResume))
                },
                favoriteGames = favoriteGames,
                searchQuery = searchQuery,
                isSearching = isSearching,
                onSearchUpdate = onSearchUpdate
            )
        }
        composable(Screen.Details.route) { backStackEntry -> //créé la route pour le DetailsScreen
            val gameId = backStackEntry.arguments?.getString("gameId")?.toIntOrNull() ?: 0
            val gameName = backStackEntry.arguments?.getString("gameName")?.let { Uri.decode(it) } ?: ""
            val gameResume = backStackEntry.arguments?.getString("gameResume")?.let { Uri.decode(it) } ?: ""
            val gameCover = backStackEntry.arguments?.getString("gameCover")?.toLongOrNull() ?: 0L
            val gameGenres = backStackEntry.arguments?.getString("gameGenres")?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList()
            val gamePlatformes = backStackEntry.arguments?.getString("gamePlatformes")?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()

            DetailsScreen(gameId, gameName, gameCover, gameGenres, gamePlatformes, gameResume, navController, favoriteGames)
        }
    }
}