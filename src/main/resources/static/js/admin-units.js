// Auth Check handled by admin-common.js

document.addEventListener('DOMContentLoaded', function() {
    loadUnits();
});

let unitModal;

function loadUnits() {
    fetch('/api/v1/admin/units', {
        headers: getHeaders()
    })
    .then(res => res.json())
    .then(data => {
        const tbody = document.getElementById('tableBody');
        tbody.innerHTML = '';
        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center py-4 text-muted">Chưa có đơn vị nào</td></tr>';
            return;
        }
        data.forEach(unit => {
            let actionBtns = '';
            if (isAdmin()) {
                actionBtns = `
                    <button class="btn btn-sm btn-outline-primary me-1" onclick="editUnit(${unit.id})">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="deleteUnit(${unit.id})">
                        <i class="fas fa-trash"></i>
                    </button>`;
            }

            const row = `
                <tr>
                    <td class="ps-4 text-muted">#${unit.id}</td>
                    <td class="fw-bold">${unit.name}</td>
                    <td class="d-none d-md-table-cell">${unit.parentId ? unit.parentId : '<span class="text-muted">-</span>'}</td>
                    <td class="text-end pe-4">
                        ${actionBtns}
                    </td>
                </tr>
            `;
            tbody.innerHTML += row;
        });
    })
    .catch(err => console.error(err));
}

function openModal() {
    document.getElementById('unitId').value = '';
    document.getElementById('unitName').value = '';
    document.getElementById('parentId').value = '';
    document.getElementById('modalTitle').textContent = 'Thêm Đơn Vị';
    
    unitModal = new bootstrap.Modal(document.getElementById('unitModal'));
    unitModal.show();
}

function editUnit(id) {
    fetch(`/api/v1/admin/units/${id}`, {
        headers: getHeaders()
    })
    .then(res => res.json())
    .then(unit => {
        document.getElementById('unitId').value = unit.id;
        document.getElementById('unitName').value = unit.name;
        document.getElementById('parentId').value = unit.parentId || '';
        document.getElementById('modalTitle').textContent = 'Sửa Đơn Vị';
        
        unitModal = new bootstrap.Modal(document.getElementById('unitModal'));
        unitModal.show();
    });
}

function saveUnit() {
    const id = document.getElementById('unitId').value;
    const name = document.getElementById('unitName').value;
    const parentId = document.getElementById('parentId').value;

    const payload = { name, parentId: parentId ? parseInt(parentId) : null };
    const method = id ? 'PUT' : 'POST';
    const url = id ? `/api/v1/admin/units/${id}` : '/api/v1/admin/units';

    fetch(url, {
        method: method,
        headers: getHeaders(),
        body: JSON.stringify(payload)
    })
    .then(res => {
        if (res.ok) {
            unitModal.hide();
            loadUnits();
        } else {
            alert('Có lỗi xảy ra');
        }
    });
}

function deleteUnit(id) {
    if (!confirm('Bạn có chắc chắn muốn xóa đơn vị này?')) return;
    
    fetch(`/api/v1/admin/units/${id}`, {
        method: 'DELETE',
        headers: getHeaders()
    })
    .then(res => {
        if (res.ok) {
            loadUnits();
        } else {
            alert('Không thể xóa (có thể đang có dữ liệu liên quan)');
        }
    });
}
