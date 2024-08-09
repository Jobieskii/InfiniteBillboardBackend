package com.github.jobieskii.public_place.worker;

import com.github.jobieskii.public_place.file_manager.FileManager;
import com.github.jobieskii.public_place.file_manager.PatchData;
import com.github.jobieskii.public_place.model.Tile;
import com.github.jobieskii.public_place.model.TileStruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/*
The background processing of images works as such:
1. Endpoints handler http requests. Receive image in body, calculates crop and offsets according to grid, places
sub-images onto `patchQueue`s
2. Tile worker handles patching and regenerating dirty tiles. Go through queue of dirty tiles prioritizing lower levels,
try lock tile for writing (skip if other thread is already writing to that one), wait lock for reading, add parent to
dirty queue,
	- at level 1 apply modifications from `patchQueue`
	- at higher levels lock children for reading, regenerate tile and unlock children,
unlock tile for reading and writing
 */
public class TileWorker implements Runnable {
    static Logger logger = LoggerFactory.getLogger(TileWorker.class);
    private static final ConcurrentHashMap<TileStruct, ConcurrentLinkedQueue<PatchData>> patchQueues = new ConcurrentHashMap<>();

    private static final PriorityBlockingQueue<TileStruct> dirtyTileQueue = new PriorityBlockingQueue<TileStruct>(16, Comparator.comparingInt(TileStruct::getLevel));
    public static final ConcurrentHashMap<TileStruct, Semaphore> writeLocks = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<TileStruct, Semaphore> readLocks = new ConcurrentHashMap<>();

    private final AtomicBoolean running = new AtomicBoolean(true);

    @Override
    public void run() {
        logger.debug("Starting TileWorker thread");
        while (running.get() || !dirtyTileQueue.isEmpty()) {
            TileStruct t = dirtyTileQueue.poll();
            if (t == null) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                continue;
            }
            if (!writeLocks.get(t).tryAcquire()) {
                logger.debug("{} already locked for writing", t);
                continue;
            }
            logger.debug("locked {} w", t);
            try {
                readLocks.get(t).acquire();
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
                throw new RuntimeException(e);
            }
            logger.debug("locked {} r", t);

            TileStruct p = t.getParent();
            if (p != null) {
                addToDirtyTileQueue(p);
            }
            if (t.getLevel() == 1) {
                applyPatches(t);
            } else {
                regenerateFromChildren(t);
            }
            writeLocks.get(t).release();
            readLocks.get(t).release();
            logger.debug("unlocked {}", t);
        }
    }

    public void stop() {
        running.set(false);
    }

    private void applyPatches(TileStruct t) {
        ConcurrentLinkedQueue<PatchData> patchQueue = patchQueues.get(t);
        while(!patchQueue.isEmpty()) {
            PatchData patch = patchQueue.poll();
            FileManager.patchFile( //TODO: make an applyPatchesOnFile method that can apply multiple patches at once
                    t.getX(),
                    t.getY(),
                    patch.getImage(),
                    patch.getOffsetXPx(),
                    patch.getOffsetYPx()
            );
        }
    }

    private void regenerateFromChildren(TileStruct t) {
        List<TileStruct> children = t.getChildren();
        logger.debug("regenerating {} from children {}", t, children);
        children.forEach(c -> {
            try {
                readLocks.computeIfAbsent(c, e -> new Semaphore(1));
                readLocks.get(c).acquire();
                logger.debug("{} locking child {}", t, c);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        try {
            FileManager.regenerateFromLowerLevel(t.getLevel(), t.getX(), t.getY());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        children.forEach(c -> {
            readLocks.get(c).release();
            logger.debug("{} unlocking child {}", t, c);
        });
    }

    public static void addToPatchQueue(TileStruct tile, PatchData patchData) {
        ConcurrentLinkedQueue<PatchData> patchQueue = TileWorker.patchQueues.computeIfAbsent(tile, k -> new ConcurrentLinkedQueue<>());
        patchQueue.add(patchData);
        addToDirtyTileQueue(tile);
    }

    protected static void addToDirtyTileQueue(TileStruct tile) {
        writeLocks.computeIfAbsent(tile, k -> new Semaphore(1));
        readLocks.computeIfAbsent(tile, k -> new Semaphore(1));
        if (!dirtyTileQueue.contains(tile)) {
            dirtyTileQueue.add(tile);
        }
    }
}
