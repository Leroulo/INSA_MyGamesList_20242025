package com.insa.mygamelist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.insa.mygamelist.data.Game
import com.insa.mygamelist.data.IGDB

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
            AsyncImage( //affichage image du jeu
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
                    overflow = TextOverflow.Ellipsis //rajoute "..." à la place de la fin du texte si il est trop long
                )
            }
            IconButton(onClick = { //change l'état lors d'un clic
                favoriteGames.value = favoriteGames.value.toMutableSet().apply {
                    if (isFavorite) remove(game.id) else add(game.id)
                }
            }) {
                Image(
                    painter = painterResource(if (isFavorite) R.drawable.baseline_star_24 else R.drawable.baseline_star_border_24), //met l'image pleine ou vide si favori ou non
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}