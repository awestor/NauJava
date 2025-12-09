class ReportsManager {
    constructor() {
        this.currentPage = 0;
        this.pageSize = 8;
        this.totalElements = 0;
        this.totalPages = 0;
        this.currentReportId = null;
        this.currentCalendarMode = 'start';
        this.calendarCurrentDate = new Date();
        this.sortColumn = 'created';
        this.sortDirection = 'desc';
        this.isLoading = false;
        this.autoRefreshInterval = null;
        this.hasProcessingReports = false;
        
        this.init();
    }

    init() {
        console.log('ReportsManager инициализирован');
        this.setupEventListeners();
        this.setupCalendar();
        this.loadReports();
        this.setupAutoRefresh();
    }

    setupEventListeners() {
        document.getElementById('startDate').addEventListener('change', () => this.checkExistingReport());
        document.getElementById('endDate').addEventListener('change', () => this.checkExistingReport());

        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('calendar-day') &&
                !e.target.classList.contains('disabled') &&
                !e.target.classList.contains('other-month')) {
                this.selectCalendarDate(e.target);
            }
        });

        document.getElementById('calendarModal').addEventListener('click', (e) => {
            if (e.target === e.currentTarget) {
                this.closeCalendar();
            }
        });

        document.getElementById('reportViewModal').addEventListener('click', (e) => {
            if (e.target === e.currentTarget) {
                this.closeReportModal();
            }
        });

        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.closeCalendar();
                this.closeReportModal();
            }
        });
    }

    setupCalendar() {
        this.updateCalendarDisplay();

        const today = new Date().toISOString().split('T')[0];
        document.getElementById('startDate').value = today;
        document.getElementById('endDate').value = today;

        this.checkExistingReport();
    }

    setupAutoRefresh() {
        this.autoRefreshInterval = setInterval(() => {
            if (this.hasProcessingReports) {
                this.loadReports(false);
            }
        }, 10000);
    }

    async loadReports(showLoading = true) {
        if (this.isLoading) return;
        
        this.isLoading = true;
        
        if (showLoading) {
            this.showLoadingState();
        }

        try {
            const url = `/admin/api/reports/page?page=${this.currentPage}&size=${this.pageSize}`;
            const response = await fetch(url);
            
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const reports = await response.json();
            await this.updateReportsCount();
            
            this.renderReports(reports);
            this.updatePagination();
            this.updateStats();
            
        } catch (error) {
            console.error('Ошибка при загрузке отчётов:', error);
            this.showNotification(`Ошибка загрузки: ${error.message}`, 'error');
            this.showEmptyState();
        } finally {
            this.isLoading = false;
            if (showLoading) {
                this.hideLoadingState();
            }
        }
    }

    async updateReportsCount() {
        try {
            const response = await fetch('/admin/api/reports/count');
            if (response.ok) {
                this.totalElements = await response.json();
                this.totalPages = Math.ceil(this.totalElements / this.pageSize);
            }
        } catch (error) {
            console.error('Ошибка при получении количества отчётов:', error);
        }
    }

    renderReports(reports) {
        const tbody = document.getElementById('reportsTableBody');
        
        if (!reports || reports.length === 0) {
            this.showEmptyState();
            return;
        }

        this.hideEmptyState();
        document.getElementById('tableContainer').style.display = 'block';
        document.getElementById('paginationSection').style.display = 'block';

        tbody.innerHTML = '';

        this.hasProcessingReports = false;

        reports.forEach(report => {
            const row = this.createReportRow(report);
            tbody.appendChild(row);
            
            if (report.status === 'PROCESSING') {
                this.hasProcessingReports = true;
            }
        });

        this.setupTableSorting();
    }

    createReportRow(report) {
        const row = document.createElement('tr');
        row.className = 'report-row';
        row.dataset.reportId = report.id;
        row.dataset.reportStatus = report.status;

        const periodCell = document.createElement('td');
        periodCell.className = 'period-cell';
        periodCell.innerHTML = `
            <div class="period-dates">${report.periodStart} - ${report.periodEnd}</div>
        `;

        const statusCell = document.createElement('td');
        const statusClass = this.getStatusClass(report.status);
        const statusText = this.getStatusText(report.status);
        statusCell.innerHTML = `
            <div class="status-badge ${statusClass}">${statusText}</div>
        `;

        const dateCell = document.createElement('td');
        const [datePart, timePart] = report.createdAt.split(' ');
        dateCell.innerHTML = `
            <div class="date-time">
                <div>${datePart}</div>
                <div class="time-text">${timePart}</div>
            </div>
        `;

        const timeCell = document.createElement('td');
        if (report.totalExecutionTime) {
            const seconds = (report.totalExecutionTime / 1000).toFixed(2);
            timeCell.innerHTML = `
                <div class="execution-time">
                    <div>${report.totalExecutionTime} мс</div>
                    <div class="time-text">${seconds} с</div>
                </div>
            `;
        } else {
            timeCell.innerHTML = '<div class="execution-time">-</div>';
        }

        const actionsCell = document.createElement('td');
        actionsCell.className = 'actions-cell';
        actionsCell.innerHTML = this.createActionButtons(report);

        row.appendChild(periodCell);
        row.appendChild(statusCell);
        row.appendChild(dateCell);
        row.appendChild(timeCell);
        row.appendChild(actionsCell);

        row.addEventListener('click', (e) => {
            if (!e.target.closest('.action-buttons')) {
                this.viewReport(report.id);
            }
        });

        return row;
    }

    createActionButtons(report) {
        const viewBtn = `
            <button class="btn-icon" onclick="reportsManager.viewReport(${report.id})"
                    title="Просмотреть отчёт">
                <img src="data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxNiIgaGVpZ2h0PSIxNiIgdmlld0JveD0iMCAwIDI0IDI0IiBmaWxsPSJub25lIiBzdHJva2U9ImN1cnJlbnRDb2xvciIgc3Ryb2tlLXdpZHRoPSIyIiBzdHJva2UtbGluZWNhcD0icm91bmQiIHN0cm9rZS1saW5lam9pbj0icm91bmQiPjxwYXRoIGQ9Ik0xIDEyczQtNyAxMS03IDExIDcgMTEgN20tMTEtN2ExIDEgMCAxIDAtMiAwIDEgMSAwIDAgMCAyIDB6Ij48L3BhdGg+PGNpcmNsZSBjeD0iMTIiIGN5PSIxMiIgcj0iMyI+PC9jaXJjbGU+PC9zdmc+"
                     alt="Просмотреть" width="16" height="16">
            </button>
        `;

        const downloadBtn = report.status === 'COMPLETED' ? `
            <button class="btn-icon" onclick="reportsManager.downloadReport(${report.id})"
                    title="Скачать отчёт">
                <img src="data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxNiIgaGVpZ2h0PSIxNiIgdmlld0JveD0iMCAwIDI0IDI0IiBmaWxsPSJub25lIiBzdHJva2U9ImN1cnJlbnRDb2xvciIgc3Ryb2tlLXdpZHRoPSIyIiBzdHJva2UtbGluZWNhcD0icm91bmQiIHN0cm9rZS1saW5lam9pbj0icm91bmQiPjxwYXRoIGQ9Ik0yMSAxNXY0YTIgMiAwIDAgMS0yIDJINWEyIDIgMCAwIDEtMi0ydi00Ij48L3BhdGg+PHBvbHlsaW5lIHBvaW50cz0iNyAxMCAxMiAxNSAxNyAxMCI+PC9wb2x5bGluZT48bGluZSB4MT0iMTIiIHkxPSIxNSIgeDI9IjEyIiB5Mj0iMyI+PC9saW5lPjwvc3ZnPg=="
                     alt="Скачать" width="16" height="16">
            </button>
        ` : '';

        const retryBtn = (report.status === 'ERROR' || report.status === 'CREATED') ? `
            <button class="btn-icon" onclick="reportsManager.retryReport(${report.id})"
                    title="Повторить формирование">
                <svg width="16" height="16" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"
                     stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="23 4 23 10 17 10"></polyline>
                    <polyline points="1 20 1 14 7 14"></polyline>
                    <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
                </svg>
            </button>
        ` : '';

        return `
            <div class="action-buttons">
                ${viewBtn}
                ${downloadBtn}
                ${retryBtn}
            </div>
        `;
    }

    updatePagination() {
        const paginationPages = document.getElementById('paginationPages');
        const prevBtn = document.getElementById('prevPageBtn');
        const nextBtn = document.getElementById('nextPageBtn');

        document.getElementById('currentPageSpan').textContent = this.currentPage + 1;
        document.getElementById('totalPagesSpan').textContent = this.totalPages;
        document.getElementById('totalElementsSpan').textContent = this.totalElements;
        document.getElementById('pageInfo').textContent = `${this.currentPage + 1}/${this.totalPages}`;

        prevBtn.disabled = this.currentPage === 0;
        nextBtn.disabled = this.currentPage >= this.totalPages - 1;

        let pagesHTML = '';
        const maxVisiblePages = 5;

        if (this.totalPages <= maxVisiblePages) {
            for (let i = 0; i < this.totalPages; i++) {
                pagesHTML += this.createPageButton(i);
            }
        } else {
            let startPage = Math.max(0, this.currentPage - Math.floor(maxVisiblePages / 2));
            let endPage = Math.min(this.totalPages - 1, startPage + maxVisiblePages - 1);

            if (endPage - startPage + 1 < maxVisiblePages) {
                startPage = Math.max(0, endPage - maxVisiblePages + 1);
            }

            if (startPage > 0) {
                pagesHTML += this.createPageButton(0);
                if (startPage > 1) {
                    pagesHTML += '<span class="pagination-ellipsis">...</span>';
                }
            }

            for (let i = startPage; i <= endPage; i++) {
                pagesHTML += this.createPageButton(i);
            }

            if (endPage < this.totalPages - 1) {
                if (endPage < this.totalPages - 2) {
                    pagesHTML += '<span class="pagination-ellipsis">...</span>';
                }
                pagesHTML += this.createPageButton(this.totalPages - 1);
            }
        }

        paginationPages.innerHTML = pagesHTML;
    }

    createPageButton(page) {
        const isActive = page === this.currentPage;
        return `
            <button class="pagination-page ${isActive ? 'active' : ''}"
                    onclick="reportsManager.goToPage(${page})">
                ${page + 1}
            </button>
        `;
    }

    goToPage(page) {
        if (page >= 0 && page < this.totalPages && page !== this.currentPage) {
            this.currentPage = page;
            this.loadReports();

            document.getElementById('tableContainer').scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        }
    }

    goToPreviousPage() {
        if (this.currentPage > 0) {
            this.goToPage(this.currentPage - 1);
        }
    }

    goToNextPage() {
        if (this.currentPage < this.totalPages - 1) {
            this.goToPage(this.currentPage + 1);
        }
    }

    changePageSize(size) {
        const newSize = parseInt(size);
        if (newSize !== this.pageSize) {
            this.pageSize = newSize;
            this.currentPage = 0;
            document.getElementById('pageSizeInfo').textContent = newSize;
            this.loadReports();
        }
    }

    async createReport() {
        const startDate = document.getElementById('startDate').value;
        const endDate = document.getElementById('endDate').value;
        const createBtn = document.getElementById('createReportBtn');

        if (!startDate || !endDate) {
            this.showNotification('Выберите даты начала и окончания периода', 'error');
            return;
        }

        if (new Date(startDate) > new Date(endDate)) {
            this.showNotification('Дата начала не может быть позже даты окончания', 'error');
            return;
        }

        const today = new Date().toISOString().split('T')[0];
        if (new Date(endDate) > new Date(today)) {
            this.showNotification('Дата окончания не может быть в будущем', 'error');
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
                    'X-CSRF-TOKEN': this.getCsrfToken()
                },
                body: JSON.stringify({
                    startDate: startDate,
                    endDate: endDate
                })
            });

            const result = await response.json();

            if (response.ok && result.status === 'success') {
                this.showNotification('Формирование отчёта начато', 'success');

                setTimeout(() => {
                    this.loadReports();
                }, 2000);

                document.getElementById('startDate').value = today;
                document.getElementById('endDate').value = today;
                this.checkExistingReport();

            } else {
                this.showNotification(result.message || 'Ошибка при создании отчёта', 'error');
            }
        } catch (error) {
            this.showNotification('Ошибка при создании отчёта: ' + error.message, 'error');
        } finally {
            createBtn.innerHTML = originalText;
            createBtn.disabled = false;
        }
    }

    async viewReport(reportId) {
        this.currentReportId = reportId;
        await this.openReportModal(reportId);
    }

    async openReportModal(reportId) {
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
            statusBadge.textContent = this.getStatusText(report.status);

            const statusClass = this.getStatusClass(report.status);
            statusBadge.classList.add(statusClass);

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

    updateStats() {
        const rows = document.querySelectorAll('.report-row');
        let completed = 0;
        let processing = 0;
        let error = 0;

        rows.forEach(row => {
            const status = row.dataset.reportStatus;
            switch (status) {
                case 'COMPLETED': completed++; break;
                case 'PROCESSING': processing++; break;
                case 'ERROR': error++; break;
            }
        });

        document.getElementById('totalCount').textContent = rows.length;
        document.getElementById('completedCount').textContent = completed;
        document.getElementById('processingCount').textContent = processing;
    }

    showLoadingState() {
        document.getElementById('loadingContainer').style.display = 'flex';
        document.getElementById('tableContainer').style.display = 'none';
        document.getElementById('emptyState').style.display = 'none';
        document.getElementById('paginationSection').style.display = 'none';

        const refreshBtn = document.getElementById('refreshBtn');
        const refreshIcon = document.getElementById('refreshIcon');
        refreshBtn.disabled = true;
        refreshIcon.textContent = '⏳';
    }

    hideLoadingState() {
        document.getElementById('loadingContainer').style.display = 'none';

        const refreshBtn = document.getElementById('refreshBtn');
        const refreshIcon = document.getElementById('refreshIcon');
        refreshBtn.disabled = false;
        refreshIcon.textContent = '🔄';
    }

    showEmptyState() {
        document.getElementById('loadingContainer').style.display = 'none';
        document.getElementById('tableContainer').style.display = 'none';
        document.getElementById('emptyState').style.display = 'block';
        document.getElementById('paginationSection').style.display = 'none';
    }

    hideEmptyState() {
        document.getElementById('emptyState').style.display = 'none';
    }

    refreshReports() {
        this.loadReports();
        this.showNotification('Список отчётов обновлён', 'info');
    }

    openCalendar(mode) {
        this.currentCalendarMode = mode;
        this.calendarCurrentDate = new Date();

        const title = document.getElementById('calendarTitle');
        const startDate = document.getElementById('startDate').value;
        const endDate = document.getElementById('endDate').value;

        if (mode === 'start') {
            title.textContent = 'Выберите дату начала';
            if (startDate) {
                this.calendarCurrentDate = new Date(startDate);
            }
        } else {
            title.textContent = 'Выберите дату окончания';
            if (endDate) {
                this.calendarCurrentDate = new Date(endDate);
            }
        }

        this.updateCalendarDisplay();
        document.getElementById('calendarModal').classList.add('active');
    }

    closeCalendar() {
        document.getElementById('calendarModal').classList.remove('active');
    }

    changeCalendarMonth(delta) {
        this.calendarCurrentDate.setMonth(this.calendarCurrentDate.getMonth() + delta);
        this.updateCalendarDisplay();
    }

    updateCalendarDisplay() {
        const monthElement = document.getElementById('calendarMonth');
        const calendarContainer = document.getElementById('calendarContainer');

        const monthNames = [
            'Январь', 'Февраль', 'Март', 'Апрель', 'Май', 'Июнь',
            'Июль', 'Август', 'Сентябрь', 'Октябрь', 'Ноябрь', 'Декабрь'
        ];

        const year = this.calendarCurrentDate.getFullYear();
        const month = this.calendarCurrentDate.getMonth();

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

    selectCalendarDate(element) {
        const selectedDate = element.dataset.date;

        if (this.currentCalendarMode === 'start') {
            document.getElementById('startDate').value = selectedDate;
        } else {
            document.getElementById('endDate').value = selectedDate;
        }

        this.closeCalendar();
        this.checkExistingReport();
    }

    async checkExistingReport() {
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

    showWeekReport() {
        const endDate = new Date();
        const startDate = new Date();
        startDate.setDate(endDate.getDate() - 6);

        document.getElementById('startDate').value = startDate.toISOString().split('T')[0];
        document.getElementById('endDate').value = endDate.toISOString().split('T')[0];

        this.checkExistingReport();
        this.showNotification('Установлен период за последнюю неделю', 'info');
    }

    showMonthReport() {
        const endDate = new Date();
        const startDate = new Date(endDate.getFullYear(), endDate.getMonth(), 1);

        document.getElementById('startDate').value = startDate.toISOString().split('T')[0];
        document.getElementById('endDate').value = endDate.toISOString().split('T')[0];

        this.checkExistingReport();
        this.showNotification('Установлен период за текущий месяц', 'info');
    }

    async showLatestReport() {
        try {
            const response = await fetch('/admin/api/reports/latest');
            if (response.ok) {
                const report = await response.json();
                this.viewReport(report.id);
            } else {
                this.showNotification('Не удалось загрузить последний отчёт', 'error');
            }
        } catch (error) {
            this.showNotification('Ошибка: ' + error.message, 'error');
        }
    }

    downloadReport(reportId) {
        window.open(`/admin/api/reports/${reportId}/download`, '_blank');
    }

    async retryReport(reportId) {
        if (confirm('Повторить формирование этого отчёта?')) {
            try {
                const response = await fetch(`/admin/api/reports/${reportId}/retry`, {
                    method: 'POST',
                    headers: {
                        'X-CSRF-TOKEN': this.getCsrfToken()
                    }
                });

                if (response.ok) {
                    this.showNotification('Повторное формирование отчёта начато', 'success');

                    setTimeout(() => {
                        this.loadReports();
                    }, 2000);
                } else {
                    this.showNotification('Ошибка при повторном формировании отчёта', 'error');
                }
            } catch (error) {
                this.showNotification('Ошибка: ' + error.message, 'error');
            }
        }
    }

    closeReportModal() {
        document.getElementById('reportViewModal').classList.remove('active');
        this.currentReportId = null;
    }

    setupTableSorting() {
        const headers = document.querySelectorAll('.reports-table th.sortable');

        headers.forEach(header => {
            header.addEventListener('click', () => {
                const column = header.dataset.sort;
                this.sortTable(column);
            });
        });
    }

    sortTable(column) {
        if (this.sortColumn === column) {
            this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
        } else {
            this.sortColumn = column;
            this.sortDirection = 'desc';
        }

        const tbody = document.getElementById('reportsTableBody');
        const rows = Array.from(tbody.querySelectorAll('tr'));

        rows.sort((a, b) => {
            if (column === 'created') {
                const aDateStr = a.cells[2].querySelector('.date-time > div:first-child').textContent.trim() +
                               ' ' + a.cells[2].querySelector('.time-text').textContent.trim();
                const bDateStr = b.cells[2].querySelector('.date-time > div:first-child').textContent.trim() +
                               ' ' + b.cells[2].querySelector('.time-text').textContent.trim();

                const aDate = this.parseDateTimeString(aDateStr);
                const bDate = this.parseDateTimeString(bDateStr);

                return this.sortDirection === 'asc' ? aDate - bDate : bDate - aDate;
            }

            if (column === 'period') {
                const aPeriodStr = a.cells[0].querySelector('.period-dates').textContent.trim();
                const bPeriodStr = b.cells[0].querySelector('.period-dates').textContent.trim();

                const aDaysCount = this.calculatePeriodDays(aPeriodStr);
                const bDaysCount = this.calculatePeriodDays(bPeriodStr);

                return this.sortDirection === 'asc' ? aDaysCount - bDaysCount : bDaysCount - aDaysCount;
            }

            return 0;
        });

        tbody.innerHTML = '';
        rows.forEach(row => tbody.appendChild(row));

        this.updateSortIndicators(column);
    }

    updateSortIndicators(activeColumn) {
        const headers = document.querySelectorAll('.reports-table th.sortable');

        headers.forEach(header => {
            header.classList.remove('sort-asc', 'sort-desc');
            if (header.dataset.sort === activeColumn) {
                header.classList.add(`sort-${this.sortDirection}`);
            }
        });
    }

    parseDateTimeString(dateTimeStr) {
        const [datePart, timePart] = dateTimeStr.split(' ');
        const [year, month, day] = datePart.split('.').map(Number);
        const [hours, minutes, seconds] = timePart.split(':').map(Number);

        return new Date(year, month - 1, day, hours, minutes, seconds);
    }

    calculatePeriodDays(periodStr) {
        const [startStr, endStr] = periodStr.split(' - ');

        const [startYear, startMonth, startDay] = startStr.split('.').map(Number);
        const [endYear, endMonth, endDay] = endStr.split('.').map(Number);

        const startDate = new Date(startYear, startMonth - 1, startDay);
        const endDate = new Date(endYear, endMonth - 1, endDay);

        const timeDiff = endDate.getTime() - startDate.getTime();
        return Math.floor(timeDiff / (1000 * 3600 * 24)) + 1;
    }

    showNotification(message, type = 'info') {
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

    getCsrfToken() {
        const metaToken = document.querySelector('meta[name="_csrf"]');
        return metaToken ? metaToken.getAttribute('content') : '';
    }

    getStatusText(status) {
        switch (status) {
            case 'COMPLETED': return 'Завершён';
            case 'PROCESSING': return 'Формируется';
            case 'ERROR': return 'Ошибка';
            case 'CREATED': return 'Создан';
            default: return status;
        }
    }

    getStatusClass(status) {
        switch (status) {
            case 'COMPLETED': return 'status-completed';
            case 'PROCESSING': return 'status-processing';
            case 'ERROR': return 'status-error';
            case 'CREATED': return 'status-created';
            default: return '';
        }
    }
}

let reportsManager;

document.addEventListener('DOMContentLoaded', function() {
    reportsManager = new ReportsManager();
});

function openCalendar(mode) { reportsManager.openCalendar(mode); }
function closeCalendar() { reportsManager.closeCalendar(); }
function changeCalendarMonth(delta) { reportsManager.changeCalendarMonth(delta); }
function checkExistingReport() { reportsManager.checkExistingReport(); }
function createReport() { reportsManager.createReport(); }
function viewReport(id) { reportsManager.viewReport(id); }
function refreshReports() { reportsManager.refreshReports(); }
function showWeekReport() { reportsManager.showWeekReport(); }
function showMonthReport() { reportsManager.showMonthReport(); }
function showLatestReport() { reportsManager.showLatestReport(); }
function downloadReport(id) { reportsManager.downloadReport(id); }
function retryReport(id) { reportsManager.retryReport(id); }
function goToPage(page) { reportsManager.goToPage(page); }
function goToPreviousPage() { reportsManager.goToPreviousPage(); }
function goToNextPage() { reportsManager.goToNextPage(); }
function changePageSize(size) { reportsManager.changePageSize(size); }
function closeReportModal() { reportsManager.closeReportModal(); }