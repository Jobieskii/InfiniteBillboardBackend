package com.github.jobieskii.public_place.component;


import com.github.jobieskii.public_place.worker.TileWorker;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;

@Component
public class ThreadPoolConfig {

    @Value("${TILEWORKER_THREADS:1}")
    private Integer tileworkerThreads;

    private final List<TileWorker> workers = new ArrayList<>();
    private final List<Thread> threads = new ArrayList<>();

    @PostConstruct
    public void startWorkers() {
        // Number of threads you want to run
        int numberOfThreads = tileworkerThreads;

        for (int i = 0; i < numberOfThreads; i++) {
            TileWorker worker = new TileWorker();
            workers.add(worker);

            Thread thread = new Thread(worker, "MyWorkerThread-" + i);
            threads.add(thread);

            thread.start();
        }
    }

    @PreDestroy
    public void stopWorkers() {

        workers.forEach(TileWorker::stop);

        // Wait for all threads to finish
        threads.forEach(thread -> {
            try {
                thread.join(); // Wait for the thread to finish execution
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
}