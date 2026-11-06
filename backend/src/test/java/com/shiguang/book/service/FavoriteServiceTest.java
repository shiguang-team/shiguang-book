package com.shiguang.book.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FavoriteServiceTest {
    @Test
    void testAddAndCheckFavoriteIdempotent() {
        FavoriteService service = new FavoriteService();
        service.addFavorite(1L, 100L);
        service.addFavorite(1L, 100L);
        assertTrue(service.isFavorited(1L, 100L));
    }
}
