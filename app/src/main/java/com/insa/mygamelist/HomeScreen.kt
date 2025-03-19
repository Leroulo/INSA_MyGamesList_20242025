package com.insa.mygamelist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.insa.mygamelist.data.IGDB

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToDetails: (Int, String, Long, List<Long>, List<Int>, String) -> Unit,
    favoriteGames: MutableState<MutableSet<Int>>,
    searchQuery: String,
    isSearching: Boolean,
    onSearchUpdate: (String, Boolean) -> Unit
) {
    val filteredGames = IGDB.games.filter { game -> //liste filtrée de jeux
        game.name.contains(searchQuery, ignoreCase = true) ||
                game.genres.any { IGDB.genres.find { g -> g.id == it }?.name?.contains(searchQuery, ignoreCase = true) == true } ||
                game.platforms.any { IGDB.platforms.find { p -> p.id.toInt() == it }?.name?.contains(searchQuery, ignoreCase = true) == true } //on regarde si le nom du jeu, ses genres ou ses plateformes comtiennent le filtre de recherche
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = Color(0xfffb8c00),
                    titleContentColor = Color.Black,
                ),
                title = {
                    if (isSearching) { //si on veut faire une recherche, on affiche la barre
                        TextField(
                            value = searchQuery,
                            onValueChange = { onSearchUpdate(it, true) },
                            placeholder = { Text("Rechercher un jeu...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("My Games List", fontWeight = FontWeight.Bold) //sinon on a le nom de l'app
                    }
                },
                actions = {
                    IconButton(onClick = { onSearchUpdate(searchQuery, !isSearching) }) { //pour faire une recherche ou la stopper
                        Icon(
                            imageVector = if (isSearching) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (isSearching) "Fermer la recherche" else "Rechercher"
                        )
                    }
                }
            )
        },
        content = { innerPadding ->
            if (filteredGames.isEmpty()) { //affichage du message si la liste filtrée est vide
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No match :(", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            } else { //sinon on affiche tous les jeux de la liste filtrée
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