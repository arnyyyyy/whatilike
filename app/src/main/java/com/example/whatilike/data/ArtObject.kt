package com.example.whatilike.data

data class ArtObject(
    val objectID: Int,
    val primaryImage: String?,
    val primaryImageSmall: String?,
    val title: String,
    val artistDisplayName: String,
    val artistNationality: String?,
    val artistBeginDate: String?,
    val artistEndDate: String?,
    val objectDate: String?,
    val medium: String?,
    val dimensions: String?,
    val department: String?,
    val culture: String?,
    val period: String?,
    val objectURL: String?,
    val isHighlight: Boolean?,
    val isPublicDomain: Boolean?
)

