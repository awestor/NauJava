package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello World again");
        //Вариант к выполнению №3
        int[] numbers = {1, 8, 3, 1, 9, 4, 2};
        ArrayList<Double> doubleList = new ArrayList<>(
                Arrays.asList(5.1, 4.4, 3.9, 7.2, 1.4, 2.2, 8.1, 2.7, 1.9)
        );

        ArrayList<Employee> employees = new ArrayList<>();
        employees.add(new Employee("Иванов Дмитрий", 31, "Разработка", 75000.0));
        employees.add(new Employee("Петров Пётр", 27, "Тестирование", 60000.0));
        employees.add(new Employee("Сидорова Анна", 28, "Аналитика", 55000.0));
        employees.add(new Employee("Кузнецов Алексей", 35, "Тестирование", 65000.0));
        employees.add(new Employee("Морозова Елена", 29, "Разработка", 70000.0));

        // Вызов метода для задания номер 1.
        AverageNumberTask1(numbers);

        // Вызов метода для задания номер 2.
        SortArrayTask2(doubleList);

        // Вызов метода для задания номер 3.
        SortEmployeesBySalaryTask3(employees);

        // Вызов метода для задания номер 4.
        UrlHeadersTask4("https://httpbin.org/headers");

        FileSyncTask5();
    }

    /**
     * Метод подсчёта среднего значения чисел в массиве
     */
    public static void AverageNumberTask1(int[] numbers){
        double average = Arrays.stream(numbers).average().orElse(0);

        System.out.println("Исходный массив: " + Arrays.toString(numbers) + "\nСредняя величина: " + average);
    }

    /**
     * Метод сортировки массива пузырьковой сортировкой
     */
    public static void SortArrayTask2(ArrayList<Double> doubleList){
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
        System.out.println("Средняя величина: " + doubleList);
    }

    /**
     * Метод сортировки сотрудников по зарплате
     */
    public static void SortEmployeesBySalaryTask3(ArrayList<Employee> employees) {
        System.out.println("Исходный порядок сотрудников: ");
        for (Employee emp : employees) {
            System.out.println(emp);
        }

        // Сортировка и вывод в консоль отсортированного списка
        System.out.println("\nСотрудники, отсортированные по зарплате: ");
        employees.stream()
            .sorted(Comparator.comparing(Employee::getSalary))
            .forEach(System.out::println);

    }

    /**
     * Метод, что отправляет запрос на указанный URL,
     * а после возвращающий заголовки запроса
     */
    public static void UrlHeadersTask4(String urlToOpen){
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlToOpen))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            HttpBinResponse parsed = mapper.readValue(response.body(), HttpBinResponse.class);

            String result = String.join(", ", parsed.getHeaders().values());
            System.out.println("Заголовки запроса: " + result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод синхронизации указанных папок
     */
    public static void FileSyncTask5(){
        Path currentDir = Paths.get("").toAbsolutePath().resolve("src/main/java/org/example");
        String folderStart = currentDir.resolve("folderStart").toString();
        String folderEnd = currentDir.resolve("folderEnd").toString();
        Task syncTask = new FileSyncTask(
                folderStart, folderEnd
        );
        syncTask.start();

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            System.err.println("Main-поток прерван: " + e.getMessage());
        }

        syncTask.stop();
    }
}