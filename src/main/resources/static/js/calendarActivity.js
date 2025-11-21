class NutritionCalendar {
    constructor() {
        this.currentDate = new Date();
        this.selectedRange = {
            start: null,
            end: null
        };
        this.isSelecting = false;
        this.nutritionData = {};
        this.chart = null;
        this.autoScrollSpeed = 5;
        this.scrollDirection = 0;
        this.autoScrollInterval = null;

        // Цели пользователя
        this.userGoals = {
            calories: 2000,
            proteins: 150,
            fats: 65,
            carbs: 250
        };

        this.initializeElements();
        this.setupEventListeners();
        this.loadInitialData();
        this.initializeChart();
        this.setDefaultSelection();
        this.renderCalendar();
        this.centerOnToday();
    }

    initializeElements() {
        this.elements = {
            currentMonth: document.getElementById('currentMonth'),
            prevMonth: document.getElementById('prevMonth'),
            nextMonth: document.getElementById('nextMonth'),
            datesRow: document.getElementById('datesRow'),
            caloriesValues: document.getElementById('caloriesValues'),
            proteinsValues: document.getElementById('proteinsValues'),
            fatsValues: document.getElementById('fatsValues'),
            carbsValues: document.getElementById('carbsValues'),
            selectedDaysCount: document.getElementById('selectedDaysCount'),
            selectionStart: document.getElementById('selectionStart'),
            selectionEnd: document.getElementById('selectionEnd'),
            nutritionChart: document.getElementById('nutritionChart'),
            calendarWrapper: document.querySelector('.calendar-wrapper')
        };
    }

    setupEventListeners() {
        this.elements.prevMonth.addEventListener('click', () => this.changeMonth(-1));
        this.elements.nextMonth.addEventListener('click', () => this.changeMonth(1));

        // Обработчики для выделения мышью
        this.elements.datesRow.addEventListener('mousedown', (e) => this.startSelection(e));
        this.elements.caloriesValues.addEventListener('mousedown', (e) => this.startSelection(e));
        this.elements.proteinsValues.addEventListener('mousedown', (e) => this.startSelection(e));
        this.elements.fatsValues.addEventListener('mousedown', (e) => this.startSelection(e));
        this.elements.carbsValues.addEventListener('mousedown', (e) => this.startSelection(e));

        document.addEventListener('mousemove', (e) => this.updateSelection(e));
        document.addEventListener('mouseup', () => this.endSelection());

        // Обработчики клавиатуры
        document.addEventListener('keydown', (e) => this.handleKeyPress(e));

        // Обработчик для автоматической прокрутки при приближении к границам
        document.addEventListener('mousemove', (e) => this.handleAutoScroll(e));
    }

    loadInitialData() {
        const year = this.currentDate.getFullYear();
        const month = this.currentDate.getMonth();
        const daysInMonth = new Date(year, month + 1, 0).getDate();

        this.nutritionData = {};
        for (let day = 1; day <= daysInMonth; day++) {
            const dateKey = this.formatDateKey(year, month, day);
            // Создаем более реалистичные данные с пропусками
            const hasData = Math.random() > 0.4;

            // Генерируем данные, которые будут демонстрировать все цветовые состояния
            const randomFactor = Math.random();
            let calories, proteins, fats, carbs;

            if (randomFactor < 0.25) {
                // Слишком мало (<50%)
                calories = Math.floor(this.userGoals.calories * 0.3);
                proteins = Math.floor(this.userGoals.proteins * 0.4);
                fats = Math.floor(this.userGoals.fats * 0.35);
                carbs = Math.floor(this.userGoals.carbs * 0.25);
            } else if (randomFactor < 0.5) {
                // Близко к цели (50-100%)
                calories = Math.floor(this.userGoals.calories * 0.8);
                proteins = Math.floor(this.userGoals.proteins * 0.9);
                fats = Math.floor(this.userGoals.fats * 0.75);
                carbs = Math.floor(this.userGoals.carbs * 0.85);
            } else if (randomFactor < 0.75) {
                // На 15-30% больше (100-130%)
                calories = Math.floor(this.userGoals.calories * 1.2);
                proteins = Math.floor(this.userGoals.proteins * 1.15);
                fats = Math.floor(this.userGoals.fats * 1.25);
                carbs = Math.floor(this.userGoals.carbs * 1.1);
            } else {
                // Слишком много (>130%)
                calories = Math.floor(this.userGoals.calories * 1.5);
                proteins = Math.floor(this.userGoals.proteins * 1.4);
                fats = Math.floor(this.userGoals.fats * 1.6);
                carbs = Math.floor(this.userGoals.carbs * 1.45);
            }

            this.nutritionData[dateKey] = {
                calories: hasData ? calories : 0,
                proteins: hasData ? proteins : 0,
                fats: hasData ? fats : 0,
                carbs: hasData ? carbs : 0
            };
        }
    }

    setDefaultSelection() {
        // Установка выделения по умолчанию (вчера + сегодня)
        const today = new Date();
        const yesterday = new Date(today);
        yesterday.setDate(yesterday.getDate() - 1);

        this.selectedRange.start = this.formatDateKey(
            yesterday.getFullYear(),
            yesterday.getMonth(),
            yesterday.getDate()
        );
        this.selectedRange.end = this.formatDateKey(
            today.getFullYear(),
            today.getMonth(),
            today.getDate()
        );

        this.updateSelectionInfo();
    }

    formatDateKey(year, month, day) {
        return `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
    }

    getDaysInMonth() {
        const year = this.currentDate.getFullYear();
        const month = this.currentDate.getMonth();
        return new Date(year, month + 1, 0).getDate();
    }

    getMonthName() {
        const months = [
            'Январь', 'Февраль', 'Март', 'Апрель', 'Май', 'Июнь',
            'Июль', 'Август', 'Сентябрь', 'Октябрь', 'Ноябрь', 'Декабрь'
        ];
        return months[this.currentDate.getMonth()] + ' ' + this.currentDate.getFullYear();
    }

    getNutrientColor(value, goal, type) {
        if (value === 0) return '';

        const percentage = (value / goal) * 100;

        if (percentage < 50) return 'nutrient-low';
        if (percentage <= 100) return 'nutrient-good';
        if (percentage <= 130) return 'nutrient-warning';
        return 'nutrient-danger';
    }

    renderCalendar() {
        this.elements.currentMonth.textContent = this.getMonthName();

        const year = this.currentDate.getFullYear();
        const month = this.currentDate.getMonth();
        const daysInMonth = this.getDaysInMonth();

        // Очистка
        this.elements.datesRow.innerHTML = '';
        this.elements.caloriesValues.innerHTML = '';
        this.elements.proteinsValues.innerHTML = '';
        this.elements.fatsValues.innerHTML = '';
        this.elements.carbsValues.innerHTML = '';

        const today = new Date();
        const todayKey = this.formatDateKey(today.getFullYear(), today.getMonth(), today.getDate());

        // Рендер дат
        for (let day = 1; day <= daysInMonth; day++) {
            const dateKey = this.formatDateKey(year, month, day);
            const dateCell = document.createElement('div');
            dateCell.className = 'date-cell';
            dateCell.textContent = day;
            dateCell.dataset.date = dateKey;

            if (dateKey === todayKey) {
                dateCell.classList.add('today');
            }

            if (this.isDateInRange(dateKey)) {
                dateCell.classList.add(this.getRangeClass(dateKey));
            }

            this.elements.datesRow.appendChild(dateCell);
        }

        // Рендер значений nutrition
        this.renderNutritionRow('calories', this.elements.caloriesValues);
        this.renderNutritionRow('proteins', this.elements.proteinsValues);
        this.renderNutritionRow('fats', this.elements.fatsValues);
        this.renderNutritionRow('carbs', this.elements.carbsValues);
    }

    renderNutritionRow(type, container) {
        const year = this.currentDate.getFullYear();
        const month = this.currentDate.getMonth();
        const daysInMonth = this.getDaysInMonth();

        for (let day = 1; day <= daysInMonth; day++) {
            const dateKey = this.formatDateKey(year, month, day);
            const valueCell = document.createElement('div');
            valueCell.className = 'value-cell';
            valueCell.dataset.date = dateKey;
            valueCell.dataset.type = type;

            const data = this.nutritionData[dateKey] || { calories: 0, proteins: 0, fats: 0, carbs: 0 };
            const value = data[type];

            // Создаем span для текста
            const valueSpan = document.createElement('span');
            valueSpan.textContent = value;
            valueCell.appendChild(valueSpan);

            // Добавляем цветовую градацию
            const colorClass = this.getNutrientColor(value, this.userGoals[type], type);
            if (colorClass) {
                valueCell.classList.add(colorClass);
            }

            if (this.isDateInRange(dateKey)) {
                valueCell.classList.add(this.getRangeClass(dateKey));
            }

            container.appendChild(valueCell);
        }
    }

    centerOnToday() {
        const today = new Date();
        const todayKey = this.formatDateKey(today.getFullYear(), today.getMonth(), today.getDate());
        const todayCell = document.querySelector(`.date-cell[data-date="${todayKey}"]`);

        if (todayCell) {
            const container = this.elements.calendarWrapper;
            const cellRect = todayCell.getBoundingClientRect();
            const containerRect = container.getBoundingClientRect();

            // Вычисляем позицию для центрирования
            const scrollLeft = todayCell.offsetLeft - (containerRect.width / 2) + (cellRect.width / 2);
            container.scrollLeft = Math.max(0, scrollLeft);
        }
    }

    startSelection(e) {
        if (e.target.classList.contains('date-cell') || e.target.classList.contains('value-cell')) {
            e.preventDefault();
            this.isSelecting = true;
            const startDate = e.target.dataset.date;
            this.selectedRange.start = startDate;
            this.selectedRange.end = startDate;
            this.renderCalendar();
        }
    }

    updateSelection(e) {
        if (!this.isSelecting) return;

        const target = e.target;
        if (target.classList.contains('date-cell') || target.classList.contains('value-cell')) {
            const endDate = target.dataset.date;
            if (endDate && this.isDateInCurrentMonth(endDate)) {
                this.selectedRange.end = endDate;
                this.normalizeRange();
                this.renderCalendar();
            }
        }
    }

    handleAutoScroll(e) {
        if (!this.isSelecting) {
            if (this.autoScrollInterval) {
                clearInterval(this.autoScrollInterval);
                this.autoScrollInterval = null;
            }
            return;
        }

        const wrapper = this.elements.calendarWrapper;
        const wrapperRect = wrapper.getBoundingClientRect();
        const mouseX = e.clientX;

        // Определяем зоны автоматической прокрутки (по 3 ячейки с каждой стороны)
        const scrollZoneWidth = 80 * 3;
        const leftZoneEnd = wrapperRect.left + scrollZoneWidth;
        const rightZoneStart = wrapperRect.right - scrollZoneWidth;

        let newScrollSpeed = 0;

        if (mouseX < leftZoneEnd) {
            // Вычисляем ускорение: чем ближе к границе, тем быстрее
            const distanceFromEdge = leftZoneEnd - mouseX;
            newScrollSpeed = -Math.min(20, Math.max(5, distanceFromEdge / 4));
        } else if (mouseX > rightZoneStart) {
            const distanceFromEdge = mouseX - rightZoneStart;
            newScrollSpeed = Math.min(20, Math.max(5, distanceFromEdge / 4));
        }

        // Если скорость изменилась, перезапускаем интервал
        if (newScrollSpeed !== this.autoScrollSpeed) {
            this.autoScrollSpeed = newScrollSpeed;

            if (this.autoScrollInterval) {
                clearInterval(this.autoScrollInterval);
            }

            if (this.autoScrollSpeed !== 0) {
                this.autoScrollInterval = setInterval(() => {
                    wrapper.scrollLeft += this.autoScrollSpeed;
                }, 16);
            }
        }
    }

    endSelection() {
        if (this.isSelecting) {
            this.isSelecting = false;

            if (this.autoScrollInterval) {
                clearInterval(this.autoScrollInterval);
                this.autoScrollInterval = null;
            }
            this.autoScrollSpeed = 0;

            this.updateSelectionInfo();
            this.updateChart();
        }
    }

    handleKeyPress(e) {
        if (e.key === 'ArrowLeft') {
            e.preventDefault();
            this.elements.calendarWrapper.scrollLeft -= 80;
        } else if (e.key === 'ArrowRight') {
            e.preventDefault();
            this.elements.calendarWrapper.scrollLeft += 80;
        }
    }

    isDateInCurrentMonth(dateKey) {
        const [year, month] = dateKey.split('-').map(Number);
        return year === this.currentDate.getFullYear() &&
               month - 1 === this.currentDate.getMonth();
    }

    isDateInRange(dateKey) {
        if (!this.selectedRange.start || !this.selectedRange.end) return false;

        const start = new Date(this.selectedRange.start);
        const end = new Date(this.selectedRange.end);
        const current = new Date(dateKey);

        return current >= start && current <= end;
    }

    getRangeClass(dateKey) {
        if (!this.selectedRange.start || !this.selectedRange.end) return '';

        if (dateKey === this.selectedRange.start || dateKey === this.selectedRange.end) {
            return 'selected';
        }
        return 'in-range';
    }

    normalizeRange() {
        if (!this.selectedRange.start || !this.selectedRange.end) return;

        const start = new Date(this.selectedRange.start);
        const end = new Date(this.selectedRange.end);

        if (start > end) {
            [this.selectedRange.start, this.selectedRange.end] = [this.selectedRange.end, this.selectedRange.start];
        }
    }

    changeMonth(direction) {
        this.currentDate.setMonth(this.currentDate.getMonth() + direction);
        this.loadInitialData();
        this.renderCalendar();
        // Не обновляем график при смене месяца, только при изменении выделения
    }

    updateSelectionInfo() {
        if (this.selectedRange.start && this.selectedRange.end) {
            const startDate = new Date(this.selectedRange.start);
            const endDate = new Date(this.selectedRange.end);
            const daysDiff = Math.ceil((endDate - startDate) / (1000 * 60 * 60 * 24)) + 1;

            this.elements.selectedDaysCount.textContent = daysDiff;
            this.elements.selectionStart.textContent = this.formatDisplayDate(this.selectedRange.start);
            this.elements.selectionEnd.textContent = this.formatDisplayDate(this.selectedRange.end);
        }
    }

    formatDisplayDate(dateKey) {
        const date = new Date(dateKey);
        return date.toLocaleDateString('ru-RU');
    }

    initializeChart() {
        const ctx = this.elements.nutritionChart.getContext('2d');

        // Создаем плагин для целевых линий
        const targetLinePlugin = {
            id: 'targetLines',
            afterDatasetsDraw(chart) {
                const { ctx, scales: { x, y } } = chart;
                const goals = chart.config._config.options.goals;

                if (!goals) return;

                ctx.save();

                // Линия цели для калорий
                const caloriesY = y.getPixelForValue(goals.calories);
                ctx.strokeStyle = '#e74c3c';
                ctx.setLineDash([5, 5]);
                ctx.lineWidth = 2;
                ctx.beginPath();
                ctx.moveTo(x.left, caloriesY);
                ctx.lineTo(x.right, caloriesY);
                ctx.stroke();

                // Подпись для линии калорий
                ctx.fillStyle = '#e74c3c';
                ctx.font = '12px Arial';
                ctx.fillText('Цель: ' + goals.calories + ' ккал', x.right - 120, caloriesY - 5);

                ctx.restore();
            }
        };

        this.chart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: [],
                datasets: [
                    {
                        label: 'Калории',
                        data: [],
                        borderColor: '#e74c3c',
                        backgroundColor: 'rgba(231, 76, 60, 0.1)',
                        tension: 0.4,
                        fill: false,
                        borderWidth: 2
                    },
                    {
                        label: 'Белки',
                        data: [],
                        borderColor: '#3498db',
                        backgroundColor: 'rgba(52, 152, 219, 0.1)',
                        tension: 0.4,
                        fill: false,
                        borderWidth: 2
                    },
                    {
                        label: 'Жиры',
                        data: [],
                        borderColor: '#f39c12',
                        backgroundColor: 'rgba(243, 156, 18, 0.1)',
                        tension: 0.4,
                        fill: false,
                        borderWidth: 2
                    },
                    {
                        label: 'Углеводы',
                        data: [],
                        borderColor: '#27ae60',
                        backgroundColor: 'rgba(39, 174, 96, 0.1)',
                        tension: 0.4,
                        fill: false,
                        borderWidth: 2
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    x: {
                        title: {
                            display: true,
                            text: 'Даты'
                        },
                        ticks: {
                            maxTicksLimit: 10,
                            callback: function(value, index, values) {
                                return index % Math.ceil(values.length / 10) === 0 ? this.getLabelForValue(value) : '';
                            }
                        }
                    },
                    y: {
                        title: {
                            display: true,
                            text: 'Значения'
                        },
                        beginAtZero: true
                    }
                },
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        mode: 'index',
                        intersect: false
                    }
                },
                interaction: {
                    intersect: false,
                    mode: 'nearest'
                },
                goals: this.userGoals
            },
            plugins: [targetLinePlugin]
        });

        this.updateChartLegend();
        // Первоначальное обновление графика для выделения по умолчанию
        this.updateChart();
    }

    updateChart() {
        if (!this.chart || !this.selectedRange.start || !this.selectedRange.end) return;

        const startDate = new Date(this.selectedRange.start);
        const endDate = new Date(this.selectedRange.end);
        const dates = [];
        const caloriesData = [];
        const proteinsData = [];
        const fatsData = [];
        const carbsData = [];

        let currentDate = new Date(startDate);
        while (currentDate <= endDate) {
            const dateKey = this.formatDateKey(
                currentDate.getFullYear(),
                currentDate.getMonth(),
                currentDate.getDate()
            );

            dates.push(this.formatChartDate(currentDate));
            const data = this.nutritionData[dateKey] || { calories: 0, proteins: 0, fats: 0, carbs: 0 };

            caloriesData.push(data.calories);
            proteinsData.push(data.proteins);
            fatsData.push(data.fats);
            carbsData.push(data.carbs);

            currentDate.setDate(currentDate.getDate() + 1);
        }

        this.chart.data.labels = dates;
        this.chart.data.datasets[0].data = caloriesData;
        this.chart.data.datasets[1].data = proteinsData;
        this.chart.data.datasets[2].data = fatsData;
        this.chart.data.datasets[3].data = carbsData;

        // Обновляем цели в конфигурации
        this.chart.options.goals = this.userGoals;

        this.chart.update('none');
    }

    formatChartDate(date) {
        return date.getDate() + '.' + (date.getMonth() + 1);
    }

    updateChartLegend() {
        const legendContainer = document.getElementById('chartLegend');
        const datasets = this.chart.data.datasets;

        legendContainer.innerHTML = '';

        datasets.forEach(dataset => {
            const legendItem = document.createElement('div');
            legendItem.className = 'legend-item';

            const colorBox = document.createElement('div');
            colorBox.className = 'legend-color';
            colorBox.style.backgroundColor = dataset.borderColor;

            const text = document.createElement('span');
            text.className = 'legend-text';
            text.textContent = dataset.label;

            legendItem.appendChild(colorBox);
            legendItem.appendChild(text);
            legendContainer.appendChild(legendItem);
        });
    }
}

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', () => {
    new NutritionCalendar();
});