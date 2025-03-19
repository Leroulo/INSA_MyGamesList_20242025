package com.insa.mygamelist

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.insa.mygamelist.data.IGDB
import com.insa.mygamelist.ui.theme.MyGamesListTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("MutableCollectionMutableState") //afin d'éviter un avertissement du compliateur
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) //initialise les différents états nécessaires
        IGDB.load(this) //charge les données
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController() //création du contôleur de navigation
            val favoriteGames = rememberSaveable { mutableStateOf(mutableSetOf<Int>()) } //on a le rememberSavable pour mémoriser
            val searchQuery = rememberSaveable { mutableStateOf("") }              //certains états lors de la navigation
            val isSearching = rememberSaveable { mutableStateOf(false) }           //comme les favoris ou les filtres de recherche
            MyGamesListTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    AppNavHost( //composable qui gère la navigation
                        navController = navController,
                        favoriteGames = favoriteGames,
                        searchQuery = searchQuery.value,
                        isSearching = isSearching.value,
                        onSearchUpdate = { query, searching ->
                            searchQuery.value = query
                            isSearching.value = searching
                })
            }
        }
    }
}

sealed class Screen(val route: String) { //classe pour définir les différentes routes dans l'app
    data object Home : Screen("home")
    data object Details : Screen("details/{gameId}/{gameName}/{gameCover}/{gameGenres}/{gamePlatformes}/{gameResume}") {
        fun createRoute(gameId: Int, gameName: String, gameCover: Long, gameGenres: List<Long>, gamePlatformes: List<Int>, gameResume: String): String { //méthode qui construit la route
            val genresString = gameGenres.joinToString(",") { it.toString() }
            val platformsString = gamePlatformes.joinToString(",") { it.toString() }
            return "details/$gameId/${Uri.encode(gameName)}/$gameCover/${Uri.encode(genresString)}/${Uri.encode(platformsString)}/${Uri.encode(gameResume)}"
        }
      }
   }
}