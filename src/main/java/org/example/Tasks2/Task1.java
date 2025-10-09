package org.example.Tasks2;

import java.util.Arrays;
import java.util.Random;

public class Task1 {
    private final int[] numbers;

    /**
     * Конструктор, что заполняет массив amount
     * количеством переменных формата int
     * в диапазоне от -100 до 100.
     */
    public Task1(int amount){
        this.numbers = new int[amount];
        Random random = new Random();
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = random.nextInt(200) - 100;
        }
    }

    /**
     * Метод подсчёта среднего значения чисел в массиве
     */
    public void averageNumberTask1(){
        double average = Arrays.stream(this.numbers).average().orElse(0);

        System.out.println("Исходный массив: " + Arrays.toString(numbers) + "\nСредняя величина: " + average);
    }
}
