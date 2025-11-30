class MealsCalendar {
    constructor() {
        this.currentDate = new Date();
        const urlParams = new URLSearchParams(window.location.search);
        const dateParam = urlParams.get('date');
        this.selectedDate = dateParam || new Date().toISOString().split('T')[0];

        this.initializeElements();
        this.setupEventListeners();
        this.renderCalendar();
        this.updateSelectedDateInfo();
    }

    initializeElements() {
        this.elements = {
            currentMonth: document.getElementById('currentMonth'),
            prevMonth: document.getElementById('prevMonth'),
            nextMonth: document.getElementById('nextMonth'),
            calendarGrid: document.getElementById('calendarGrid'),
            selectedDateDisplay: document.getElementById('selectedDateDisplay')
        };
    }

    setupEventListeners() {
        this.elements.prevMonth.addEventListener('click', () => this.changeMonth(-1));
        this.elements.nextMonth.addEventListener('click', () => this.changeMonth(1));
    }

    getMonthName() {
        const months = [
            'Январь', 'Февраль', 'Март', 'Апрель', 'Май', 'Июнь',
            'Июль', 'Август', 'Сентябрь', 'Октябрь', 'Ноябрь', 'Декабрь'
        ];
        return months[this.currentDate.getMonth()] + ' ' + this.currentDate.getFullYear();
    }

    getDaysInMonth() {
        const year = this.currentDate.getFullYear();
        const month = this.currentDate.getMonth();
        return new Date(year, month + 1, 0).getDate();
    }

    getFirstDayOfMonth() {
        const year = this.currentDate.getFullYear();
        const month = this.currentDate.getMonth();
        const firstDay = new Date(year, month, 1).getDay();
        // Преобразование к формату: 0 - понедельник, 6 - воскресенье
        return firstDay === 0 ? 6 : firstDay - 1;
    }

    formatDate(date) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }

    isSameDay(date1, date2) {
        return date1.getFullYear() === date2.getFullYear() &&
               date1.getMonth() === date2.getMonth() &&
               date1.getDate() === date2.getDate();
    }

    renderCalendar() {
        this.elements.currentMonth.textContent = this.getMonthName();

        const year = this.currentDate.getFullYear();
        const month = this.currentDate.getMonth();
        const daysInMonth = this.getDaysInMonth();
        const firstDay = this.getFirstDayOfMonth();

        this.elements.calendarGrid.innerHTML = '';

        const dayNames = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];
        dayNames.forEach(dayName => {
            const dayHeader = document.createElement('div');
            dayHeader.className = 'calendar-day-header';
            dayHeader.textContent = dayName;
            this.elements.calendarGrid.appendChild(dayHeader);
        });

        for (let i = 0; i < firstDay; i++) {
            const emptyDay = document.createElement('div');
            emptyDay.className = 'calendar-day other-month';
            this.elements.calendarGrid.appendChild(emptyDay);
        }

        const today = new Date();
        const selectedDateObj = new Date(this.selectedDate);

        for (let day = 1; day <= daysInMonth; day++) {
            const date = new Date(year, month, day);
            const dateFormatted = this.formatDate(date);

            const dayElement = document.createElement('div');
            dayElement.className = 'calendar-day';
            dayElement.textContent = day;
            dayElement.dataset.date = dateFormatted;

            if (this.isSameDay(date, today)) {
                dayElement.classList.add('today');
            }

            if (this.isSameDay(date, selectedDateObj)) {
                dayElement.classList.add('selected');
            }

            dayElement.addEventListener('click', () => {
                this.selectDate(dateFormatted);
            });

            this.elements.calendarGrid.appendChild(dayElement);
        }

        const totalCells = 42; // 6 строк × 7 дней
        const currentCells = firstDay + daysInMonth;
        const remainingCells = totalCells - currentCells;

        for (let i = 0; i < remainingCells; i++) {
            const emptyDay = document.createElement('div');
            emptyDay.className = 'calendar-day other-month';
            this.elements.calendarGrid.appendChild(emptyDay);
        }
    }

    selectDate(date) {
        this.selectedDate = date;
        this.renderCalendar();
        this.updateSelectedDateInfo();
        this.updatePageDate(date);
    }

    updateSelectedDateInfo() {
        const date = new Date(this.selectedDate);
        const formattedDate = date.toLocaleDateString('ru-RU', {
            day: 'numeric',
            month: 'long',
            year: 'numeric'
        });
        this.elements.selectedDateDisplay.textContent = formattedDate;
    }

    updatePageDate(date) {
        window.location.href = '/view/meals/list?date=' + date;
    }

    changeMonth(direction) {
        this.currentDate.setMonth(this.currentDate.getMonth() + direction);
        this.renderCalendar();
    }
}

let mealTypes = [];
let products = [];
let currentMealData = null;

document.addEventListener('DOMContentLoaded', () => {
    new MealsCalendar();
});

function updateDate(date) {
    window.location.href = '/view/meals/list?date=' + date;
}

function createNewMeal() {
    openModal('create');
}

function editMeal(mealId) {
    openModal('edit', mealId);
}

function openModal(mode, mealId = null) {
    const modal = document.getElementById('mealModal');
    const title = document.getElementById('modalTitle');
    const submitBtn = document.getElementById('submitBtn');

    resetForm();
    document.getElementById('mealId').value = '';

    if (mode === 'create') {
        title.textContent = 'Создать приём пищи';
        submitBtn.textContent = 'Создать';
        addProductRow();
    } else {
        title.textContent = 'Редактировать приём пищи';
        submitBtn.textContent = 'Сохранить';
        document.getElementById('mealId').value = mealId;
        loadMealData(mealId);
    }

    let mouseDownOnOverlay = false;

    modal.addEventListener('mousedown', function(e) {
        if (e.target === this) {
            mouseDownOnOverlay = true;
        }
    });

    modal.addEventListener('mouseup', function(e) {
        if (e.target === this && mouseDownOnOverlay) {
            closeModal();
        }
        mouseDownOnOverlay = false;
    });

    loadFormData();
    modal.classList.add('active');
}

function closeModal() {
    const modal = document.getElementById('mealModal');
    modal.classList.remove('active');
    resetForm();
}

function resetForm() {
    document.getElementById('productsContainer').innerHTML = '';
    document.getElementById('mealTypeSelect').value = '';
    updateAddButtonState();
    hideEmptyMessage();
}

function loadMealData(mealId) {
    fetch(`/api/meals/${mealId}`, {
        headers: {
            'X-CSRF-TOKEN': getCsrfToken()
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка загрузки данных');
            }
            return response.json();
        })
        .then(meal => {
            currentMealData = meal;
            populateMealForm(meal);
        })
        .catch(error => {
            console.error('Error loading meal data:', error);
            alert('Ошибка при загрузке данных приёма пищи');
        });
}

function populateMealForm(meal) {
    document.getElementById('mealTypeSelect').value = meal.mealType;

    const container = document.getElementById('productsContainer');
    container.innerHTML = '';

    if (meal.mealEntries && meal.mealEntries.length > 0) {
        meal.mealEntries.forEach(entry => {
            addProductRow(entry.productName, entry.quantityGrams);
        });
    } else {
        addProductRow();
    }

    updateAddButtonState();
}

function loadFormData() {
    if (mealTypes.length === 0) {
        fetch('/api/meal-types', {
            headers: {
                'X-CSRF-TOKEN': getCsrfToken()
            }
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Ошибка загрузки типов приёмов пищи');
                }
                return response.json();
            })
            .then(data => {
                mealTypes = data;
                populateMealTypes();
            })
            .catch(error => {
                console.error('Error loading meal types:', error);
            });
    } else {
        populateMealTypes();
    }

    if (products.length === 0) {
        fetch('/api/products/all', {
            headers: {
                'X-CSRF-TOKEN': getCsrfToken()
            }
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Ошибка загрузки продуктов');
                }
                return response.json();
            })
            .then(data => {
                products = data;
                populateProducts();
            })
            .catch(error => {
                console.error('Error loading products:', error);
            });
    } else {
        populateProducts();
    }
}

function populateMealTypes() {
    const select = document.getElementById('mealTypeSelect');
    const currentValue = select.value;
    select.innerHTML = '<option value="">Выберите тип приёма пищи</option>';

    mealTypes.forEach(type => {
        const option = document.createElement('option');
        option.value = type.name;
        option.textContent = type.name;
        select.appendChild(option);
    });

    if (currentValue) {
        select.value = currentValue;
    }
}

function populateProducts() {
    const productSelects = document.querySelectorAll('.product-select');
    productSelects.forEach(select => {
        const currentValue = select.value;
        select.innerHTML = '<option value="">Выберите продукт</option>';

        products.forEach(product => {
            const option = document.createElement('option');
            option.value = product.name;
            option.textContent = product.name;
            option.dataset.productId = product.id;
            select.appendChild(option);
        });

        if (currentValue) {
            select.value = currentValue;
        }
    });
}

function addProductRow(productName = '', weight = '') {
    const container = document.getElementById('productsContainer');
    const rowCount = container.children.length;

    if (rowCount >= 8) {
        alert('Максимальное количество продуктов - 8');
        return;
    }

    hideEmptyMessage();

    const row = document.createElement('div');
    row.className = 'product-row';
    row.innerHTML = `
        <div class="product-fields">
            <div>
                <label class="form-label">Вес (г)</label>
                <input type="number" class="form-input weight-input"
                       value="${weight}" min="1" max="5000"
                       placeholder="Вес" required
                       oninput="validateProductRow(this)">
            </div>
            <div style="flex: 1;">
                <label class="form-label">Продукт</label>
                <select class="form-select product-select" required onchange="validateProductRow(this)">
                    <option value="">Выберите продукт</option>
                </select>
            </div>
        </div>
        <button type="button" class="remove-product" onclick="removeProductRow(this)" title="Удалить продукт">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M18 6L6 18M6 6l12 12"/>
            </svg>
        </button>
    `;

    container.appendChild(row);

    const productSelect = row.querySelector('.product-select');
    if (products && products.length > 0) {
        products.forEach(product => {
            const option = document.createElement('option');
            option.value = product.name;
            option.textContent = product.name;
            option.dataset.productId = product.id;
            productSelect.appendChild(option);
        });
    }

    if (productName) {
        productSelect.value = productName;
    }

    updateAddButtonState();

    const allRows = container.querySelectorAll('.product-row');
    if (allRows.length === 1) {
        allRows[0].querySelector('.remove-product').style.visibility = 'hidden';
    } else {
        allRows.forEach(row => {
            row.querySelector('.remove-product').style.visibility = 'visible';
        });
    }
}

function validateProductRow(element) {
    const hasValidRows = checkForValidRows();
    if (!hasValidRows) {
        showEmptyMessage();
    } else {
        hideEmptyMessage();
    }
}

function checkForValidRows() {
    const productRows = document.querySelectorAll('.product-row');
    for (let row of productRows) {
        const weightInput = row.querySelector('.weight-input');
        const productSelect = row.querySelector('.product-select');

        if (weightInput && productSelect &&
            weightInput.value && productSelect.value) {
            return true;
        }
    }
    return false;
}

function showEmptyMessage() {
    const container = document.getElementById('productsContainer');
    const existingMessage = container.querySelector('.empty-products-message');
    if (!existingMessage && container.children.length === 0) {
        container.innerHTML = '<div class="empty-products-message">Добавьте хотя бы один продукт</div>';
    }
}

function hideEmptyMessage() {
    const container = document.getElementById('productsContainer');
    const emptyMessage = container.querySelector('.empty-products-message');
    if (emptyMessage) {
        emptyMessage.remove();
    }
}

function removeProductRow(button) {
    const container = document.getElementById('productsContainer');
    const rows = container.querySelectorAll('.product-row');

    if (rows.length <= 1) {
        alert('Должна остаться хотя бы одна строка с продуктом');
        return;
    }

    const row = button.closest('.product-row');
    row.remove();

    const hasValidRows = checkForValidRows();
    if (!hasValidRows) {
        showEmptyMessage();
    }

    updateAddButtonState();

    const remainingRows = container.querySelectorAll('.product-row');
    if (remainingRows.length === 1) {
        remainingRows[0].querySelector('.remove-product').style.visibility = 'hidden';
    }
}

function updateAddButtonState() {
    const container = document.getElementById('productsContainer');
    const addButton = document.getElementById('addProductBtn');
    const rowCount = container.querySelectorAll('.product-row').length;
    addButton.disabled = rowCount >= 8;
}

function deleteMeal(mealId) {
    if (confirm('Вы уверены, что хотите удалить этот приём пищи?')) {
        fetch('/api/meals/' + mealId, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': getCsrfToken()
            }
        })
        .then(response => {
            if (response.ok) {
                location.reload();
            } else if (response.status === 404) {
                alert('Приём пищи не найден');
            } else if (response.status === 403) {
                alert('Нет прав для удаления этого приёма пищи');
            } else {
                alert('Ошибка при удалении приёма пищи');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Ошибка при удалении приёма пищи');
        });
    }
}

document.getElementById('mealForm').addEventListener('submit', function(e) {
    e.preventDefault();

    const mealId = document.getElementById('mealId').value;
    const mealTypeName = document.getElementById('mealTypeSelect').value;
    const productRows = document.querySelectorAll('.product-row');

    const productMap = new Map();
    let isValid = true;

    productRows.forEach(row => {
        const weightInput = row.querySelector('.weight-input');
        const productSelect = row.querySelector('.product-select');

        if (weightInput.value && productSelect.value) {
            const productName = productSelect.value;
            const weight = parseInt(weightInput.value);

            if (productMap.has(productName)) {
                productMap.set(productName, productMap.get(productName) + weight);
            } else {
                productMap.set(productName, weight);
            }
        } else if (weightInput.value || productSelect.value) {
            isValid = false;
            if (!weightInput.value) weightInput.style.borderColor = '#cf222e';
            if (!productSelect.value) productSelect.style.borderColor = '#cf222e';
        }
    });

    if (!isValid) {
        alert('Заполните все поля для добавленных продуктов');
        return;
    }

    if (productMap.size === 0) {
        alert('Добавьте хотя бы один продукт');
        return;
    }

    if (!mealTypeName) {
        alert('Выберите тип приёма пищи');
        return;
    }

    const productNames = Array.from(productMap.keys());
    const quantities = Array.from(productMap.values());

    const isEdit = !!mealId;
    let payload, url, method;

    if (isEdit) {
        payload = {
            id: parseInt(mealId),
            mealTypeName: mealTypeName,
            productNames: productNames,
            quantities: quantities
        };
        url = `/api/meals/update/${mealId}`;
        method = 'PUT';
    } else {
        payload = {
            mealTypeName: mealTypeName,
            productNames: productNames,
            quantities: quantities
        };
        url = '/api/meals/create';
        method = 'POST';
    }

    const submitBtn = document.getElementById('submitBtn');
    const originalText = submitBtn.textContent;
    submitBtn.disabled = true;
    submitBtn.textContent = 'Сохранение...';

    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': getCsrfToken()
        },
        body: JSON.stringify(payload)
    })
    .then(response => {
        if (response.ok) {
            closeModal();
            location.reload();
        } else {
            throw new Error(`HTTP ${response.status}`);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Ошибка при сохранении приёма пищи');
    })
    .finally(() => {
        submitBtn.disabled = false;
        submitBtn.textContent = originalText;
    });
});

document.addEventListener('input', function(e) {
    if (e.target.classList.contains('weight-input') || e.target.classList.contains('product-select')) {
        e.target.style.borderColor = '';
    }
});

function getCsrfToken() {
    let token = '';
    const metaToken = document.querySelector('meta[name="_csrf"]');
    if (metaToken) {
        token = metaToken.getAttribute('content');
    }
    return token;
}