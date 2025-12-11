// Auth Check
const token = localStorage.getItem('adminToken');
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
        const modalEl = document.getElementById('approvalModal');
        const isModalOpen = modalEl && modalEl.classList.contains('show');
        
        if (!isModalOpen) {
            loadData(true); // true = silent mode (optional, currently logic same)
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
    
    let url = `/api/v1/admin/registrations?page=1&size=50`;
    if(week) url += `&week=${week}`;
    if(status) url += `&status=${status}`;
    if(province) url += `&province=${encodeURIComponent(province)}`;
    if(keyword) url += `&keyword=${encodeURIComponent(keyword)}`;

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
                    <button class="btn btn-sm btn-outline-primary" onclick="openModal(${item.id})">
                        <i class="fas fa-edit"></i> Xử lý
                    </button>`;
            }

            const row = `
                <tr>
                    <td class="ps-4">
                        <div class="fw-bold text-dark">${item.soldier_name}</div>
                    </td>
                    <td class="d-none d-md-table-cell"><span class="badge bg-light text-dark border">${item.unit_name || 'N/A'}</span></td>
                    <td>
                        <div class="fw-bold text-primary" style="max-width: 200px; white-space: normal;">${item.relative_name}</div>
                        <div class="small text-muted"><i class="fas fa-phone-alt me-1"></i> ${item.relative_phone}</div>
                    </td>
                    <td class="d-none d-md-table-cell">Tuần ${item.visit_week}</td>
                    <td class="d-none d-md-table-cell">${item.province || '-'}</td>
                    <td>${getStatusBadge(item.status)}</td>
                    <td class="d-none d-md-table-cell"><span class="text-muted small fst-italic">${item.note || '-'}</span></td>
                    <td class="text-end pe-4">
                        ${actionBtn}
                        <button class="btn btn-sm btn-outline-danger ms-1" onclick="deleteRegistration(${item.id})">
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

function loadStats() {
    fetch('/api/v1/admin/stats', {
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

function getStatusBadge(status) {
    if(status === 'PENDING') return '<span class="status-badge bg-pending">Chờ duyệt</span>';
    if(status === 'APPROVED') return '<span class="status-badge bg-approved">Đồng ý</span>';
    if(status === 'REJECTED') return '<span class="status-badge bg-rejected">Từ chối</span>';
    return status;
}

// Modal Logic
let approvalModal;
function openModal(id) {
    document.getElementById('currentRegId').value = id;
    document.getElementById('approvalNote').value = '';
    approvalModal = new bootstrap.Modal(document.getElementById('approvalModal'));
    approvalModal.show();
}

function submitStatus(status) {
    const id = document.getElementById('currentRegId').value;
    const note = document.getElementById('approvalNote').value;

    fetch(`/api/v1/admin/registrations/${id}/status`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ status, note })
    })
        .then(res => {
            if(res.ok) {
                approvalModal.hide();
                loadData();
                loadStats();
            } else {
                alert('Có lỗi xảy ra');
            }
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
    