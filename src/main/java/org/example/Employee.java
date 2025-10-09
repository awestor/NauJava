package org.example;

public class Employee {
    private String fullName;
    private Integer age;
    private String department;
    private Double salary;

    public Employee(String fullName, Integer age, String department, Double salary) {
        this.fullName = fullName;
        this.age = age;
        this.department = department;
        this.salary = salary;
    }

    /**
     * Гетер для получения данных по зарплате
      */
    public Double getSalary() {
        return salary;
    }

    // Переопределение для вывода, чтобы не делать это
    // через множество гетеров для каждого поля
    @Override
    public String toString() {
        return "Employee{" +
                "fullName='" + fullName + '\'' +
                ", age=" + age +
                ", department='" + department + '\'' +
                ", salary=" + salary +
                '}';
    }
}