package com.insa.mygamelist

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.insa.mygamelist.data.IGDB

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
                    IconButton(onClick = { navController.popBackStack() }) {  //bouton retour
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    val isFavorite = favoriteGames.value.contains(gameId) //favoris
                    IconButton(onClick = {
                        favoriteGames.value = favoriteGames.value.toMutableSet().apply {
                            if (isFavorite) remove(gameId) else add(gameId)
                        }
                    }) {
                        Image(
                            painter = painterResource(if (isFavorite) R.drawable.baseline_star_24 else R.drawable.baseline_star_border_24), //utilisation des deux dessins créés pour sélectionner ou non les jeux favoris
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

                AsyncImage( //image du jeu
                    model = "https:" + IGDB.covers.find { it.id == gameCover }?.url,
                    contentDescription = null,
                    modifier = Modifier.height(300.dp)
                )

                val listLogo = mutableListOf<String?>() //liste des logos

                for (id in gamePlatformes) { //pour chaque plateforme, on trouve son logo grâce à son ID "id"
                    val platform = IGDB.platforms.find { it.id == id.toLong() }

                    val idLogo: Int? = platform?.platform_logo

                    idLogo?.let {
                        val logoUrl = IGDB.platform_logos.find { it.id.toInt() == idLogo }?.url
                        listLogo.add(logoUrl) //on ajoute les urls correspondants aux id dans la liste
                    }
                }

                LazyRow(
                    modifier = Modifier
                        .padding(all = 20.dp)
                        .height(100.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(listLogo.filterNotNull()) { logo ->
                        AsyncImage( //affichage des logos
                            model = "https:$logo",
                            contentDescription = "Platform Logo",
                            modifier = Modifier
                                .width(100.dp)
                                .padding(10.dp)
                                .wrapContentHeight(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                val listGenre = gameGenres.joinToString(", ") { id ->
                    IGDB.genres.find { it.id == id }?.name ?: "" //on converti les id en string (nom du genre)
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