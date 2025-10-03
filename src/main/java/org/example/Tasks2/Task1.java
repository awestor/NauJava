package org.example.Tasks2;

import java.util.Arrays;

public class Task1 {
    private int[] numbers;

    public Task1(){
         numbers = new int[]{1, 8, 3, 1, 9, 4, 2};
    }

    /**
     * Метод подсчёта среднего значения чисел в массиве
     */
    public void AverageNumberTask1(){
        double average = Arrays.stream(this.numbers).average().orElse(0);

        System.out.println("Исходный массив: " + Arrays.toString(numbers) + "\nСредняя величина: " + average);
    }
}
