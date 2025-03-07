package com.insa.mygamelist

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            MyGamesListTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    AppNavHost(navController)
                }
            }
        }
    }
}

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Details : Screen("details/{gameId}/{gameName}/{gameCover}/{gameGenres}/{gamePlatformes}/{gameResume}") {
        fun createRoute(gameId: Int, gameName: String, gameCover: Long, gameGenres: List<Long>, gamePlatformes: List<Int>, gameResume: String): String {
            val genresString = gameGenres.joinToString(",") { it.toString() }
            val platformsString = gamePlatformes.joinToString(",") { it.toString() }
            return "details/$gameId/${Uri.encode(gameName)}/$gameCover/${Uri.encode(genresString)}/${Uri.encode(platformsString)}/${Uri.encode(gameResume)}"
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onNavigateToDetails: (Int, String, Long, List<Long>, List<Int>, String) -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    val filteredGames = IGDB.games.filter { game ->
        game.name.contains(searchQuery, ignoreCase = true) ||
                game.genres.any { IGDB.genres.find { g -> g.id == it }?.name?.contains(searchQuery, ignoreCase = true) == true } ||
                game.platforms.any { IGDB.platforms.find { p -> p.id.toInt() == it }?.name?.contains(searchQuery, ignoreCase = true) == true }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = Color(0xfffb8c00),
                    titleContentColor = Color.Black,
                ),
                title = {
                    if (isSearching) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Rechercher un jeu...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("My Games List", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = { isSearching = !isSearching }) {
                        Icon(
                            imageVector = if (isSearching) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (isSearching) "Fermer la recherche" else "Rechercher"
                        )
                    }
                }
            )
        },
        content = { innerPadding ->
            if (filteredGames.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No match :(", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    items(filteredGames) { game ->
                        Element(game = game, onClick = {
                            onNavigateToDetails(game.id, game.name, game.cover, game.genres, game.platforms, game.summary)
                        })
                    }
                }
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    gameId: Int,
    gameName: String,
    gameCover: Long,
    gameGenres: List<Long>,
    gamePlatformes: List<Int>,
    gameResume: String,
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = Color(0xFFFB8200),
                    titleContentColor = Color.Black,
                ),
                title = {
                    Text(
                        text = gameName,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = gameName,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.padding(vertical = 30.dp),
                    fontSize = 20.sp
                )
                AsyncImage(
                    model = "https:" + IGDB.covers.find { it.id == gameCover }?.url,
                    contentDescription = null,
                    modifier = Modifier.height(300.dp)
                )
                val listGenre = gameGenres.joinToString(", ") { id ->
                    IGDB.genres.find { it.id == id }?.name ?: ""
                }
                Text(
                    text = listGenre,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                Text(
                    text = gameResume,
                    modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState())
                )
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
            Column (modifier = Modifier.weight(1f)){
                Text(
                    text = game.name,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.padding(start = 10.dp, top = 10.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val listGenre = game.genres.joinToString(", ") { id ->
                    IGDB.genres.find { it.id == id }?.name ?: ""
                }
                Text(
                    text = "Genres : $listGenre",
                    modifier = Modifier.padding(start = 10.dp, top = 5.dp, bottom = 5.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton() {
                Image(
                    painter = painterResource(R.drawable.baseline_star_24),
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp)
                )
                Image(
                    painter = painterResource(R.drawable.baseline_star_border_24),
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                onNavigateToDetails = { gameId, gameName, gameCover, gameGenres, gamePlatformes, gameResume ->
                    navController.navigate(Screen.Details.createRoute(gameId, gameName, gameCover, gameGenres, gamePlatformes, gameResume))
                }
            )
        }
        composable(Screen.Details.route) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")?.toIntOrNull() ?: 0
            val gameName = backStackEntry.arguments?.getString("gameName")?.let { Uri.decode(it) } ?: ""
            val gameResume = backStackEntry.arguments?.getString("gameResume")?.let { Uri.decode(it) } ?: ""
            val gameCover = backStackEntry.arguments?.getString("gameCover")?.toLongOrNull() ?: 0L
            val gameGenres = backStackEntry.arguments?.getString("gameGenres")?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList()
            val gamePlatformes = backStackEntry.arguments?.getString("gamePlatformes")?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()

            DetailsScreen(gameId, gameName, gameCover, gameGenres, gamePlatformes, gameResume, navController)
        }
    }
}