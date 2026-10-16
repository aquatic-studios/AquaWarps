package com.aquaticstudios.aquawarps.database;

import com.aquaticstudios.aquawarps.model.PlayerData;
import com.aquaticstudios.aquawarps.model.Rating;
import com.aquaticstudios.aquawarps.model.Report;
import com.aquaticstudios.aquawarps.model.Warp;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface StorageProvider {

    CompletableFuture<Void> init();

    CompletableFuture<List<Warp>> loadWarps();

    CompletableFuture<Void> saveWarp(Warp warp);

    CompletableFuture<Void> saveWarps(Collection<Warp> warps);

    CompletableFuture<Void> deleteWarp(Warp warp);

    CompletableFuture<PlayerData> loadPlayer(UUID uuid, String name);

    CompletableFuture<Void> savePlayer(PlayerData data);

    CompletableFuture<Map<UUID, Long>> loadLastSeen();

    CompletableFuture<List<Rating>> loadRatings(String warpId);

    CompletableFuture<Map<String, Rating>> loadPlayerRatings(UUID player);

    CompletableFuture<Map<String, Integer>> loadFavoriteCounts();

    CompletableFuture<Void> saveRating(Rating rating);

    CompletableFuture<Void> deleteRating(String warpId, UUID player);

    CompletableFuture<List<Report>> loadReports();

    CompletableFuture<Void> saveReport(Report report);

    CompletableFuture<List<Visit>> loadVisits(long since);

    CompletableFuture<Void> saveVisit(String warpId, UUID player);

    CompletableFuture<Void> purgeVisits(long before);

    CompletableFuture<Long> count(String table);

    void shutdown();
}
