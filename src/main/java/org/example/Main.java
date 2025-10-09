package org.example;

import org.example.Tasks2.*;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        //Вариант к выполнению №3
        // Вызов метода для задания номер 1.
        System.out.println("Task1: " +
                "\nВведите количество элементов массива ->");
        int amount = input();
        Task1 task1 = new Task1(amount);
        task1.averageNumberTask1();

        // Вызов метода для задания номер 2.
        System.out.println("\nTask2:" +
                "\nВведите количество элементов списка ->");
        amount = input();
        Task2 task2 = new Task2(amount);
        task2.sortArrayTask2();

        // Вызов метода для задания номер 3.
        System.out.println("\nTask3:");
        Task3 task3 = new Task3();
        task3.sortEmployeesBySalaryTask3();


        // Вызов метода для задания номер 4.
        System.out.println("\nTask4:");
        Task4 task4 = new Task4();
        task4.urlHeadersTask4();

        // Вызов метода для задания номер 5.
        System.out.println("\nTask5:");
        Task5 task5 = new Task5();
        task5.start();
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            System.err.println("Main-поток прерван: " + e.getMessage());
        }
        task5.stop();
    }

    public static int input(){
        Scanner scanner = new Scanner(System.in);
        int number;

        do {
            while (!scanner.hasNextInt()) {
                System.out.println("Ошибка: нужно ввести целое число(Int).");
                scanner.next(); // в случае некорректного ввода совершается пропуск
                System.out.println("Введите повторно -> ");
            }
            number = scanner.nextInt();
            if (number < 0) {
                System.out.println("Ошибка: число не должно быть отрицательным." +
                        "\nВведите повторно -> ");
                }
        } while (number < 0);

        return number;
    }
}