package com.example.whatilike.data

data class ArtObject(
    val objectID: Int = 0,
    val primaryImage: String? = null,
    val primaryImageSmall: String? = null,
    val title: String = "",
    val artistDisplayName: String = "",
    val artistNationality: String? = null,
    val artistBeginDate: String? = null,
    val artistEndDate: String? = null,
    val objectDate: String? = null,
    val medium: String? = null,
    val dimensions: String? = null,
    val department: String? = null,
    val culture: String? = null,
    val period: String? = null,
    val objectURL: String? = null,
    val isHighlight: Boolean? = null,
    val isPublicDomain: Boolean? = null,
    var isLiked: Boolean = false
)
