package fr.leboncoin.data.mapper

import fr.leboncoin.data.local.entity.AlbumEntity
import fr.leboncoin.data.network.model.AlbumDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlbumMapperTest {

    @Test
    fun `toEntity maps all fields correctly`() {
        val dto = AlbumDto(
            id = 1,
            albumId = 2,
            title = "Test Title",
            url = "https://example.com/image.png",
            thumbnailUrl = "https://example.com/thumb.png",
            isFavorite = true
        )

        val entity = dto.toEntity()

        assertEquals(1, entity.id)
        assertEquals(2, entity.albumId)
        assertEquals("Test Title", entity.title)
        assertEquals("https://example.com/image.png", entity.url)
        assertEquals("https://example.com/thumb.png", entity.thumbnailUrl)
        assertTrue(entity.isFavorite)
    }

    @Test
    fun `toEntity defaults isFavorite to false`() {
        val dto = AlbumDto(
            id = 1, albumId = 1, title = "T", url = "u", thumbnailUrl = "t"
        )

        val entity = dto.toEntity()

        assertFalse(entity.isFavorite)
    }

    @Test
    fun `toDto maps all fields correctly`() {
        val entity = AlbumEntity(
            id = 3,
            albumId = 4,
            title = "Entity Title",
            url = "https://example.com/entity.png",
            thumbnailUrl = "https://example.com/entity_thumb.png",
            isFavorite = true,
            cachedAt = 1234567890L
        )

        val dto = entity.toDto()

        assertEquals(3, dto.id)
        assertEquals(4, dto.albumId)
        assertEquals("Entity Title", dto.title)
        assertEquals("https://example.com/entity.png", dto.url)
        assertEquals("https://example.com/entity_thumb.png", dto.thumbnailUrl)
        assertTrue(dto.isFavorite)
    }

    @Test
    fun `toDto does not carry cachedAt timestamp`() {
        val entity = AlbumEntity(
            id = 1, albumId = 1, title = "T", url = "u", thumbnailUrl = "t",
            cachedAt = 9999L
        )

        val dto = entity.toDto()

        // AlbumDto should not have a cachedAt field
        assertEquals(1, dto.id)
    }

    @Test
    fun `toEntityList maps list correctly`() {
        val dtos = listOf(
            AlbumDto(id = 1, albumId = 1, title = "A1", url = "u1", thumbnailUrl = "t1"),
            AlbumDto(id = 2, albumId = 1, title = "A2", url = "u2", thumbnailUrl = "t2"),
            AlbumDto(id = 3, albumId = 2, title = "A3", url = "u3", thumbnailUrl = "t3"),
        )

        val entities = dtos.toEntityList()

        assertEquals(3, entities.size)
        assertEquals("A1", entities[0].title)
        assertEquals("A2", entities[1].title)
        assertEquals("A3", entities[2].title)
    }

    @Test
    fun `toDtoList maps list correctly`() {
        val entities = listOf(
            AlbumEntity(id = 1, albumId = 1, title = "E1", url = "u1", thumbnailUrl = "t1"),
            AlbumEntity(id = 2, albumId = 1, title = "E2", url = "u2", thumbnailUrl = "t2"),
        )

        val dtos = entities.toDtoList()

        assertEquals(2, dtos.size)
        assertEquals("E1", dtos[0].title)
        assertEquals("E2", dtos[1].title)
    }

    @Test
    fun `toEntityList with empty list returns empty list`() {
        val dtos = emptyList<AlbumDto>()
        val entities = dtos.toEntityList()
        assertTrue(entities.isEmpty())
    }

    @Test
    fun `toDtoList with empty list returns empty list`() {
        val entities = emptyList<AlbumEntity>()
        val dtos = entities.toDtoList()
        assertTrue(dtos.isEmpty())
    }

    @Test
    fun `roundtrip dto to entity to dto preserves data`() {
        val original = AlbumDto(
            id = 42,
            albumId = 7,
            title = "Round Trip",
            url = "https://example.com/rt.png",
            thumbnailUrl = "https://example.com/rt_thumb.png",
            isFavorite = true
        )

        val roundTripped = original.toEntity().toDto()

        assertEquals(original.id, roundTripped.id)
        assertEquals(original.albumId, roundTripped.albumId)
        assertEquals(original.title, roundTripped.title)
        assertEquals(original.url, roundTripped.url)
        assertEquals(original.thumbnailUrl, roundTripped.thumbnailUrl)
        assertEquals(original.isFavorite, roundTripped.isFavorite)
    }
}

