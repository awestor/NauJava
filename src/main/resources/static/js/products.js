let allProducts = [];
let filteredProducts = [];
let currentPage = 1;
let pageSize = 8;
let currentSort = {
    column: 'order',
    direction: 'desc'
};
let searchTimeout = null;

document.addEventListener('DOMContentLoaded', function() {
    initializeProductsData();
    initializeTableSorting();
    initializeProductModals();
    initializeSearch();
    initializePagination();

    setTimeout(() => {
        const orderHeader = document.querySelector('th[data-sort="order"]');
        if (orderHeader) {
            orderHeader.click();
        }
    }, 100);
});

function initializeProductsData() {
    const productRows = document.querySelectorAll('.product-row');
    allProducts = Array.from(productRows).map((row, index) => {
        const originalOrder = parseInt(row.querySelector('.order-cell').textContent);
        return {
            id: row.getAttribute('data-product-id'),
            name: row.querySelector('.product-name').textContent.trim(),
            calories: parseFloat(row.cells[2].textContent.split(' ')[0]) || 0,
            proteins: parseFloat(row.cells[3].textContent.split(' ')[0]) || 0,
            fats: parseFloat(row.cells[4].textContent.split(' ')[0]) || 0,
            carbs: parseFloat(row.cells[5].textContent.split(' ')[0]) || 0,
            originalOrder: originalOrder,
            element: row
        };
    });

    filteredProducts = [...allProducts];
    updateStats();
    checkPaginationNeeded();
}

function initializeSearch() {
    const searchInput = document.getElementById('searchInput');

    searchInput.addEventListener('input', function(e) {
        if (searchTimeout) {
            clearTimeout(searchTimeout);
        }

        searchTimeout = setTimeout(() => {
            performSearch(e.target.value.trim());
        }, 200);
    });

    searchInput.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            e.target.value = '';
            performSearch('');
        }
    });
}

function performSearch(searchTerm) {
    if (searchTerm === '') {
        filteredProducts = [...allProducts];
    } else {
        const searchLower = searchTerm.toLowerCase();
        filteredProducts = allProducts.filter(product =>
            product.name.toLowerCase().includes(searchLower)
        );
    }

    currentPage = 1;
    applySortingAndPagination();
    updateStats();
    checkPaginationNeeded();
}

function initializePagination() {
    const pageSizeSelect = document.getElementById('pageSizeSelect');
    const prevPageBtn = document.getElementById('prevPage');
    const nextPageBtn = document.getElementById('nextPage');

    pageSizeSelect.addEventListener('change', function(e) {
        pageSize = parseInt(e.target.value);
        currentPage = 1;
        applySortingAndPagination();
        updatePaginationControls();
    });

    prevPageBtn.addEventListener('click', function() {
        if (currentPage > 1) {
            currentPage--;
            applySortingAndPagination();
            updatePaginationControls();
        }
    });

    nextPageBtn.addEventListener('click', function() {
        const totalPages = Math.ceil(filteredProducts.length / pageSize);
        if (currentPage < totalPages) {
            currentPage++;
            applySortingAndPagination();
            updatePaginationControls();
        }
    });
}

function applySortingAndPagination() {
    const sortedProducts = sortProducts([...filteredProducts]);

    const startIndex = (currentPage - 1) * pageSize;
    const endIndex = startIndex + pageSize;
    const paginatedProducts = sortedProducts.slice(startIndex, endIndex);

    updateTableDisplay(paginatedProducts);
    updatePaginationInfo();
}

function sortProducts(products) {
    return products.sort((a, b) => {
        const aValue = getProductValue(a, currentSort.column);
        const bValue = getProductValue(b, currentSort.column);

        if (currentSort.column === 'order') {
            return currentSort.direction === 'asc' ?
                a.originalOrder - b.originalOrder :
                b.originalOrder - a.originalOrder;
        }

        if (['calories', 'proteins', 'fats', 'carbs'].includes(currentSort.column)) {
            return currentSort.direction === 'asc' ? aValue - bValue : bValue - aValue;
        }

        if (currentSort.direction === 'asc') {
            return aValue.localeCompare(bValue);
        } else {
            return bValue.localeCompare(aValue);
        }
    });
}

function getProductValue(product, column) {
    switch (column) {
        case 'name':
            return product.name;
        case 'calories':
            return product.calories;
        case 'proteins':
            return product.proteins;
        case 'fats':
            return product.fats;
        case 'carbs':
            return product.carbs;
        default:
            return '';
    }
}

function updateTableDisplay(productsToShow) {
    const tbody = document.getElementById('productsTableBody');

    tbody.innerHTML = '';

    productsToShow.forEach((product) => {
        const row = product.element.cloneNode(true);
        row.querySelector('.order-cell').textContent = product.originalOrder;
        tbody.appendChild(row);
    });
}

function updatePaginationInfo() {
    const shownCount = document.getElementById('shownCount');
    const totalCount = document.getElementById('totalCount');
    const startIndex = (currentPage - 1) * pageSize + 1;
    const endIndex = Math.min(currentPage * pageSize, filteredProducts.length);

    shownCount.textContent = `${startIndex}-${endIndex}`;
    totalCount.textContent = filteredProducts.length;
}

function updatePaginationControls() {
    const totalPages = Math.ceil(filteredProducts.length / pageSize);
    const paginationPages = document.getElementById('paginationPages');
    const prevPageBtn = document.getElementById('prevPage');
    const nextPageBtn = document.getElementById('nextPage');

    prevPageBtn.disabled = currentPage === 1;
    nextPageBtn.disabled = currentPage === totalPages || totalPages === 0;

    paginationPages.innerHTML = '';

    if (totalPages <= 7) {
        for (let i = 1; i <= totalPages; i++) {
            addPageButton(i, paginationPages);
        }
    } else {
        addPageButton(1, paginationPages);

        if (currentPage > 3) {
            addEllipsis(paginationPages);
        }

        const startPage = Math.max(2, currentPage - 1);
        const endPage = Math.min(totalPages - 1, currentPage + 1);

        for (let i = startPage; i <= endPage; i++) {
            addPageButton(i, paginationPages);
        }

        if (currentPage < totalPages - 2) {
            addEllipsis(paginationPages);
        }

        addPageButton(totalPages, paginationPages);
    }
}

function addPageButton(pageNumber, container) {
    const pageBtn = document.createElement('button');
    pageBtn.className = `pagination-page ${pageNumber === currentPage ? 'active' : ''}`;
    pageBtn.textContent = pageNumber;
    pageBtn.addEventListener('click', () => {
        currentPage = pageNumber;
        applySortingAndPagination();
        updatePaginationControls();
    });
    container.appendChild(pageBtn);
}

function addEllipsis(container) {
    const ellipsis = document.createElement('span');
    ellipsis.className = 'pagination-ellipsis';
    ellipsis.textContent = '...';
    container.appendChild(ellipsis);
}

function checkPaginationNeeded() {
    const paginationContainer = document.getElementById('paginationContainer');

    if (filteredProducts.length > pageSize) {
        paginationContainer.style.display = 'flex';
        updatePaginationControls();
    } else {
        paginationContainer.style.display = 'none';
    }
}

function updateStats() {
    const totalCountElement = document.getElementById('totalProductsCount');
    const filteredCountElement = document.getElementById('filteredProductsCount');

    if (totalCountElement) {
        totalCountElement.textContent = allProducts.length;
    }
    if (filteredCountElement) {
        filteredCountElement.textContent = allProducts.length - filteredProducts.length;
    }
}

function initializeTableSorting() {
    const table = document.querySelector('.products-table');
    const headers = table.querySelectorAll('th.sortable');

    headers.forEach(header => {
        header.addEventListener('click', function() {
            const column = this.getAttribute('data-sort');
            sortTable(column);
        });
    });

    function sortTable(column) {
        if (currentSort.column === column) {
            currentSort.direction = currentSort.direction === 'asc' ? 'desc' : 'asc';
        } else {
            currentSort.column = column;
            currentSort.direction = 'asc';
        }

        applySortingAndPagination();
        updateSortIndicators(column);
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

            const deletedProduct = allProducts.find(p => p.id === productId.toString());
            const deletedOrder = deletedProduct ? deletedProduct.originalOrder : null;

            allProducts = allProducts.filter(p => p.id !== productId.toString());
            filteredProducts = filteredProducts.filter(p => p.id !== productId.toString());

            if (deletedOrder !== null) {
                allProducts.forEach(product => {
                    if (product.originalOrder > deletedOrder) {
                        product.originalOrder -= 1;
                    }
                });
                filteredProducts.forEach(product => {
                    if (product.originalOrder > deletedOrder) {
                        product.originalOrder -= 1;
                    }
                });
            }

            const totalPages = Math.ceil(filteredProducts.length / pageSize);
            if (currentPage > totalPages && totalPages > 0) {
                currentPage = totalPages;
            }

            applySortingAndPagination();
            updateStats();
            checkPaginationNeeded();
        } else {
            throw new Error(`HTTP ${response.status}`);
        }
    })
    .catch(error => {
        console.error('Error deleting product:', error);
        alert('Ошибка при удалении продукта');
    });
}

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

if (!document.querySelector('style[data-products]')) {
    const style = document.createElement('style');
    style.setAttribute('data-products', 'true');
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
}