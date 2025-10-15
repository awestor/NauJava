package ru.daniil.NauJava.config;

import java.util.Scanner;

public class ConsoleInputHelper {

    private final Scanner scanner;

    public ConsoleInputHelper(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Метод, что используется для чтения целых чисел из консоли
     * @param message сообщение для вывода
     * @return целое число
     */
    public long readLong(String message) {
        while (true) {
            System.out.print(message);
            try {
                return Long.parseLong(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое число.");
            }
        }
    }

    /**
     * Метод, что используется для вещественных чисел из консоли
     * @param message сообщение для вывода
     * @return вещественное число
     */
    public double readDouble(String message) {
        while (true) {
            System.out.print(message);
            try {
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число.");
            }
        }
    }

    /**
     * Метод, что используется для чтения строковых переменных из консоли
     * @param message сообщение для вывода
     * @return строка
     */
    public String readString(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }
}
