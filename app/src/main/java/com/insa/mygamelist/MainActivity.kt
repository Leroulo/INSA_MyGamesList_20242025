package com.insa.mygamelist

import android.graphics.drawable.Icon
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.insa.mygamelist.data.Game
import com.insa.mygamelist.data.IGDB
import com.insa.mygamelist.ui.theme.MyGamesListTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        IGDB.load(this)
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()

            MyGamesListTheme() {
                Box(modifier = Modifier.fillMaxSize()) {
                    AppNavHost(navController, Modifier)
                }
            }
        }
    }
}

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Details : Screen("details/{gameId}/{gameName}") {
        fun createRoute(gameId: Int, gameName: String) = "details/$gameId/$gameName"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigateToDetails: (Int, String) -> Unit, navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = Color(0xfffb8c00),
                    titleContentColor = Color.Black,
                ),

                    title = {
                        Row() {
                            IconButton(onClick = { navController.navigateUp()}) {
                            }
                            Text("My Games List", fontWeight = FontWeight.Bold,modifier = Modifier.padding(start = 60.dp))
                        }
                }
            )
        },
        content = { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(IGDB.games) { game ->
                    Element(game = game, onClick = { onNavigateToDetails(game.id, game.name) })
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(gameId: Int, gameName: String) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = Color(0xFFFB8200),
                    titleContentColor = Color.Black,
                ),
                title = {
                    Box(modifier = Modifier .padding(start=50.dp)) {
                        Text("$gameName",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis)
                    }
                }
            )
        },
        content = { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                Text("Game ID: $gameId")
            }
        }
    )
}

@Composable
fun Element(game: Game, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp)
            .height(90.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(Color.LightGray)
            .clickable { onClick() }
    ) {
        Row(modifier = Modifier.align(Alignment.CenterStart)) {
            AsyncImage(
                model = "https:" + IGDB.covers.find { it.id == game.cover }?.url,
                contentDescription = null,
                modifier = Modifier
                    .height(80.dp)
                    .padding(10.dp)
            )
            Column {
                Text(
                    text = game.name,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.padding(start = 10.dp, top = 10.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                var listGenre: String = ""
                for (id in game.genres) {
                    val genre: String? = IGDB.genres.find { it.id == id }?.name
                    genre?.let {
                        if (listGenre == "") {
                            listGenre += genre
                        } else {
                            listGenre += ",$genre"
                        }
                    }
                }
                Text(
                    text = "Genres: $listGenre",
                    modifier = Modifier.padding(start = 10.dp, top = 5.dp, bottom = 5.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(onNavigateToDetails = { gameId, gameName ->
                navController.navigate(Screen.Details.createRoute(gameId, gameName))
            },navController)
        }
        composable(Screen.Details.route) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")?.toIntOrNull() ?: 0
            val gameName = backStackEntry.arguments?.getString("gameName") ?: ""
            DetailsScreen(gameId, gameName)
        }
    }
}