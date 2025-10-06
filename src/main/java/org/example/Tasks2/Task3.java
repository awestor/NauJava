package org.example.Tasks2;

import org.example.Employee;

import java.util.ArrayList;
import java.util.Comparator;

public class Task3 {
    private ArrayList<Employee> employees;

    public Task3(){
        employees = new ArrayList<>();
        employees.add(new Employee("Иванов Дмитрий", 31, "Разработка", 75000.0));
        employees.add(new Employee("Петров Пётр", 27, "Тестирование", 60000.0));
        employees.add(new Employee("Сидорова Анна", 28, "Аналитика", 55000.0));
        employees.add(new Employee("Кузнецов Алексей", 35, "Тестирование", 65000.0));
        employees.add(new Employee("Морозова Елена", 29, "Разработка", 70000.0));
    }

    /**
     * Метод сортировки сотрудников по зарплате
     */
    public void sortEmployeesBySalaryTask3() {
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
}
