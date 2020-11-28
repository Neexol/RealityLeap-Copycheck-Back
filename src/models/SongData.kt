package ru.rtuitlab.copycheck.models

import com.google.gson.annotations.SerializedName

data class SongData(
    @SerializedName("artist") val artist: String,
    @SerializedName("title") val title: String,
    @SerializedName("album") val album: String,
    @SerializedName("release_date") val releaseDate: String,
    @SerializedName("label") val label: String,
    @SerializedName("timecode") val timeCode: String,
    @SerializedName("song_link") val songLink: String
)