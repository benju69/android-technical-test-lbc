package fr.leboncoin.data.mapper

import fr.leboncoin.data.local.entity.AlbumEntity
import fr.leboncoin.data.network.model.AlbumDto

fun AlbumDto.toEntity(): AlbumEntity {
    return AlbumEntity(
        id = id,
        albumId = albumId,
        title = title,
        url = url,
        thumbnailUrl = thumbnailUrl,
        isFavorite = isFavorite
    )
}

fun AlbumEntity.toDto(): AlbumDto {
    return AlbumDto(
        id = id,
        albumId = albumId,
        title = title,
        url = url,
        thumbnailUrl = thumbnailUrl,
        isFavorite = isFavorite
    )
}

fun List<AlbumDto>.toEntityList(): List<AlbumEntity> {
    return map { it.toEntity() }
}

fun List<AlbumEntity>.toDtoList(): List<AlbumDto> {
    return map { it.toDto() }
}

