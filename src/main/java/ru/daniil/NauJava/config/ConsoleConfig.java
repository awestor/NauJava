package ru.daniil.NauJava.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.daniil.NauJava.controller.CommandProcessor;

import java.util.Objects;
import java.util.Scanner;

@Configuration
public class ConsoleConfig {
    /*
    @Value("${app.name}")
    private String projectName;
    @Value("${app.version}")
    private String version;
    @Autowired
    private CommandProcessor commandProcessor;
    */
    /**
     * Компонент, что будет модернизировать вывод в консоль.
     * @return активный ввод/вывод данных в консоли
     */

    @Bean
    public CommandLineRunner commandScanner()
    {
        return args ->
        {
            /*try (Scanner scanner = new Scanner(System.in))
            {
                ConsoleInputHelper input = new ConsoleInputHelper(scanner);
                System.out.printf("\nНазвание приложения: %s \nТекущая версия: %s \nВведите команду: " +
                    "\n'create' -> для создания продукта. \n'read' -> для чтения данных продукта." +
                    "\n'update-desc' -> для изменения описания продукта. \n'update-cal' -> для изменения калорийных данных продукта." +
                    "\n'delete' -> для удаления продукта. \n'exit' -> для выхода.\n",
                        projectName, version);
                while (true) {
                    String toDoInput = input.readString("> ").trim();
                    String[] tokens = toDoInput.split(" ");

                    if(Objects.equals(tokens[0], "exit")){
                            System.out.println("Выход из программы...");
                            break;
                    }
                    String fullCommand = "";
                    switch (tokens[0].toLowerCase()) {
                        case "create" -> {
                            long id = input.readLong("Введите ID продукта: ");
                            String name = input.readString("Введите название: ");
                            String description = input.readString("Введите описание: ");
                            double calories = input.readDouble("Введите калории (число): ");

                            fullCommand = String.format("create %d %s %s %s", id, name, description, calories);
                        }
                        case "read" -> {
                            long id = input.readLong("Введите ID продукта: ");
                            fullCommand = String.format("read %d", id);
                        }
                        case "update-desc" -> {
                            long id = input.readLong("Введите ID продукта: ");
                            String newDesc = input.readString("Введите новое описание: ");
                            fullCommand = String.format("update-description %d %s", id, newDesc);
                        }
                        case "update-cal" -> {
                            long id = input.readLong("Введите ID продукта: ");
                            double newCal = input.readDouble("Введите новое количество калорий: ");
                            fullCommand = String.format("update-calories %d %.2f", id, newCal);
                        }
                        case "delete" -> {
                            long id = input.readLong("Введите ID продукта: ");
                            fullCommand = String.format("delete %d", id);
                        }
                        default -> {

                        }
                    }
                    commandProcessor.processCommand(fullCommand);
                }
            }*/
        };
    }
}
