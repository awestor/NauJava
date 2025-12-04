let allUsers = [];
let currentPage = 1;
let pageSize = 16;
let currentSort = {
    column: 'lastActivity',
    direction: 'desc'
};
let isLoading = false;
let lastUpdateTime = null;

const API = {
    users: '/admin/api/users',
    userDetails: (login) => `/admin/api/users/${login}`,
    usersStats: '/admin/api/users/stats'
};

document.addEventListener('DOMContentLoaded', function() {
    initializePageSizeSelector();
    initializePagination();
    loadUsersData();
    initializeUserTableEvents();
    updateLastUpdateTime();
});

function initializePageSizeSelector() {
    const pageSizeSelect = document.getElementById('pageSizeSelect');

    pageSizeSelect.addEventListener('change', function(e) {
        pageSize = parseInt(e.target.value);
        currentPage = 1;
        applyPagination();
        updatePaginationControls();
    });
}

function initializePagination() {
    const prevPageBtn = document.getElementById('prevPage');
    const nextPageBtn = document.getElementById('nextPage');

    prevPageBtn.addEventListener('click', function() {
        if (currentPage > 1) {
            currentPage--;
            applyPagination();
            updatePaginationControls();
        }
    });

    nextPageBtn.addEventListener('click', function() {
        const totalPages = Math.ceil(allUsers.length / pageSize);
        if (currentPage < totalPages) {
            currentPage++;
            applyPagination();
            updatePaginationControls();
        }
    });
}

function initializeUserTableEvents() {
    const table = document.querySelector('.users-table');
    const headers = table.querySelectorAll('th.sortable');

    headers.forEach(header => {
        header.addEventListener('click', function() {
            const column = this.getAttribute('data-sort');
            sortUsers(column);
        });
    });
}

function loadUsersData() {
    if (isLoading) return;

    isLoading = true;
    showLoadingState();

    fetch(API.users, {
        headers: {
            'X-CSRF-TOKEN': getCsrfToken()
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        allUsers = data;
        lastUpdateTime = new Date();
        updateLastUpdateTime();
        applySorting();
        applyPagination();
        updatePaginationControls();
        updateStats();
        checkPaginationNeeded();
        hideNoDataMessage();
    })
    .catch(error => {
        console.error('Error loading users:', error);
        showNoDataMessage();
    })
    .finally(() => {
        isLoading = false;
        hideLoadingState();
    });
}

function showLoadingState() {
    const tbody = document.getElementById('usersTableBody');
    tbody.innerHTML = `
        <tr class="loading-row">
            <td colspan="4">
                <div class="loading-spinner"></div>
                <div>Загрузка пользователей...</div>
            </td>
        </tr>
    `;
}

function hideLoadingState() {
    if (allUsers.length === 0) {
        return;
    }
}

function sortUsers(column) {
    if (currentSort.column === column) {
        currentSort.direction = currentSort.direction === 'asc' ? 'desc' : 'asc';
    } else {
        currentSort.column = column;
        currentSort.direction = 'asc';
    }

    applySorting();
    applyPagination();
    updateSortIndicators(column);
}

function applySorting() {
    allUsers.sort((a, b) => {
        const aValue = getUserValue(a, currentSort.column);
        const bValue = getUserValue(b, currentSort.column);

        if (currentSort.column === 'streak') {
            const aNum = parseInt(aValue) || 0;
            const bNum = parseInt(bValue) || 0;
            return currentSort.direction === 'asc' ? aNum - bNum : bNum - aNum;
        }

        if (currentSort.column === 'lastActivity') {
            const aDate = aValue ? new Date(aValue) : new Date(0);
            const bDate = bValue ? new Date(bValue) : new Date(0);
            return currentSort.direction === 'asc' ? aDate - bDate : bDate - aDate;
        }

        if (currentSort.direction === 'asc') {
            return aValue.localeCompare(bValue);
        } else {
            return bValue.localeCompare(aValue);
        }
    });
}

function getUserValue(user, column) {
    switch (column) {
        case 'login':
            return user.login || '';
        case 'fio':
            return user.fio || '';
        case 'streak':
            return user.streak || 0;
        case 'lastActivity':
            return user.lastActivity || '';
        default:
            return '';
    }
}

function updateSortIndicators(activeColumn) {
    const headers = document.querySelectorAll('th.sortable');
    headers.forEach(header => {
        header.classList.remove('sort-asc', 'sort-desc');
        if (header.getAttribute('data-sort') === activeColumn) {
            header.classList.add(`sort-${currentSort.direction}`);
        }
    });
}

function applyPagination() {
    const startIndex = (currentPage - 1) * pageSize;
    const endIndex = startIndex + pageSize;
    const paginatedUsers = allUsers.slice(startIndex, endIndex);

    updateTableDisplay(paginatedUsers);
    updatePaginationInfo();
}

function updateTableDisplay(usersToShow) {
    const tbody = document.getElementById('usersTableBody');

    if (usersToShow.length === 0) {
        showNoDataMessage();
        return;
    }

    let tableHTML = '';

    usersToShow.forEach((user, index) => {
        const globalIndex = (currentPage - 1) * pageSize + index + 1;

        tableHTML += `
            <tr class="user-row" data-login="${user.login}" data-fio="${user.fio}">
                <td>
                    <div class="userLogin">${user.login}</div>
                    <div class="user-email">${user.email || 'Не указан'}</div>
                </td>
                <td class="user-fio">${user.fio}</td>
                <td>
                    <div class="last-activity">${formatDateTime(user.lastActivity)}</div>
                    <div class="activity-ago">${getTimeAgo(user.lastActivity)}</div>
                </td>
                <td>
                    <div class="streak-badge">${user.streak || 0} дней</div>
                </td>
            </tr>
        `;
    });

    tbody.innerHTML = tableHTML;

    const userRows = document.querySelectorAll('.user-row');
    userRows.forEach(row => {
        row.addEventListener('click', function(e) {
            const login = this.getAttribute('data-login');
            const fio = this.getAttribute('data-fio');
            openUserDetailsModal(login, fio);
        });

        row.addEventListener('mouseenter', function() {
            this.style.cursor = 'pointer';
        });
    });
}

function openUserDetailsModal(login, fio) {
    const modal = document.getElementById('userDetailsModal');
    const loadingOverlay = document.getElementById('userDetailsLoading');
    const content = document.getElementById('userDetailsContent');

    content.style.display = 'none';
    loadingOverlay.style.display = 'flex';

    modal.classList.add('active');

    fetch(API.userDetails(login), {
        headers: {
            'X-CSRF-TOKEN': getCsrfToken()
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        return response.json();
    })
    .then(userDetails => {
        populateUserDetails(userDetails);
        loadingOverlay.style.display = 'none';
        content.style.display = 'block';
    })
    .catch(error => {
        console.error('Error loading user details:', error);
        loadingOverlay.innerHTML = `
            <div style="color: #cf222e; font-size: 16px; text-align: center;">
                <div style="font-size: 48px; margin-bottom: 16px;">⚠️</div>
                <div>Ошибка при загрузке данных пользователя</div>
                <div style="font-size: 12px; margin-top: 8px;">${error.message}</div>
            </div>
        `;
    });
}

function populateUserDetails(userDetails) {
    document.getElementById('detailLogin').textContent = userDetails.login || 'Не указан';
    document.getElementById('detailEmail').textContent = userDetails.email || 'Не указан';
    document.getElementById('detailSurname').textContent = userDetails.surname || '-';
    document.getElementById('detailName').textContent = userDetails.name || '-';
    document.getElementById('detailPatronymic').textContent = userDetails.patronymic || '-';
    document.getElementById('detailStreak').textContent = userDetails.currentStreak || 0;
    document.getElementById('detailLastActivity').textContent = formatDateTime(userDetails.lastActivity);
    document.getElementById('detailCreatedAt').textContent = formatDateTime(userDetails.createdAt);
    document.getElementById('detailCalorieGoal').textContent = userDetails.dailyCalorieGoal ?
        userDetails.dailyCalorieGoal + ' ккал' : 'Не установлена';
    document.getElementById('detailActivityLevel').textContent = userDetails.activityLevel || 'Не указан';
}

function closeUserDetailsModal() {
    const modal = document.getElementById('userDetailsModal');
    modal.classList.remove('active');
}

function updatePaginationInfo() {
    const shownCount = document.getElementById('shownCount');
    const totalCount = document.getElementById('totalCount');
    const startIndex = (currentPage - 1) * pageSize + 1;
    const endIndex = Math.min(currentPage * pageSize, allUsers.length);

    shownCount.textContent = `${startIndex}-${endIndex}`;
    totalCount.textContent = allUsers.length;
}

function updatePaginationControls() {
    const totalPages = Math.ceil(allUsers.length / pageSize);
    const paginationPages = document.getElementById('paginationPages');
    const prevPageBtn = document.getElementById('prevPage');
    const nextPageBtn = document.getElementById('nextPage');

    prevPageBtn.disabled = currentPage === 1 || totalPages === 0;
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
        applyPagination();
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

    if (allUsers.length > pageSize) {
        paginationContainer.style.display = 'flex';
        updatePaginationControls();
    } else {
        paginationContainer.style.display = 'none';
    }
}

function updateStats() {
    const totalCountElement = document.getElementById('totalUsersCount');
    const activeTodayElement = document.getElementById('activeTodayCount');
    const avgStreakElement = document.getElementById('avgStreakCount');

    if (totalCountElement) {
        totalCountElement.textContent = allUsers.length;
    }

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const activeToday = allUsers.filter(user => {
        if (!user.lastActivity) return false;
        const lastActivity = new Date(user.lastActivity);
        lastActivity.setHours(0, 0, 0, 0);
        return lastActivity.getTime() === today.getTime();
    }).length;

    const totalStreak = allUsers.reduce((sum, user) => sum + (user.streak || 0), 0);
    const avgStreak = allUsers.length > 0 ? Math.round(totalStreak / allUsers.length) : 0;

    if (activeTodayElement) {
        activeTodayElement.textContent = activeToday;
    }

    if (avgStreakElement) {
        avgStreakElement.textContent = avgStreak;
    }
}

function updateLastUpdateTime() {
    const lastUpdateElement = document.getElementById('lastUpdateTime');
    if (lastUpdateTime) {
        lastUpdateElement.textContent = lastUpdateTime.toLocaleTimeString('ru-RU');
    }
}

function showNoDataMessage() {
    const noDataMessage = document.getElementById('noDataMessage');
    noDataMessage.style.display = 'block';
}

function hideNoDataMessage() {
    const noDataMessage = document.getElementById('noDataMessage');
    if (allUsers.length > 0) {
        noDataMessage.style.display = 'none';
    }
}

function formatDateTime(dateTimeString) {
    if (!dateTimeString) return 'Никогда';

    const date = new Date(dateTimeString);

    if (isNaN(date.getTime())) {
        return 'Неверная дата';
    }

    const now = new Date();
    const diffMs = now - date;
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (diffDays === 0) {
        return `Сегодня, ${date.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' })}`;
    } else if (diffDays === 1) {
        return `Вчера, ${date.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' })}`;
    } else if (diffDays < 7) {
        return `${diffDays} дней назад`;
    } else {
        return date.toLocaleDateString('ru-RU', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }
}

function getTimeAgo(dateTimeString) {
    if (!dateTimeString) return '';

    const date = new Date(dateTimeString);
    const now = new Date();
    const diffMs = now - date;

    const diffMinutes = Math.floor(diffMs / (1000 * 60));
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (diffMinutes < 60) {
        return `${diffMinutes} мин. назад`;
    } else if (diffHours < 24) {
        return `${diffHours} ч. назад`;
    } else if (diffDays < 30) {
        return `${diffDays} дн. назад`;
    } else {
        const diffMonths = Math.floor(diffDays / 30);
        return `${diffMonths} мес. назад`;
    }
}

function getCsrfToken() {
    const metaToken = document.querySelector('meta[name="_csrf"]');
    return metaToken ? metaToken.getAttribute('content') : '';
}

// Автообновление данных каждые 5 минут
setInterval(() => {
    loadUsersData();
}, 5 * 60 * 1000);