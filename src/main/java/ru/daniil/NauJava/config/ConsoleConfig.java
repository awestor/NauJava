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
    @Value("${app.name}")
    private String projectName;
    @Value("${app.version}")
    private String version;
    @Autowired
    private CommandProcessor commandProcessor;

    /**
     * Компонент, что будет модернизировать вывод в консоль.
     */
    @Bean
    public CommandLineRunner commandScanner()
    {
        return args ->
        {
            try (Scanner scanner = new Scanner(System.in))
            {
                System.out.println("Название приложения: " + projectName +
                                "\nТекущая версия: " + version +
                                "\nВведите команду: " +
                                "\n'create' -> для создания продукта." +
                                "\n'read' -> для чтения данных продукта." +
                                "\n'update-desc' -> для изменения описания продукта." +
                                "\n'update-cal' -> для изменения калорийных данных продукта." +
                                "\n'delete' -> для удаления продукта." +
                                "\n'exit' -> для выхода.");
                while (true) {
                    System.out.print("> ");
                    String input = scanner.nextLine().trim();
                    String[] tokens = input.split(" ");

                    if(Objects.equals(tokens[0], "exit")){
                            System.out.println("Выход из программы...");
                            break;
                    }
                    String fullCommand = "";
                    switch (tokens[0].toLowerCase()) {
                        case "create" -> {
                            try {
                                System.out.print("Введите ID (целое число): ");
                                long id = Long.parseLong(scanner.nextLine());

                                System.out.print("Введите название: ");
                                String name = scanner.nextLine().trim();

                                System.out.print("Введите описание: ");
                                String description = scanner.nextLine().trim();

                                System.out.print("Введите калории (число): ");
                                double calories = Double.parseDouble(scanner.nextLine());

                                fullCommand = String.format("create %d %s %s %s", id, name, description, calories);

                            } catch (NumberFormatException e) {
                                System.out.println("Ошибка: ID и калории должны быть числовыми значениями.");
                            }
                        }
                        case "read" -> {
                            try {
                                System.out.print("Введите ID продукта: ");
                                long id = Long.parseLong(scanner.nextLine());
                                fullCommand = String.format("read %d", id);
                            } catch (NumberFormatException e) {
                                System.out.println("Ошибка: ID должен быть числом.");
                            }
                        }
                        case "update-desc" -> {
                            try {
                                System.out.print("Введите ID продукта: ");
                                long id = Long.parseLong(scanner.nextLine());

                                System.out.print("Введите новое описание: ");
                                String newDesc = scanner.nextLine().trim();

                                commandProcessor.processCommand(String.format("update-description %d %s", id, newDesc));
                            } catch (NumberFormatException e) {
                                System.out.println("Ошибка: ID должен быть числом.");
                            }
                        }
                        case "update-cal" -> {
                            try {
                                System.out.print("Введите ID продукта: ");
                                long id = Long.parseLong(scanner.nextLine());

                                System.out.print("Введите новое количество калорий: ");
                                String newDesc = scanner.nextLine().trim();

                                commandProcessor.processCommand(String.format("update-calories %d %s", id, newDesc));
                            } catch (NumberFormatException e) {
                                System.out.println("Ошибка: ID должен быть числом.");
                            }
                        }
                        case "delete" -> {
                            try {
                                System.out.print("Введите ID продукта: ");
                                long id = Long.parseLong(scanner.nextLine());
                                fullCommand = String.format("delete %d", id);
                            } catch (NumberFormatException e) {
                                System.out.println("Ошибка: ID должен быть числом.");
                            }
                        }
                        default -> {

                        }
                    }
                    commandProcessor.processCommand(fullCommand);
                }
            }
        };
    }
}
