package org.example.Tasks2;
import org.example.Task;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Task5 implements Task {
    private final Path folderStart;
    private final Path folderEnd;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread syncThread;

    public Task5(){
        Path currentDir = Paths.get("").toAbsolutePath().resolve("src/main/java/org/example");
        folderStart = currentDir.resolve("folderStart");
        folderEnd = currentDir.resolve("folderEnd");
    }

    /**
     * Методы для запуска и остановки синхронизации указанных файлов папок
     */
    @Override
    public void start(){
        if (running.get()) {
            System.out.println("Синхронизация уже запущена.");
            return;
        }
        if (!Files.exists(folderStart) || !Files.exists(folderEnd)) {
            System.err.println("Одна из папок не существует: " + folderStart + " или " + folderEnd);
            return;
        }

        running.set(true);
        syncThread = new Thread(() -> {
            try {
                System.out.println("Синхронизация началась...");
                while (running.get()) {
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderStart)) {
                        for (Path file : stream) {
                            Path targetFile = folderEnd.resolve(file.getFileName());
                            Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                            System.out.println("Синхронизирован файл: " + file.getFileName());
                            Thread.sleep(1000); // Пауза между синхронизацией файлов
                        }
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("Синхронизация прервана.");
            } catch (IOException e) {
                System.err.println("Ошибка синхронизации: " + e.getMessage());
            }
        });
        syncThread.start();
    }
    @Override
    public void stop(){
        if (!running.get()) {
            System.out.println("Синхронизация не запущена.");
            return;
        }

        running.set(false);
        syncThread.interrupt();
        try {
            syncThread.join();
            System.out.println("Синхронизация остановлена.");
        } catch (InterruptedException e) {
            System.err.println("Ошибка остановки: " + e.getMessage());
        }
    }
}
