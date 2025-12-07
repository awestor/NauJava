let currentReportId = null;
let currentCalendarMode = 'start';
let calendarCurrentDate = new Date();

document.addEventListener('DOMContentLoaded', function() {
    initializeCalendar();
    updateReportsStats();
    setupTableSorting();
    setupEventListeners();

    setInterval(updateReportsStats, 30000);
});

function initializeCalendar() {
    updateCalendarDisplay();

    const today = new Date().toISOString().split('T')[0];
    document.getElementById('startDate').value = today;
    document.getElementById('endDate').value = today;

    checkExistingReport();
}

function setupEventListeners() {
    document.getElementById('startDate').addEventListener('change', checkExistingReport);
    document.getElementById('endDate').addEventListener('change', checkExistingReport);

    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('calendar-day') &&
            !e.target.classList.contains('disabled') &&
            !e.target.classList.contains('other-month')) {
            selectCalendarDate(e.target);
        }
    });

    document.getElementById('calendarModal').addEventListener('click', function(e) {
        if (e.target === this) {
            closeCalendar();
        }
    });

    document.getElementById('reportViewModal').addEventListener('click', function(e) {
        if (e.target === this) {
            closeReportModal();
        }
    });
}

function openCalendar(mode) {
    currentCalendarMode = mode;
    calendarCurrentDate = new Date();

    const title = document.getElementById('calendarTitle');
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;

    if (mode === 'start') {
        title.textContent = 'Выберите дату начала';
        if (startDate) {
            calendarCurrentDate = new Date(startDate);
        }
    } else {
        title.textContent = 'Выберите дату окончания';
        if (endDate) {
            calendarCurrentDate = new Date(endDate);
        }
    }

    updateCalendarDisplay();
    document.getElementById('calendarModal').classList.add('active');
}

function closeCalendar() {
    document.getElementById('calendarModal').classList.remove('active');
}

function changeCalendarMonth(delta) {
    calendarCurrentDate.setMonth(calendarCurrentDate.getMonth() + delta);
    updateCalendarDisplay();
}

function updateCalendarDisplay() {
    const monthElement = document.getElementById('calendarMonth');
    const calendarContainer = document.getElementById('calendarContainer');

    const monthNames = [
        'Январь', 'Февраль', 'Март', 'Апрель', 'Май', 'Июнь',
        'Июль', 'Август', 'Сентябрь', 'Октябрь', 'Ноябрь', 'Декабрь'
    ];

    const year = calendarCurrentDate.getFullYear();
    const month = calendarCurrentDate.getMonth();

    monthElement.textContent = `${monthNames[month]} ${year}`;

    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const daysInMonth = lastDay.getDate();
    const startingDay = firstDay.getDay();

    const today = new Date();
    const todayStr = today.toISOString().split('T')[0];

    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;

    const weekdays = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];
    let calendarHTML = '';

    weekdays.forEach(day => {
        calendarHTML += `<div class="calendar-day-header">${day}</div>`;
    });

    for (let i = 0; i < (startingDay === 0 ? 6 : startingDay - 1); i++) {
        calendarHTML += '<div class="calendar-day other-month"></div>';
    }

    for (let day = 1; day <= daysInMonth; day++) {
        const date = new Date(year, month, day + 1);
        const dateStr = date.toISOString().split('T')[0];

        let dayClass = 'calendar-day';

        if (dateStr === todayStr) {
            dayClass += ' today';
        }

        if (dateStr === startDate || dateStr === endDate) {
            dayClass += ' selected';
        }

        if (date > today && !(dateStr === todayStr)) {
            dayClass += ' disabled';
        }

        calendarHTML += `<div class="${dayClass}" data-date="${dateStr}">${day}</div>`;
    }

    calendarContainer.innerHTML = calendarHTML;
}

function selectCalendarDate(element) {
    const selectedDate = element.getAttribute('data-date');

    if (currentCalendarMode === 'start') {
        document.getElementById('startDate').value = selectedDate;
    } else {
        document.getElementById('endDate').value = selectedDate;
    }

    closeCalendar();
    checkExistingReport();
}

async function checkExistingReport() {
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;
    const warningElement = document.getElementById('existingReportWarning');

    if (!startDate || !endDate) {
        warningElement.style.display = 'none';
        return;
    }

    try {
        const response = await fetch(`/admin/api/reports/check?startDate=${startDate}&endDate=${endDate}`);
        if (response.ok) {
            const exists = await response.json();
            warningElement.style.display = exists ? 'block' : 'none';
        }
    } catch (error) {
        console.error('Error checking report:', error);
    }
}

async function createReport() {
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;
    const createBtn = document.getElementById('createReportBtn');

    if (!startDate || !endDate) {
        showNotification('Выберите даты начала и окончания периода', 'error');
        return;
    }

    if (new Date(startDate) > new Date(endDate)) {
        showNotification('Дата начала не может быть позже даты окончания', 'error');
        return;
    }

    const today = new Date().toISOString().split('T')[0];
    if (new Date(endDate) > new Date(today)) {
        showNotification('Дата окончания не может быть в будущем', 'error');
        return;
    }

    const originalText = createBtn.innerHTML;
    createBtn.innerHTML = '<div class="loading-spinner small"></div>';
    createBtn.disabled = true;

    try {
        const response = await fetch('/admin/api/reports/generate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': getCsrfToken()
            },
            body: JSON.stringify({
                startDate: startDate,
                endDate: endDate
            })
        });

        const result = await response.json();

        if (response.ok && result.status === 'success') {
            showNotification('Формирование отчёта начато', 'success');

            setTimeout(() => {
                location.reload();
            }, 1500);
        } else {
            showNotification(result.message || 'Ошибка при создании отчёта', 'error');
        }
    } catch (error) {
        showNotification('Ошибка при создании отчёта: ' + error.message, 'error');
    } finally {
        createBtn.innerHTML = originalText;
        createBtn.disabled = false;
    }
}

function viewReport(reportId) {
    currentReportId = reportId;
    openReportModal(reportId);
}

async function openReportModal(reportId) {
    const modal = document.getElementById('reportViewModal');
    const loadingElement = document.getElementById('reportLoading');
    const contentElement = document.getElementById('reportContent');
    const downloadBtn = document.getElementById('downloadReportBtn');

    contentElement.style.display = 'none';
    loadingElement.style.display = 'flex';

    modal.classList.add('active');

    try {
        const reportResponse = await fetch(`/admin/api/reports/${reportId}/data`);
        if (!reportResponse.ok) {
            throw new Error('Отчёт не найден');
        }

        const report = await reportResponse.json();

        document.getElementById('reportPeriod').textContent =
            report.periodStart + ' - ' + report.periodEnd;

        document.getElementById('reportExecutionTime').textContent =
            report.totalExecutionTime ? `${report.totalExecutionTime} мс` : '-';

        const statusBadge = document.getElementById('reportStatusBadge');
        statusBadge.className = 'status-badge';
        statusBadge.textContent = getStatusText(report.status);

        if (report.status === 'COMPLETED') {
            statusBadge.classList.add('status-completed');
        } else if (report.status === 'PROCESSING') {
            statusBadge.classList.add('status-processing');
        } else if (report.status === 'ERROR') {
            statusBadge.classList.add('status-error');
        } else {
            statusBadge.classList.add('status-created');
        }

        const contentResponse = await fetch(`/admin/api/reports/${reportId}/content`);
        if (contentResponse.ok) {
            const content = await contentResponse.text();
            contentElement.textContent = content;
        } else {
            contentElement.textContent = 'Не удалось загрузить содержимое отчёта';
        }

        downloadBtn.style.display = report.status === 'COMPLETED' ? 'inline-flex' : 'none';

    } catch (error) {
        contentElement.textContent = 'Ошибка при загрузке отчёта: ' + error.message;
    } finally {
        loadingElement.style.display = 'none';
        contentElement.style.display = 'block';
    }
}

async function refreshReportContent() {
    if (!currentReportId) return;

    const loadingElement = document.getElementById('reportLoading');
    const contentElement = document.getElementById('reportContent');

    contentElement.style.display = 'none';
    loadingElement.style.display = 'flex';

    try {
        const response = await fetch(`/admin/api/reports/${currentReportId}/content`);
        if (response.ok) {
            const content = await response.text();
            contentElement.textContent = content;
        }
    } catch (error) {
        contentElement.textContent = 'Ошибка при обновлении отчёта: ' + error.message;
    } finally {
        loadingElement.style.display = 'none';
        contentElement.style.display = 'block';
    }
}

function closeReportModal() {
    document.getElementById('reportViewModal').classList.remove('active');
    currentReportId = null;
}

function showLatestReport() {
    fetch('/admin/api/reports/latest')
        .then(response => {
            if (response.ok) {
                return response.json();
            }
            throw new Error('Отчёт не найден');
        })
        .then(report => {
            viewReport(report.id);
        })
        .catch(error => {
            showNotification('Не удалось загрузить последний отчёт: ' + error.message, 'error');
        });
}

function showAllReports() {
    const tableBody = document.getElementById('reportsTableBody');
    tableBody.scrollIntoView({ behavior: 'smooth' });
}

function showWeekReport() {
    const endDate = new Date();
    const startDate = new Date();
    startDate.setDate(endDate.getDate() - 6);

    document.getElementById('startDate').value = startDate.toISOString().split('T')[0];
    document.getElementById('endDate').value = endDate.toISOString().split('T')[0];

    checkExistingReport();
    showNotification('Установлен период за последнюю неделю', 'info');
}

function showMonthReport() {
    const endDate = new Date();
    const startDate = new Date(endDate.getFullYear(), endDate.getMonth(), 1);

    document.getElementById('startDate').value = startDate.toISOString().split('T')[0];
    document.getElementById('endDate').value = endDate.toISOString().split('T')[0];

    checkExistingReport();
    showNotification('Установлен период за текущий месяц', 'info');
}

function refreshReportsList() {
    location.reload();
}

// Сортировка таблицы
function setupTableSorting() {
    const table = document.querySelector('.reports-table');
    const headers = table.querySelectorAll('th.sortable');
    let currentSort = {
        column: 'created',
        direction: 'asc'
    };

    headers.forEach(header => {
        header.addEventListener('click', function() {
            const column = this.getAttribute('data-sort');
            sortTable(column);
        });
    });

    sortTable('created');

    function sortTable(column) {
        if (currentSort.column === column) {
            currentSort.direction = currentSort.direction === 'asc' ? 'desc' : 'asc';
        } else {
            currentSort.column = column;
            currentSort.direction = 'asc';
        }

        const tbody = table.querySelector('tbody');
        const rows = Array.from(tbody.querySelectorAll('tr'));

        rows.sort((a, b) => {
            if (column === 'created') {
                const aDateStr = a.cells[2].querySelector('.date-time > div:first-child').textContent.trim() +
                               ' ' + a.cells[2].querySelector('.time-text').textContent.trim();
                const bDateStr = b.cells[2].querySelector('.date-time > div:first-child').textContent.trim() +
                               ' ' + b.cells[2].querySelector('.time-text').textContent.trim();

                const aDate = parseDateTimeString(aDateStr);
                const bDate = parseDateTimeString(bDateStr);

                return currentSort.direction === 'asc' ? aDate - bDate : bDate - aDate;
            }

            if (column === 'period') {
                const aPeriodStr = a.cells[0].querySelector('.period-dates').textContent.trim();
                const bPeriodStr = b.cells[0].querySelector('.period-dates').textContent.trim();

                const aDaysCount = calculatePeriodDays(aPeriodStr);
                const bDaysCount = calculatePeriodDays(bPeriodStr);

                return currentSort.direction === 'asc' ? aDaysCount - bDaysCount : bDaysCount - aDaysCount;
            }

            return 0;
        });

        tbody.innerHTML = '';
        rows.forEach(row => tbody.appendChild(row));

        updateSortIndicators(column);
    }

    function parseDateTimeString(dateTimeStr) {
        const [datePart, timePart] = dateTimeStr.split(' ');
        const [year, month, day] = datePart.split('.').map(Number);
        const [hours, minutes, seconds] = timePart.split(':').map(Number);

        return new Date(year, month - 1, day, hours, minutes, seconds);
    }

    function calculatePeriodDays(periodStr) {
        const [startStr, endStr] = periodStr.split(' - ');

        const [startYear, startMonth, startDay] = startStr.split('.').map(Number);
        const [endYear, endMonth, endDay] = endStr.split('.').map(Number);

        const startDate = new Date(startYear, startMonth - 1, startDay);
        const endDate = new Date(endYear, endMonth - 1, endDay);

        const timeDiff = endDate.getTime() - startDate.getTime();
        return Math.floor(timeDiff / (1000 * 3600 * 24)) + 1;
    }

    function updateSortIndicators(activeColumn) {
        headers.forEach(header => {
            header.classList.remove('sort-asc', 'sort-desc');
            if (header.getAttribute('data-sort') === activeColumn) {
                header.classList.add(`sort-${currentSort.direction}`);
            }
        });
    }
}

// Статистика
function updateReportsStats() {
    const rows = document.querySelectorAll('.report-row');
    let completed = 0;
    let processing = 0;

    rows.forEach(row => {
        const status = row.getAttribute('data-report-status');
        if (status === 'COMPLETED') {
            completed++;
        } else if (status === 'PROCESSING') {
            processing++;
        }
    });

    document.getElementById('completedCount').textContent = completed;
    document.getElementById('processingCount').textContent = processing;
}

function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('ru-RU');
}

function getStatusText(status) {
    switch (status) {
        case 'COMPLETED': return 'Завершён';
        case 'PROCESSING': return 'Формируется';
        case 'ERROR': return 'Ошибка';
        case 'CREATED': return 'Создан';
        default: return status;
    }
}

function downloadCurrentReport() {
    if (!currentReportId) return;

    window.open(`/admin/api/reports/${currentReportId}/download`, '_blank');
}

function downloadReport(reportId) {
    window.open(`/admin/api/reports/${reportId}/download`, '_blank');
}

function retryReport(reportId) {
    if (confirm('Повторить формирование этого отчёта?')) {
        fetch(`/admin/api/reports/${reportId}/retry`, {
            method: 'POST',
            headers: {
                'X-CSRF-TOKEN': getCsrfToken()
            }
        })
        .then(response => {
            if (response.ok) {
                showNotification('Повторное формирование отчёта начато', 'success');
                setTimeout(() => location.reload(), 2000);
            } else {
                showNotification('Ошибка при повторном формировании отчёта', 'error');
            }
        })
        .catch(error => {
            showNotification('Ошибка: ' + error.message, 'error');
        });
    }
}

function showNotification(message, type = 'info') {
    const container = document.getElementById('notificationsContainer');
    const notification = document.createElement('div');

    notification.className = `notification notification-${type}`;
    notification.textContent = message;

    container.appendChild(notification);

    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 300);
    }, 5000);
}

function getCsrfToken() {
    const metaToken = document.querySelector('meta[name="_csrf"]');
    return metaToken ? metaToken.getAttribute('content') : '';
}

function changePageSize(size) {
    const url = new URL(window.location.href);
    const oldSize = url.searchParams.get('size') || '16';
    const oldPage = parseInt(url.searchParams.get('page') || '0');

    url.searchParams.set('size', size);

    const newPage = Math.floor((oldPage * parseInt(oldSize)) / parseInt(size));
    url.searchParams.set('page', newPage);

    window.location.href = url.toString();
}

function goToPage(page) {
    const url = new URL(window.location.href);
    url.searchParams.set('page', page);
    window.location.href = url.toString();
}

function goToPreviousPage() {
    const url = new URL(window.location.href);
    const page = parseInt(url.searchParams.get('page') || '0');
    if (page > 0) {
        url.searchParams.set('page', page - 1);
        window.location.href = url.toString();
    }
}

function goToNextPage() {
    const url = new URL(window.location.href);
    const page = parseInt(url.searchParams.get('page') || '0');
    const totalElements = parseInt(document.querySelector('.pagination-info span:last-child').textContent);
    const pageSize = parseInt(document.getElementById('pageSizeSelect').value);
    const totalPages = Math.ceil(totalElements / pageSize);

    if (page < totalPages - 1) {
        url.searchParams.set('page', page + 1);
        window.location.href = url.toString();
    }
}

function startAutoRefresh() {
    const processingRows = document.querySelectorAll('.report-row[data-report-status="PROCESSING"]');

    if (processingRows.length > 0) {
        setTimeout(() => {
            refreshReportsList();
        }, 10000);
    }
}

document.addEventListener('DOMContentLoaded', function() {
    startAutoRefresh();
});

function refreshReportsList() {
    const urlParams = new URLSearchParams(window.location.search);
    const page = urlParams.get('page') || '0';
    const size = urlParams.get('size') || '16';

    const url = new URL(window.location.href);
    url.searchParams.set('page', page);
    url.searchParams.set('size', size);
    window.location.href = url.toString();
}

const style = document.createElement('style');
style.textContent = `
    .loading-spinner.small {
        width: 20px;
        height: 20px;
        border-width: 2px;
        display: inline-block;
        margin: 0;
    }

    .loading-overlay div {
        margin-top: 16px;
        color: #656d76;
    }
`;
document.head.appendChild(style);