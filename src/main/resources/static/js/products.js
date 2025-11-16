document.addEventListener('DOMContentLoaded', function() {
    const table = document.getElementById('productsTable');
    const headers = table.querySelectorAll('th[data-sort]');
    let currentSort = {
        column: null,
        direction: 'asc'
    };

    // Добавляем обработчики событий для заголовков таблицы
    headers.forEach(header => {
        header.addEventListener('click', function() {
            const column = this.getAttribute('data-sort');
            sortTable(column);
        });
    });

    // Функция сортировки таблицы
    function sortTable(column) {
        const tbody = table.querySelector('tbody');
        const rows = Array.from(tbody.querySelectorAll('tr'));

        // Определяем направление сортировки
        if (currentSort.column === column) {
            currentSort.direction = currentSort.direction === 'asc' ? 'desc' : 'asc';
        } else {
            currentSort.column = column;
            currentSort.direction = 'asc';
        }

        // Сортируем строки
        rows.sort((a, b) => {
            const aValue = getCellValue(a, column);
            const bValue = getCellValue(b, column);

            // Для числовых значений
            if (['id', 'calories', 'proteins', 'fats', 'carbs'].includes(column)) {
                const aNum = parseFloat(aValue) || 0;
                const bNum = parseFloat(bValue) || 0;

                return currentSort.direction === 'asc' ? aNum - bNum : bNum - aNum;
            }

            // Для текстовых значений
            if (currentSort.direction === 'asc') {
                return aValue.localeCompare(bValue);
            } else {
                return bValue.localeCompare(aValue);
            }
        });

        // Обновляем таблицу
        rows.forEach(row => tbody.appendChild(row));

        // Обновляем индикаторы сортировки
        updateSortIndicators(column);
    }

    // Получаем значение ячейки для сортировки
    function getCellValue(row, column) {
        const cellIndex = getColumnIndex(column);
        const cell = row.cells[cellIndex];

        if (!cell) return '';

        // Для столбца "Created By" учитываем, что может быть "System"
        if (column === 'createdBy') {
            return cell.textContent.trim();
        }

        return cell.textContent.trim();
    }

    // Получаем индекс столбца по его data-sort атрибуту
    function getColumnIndex(column) {
        const headers = table.querySelectorAll('th[data-sort]');
        for (let i = 0; i < headers.length; i++) {
            if (headers[i].getAttribute('data-sort') === column) {
                return i;
            }
        }
        return -1;
    }

    // Обновляем индикаторы сортировки
    function updateSortIndicators(activeColumn) {
        headers.forEach(header => {
            header.classList.remove('sort-asc', 'sort-desc');

            if (header.getAttribute('data-sort') === activeColumn) {
                header.classList.add(`sort-${currentSort.direction}`);
            }
        });
    }

    // Обработчики для кнопок удаления
    const deleteButtons = document.querySelectorAll('.btn-delete');
    deleteButtons.forEach(button => {
        button.addEventListener('click', function() {
            const productId = this.getAttribute('data-id');
            if (confirm('Are you sure you want to delete this product?')) {
                fetch(`/api/products/delete${productId}`, {
                    method: 'DELETE'
                })
                .then(response => {
                    if (response.ok) {
                        // Удаляем строку из таблицы
                        const row = this.closest('tr');
                        row.remove();

                        // Обновляем статистику после удаления
                        updateStats();
                    } else {
                        alert('Error deleting product');
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('Error deleting product');
                });
            }
        });
    });

    // Функция для обновления статистики - уменьшает счетчики
    function updateStats() {
        // Получаем текущие значения счетчиков
        const totalCountElement = document.getElementById('totalCount');
        const userCountElement = document.getElementById('userCount');

        let totalCount = parseInt(totalCountElement.textContent) || 0;
        let userCount = parseInt(userCountElement.textContent) || 0;

        // Уменьшаем общее количество продуктов на 1
        totalCount = Math.max(0, totalCount - 1);

        // Уменьшаем количество пользовательских продуктов на 1
        // (поскольку кнопки удаления есть только у пользовательских продуктов)
        userCount = Math.max(0, userCount - 1);

        // Обновляем отображение статистики
        totalCountElement.textContent = totalCount;
        userCountElement.textContent = userCount;
    }
});