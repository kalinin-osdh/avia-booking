package ru.kalinin.flight.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import ru.kalinin.flight.entity.enums.SeatStatus;

@Service
@RequiredArgsConstructor
public class FlightCacheService {
    private final CacheManager cacheManager;
    private static final String FLIGHT_BY_NUMBER_CACHE = "flightByNumber";

    public void evictFlight(String flightNumber) {
        evictFlights(flightNumber);
    }

    public void evictFlights(String... flightNumbers) {
        Cache cache = cacheManager.getCache(FLIGHT_BY_NUMBER_CACHE);

        if(cache == null){
            return;
        }

        for (String flightNumber: flightNumbers){
            cache.evict(flightNumber + ":");

            for (SeatStatus status : SeatStatus.values()){
                cache.evict(flightNumber + ":" + status.name());
            }
        }
    }

}
