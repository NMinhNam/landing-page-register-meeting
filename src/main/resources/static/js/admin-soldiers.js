// Auth Check
const token = localStorage.getItem('adminToken');
if (!token) {
    window.location.href = '/admin/login';
}

document.addEventListener('DOMContentLoaded', function() {
    loadUnitsForSelect();
    loadSoldiers();
});

let soldierModal;
let unitsCache = [];

function loadUnitsForSelect() {
    fetch('/api/v1/admin/units', {
        headers: { 'Authorization': `Bearer ${token}` }
    })
    .then(res => res.json())
    .then(data => {
        unitsCache = data;
        const filterSelect = document.getElementById('filterUnit');
        const modalSelect = document.getElementById('soldierUnit');
        
        data.forEach(unit => {
            // Populate Filter
            const opt1 = document.createElement('option');
            opt1.value = unit.id;
            opt1.textContent = unit.name;
            filterSelect.appendChild(opt1);

            // Populate Modal
            const opt2 = document.createElement('option');
            opt2.value = unit.id;
            opt2.textContent = unit.name;
            modalSelect.appendChild(opt2);
        });
    });
}

function loadSoldiers() {
    const unitId = document.getElementById('filterUnit').value;
    const keyword = document.getElementById('searchKeyword').value;
    
    let url = `/api/v1/admin/soldiers?`;
    if(unitId) url += `&unitId=${unitId}`;
    if(keyword) url += `&keyword=${keyword}`;

    fetch(url, {
        headers: { 'Authorization': `Bearer ${token}` }
    })
    .then(res => res.json())
    .then(data => {
        const tbody = document.getElementById('tableBody');
        tbody.innerHTML = '';
        
        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center py-4 text-muted">Không tìm thấy quân nhân nào</td></tr>';
            return;
        }

        data.forEach(s => {
            const unitName = s.unitName || (unitsCache.find(u => u.id === s.unitId)?.name) || 'N/A';
            const statusBadge = s.status === 'ACTIVE' 
                ? '<span class="badge bg-success">Đang công tác</span>' 
                : '<span class="badge bg-secondary">Đã ra quân</span>';

            const row = `
                <tr>
                    <td class="ps-4 font-monospace text-muted">${s.code}</td>
                    <td class="fw-bold">${s.name}</td>
                    <td class="d-none d-md-table-cell">${unitName}</td>
                    <td>${statusBadge}</td>
                    <td class="text-end pe-4">
                        <button class="btn btn-sm btn-outline-primary me-1" onclick="editSoldier(${s.id})">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn btn-sm btn-outline-danger" onclick="deleteSoldier(${s.id})">
                            <i class="fas fa-trash"></i>
                        </button>
                    </td>
                </tr>
            `;
            tbody.innerHTML += row;
        });
    });
}

function openModal() {
    document.getElementById('soldierId').value = '';
    document.getElementById('soldierCode').value = '';
    document.getElementById('soldierName').value = '';
    document.getElementById('soldierUnit').value = '';
    document.getElementById('soldierStatus').value = 'ACTIVE';
    document.getElementById('modalTitle').textContent = 'Thêm Quân Nhân';
    
    soldierModal = new bootstrap.Modal(document.getElementById('soldierModal'));
    soldierModal.show();
}

function editSoldier(id) {
    fetch(`/api/v1/admin/soldiers/${id}`, {
        headers: { 'Authorization': `Bearer ${token}` }
    })
    .then(res => res.json())
    .then(s => {
        document.getElementById('soldierId').value = s.id;
        document.getElementById('soldierCode').value = s.code;
        document.getElementById('soldierName').value = s.name;
        document.getElementById('soldierUnit').value = s.unitId;
        document.getElementById('soldierStatus').value = s.status;
        document.getElementById('modalTitle').textContent = 'Sửa Quân Nhân';
        
        soldierModal = new bootstrap.Modal(document.getElementById('soldierModal'));
        soldierModal.show();
    });
}

function saveSoldier() {
    const id = document.getElementById('soldierId').value;
    const code = document.getElementById('soldierCode').value;
    const name = document.getElementById('soldierName').value;
    const unitId = document.getElementById('soldierUnit').value;
    const status = document.getElementById('soldierStatus').value;

    const payload = { 
        code, 
        name, 
        unitId: unitId ? parseInt(unitId) : null, 
        status 
    };
    const method = id ? 'PUT' : 'POST';
    const url = id ? `/api/v1/admin/soldiers/${id}` : '/api/v1/admin/soldiers';

    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(payload)
    })
    .then(res => {
        if (res.ok) {
            soldierModal.hide();
            loadSoldiers();
        } else {
            alert('Có lỗi xảy ra');
        }
    });
}

function deleteSoldier(id) {
    if (!confirm('Bạn có chắc chắn muốn xóa quân nhân này?')) return;
    
    fetch(`/api/v1/admin/soldiers/${id}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${token}` }
    })
    .then(res => {
        if (res.ok) {
            loadSoldiers();
        } else {
            alert('Không thể xóa (có thể đang có dữ liệu liên quan)');
        }
    });
}
