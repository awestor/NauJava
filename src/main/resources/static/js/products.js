document.addEventListener('DOMContentLoaded', function() {
    initializeTableSorting();
    initializeProductModals();

    setTimeout(() => {
        saveOriginalOrderNumbers();
        const orderHeader = document.querySelector('th[data-sort="order"]');
        if (orderHeader) {
            orderHeader.click();
        }
    }, 100);
});

function validateProductName(input) {
    const value = input.value;
    const length = value.length;

    if (length > 100) {
        input.classList.add('error');
        return false;
    } else {
        input.classList.remove('error');
        return true;
    }
}

function initializeTableSorting() {
    const table = document.querySelector('.products-table');
    const headers = table.querySelectorAll('th.sortable');
    let currentSort = {
        column: 'order',
        direction: 'desc'
    };

    headers.forEach(header => {
        header.addEventListener('click', function() {
            const column = this.getAttribute('data-sort');
            sortTable(column);
        });
    });

    function sortTable(column) {
        const tbody = table.querySelector('tbody');
        const rows = Array.from(tbody.querySelectorAll('tr'));

        if (currentSort.column === column) {
            currentSort.direction = currentSort.direction === 'asc' ? 'desc' : 'asc';
        } else {
            currentSort.column = column;
            currentSort.direction = 'asc';
        }

        rows.sort((a, b) => {
            const aValue = getCellValue(a, column);
            const bValue = getCellValue(b, column);

            if (column === 'order') {
                const aNum = parseInt(aValue) || 0;
                const bNum = parseInt(bValue) || 0;
                return currentSort.direction === 'asc' ? aNum - bNum : bNum - aNum;
            }

            if (['calories', 'proteins', 'fats', 'carbs'].includes(column)) {
                const aNum = parseFloat(aValue) || 0;
                const bNum = parseFloat(bValue) || 0;
                return currentSort.direction === 'asc' ? aNum - bNum : bNum - aNum;
            }

            if (currentSort.direction === 'asc') {
                return aValue.localeCompare(bValue);
            } else {
                return bValue.localeCompare(aValue);
            }
        });

        tbody.innerHTML = '';
        rows.forEach(row => tbody.appendChild(row));

        updateSortIndicators(column);
    }

    function getCellValue(row, column) {
        const cellIndex = getColumnIndex(column);
        const cell = row.cells[cellIndex];
        if (!cell) return '';

        if (column === 'order') {
            return cell.textContent.trim();
        }

        if (column === 'name') {
            return cell.textContent.trim();
        }

        const text = cell.textContent.trim();
        if (['calories', 'proteins', 'fats', 'carbs'].includes(column)) {
            return text.split(' ')[0];
        }

        return text;
    }

    function getColumnIndex(column) {
        const headers = table.querySelectorAll('th[data-sort]');
        for (let i = 0; i < headers.length; i++) {
            if (headers[i].getAttribute('data-sort') === column) {
                return i;
            }
        }
        return -1;
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

function saveOriginalOrderNumbers() {
    const rows = document.querySelectorAll('.products-table tbody tr');
    rows.forEach((row, index) => {
        const orderCell = row.cells[0];
        orderCell.setAttribute('data-original-order', (index + 1).toString());
    });
}

function restoreOriginalOrderNumbers(rows) {
    rows.forEach(row => {
        const orderCell = row.cells[0];
        const originalOrder = orderCell.getAttribute('data-original-order');
        if (originalOrder) {
            orderCell.textContent = originalOrder;
        }
    });
}

function renumberTableRows() {
    const rows = document.querySelectorAll('.products-table tbody tr');
    rows.forEach((row, index) => {
        const orderCell = row.cells[0];
        const newOrder = index + 1;
        orderCell.textContent = newOrder;
        orderCell.setAttribute('data-original-order', newOrder.toString());
    });
}

function initializeProductModals() {
    document.getElementById('editProductForm').addEventListener('submit', function(e) {
        e.preventDefault();
        updateProduct();
    });
    const modal = document.getElementById('editProductModal');
    let mouseDownOnOverlay = false;

    modal.addEventListener('mousedown', function(e) {
        if (e.target === this) {
            mouseDownOnOverlay = true;
        }
    });

    modal.addEventListener('mouseup', function(e) {
        if (e.target === this && mouseDownOnOverlay) {
            closeEditModal();
        }
        mouseDownOnOverlay = false;
    });
}

function editProduct(productId) {
    fetch(`/api/products/${productId}`, {
        headers: {
            'X-CSRF-TOKEN': getCsrfToken()
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Ошибка загрузки данных продукта');
        }
        return response.json();
    })
    .then(product => {
        if (product) {
            openEditModal(product);
        } else {
            alert('Продукт не найден');
        }
    })
    .catch(error => {
        console.error('Error loading product:', error);
        alert('Ошибка при загрузке данных продукта');
    });
}

function openEditModal(product) {
    const modal = document.getElementById('editProductModal');

    document.getElementById('editProductId').value = product.id;
    document.getElementById('editProductName').value = product.name;
    document.getElementById('editProductCalories').value = product.caloriesPer100g;
    document.getElementById('editProductProteins').value = product.proteinsPer100g;
    document.getElementById('editProductFats').value = product.fatsPer100g;
    document.getElementById('editProductCarbs').value = product.carbsPer100g;

    modal.classList.add('active');
}

function closeEditModal() {
    const modal = document.getElementById('editProductModal');
    modal.classList.remove('active');
    document.getElementById('editProductForm').reset();
}

function updateProduct() {
    const productId = document.getElementById('editProductId').value;
    const nameInput = document.getElementById('editProductName');

    if (!validateProductName(nameInput)) {
        alert('Название продукта не может превышать 200 символов');
        nameInput.focus();
        return;
    }

    const formData = {
        id: parseInt(productId),
        name: document.getElementById('editProductName').value,
        caloriesPer100g: parseFloat(document.getElementById('editProductCalories').value),
        proteinsPer100g: parseFloat(document.getElementById('editProductProteins').value),
        fatsPer100g: parseFloat(document.getElementById('editProductFats').value),
        carbsPer100g: parseFloat(document.getElementById('editProductCarbs').value)
    };

    if (!formData.name.trim()) {
        alert('Введите название продукта');
        return;
    }

    const submitBtn = document.getElementById('editSubmitBtn');
    const originalText = submitBtn.textContent;
    submitBtn.disabled = true;
    submitBtn.textContent = 'Сохранение...';

    fetch(`/api/products/${productId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': getCsrfToken()
        },
        body: JSON.stringify(formData)
    })
    .then(response => {
        if (response.ok) {
            closeEditModal();
            showNotification('Продукт успешно обновлён', 'success');
            setTimeout(() => location.reload(), 1000);
        } else {
            throw new Error(`HTTP ${response.status}`);
        }
    })
    .catch(error => {
        console.error('Error updating product:', error);
        alert('Ошибка при обновлении продукта');
    })
    .finally(() => {
        submitBtn.disabled = false;
        submitBtn.textContent = originalText;
    });
}

function deleteProduct(productId) {
    if (!confirm('Вы уверены, что хотите удалить этот продукт?')) {
        return;
    }

    fetch(`/api/products/${productId}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': getCsrfToken()
        }
    })
    .then(response => {
        if (response.ok) {
            showNotification('Продукт успешно удалён', 'success');
            const row = document.querySelector(`tr[data-product-id="${productId}"]`);
            if (row) {
                row.remove();
                renumberTableRows();
            }
            updateStats();
        } else {
            throw new Error(`HTTP ${response.status}`);
        }
    })
    .catch(error => {
        console.error('Error deleting product:', error);
        alert('Ошибка при удалении продукта');
    });
}

function renumberTableRows() {
    const rows = document.querySelectorAll('.products-table tbody tr');
    rows.forEach((row, index) => {
        const orderCell = row.cells[0];
        orderCell.textContent = index + 1;
    });
}

function updateStats() {
    const totalCountElement = document.querySelector('.stat-value');
    if (totalCountElement) {
        const currentCount = parseInt(totalCountElement.textContent) || 0;
        totalCountElement.textContent = Math.max(0, currentCount - 1);
    }
}

function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;

    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 12px 20px;
        border-radius: 6px;
        color: white;
        font-weight: 500;
        z-index: 10000;
        animation: slideIn 0.3s ease;
    `;

    if (type === 'success') {
        notification.style.background = '#2ea043';
    } else if (type === 'error') {
        notification.style.background = '#cf222e';
    } else {
        notification.style.background = '#656d76';
    }

    document.body.appendChild(notification);

    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 300);
    }, 3000);
}

function getCsrfToken() {
    const metaToken = document.querySelector('meta[name="_csrf"]');
    return metaToken ? metaToken.getAttribute('content') : '';
}

const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }

    @keyframes slideOut {
        from { transform: translateX(0); opacity: 1; }
        to { transform: translateX(100%); opacity: 0; }
    }
`;
document.head.appendChild(style);