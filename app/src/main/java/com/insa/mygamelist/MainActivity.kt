package com.insa.mygamelist

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
            val favoriteGames = rememberSaveable { mutableStateOf(mutableSetOf<Int>()) }
            val searchQuery = rememberSaveable { mutableStateOf("") }
            val isSearching = rememberSaveable { mutableStateOf(false) }
            MyGamesListTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    AppNavHost(
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
    onNavigateToDetails: (Int, String, Long, List<Long>, List<Int>, String) -> Unit,
    favoriteGames: MutableState<MutableSet<Int>>,
    searchQuery: String,
    isSearching: Boolean,
    onSearchUpdate: (String, Boolean) -> Unit
) {
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
                            onValueChange = { onSearchUpdate(it, true) },
                            placeholder = { Text("Rechercher un jeu...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("My Games List", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = { onSearchUpdate(searchQuery, !isSearching) }) {
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
                        }, favoriteGames)
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
    navController: NavController,
    favoriteGames: MutableState<MutableSet<Int>>
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
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    val isFavorite = favoriteGames.value.contains(gameId)
                    IconButton(onClick = {
                        favoriteGames.value = favoriteGames.value.toMutableSet().apply {
                            if (isFavorite) remove(gameId) else add(gameId)
                        }
                    }) {
                        Image(
                            painter = painterResource(if (isFavorite) R.drawable.baseline_star_24 else R.drawable.baseline_star_border_24),
                            contentDescription = "Toggle Favorite"
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

                val listLogo = mutableListOf<String?>()
                for (id in gamePlatformes) {
                    val idLogo: Int? = IGDB.platforms.find { it.id.toInt() == id }?.nb_logo;
                    idLogo?.let {
                        listLogo.add(IGDB.platform_logos.find { it.id.toInt() == idLogo }?.url)
                    }
                }

                LazyRow(
                    modifier = Modifier.padding(all = 20.dp).height(100.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(listLogo) { logo ->
                        AsyncImage(
                            model = "https:" + logo,
                            contentDescription = null,
                            modifier = Modifier.width(100.dp)
                                .padding(10.dp)
                                .wrapContentHeight(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

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
fun Element(game: Game, onClick: () -> Unit, favoriteGames: MutableState<MutableSet<Int>>) {
    val isFavorite = favoriteGames.value.contains(game.id)

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
            Column(modifier = Modifier.weight(1f)) {
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
            IconButton(onClick = {
                favoriteGames.value = favoriteGames.value.toMutableSet().apply {
                    if (isFavorite) remove(game.id) else add(game.id)
                }
            }) {
                Image(
                    painter = painterResource(if (isFavorite) R.drawable.baseline_star_24 else R.drawable.baseline_star_border_24),
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    favoriteGames: MutableState<MutableSet<Int>>,
    searchQuery: String,
    isSearching: Boolean,
    onSearchUpdate: (String, Boolean) -> Unit
) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                onNavigateToDetails = { gameId, gameName, gameCover, gameGenres, gamePlatformes, gameResume ->
                    navController.navigate(Screen.Details.createRoute(gameId, gameName, gameCover, gameGenres, gamePlatformes, gameResume))
                },
                favoriteGames = favoriteGames,
                searchQuery = searchQuery,
                isSearching = isSearching,
                onSearchUpdate = onSearchUpdate
            )
        }
        composable(Screen.Details.route) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")?.toIntOrNull() ?: 0
            val gameName = backStackEntry.arguments?.getString("gameName")?.let { Uri.decode(it) } ?: ""
            val gameResume = backStackEntry.arguments?.getString("gameResume")?.let { Uri.decode(it) } ?: ""
            val gameCover = backStackEntry.arguments?.getString("gameCover")?.toLongOrNull() ?: 0L
            val gameGenres = backStackEntry.arguments?.getString("gameGenres")?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList()
            val gamePlatformes = backStackEntry.arguments?.getString("gamePlatformes")?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()

            DetailsScreen(gameId, gameName, gameCover, gameGenres, gamePlatformes, gameResume, navController, favoriteGames)
        }
    }
}}