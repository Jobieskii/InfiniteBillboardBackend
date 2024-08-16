package com.github.jobieskii.public_place.worker;

import com.github.jobieskii.public_place.model.TileStruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ScheduledStompRefreshList {
    static Logger logger = LoggerFactory.getLogger(ScheduledStompRefreshList.class);

    public static final Set<TileStruct> synchronizedDirtyClientTilesSet = Collections.synchronizedSet(new HashSet<>());

    private SimpMessagingTemplate template;

    @Autowired
    public ScheduledStompRefreshList(SimpMessagingTemplate template) {
        this.template = template;
    }

    @Scheduled(fixedRate = 1000)
    public void sendMessage() {
        synchronized (synchronizedDirtyClientTilesSet) {
            if (!synchronizedDirtyClientTilesSet.isEmpty()) {
                List<String> updates = synchronizedDirtyClientTilesSet.stream()
                        .filter(e -> e.getLevel()==1)
                        .map(e -> "%d/%d".formatted(e.getX(), e.getY()))
                        .toList();

                logger.info("Sending list of {} updates to /topic/tile-updates", updates.size());

                template.convertAndSend(
                        "/topic/tile-updates", updates
                );
                synchronizedDirtyClientTilesSet.clear();
            }
        }

    }

    public static void putDirtyClientTile(TileStruct tile) {
        synchronized (synchronizedDirtyClientTilesSet) {
            synchronizedDirtyClientTilesSet.add(tile);
        }
    }

    public static void putDirtyClientTiles(Collection<TileStruct> tiles) {
        synchronized (synchronizedDirtyClientTilesSet) {
            synchronizedDirtyClientTilesSet.addAll(tiles);
        }
    }
}
