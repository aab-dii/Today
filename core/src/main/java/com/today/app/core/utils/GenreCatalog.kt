package com.today.app.core.utils

object GenreCatalog {

    private val names = mapOf(
        28 to "Action",
        12 to "Adventure",
        16 to "Animation",
        35 to "Comedy",
        80 to "Crime",
        99 to "Documentary",
        18 to "Drama",
        10751 to "Family",
        14 to "Fantasy",
        36 to "History",
        27 to "Horror",
        10402 to "Music",
        9648 to "Mystery",
        10749 to "Romance",
        878 to "Sci-Fi",
        10770 to "TV Movie",
        53 to "Thriller",
        10752 to "War",
        37 to "Western",
        10759 to "Action & Adventure",
        10762 to "Kids",
        10763 to "News",
        10764 to "Reality",
        10765 to "Sci-Fi & Fantasy",
        10766 to "Soap",
        10767 to "Talk",
        10768 to "War & Politics"
    )

    fun name(id: Int): String? = names[id]

    fun namesFromCsv(csv: String): List<String> =
        csv.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .mapNotNull { names[it] }

    fun idForName(genreName: String): Int? =
        names.entries.firstOrNull { it.value.equals(genreName, ignoreCase = true) }?.key

    fun idsCsvFromNames(genreNames: List<String>): String =
        genreNames.mapNotNull { idForName(it) }.joinToString(", ")
}
