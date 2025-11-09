package ru.daniil.NauJava.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.daniil.NauJava.entity.Report;
import ru.daniil.NauJava.entity.ReportData;
import ru.daniil.NauJava.entity.ReportStatus;
import ru.daniil.NauJava.repository.ProductRepository;
import ru.daniil.NauJava.repository.ReportRepository;
import ru.daniil.NauJava.repository.UserRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public ReportService(ReportRepository reportRepository,
                         UserRepository userRepository,
                         ProductRepository productRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    /**
     * Создает новый отчет
     * @return ID созданного отчета
     */
    public Long createReport() {
        Report report = new Report();
        report.setStatus(ReportStatus.CREATED);
        report.setContent("Отчет формируется...");

        Report savedReport = reportRepository.save(report);
        return savedReport.getId();
    }

    /**
     * Получает содержимое отчета по ID
     * @param reportId ID отчета
     * @return содержимое отчета или null если отчет не найден
     */
    public String getReportContent(Long reportId) {
        return reportRepository.findById(reportId)
                .map(Report::getContent)
                .orElse(null);
    }

    /**
     * Получает статус отчета по ID
     * @param reportId ID отчета
     * @return статус отчета или null если отчет не найден
     */
    public ReportStatus getReportStatus(Long reportId) {
        return reportRepository.findById(reportId)
                .map(Report::getStatus)
                .orElse(null);
    }

    /**
     * Получает все отчеты, отсортированные по дате создания
     * @return список отчетов
     */
    public List<Report> getAllReports() {
        return reportRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Асинхронно формирует отчет с использованием нескольких потоков
     * @param reportId ID отчета для формирования
     */
    @Async
    public void generateReportAsync(Long reportId) {
        CompletableFuture.runAsync(() -> {
            try {
                Report report = reportRepository.findById(reportId)
                        .orElseThrow(() -> new RuntimeException("Report not found: " + reportId));

                // Результаты для хранения данных из потоков
                AtomicReference<ReportData> usersResult = new AtomicReference<>();
                AtomicReference<ReportData> productsResult = new AtomicReference<>();
                AtomicReference<Exception> usersException = new AtomicReference<>();
                AtomicReference<Exception> productsException = new AtomicReference<>();
                long totalStartTime = System.currentTimeMillis();

                Thread usersThread = new Thread(() -> {
                    try {
                        Thread.currentThread().setName("Direct-Users-Thread-Counter");
                        long startTime = System.currentTimeMillis();

                        Thread.sleep(20);

                        long userCount = userRepository.count();
                        long executionTime = System.currentTimeMillis() - startTime;

                        usersResult.set(new ReportData(userCount, executionTime));

                    } catch (Exception e) {
                        usersException.set(e);
                    }
                });

                Thread productsThread = new Thread(() -> {
                    try {
                        Thread.currentThread().setName("Direct-Products-Thread-Counter");
                        long startTime = System.currentTimeMillis();

                        Thread.sleep(15);

                        long productCount = productRepository.count();
                        long executionTime = System.currentTimeMillis() - startTime;

                        productsResult.set(new ReportData(productCount, executionTime));

                    } catch (Exception e) {
                        productsException.set(e);
                    }
                });

                usersThread.start();
                productsThread.start();

                try {
                    usersThread.join(30000);
                    productsThread.join(30000);

                    // На случай если потоки не завершились
                    if (usersThread.isAlive() || productsThread.isAlive()) {
                        throw new RuntimeException("Thread execution timeout");
                    }

                    if (usersException.get() != null) {
                        throw new RuntimeException("User calculation failed", usersException.get());
                    }
                    if (productsException.get() != null) {
                        throw new RuntimeException("Product calculation failed", productsException.get());
                    }

                    long totalExecutionTime = System.currentTimeMillis() - totalStartTime;

                    ReportData usersData = usersResult.get();
                    ReportData productsData = productsResult.get();

                    if (usersData == null || productsData == null) {
                        throw new RuntimeException("One of the calculations returned null result");
                    }

                    String content = String.format(
                            """
                                    Отчет сформирован успешно!
                                    
                                    Статистика системы:
                                    • Всего пользователей: %d
                                    • Всего продуктов: %d
                                    
                                    Время выполнения операций:
                                    • Подсчет пользователей: %d мс
                                    • Подсчет продуктов: %d мс
                                    • Общее время: %d мс""",
                            usersData.count(), productsData.count(),
                            usersData.executionTime(), productsData.executionTime(),
                            totalExecutionTime
                    );

                    report.setStatus(ReportStatus.COMPLETED);
                    report.setContent(content);
                    report.setCompletedAt(java.time.LocalDateTime.now());
                    report.setExecutionTimeUsers(usersData.executionTime());
                    report.setExecutionTimeProducts(productsData.executionTime());

                    reportRepository.save(report);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Report generation interrupted", e);
                }

            } catch (Exception e) {
                reportRepository.findById(reportId).ifPresent(report -> {
                    report.setStatus(ReportStatus.ERROR);
                    report.setContent("Ошибка при формировании отчета: " + e.getMessage());
                    reportRepository.save(report);
                });
            }
        });
    }
}