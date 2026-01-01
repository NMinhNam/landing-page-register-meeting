// Auth Check
const token = localStorage.getItem('adminToken');
const storedAdminId = localStorage.getItem('adminId');

// Force re-login if token exists but adminId is missing (Old session)
if (token && !storedAdminId) {
    alert("Phiên làm việc cũ đã hết hạn cấu trúc. Vui lòng đăng nhập lại.");
    logout();
}

if (!token) {
    window.location.href = '/admin/login';
}

// Setup on load
document.addEventListener('DOMContentLoaded', function() {
    loadData();
    loadStats();
    
    // Auto refresh every 10 seconds
    setInterval(() => {
        // Only refresh if Modal is NOT open
        const approvalModalEl = document.getElementById('approvalModal');
        const detailModalEl = document.getElementById('detailModal');
        const isModalOpen = (approvalModalEl && approvalModalEl.classList.contains('show')) || 
                            (detailModalEl && detailModalEl.classList.contains('show'));
        
        if (!isModalOpen) {
            loadData(true); // true = silent mode
            loadStats();
        }
    }, 10000);
});

function logout() {
    localStorage.removeItem('adminToken');
    localStorage.removeItem('adminRole');
    localStorage.removeItem('adminUser');
    window.location.href = '/admin/login';
}

let debounceTimer;
function debounceLoadData() {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => {
        loadData();
    }, 500);
}

function loadData(isSilent = false) {
    const week = document.getElementById('filterWeek').value;
    const status = document.getElementById('filterStatus').value;
    const province = document.getElementById('filterProvince').value;
    const keyword = document.getElementById('searchKeyword').value;
    const role = localStorage.getItem('adminRole');
    const adminId = localStorage.getItem('adminId');
    
    // Also reload stats when data reloads (filtered by week)
    if(!isSilent) loadStats(week);

    let url = `/api/v1/admin/registrations?page=1&size=50`;
    if(week) url += `&week=${week}`;
    if(status) url += `&status=${status}`;
    if(province) url += `&province=${encodeURIComponent(province)}`;
    if(keyword) url += `&keyword=${encodeURIComponent(keyword)}`;
    if(adminId) url += `&adminId=${adminId}`;

    // Show loading only if not auto-refreshing
    const tbody = document.getElementById('tableBody');
    if (!isSilent && tbody.innerHTML.trim() === '') {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center py-4 text-muted"><div class="spinner-border text-primary" role="status"></div></td></tr>';
    }

    fetch(url, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(res => res.json())
    .then(data => {
        tbody.innerHTML = '';
        
        if(data.data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-center py-4 text-muted">Không có dữ liệu</td></tr>';
            return;
        }

        data.data.forEach(item => {
            let actionBtn = '';
            if (role !== 'VIEWER') {
                actionBtn = `
                    <button class="btn btn-sm btn-outline-primary" onclick="event.stopPropagation(); openModal(${item.id})" title="Xử lý">
                        <i class="fas fa-edit"></i>
                    </button>`;
            }

            const row = `
                <tr onclick="showDetail(${item.id})" style="cursor: pointer;">
                    <!-- Mobile: Combined Info -->
                    <td class="ps-4 d-md-none">
                        <div class="fw-bold text-dark">${item.soldier_name}</div>
                        <div class="small text-muted text-truncate" style="max-width: 200px;">
                            <i class="fas fa-user-friends me-1"></i>${item.relative_name} (${item.relative_name ? item.relative_name.split(',').length : 0})
                        </div>
                        <div class="small text-muted mt-1">
                            <i class="far fa-clock me-1"></i>${formatDate(item.created_at)}
                        </div>
                    </td>

                    <!-- Desktop: Detailed Columns -->
                    <td class="ps-4 text-muted small d-none d-md-table-cell">
                        ${formatDate(item.created_at)}
                    </td>
                    <td class="d-none d-md-table-cell">
                        <div class="fw-bold text-dark">${item.soldier_name}</div>
                    </td>
                    <td class="d-none d-md-table-cell"><span class="badge bg-light text-dark border text-wrap" style="max-width: 150px;">${item.unit_name || 'N/A'}</span></td>
                    <td class="d-none d-md-table-cell">
                        <div class="fw-bold text-primary text-truncate" style="max-width: 150px;" title="${item.relative_name}">
                            ${item.relative_name}
                        </div>
                        <span class="badge bg-secondary rounded-pill" title="Số lượng người">${item.relative_name ? item.relative_name.split(',').length : 0} người</span>
                    </td>
                    <td class="d-none d-md-table-cell">
                        <a href="tel:${item.relative_phone}" class="text-decoration-none fw-bold text-dark" onclick="event.stopPropagation()"><i class="fas fa-phone-alt me-1 text-muted"></i>${item.relative_phone}</a>
                    </td>
                    <td class="d-none d-md-table-cell">
                        <div>Tuần <span class="fw-bold">${item.visit_week}</span></div>
                        <small class="text-muted">${item.province || '-'}</small>
                    </td>
                    
                    <!-- Shared: Status -->
                    <td>
                        ${getStatusBadge(item.status)}
                        ${item.note ? `<div class="small text-muted fst-italic mt-1 text-truncate" style="max-width: 150px;" title="${item.note}">${item.note}</div>` : ''}
                    </td>

                    <!-- Shared: Actions -->
                    <td class="text-end pe-4 text-nowrap">
                        ${actionBtn}
                        <button class="btn btn-sm btn-outline-danger" onclick="event.stopPropagation(); deleteRegistration(${item.id})" title="Xóa">
                            <i class="fas fa-trash"></i>
                        </button>
                    </td>
                </tr>
            `;
            tbody.innerHTML += row;
        });
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

function loadStats(week) {
    let url = '/api/v1/admin/stats';
    if(week) url += `?week=${week}`;

    fetch(url, {
        headers: { 'Authorization': `Bearer ${token}` }
    })
    .then(res => res.json())
    .then(data => {
        // 1. Status Counts
        if(data.byStatus) {
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
                    <div class="col-md-3 mb-2">
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
    const week = document.getElementById('filterWeek').value;
    const status = document.getElementById('filterStatus').value;
    const province = document.getElementById('filterProvince').value;
    const adminId = localStorage.getItem('adminId');

    console.log('[Export] Starting export...', { week, status, province, adminId });

    let url = `/api/v1/admin/export/registrations?v=1`;
    if(adminId && adminId !== 'null') url += `&adminId=${adminId}`;
    if(week) url += `&week=${week}`;
    if(status) url += `&status=${status}`;
    if(province) url += `&province=${encodeURIComponent(province)}`;
    
    // Show a small toast or indicator
    const btnExport = document.getElementById('btnExport');
    let originalText = '';
    
    if (btnExport) {
        originalText = btnExport.innerHTML;
        btnExport.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Đang tải...';
        btnExport.disabled = true;
    }

    fetch(url, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
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
        // Force BOM for Excel
        const newBlob = new Blob([blob], { type: 'text/csv;charset=utf-8;' });
        
        const url = window.URL.createObjectURL(newBlob);
        const a = document.createElement('a');
        a.style.display = 'none';
        a.href = url;
        a.download = `ds_dang_ky_tuan_${week || 'all'}_${new Date().getTime()}.csv`;
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
    if(status === 'PENDING') return '<span class="status-badge bg-pending">Chờ duyệt</span>';
    if(status === 'APPROVED') return '<span class="status-badge bg-approved">Đồng ý</span>';
    if(status === 'REJECTED') return '<span class="status-badge bg-rejected">Từ chối</span>';
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
        headers: { 'Authorization': `Bearer ${token}` }
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
                        <div class="col-6">
                            <small class="text-muted d-block">Tuần</small>
                            <span class="badge bg-info">Tuần ${data.visitWeek}</span>
                        </div>
                        <div class="col-6">
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
                        <p class="mb-0 small fw-medium text-dark">${data.note || 'Không có ghi chú từ quản lý'}</p>
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
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ status, note, adminId })
    })
        .then(async res => {
            if(res.ok) {
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
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })
        .then(res => {
            if (res.ok) {
                loadData();
                loadStats();
            } else {
                alert('Không thể xóa đơn đăng ký này.');
            }
        })
        .catch(err => console.error(err));
    }
    