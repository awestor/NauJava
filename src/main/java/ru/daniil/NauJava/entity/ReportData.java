package ru.daniil.NauJava.entity;

/**
 * Класс, используемый для хранения информации при формировании отчёта
 * @param count количество анализируемого предмета
 * @param executionTime время выполнения операции
 */
public record ReportData(long count, long executionTime) {
}
