package org.example.Tasks2;

import org.example.FileSyncTask;
import org.example.Task;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Task5 {
    private Task syncTask;

    public Task5(){
        Path currentDir = Paths.get("").toAbsolutePath().resolve("src/main/java/org/example");
        String folderStart = currentDir.resolve("folderStart").toString();
        String folderEnd = currentDir.resolve("folderEnd").toString();
        syncTask = new FileSyncTask(
                folderStart, folderEnd
        );
    }

    /**
     * Методы для запуска и остановки синхронизации указанных файлов папок
     */
    public void Start(){
        syncTask.start();
    }
    public void Stop(){
        syncTask.stop();
    }
}
