package com.insa.mygamelist.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.insa.mygamelist.R

object IGDB {

    lateinit var covers: List<Cover>
    lateinit var games: List<Game>
    lateinit var genres: List<Genre>
    lateinit var platform_logos: List<Logo>
    lateinit var platforms: List<Platforme>


    fun load(context: Context) {
        val coversFromJson: List<Cover> = Gson().fromJson(
            context.resources.openRawResource(R.raw.covers).bufferedReader(),
            object : TypeToken<List<Cover>>() {}.type
        )

        val gamesFromJson: List<Game> = Gson().fromJson(
            context.resources.openRawResource(R.raw.games).bufferedReader(),
            object : TypeToken<List<Game>>() {}.type
        )

        val genresFromJson: List<Genre> = Gson().fromJson(
            context.resources.openRawResource(R.raw.genres).bufferedReader(),
            object : TypeToken<List<Genre>>() {}.type
        )

        val platform_logosFromJson: List<Logo> = Gson().fromJson(
            context.resources.openRawResource(R.raw.platform_logos).bufferedReader(),
            object : TypeToken<List<Logo>>() {}.type
        )

        val platformsFromJson: List<Platforme> = Gson().fromJson(
            context.resources.openRawResource(R.raw.platforms).bufferedReader(),
            object : TypeToken<List<Platforme>>() {}.type
        )

        covers = coversFromJson
        games = gamesFromJson
        genres = genresFromJson
        platform_logos = platform_logosFromJson
        platforms = platformsFromJson

    }
}

data class Cover(val id: Long, val url: String)
data class Game(val id: Int, val cover: Long, val date: Long, val genres: List<Long>, val name: String, val platforms: List<Int>, val summary: String, val note: Float)
data class Genre(val id: Long, val name: String)
data class Logo(val id: Long, val url: String)
data class Platforme(val id: Long, val name: String, val nb_logo: Int)

