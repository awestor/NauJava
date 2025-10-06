package org.example.Tasks2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

public class Task2 {
    private ArrayList<Double> doubleList;

    /**
     * Конструктор, что заполняет список amount
     * количеством переменных формата double
     * в диапазоне от -10 до 10 с точностью до 2-х знаков.
     */
    public Task2(int amount){
        doubleList = (ArrayList<Double>) new Random()
                .doubles(amount, -10.0, 10.0) // N чисел от -10.0 до 10.0
                .map(d -> Math.round(d * 100.0) / 100.0) // Округление до 2 цифр после целой части
                .boxed()
                .collect(Collectors.toList());
    }

    /**
     * Метод сортировки списка пузырьковой сортировкой
     */
    public void sortArrayTask2(){
        int n = doubleList.size();
        System.out.println("\nИсходный массив: " + doubleList);
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (doubleList.get(j) > doubleList.get(j + 1)) {
                    Double temp = doubleList.get(j);
                    doubleList.set(j, doubleList.get(j + 1));
                    doubleList.set(j + 1, temp);
                }
            }
        }
        System.out.println("Отсортированный массив: " + doubleList);
    }
}
