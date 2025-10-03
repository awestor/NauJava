package org.example.Tasks2;

import java.util.ArrayList;
import java.util.Arrays;

public class Task2 {
    private ArrayList<Double> doubleList;

    public Task2(){
        doubleList = new ArrayList<>(
                Arrays.asList(5.1, 4.4, 3.9, 7.2, 1.4, 2.2, 8.1, 2.7, 1.9)
        );
    }

    /**
     * Метод сортировки массива пузырьковой сортировкой
     */
    public void SortArrayTask2(){
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
