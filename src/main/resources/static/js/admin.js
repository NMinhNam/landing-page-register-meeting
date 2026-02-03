// Auth Check handled by admin-common.js

// Global variable to hold the refresh interval
let refreshIntervalId = null;
let currentRefreshInterval = 10000; // Default to 10 seconds

// Setup on load
document.addEventListener('DOMContentLoaded', function () {
    loadProvinces();
    loadData();
    loadStats();

    // Start auto refresh with default interval
    startAutoRefresh();
});

// Function to start auto refresh
function startAutoRefresh() {
    // Clear any existing interval
    if (refreshIntervalId) {
        clearInterval(refreshIntervalId);
    }

    // Only set interval if currentRefreshInterval is greater than 0
    if (currentRefreshInterval > 0) {
        refreshIntervalId = setInterval(() => {
            // Only refresh if Modal is NOT open
            const approvalModalEl = document.getElementById('approvalModal');
            const detailModalEl = document.getElementById('detailModal');
            const isModalOpen = (approvalModalEl && approvalModalEl.classList.contains('show')) ||
                (detailModalEl && detailModalEl.classList.contains('show'));

            if (!isModalOpen) {
                loadData(true); // true = silent mode
                loadStats();
            }
        }, currentRefreshInterval);
    }
}

// Function to change the refresh interval
function changeRefreshInterval() {
    const selectElement = document.getElementById('refreshInterval');
    currentRefreshInterval = parseInt(selectElement.value);

    // Restart the auto refresh with the new interval
    startAutoRefresh();
}

function loadProvinces() {
    fetch('/data/provinces.json')
        .then(res => res.json())
        .then(provinces => {
            const filterProvinceSelect = document.getElementById('filterProvince');

            // Add provinces to select dropdown
            provinces.forEach(p => {
                const option = document.createElement('option');
                option.value = p.name;
                option.textContent = p.name;
                filterProvinceSelect.appendChild(option);
            });
        })
        .catch(err => console.error('Error loading provinces:', err));
}



let debounceTimer;
function debounceLoadData() {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => {
        loadData();
    }, 500);
}

function loadData(isSilent = false) {
    const month = document.getElementById('filterMonth').value;
    const week = document.getElementById('filterWeek').value;
    const year = document.getElementById('filterYear').value;
    const status = document.getElementById('filterStatus').value;
    const province = document.getElementById('filterProvince').value;
    const keyword = document.getElementById('searchKeyword').value;
    const role = localStorage.getItem('adminRole');
    const adminId = localStorage.getItem('adminId');

    // Also reload stats when data reloads (filtered by week or month)
    if (!isSilent) loadStats(week, month, year);

    let url = `/api/v1/admin/registrations?page=1&size=50`;
    if (month) url += `&month=${month}`;
    if (week) url += `&week=${week}`;
    if (year) url += `&year=${year}`;
    if (status) url += `&status=${status}`;
    if (province) url += `&province=${encodeURIComponent(province)}`;
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`;
    if (adminId && adminId !== 'null' && adminId !== 'undefined') url += `&adminId=${adminId}`;

    // Show loading only if not auto-refreshing
    const tbody = document.getElementById('tableBody');
    if (!isSilent && tbody.innerHTML.trim() === '') {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center py-4 text-muted"><div class="spinner-border text-primary" role="status"></div></td></tr>';
    }

    fetch(url, {
        headers: getHeaders()
    })
        .then(res => res.json())
        .then(data => {
            tbody.innerHTML = '';

            if (data.data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="8" class="text-center py-4 text-muted">Không có dữ liệu</td></tr>';
                return;
            }

            data.data.forEach(item => {
                let actionBtn = '';
                // Only show action buttons if status is PENDING and user is not VIEWER
                if (role !== 'VIEWER' && item.status === 'PENDING') {
                    actionBtn = `
                    <button class="btn btn-sm btn-outline-primary" onclick="event.stopPropagation(); openModal(${item.id})" title="Xử lý">
                        <i class="fas fa-edit"></i>
                    </button>`;
                }

                // Normalize Data Fields (Handle snake_case vs camelCase)
                const soldierName = item.soldier_name || item.soldierName || 'N/A';
                const unitName = item.unit_name || item.unitName || 'N/A';
                const relativeName = item.relative_name || item.relativeName || 'Khách';
                const relativePhone = item.relative_phone || item.relativePhone || '';
                const visitWeek = item.visit_week || item.visitWeek || '?';
                const visitMonth = item.visit_month || item.visitMonth || '?';
                const visitYear = item.visit_year || item.visitYear || '?';
                const province = item.province || 'Chưa rõ';
                const countPeople = relativeName.split(',').length;

                const row = `
                <tr onclick="showDetail(${item.id})" style="cursor: pointer;">
                    <!-- 1. Thông tin chung (Soldier/Unit/Date) -->
                    <td class="ps-4">
                        <div class="fw-bold text-dark">${soldierName}</div>
                        <div class="small text-secondary">${unitName}</div>
                        <div class="small text-muted mt-1"><i class="far fa-clock me-1"></i>${formatDate(item.created_at || item.createdAt)}</div>
                    </td>

                    <!-- 2. Người đăng ký -->
                    <td>
                        <div class="fw-bold text-primary">${relativeName}</div>
                        <div class="small text-dark"><i class="fas fa-phone-alt me-1 text-muted" style="font-size: 0.8em;"></i>${relativePhone}</div>
                        <span class="badge bg-light text-dark border mt-1">${countPeople} người</span>
                    </td>

                    <!-- 3. Lịch trình (Desktop only) -->
                    <td class="d-none d-md-table-cell">
                        <div class="fw-bold">Tuần ${visitWeek}</div>
                        <div class="small">Tháng ${visitMonth} / ${visitYear}</div>
                        <div class="small text-muted mt-1"><i class="fas fa-map-marker-alt me-1"></i>${province}</div>
                    </td>

                    <!-- 4. Trạng thái -->
                    <td class="text-center">
                        ${getStatusBadge(item.status)}
                    </td>

                    <!-- 5. Thao tác -->
                    <td class="text-end pe-4 text-nowrap">
                        ${actionBtn}
                        <button class="btn btn-sm btn-outline-danger border-0" onclick="event.stopPropagation(); deleteRegistration(${item.id})" title="Xóa">
                            <i class="fas fa-trash"></i>
                        </button>
                    </td>
                </tr>
            `;
                tbody.innerHTML += row;
            });
        })
        .catch(err => console.error(err));
}

function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleString('vi-VN', {
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit'
    });
}

function getWeekMonthDisplay(week, dateString) {
    if (!dateString) return `Tuần ${week}`;

    const date = new Date(dateString);
    const dayOfMonth = date.getDate();
    const year = date.getFullYear();

    // Get first day of month (1=Monday, 7=Sunday)
    const firstDay = new Date(date.getFullYear(), date.getMonth(), 1);
    const firstDayOfWeek = firstDay.getDay(); // 0=Sunday, 1=Monday, ..., 6=Saturday
    const firstDayOfMonth = (firstDayOfWeek === 0) ? 7 : firstDayOfWeek; // Convert to 1=Monday, 7=Sunday

    let month = date.getMonth() + 1; // 1-12
    let calculatedYear = year;

    // Logic: Nếu ngày nằm trước thứ 2 đầu tiên thì thuộc tháng trước
    if (firstDayOfMonth === 7) {
        // Ngày 1 là CN, ngày 2 trở đi là tuần 1 của tháng này
        if (dayOfMonth === 1) {
            month = month - 1; // Ngày 1 (CN) thuộc tháng trước
            if (month === 0) {
                month = 12;
                calculatedYear = year - 1; // Adjust year when moving to previous month
            }
        }
    } else {
        // Ngày 1 là Thứ 2-7
        const daysBeforeFirstMonday = (8 - firstDayOfMonth) % 7;
        if (dayOfMonth <= daysBeforeFirstMonday) {
            month = month - 1; // Thuộc tháng trước
            if (month === 0) {
                month = 12;
                calculatedYear = year - 1; // Adjust year when moving to previous month
            }
        }
    }

    const monthStr = month.toString().padStart(2, '0');
    return `${week}/${monthStr}`;
}

function loadStats(week, month, year) {
    let url = '/api/v1/admin/stats';
    let params = [];
    if (month) params.push(`month=${month}`);
    if (week) params.push(`week=${week}`);
    if (year) params.push(`year=${year}`);
    const adminId = localStorage.getItem('adminId');
    if (adminId && adminId !== 'null' && adminId !== 'undefined') params.push(`adminId=${adminId}`);
    if (params.length > 0) url += `?${params.join('&')}`;

    fetch(url, {
        headers: getHeaders()
    })
        .then(res => res.json())
        .then(data => {
            // 1. Status Counts
            if (data.byStatus) {
                const pending = data.byStatus.find(s => s.status === 'PENDING')?.count || 0;
                const approved = data.byStatus.find(s => s.status === 'APPROVED')?.count || 0;
                const rejected = data.byStatus.find(s => s.status === 'REJECTED')?.count || 0;

                document.getElementById('countPending').textContent = pending;
                document.getElementById('countApproved').textContent = approved;
                document.getElementById('countRejected').textContent = rejected;
            }

            // 2. Province Stats
            const provinceContainer = document.getElementById('provinceStatsContainer');
            if (data.byProvince && data.byProvince.length > 0) {
                provinceContainer.innerHTML = '';
                data.byProvince.forEach(p => {
                    const col = `
                    <div class="col-12 mb-2">
                        <div class="d-flex justify-content-between align-items-center border rounded p-2 bg-light">
                            <span class="small fw-bold text-truncate" style="max-width: 70%;" title="${p.province}">${p.province || 'Chưa rõ'}</span>
                            <span class="badge bg-primary rounded-pill">${p.count}</span>
                        </div>
                    </div>
                `;
                    provinceContainer.innerHTML += col;
                });
            } else {
                provinceContainer.innerHTML = '<div class="col-12 text-center text-muted small">Chưa có dữ liệu thống kê theo tỉnh</div>';
            }
        });
}

function exportData() {
    // Get current filter values
    const currentMonth = document.getElementById('filterMonth').value;
    const currentWeek = document.getElementById('filterWeek').value;
    const currentYear = document.getElementById('filterYear').value;
    const adminId = localStorage.getItem('adminId');

    // Create modal for user to select export parameters
    const modalHtml = `
        <div class="modal fade" id="exportModal" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">Xuất báo cáo Excel</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
                        <div class="mb-3">
                            <label class="form-label">Tuần (bắt buộc)</label>
                            <select class="form-select" id="exportWeek">
                                <option value="">-- Chọn tuần --</option>
                                <option value="1">Tuần 1</option>
                                <option value="2">Tuần 2</option>
                                <option value="3">Tuần 3</option>
                                <option value="4">Tuần 4</option>
                                <option value="5">Tuần 5</option>
                            </select>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Tháng (bắt buộc)</label>
                            <select class="form-select" id="exportMonth">
                                <option value="">-- Chọn tháng --</option>
                                <option value="01">Tháng 1</option>
                                <option value="02">Tháng 2</option>
                                <option value="03">Tháng 3</option>
                                <option value="04">Tháng 4</option>
                                <option value="05">Tháng 5</option>
                                <option value="06">Tháng 6</option>
                                <option value="07">Tháng 7</option>
                                <option value="08">Tháng 8</option>
                                <option value="09">Tháng 9</option>
                                <option value="10">Tháng 10</option>
                                <option value="11">Tháng 11</option>
                                <option value="12">Tháng 12</option>
                            </select>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Năm (bắt buộc)</label>
                            <select class="form-select" id="exportYear">
                                <option value="">-- Chọn năm --</option>
                                <option value="2024">Năm 2024</option>
                                <option value="2025">Năm 2025</option>
                                <option value="2026">Năm 2026</option>
                            </select>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                        <button type="button" class="btn btn-primary" onclick="performExport()">Xuất báo cáo</button>
                    </div>
                </div>
            </div>
        </div>
    `;

    // Add modal to document if not exists
    if (!document.getElementById('exportModal')) {
        document.body.insertAdjacentHTML('beforeend', modalHtml);
    }

    // Set default values to current filters
    document.getElementById('exportWeek').value = currentWeek;
    document.getElementById('exportMonth').value = currentMonth;
    document.getElementById('exportYear').value = currentYear;

    // Show modal
    const exportModal = new bootstrap.Modal(document.getElementById('exportModal'));
    exportModal.show();
}

function performExport() {
    const week = document.getElementById('exportWeek').value;
    const month = document.getElementById('exportMonth').value;
    const year = document.getElementById('exportYear').value;
    const adminId = localStorage.getItem('adminId');

    // Validate required fields
    if (!week || !month || !year) {
        alert('Vui lòng chọn đầy đủ Tuần, Tháng và Năm để xuất báo cáo');
        return;
    }

    console.log('[Export] Starting export...', { month, week, year, adminId });

    let url = `/api/v1/admin/export/registrations?v=1`;
    if (adminId && adminId !== 'null') url += `&adminId=${adminId}`;
    if (month) url += `&month=${month}`;
    if (week) url += `&week=${week}`;
    if (year) url += `&year=${year}`;

    // Show a small toast or indicator
    const btnExport = document.getElementById('btnExport');
    let originalText = '';

    if (btnExport) {
        originalText = btnExport.innerHTML;
        btnExport.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Đang tải...';
        btnExport.disabled = true;
    }

    // Close modal
    const exportModal = bootstrap.Modal.getInstance(document.getElementById('exportModal'));
    exportModal.hide();

    fetch(url, {
        headers: getHeaders()
    })
        .then(async resp => {
            if (!resp.ok) {
                const errText = await resp.text();
                throw new Error(`Lỗi server (${resp.status}): ${errText}`);
            }
            return resp.blob();
        })
        .then(blob => {
            console.log('[Export] Blob received size:', blob.size);

            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.style.display = 'none';
            a.href = url;
            a.download = `ds_dang_ky_tuan_${week || 'all'}_${new Date().getTime()}.xlsx`;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
        })
        .catch(err => {
            console.error('[Export] Failed:', err);
            alert('Không thể xuất báo cáo: ' + err.message);
        })
        .finally(() => {
            if (btnExport) {
                btnExport.innerHTML = originalText;
                btnExport.disabled = false;
            }
        });
}

function getStatusBadge(status) {
    if (status === 'PENDING') return '<span class="status-badge bg-pending">Chờ duyệt</span>';
    if (status === 'APPROVED') return '<span class="status-badge bg-approved">Đồng ý</span>';
    if (status === 'REJECTED') return '<span class="status-badge bg-rejected">Từ chối</span>';
    if (status === 'CANCELLED') return '<span class="status-badge bg-secondary">Đơn hủy</span>';
    return status;
}

// Modal Logic
let approvalModal;
let detailModal;

function showDetail(id) {
    const modalEl = document.getElementById('detailModal');
    const bodyEl = document.getElementById('detailModalBody');
    bodyEl.innerHTML = '<div class="text-center py-5"><div class="spinner-border text-primary" role="status"></div></div>';

    detailModal = new bootstrap.Modal(modalEl);
    detailModal.show();

    fetch(`/api/v1/admin/registrations/${id}`, {
        headers: getHeaders()
    })
        .then(res => res.json())
        .then(data => {
            let relativesHtml = '';
            if (data.relatives && data.relatives.length > 0) {
                data.relatives.forEach((rel, index) => {
                    relativesHtml += `
                    <div class="p-3 mb-2 border rounded bg-light">
                        <div class="row">
                            <div class="col-md-6">
                                <small class="text-muted d-block">Họ tên người thứ ${index + 1}</small>
                                <span class="fw-bold">${rel.name}</span>
                            </div>
                            <div class="col-md-6">
                                <small class="text-muted d-block">Quan hệ</small>
                                <span class="fw-bold">${rel.relationship}</span>
                            </div>
                            <div class="col-md-12 mt-2">
                                <small class="text-muted d-block">Số CCCD/CMND</small>
                                <span class="fw-bold">${rel.idNumber || 'Không có'}</span>
                            </div>
                        </div>
                    </div>
                `;
                });
            } else {
                relativesHtml = '<p class="text-muted">Không có thông tin người thân đi cùng</p>';
            }

            bodyEl.innerHTML = `
            <div class="p-3">
                <div class="mb-4">
                    <h6 class="text-primary fw-bold mb-2 border-bottom pb-2">Thông tin quân nhân</h6>
                    <div class="ps-2">
                        <small class="text-muted d-block">Họ và tên</small>
                        <span class="fs-5 fw-bold text-dark">${data.manualSoldierName}</span>
                        <small class="text-muted d-block mt-2">Đơn vị</small>
                        <span class="fw-bold text-secondary">${data.manualUnitName}</span>
                    </div>
                </div>

                <div class="mb-4">
                    <h6 class="text-primary fw-bold mb-2 border-bottom pb-2">Thông tin thăm gặp</h6>
                    <div class="row g-2 ps-1">
                        <div class="col-4">
                            <small class="text-muted d-block">Tuần</small>
                            <span class="badge bg-info">Tuần ${data.visitWeek}</span>
                        </div>
                        <div class="col-4">
                            <small class="text-muted d-block">Tháng</small>
                            <span class="badge bg-secondary">Tháng ${data.visitMonth}</span>
                        </div>
                        <div class="col-4">
                            <small class="text-muted d-block">Năm</small>
                            <span class="badge bg-success">Năm ${data.visitYear}</span>
                        </div>
                        <div class="col-12 mt-2">
                            <small class="text-muted d-block">Tỉnh/TP</small>
                            <span class="fw-bold text-dark">${data.province}</span>
                        </div>
                        <div class="col-12 mt-2">
                            <small class="text-muted d-block">SĐT đại diện</small>
                            <a href="tel:${data.representativePhone}" class="text-decoration-none fw-bold fs-6">${data.representativePhone}</a>
                        </div>
                    </div>
                </div>

                <div class="mb-4">
                    <h6 class="text-primary fw-bold mb-2 border-bottom pb-2">Danh sách người thân</h6>
                    ${relativesHtml}
                </div>

                <div>
                    <h6 class="text-primary fw-bold mb-2 border-bottom pb-2">Trạng thái & Ghi chú</h6>
                    <div class="d-flex align-items-center gap-2 mb-2 ps-2">
                        ${getStatusBadge(data.status)}
                        ${data.approvedAt ? `<small class="text-muted" style="font-size: 0.7rem;">Xử lý: ${formatDate(data.approvedAt)}</small>` : ''}
                    </div>
                    <div class="p-3 bg-light rounded border-start border-4 border-primary mx-2">
                        <p class="mb-0 small fw-medium text-dark">${data.note || 'Không có ghi chú từ người thăm'}</p>
                    </div>
                </div>
            </div>
        `;
        })
        .catch(err => {
            bodyEl.innerHTML = `<div class="alert alert-danger">Có lỗi xảy ra khi tải dữ liệu: ${err.message}</div>`;
        });
}

function openModal(id) {
    document.getElementById('currentRegId').value = id;
    document.getElementById('approvalNote').value = '';
    approvalModal = new bootstrap.Modal(document.getElementById('approvalModal'));
    approvalModal.show();
}

function submitStatus(status) {
    const id = document.getElementById('currentRegId').value;
    const note = document.getElementById('approvalNote').value;
    const adminId = localStorage.getItem('adminId');

    fetch(`/api/v1/admin/registrations/${id}/status`, {
        method: 'PUT',
        headers: getHeaders(),
        body: JSON.stringify({ status, note, adminId })
    })
        .then(async res => {
            if (res.ok) {
                approvalModal.hide();
                loadData();
                loadStats();
            } else {
                const errData = await res.json();
                const msg = errData.message || 'Bạn không có quyền xử lý đơn này';
                alert(msg);

                // Auto logout if session invalid
                if (msg.includes("phiên làm việc") || msg.includes("Admin không hợp lệ")) {
                    logout();
                }
            }
        })
        .catch(err => {
            alert('Có lỗi xảy ra: ' + err.message);
        });
}

function deleteRegistration(id) {
    if (!confirm('Bạn có chắc chắn muốn xóa đơn đăng ký này? Hành động này không thể hoàn tác.')) return;

    fetch(`/api/v1/admin/registrations/${id}`, {
        method: 'DELETE',
        headers: getHeaders()
    }).then(res => {
        if (res.ok) {
            loadData();
            loadStats();
        } else {
            alert('Không thể xóa đơn đăng ký này.');
        }
    })
        .catch(err => console.error(err));
}
