package org.example;

import org.example.Tasks2.*;

public class Main {
    public static void main(String[] args) {
        //Вариант к выполнению №3
        // Вызов метода для задания номер 1.
        System.out.println("\nTask1:");
        Task1 task1 = new Task1();
        task1.AverageNumberTask1();

        // Вызов метода для задания номер 2.
        System.out.println("\nTask2:");
        Task2 task2 = new Task2();
        task2.SortArrayTask2();

        // Вызов метода для задания номер 3.
        System.out.println("\nTask3:");
        Task3 task3 = new Task3();
        task3.SortEmployeesBySalaryTask3();


        // Вызов метода для задания номер 4.
        System.out.println("\nTask4:");
        Task4 task4 = new Task4();
        task4.UrlHeadersTask4();

        // Вызов метода для задания номер 5.
        System.out.println("\nTask5:");
        Task5 task5 = new Task5();
        task5.Start();
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            System.err.println("Main-поток прерван: " + e.getMessage());
        }
        task5.Stop();
    }
}