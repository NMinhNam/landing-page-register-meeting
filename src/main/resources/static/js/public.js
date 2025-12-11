document.addEventListener('DOMContentLoaded', function() {
    loadProvinces();

    // Event listeners
    document.getElementById('btnAddRelative').addEventListener('click', addRelative);
    document.getElementById('registrationForm').addEventListener('submit', submitRegistration);
});

let pollingInterval;
let relativeCount = 1;

function loadProvinces() {
    fetch('/data/provinces.json')
        .then(response => response.json())
        .then(data => {
            const select = document.getElementById('province');
            // Sort by name alphabetically
            data.sort((a, b) => a.name.localeCompare(b.name));
            data.forEach(p => {
                const option = document.createElement('option');
                option.value = p.name;
                option.textContent = p.name;
                select.appendChild(option);
            });
        })
        .catch(err => console.error('Error loading provinces:', err));
}

function addRelative() {
    if (relativeCount >= 3) {
        alert("Tối đa 3 người thăm.");
        return;
    }
    relativeCount++;
    const container = document.getElementById('relativesContainer');
    const div = document.createElement('div');
    div.className = 'relative-item card mb-2 bg-light border';
    div.innerHTML = `
        <div class="card-body p-2">
            <div class="d-flex justify-content-between mb-1">
                <span class="fw-bold text-secondary small">Người thứ ${relativeCount}</span>
                <button type="button" class="btn-close btn-sm" onclick="removeRelative(this)"></button>
            </div>
            <div class="row g-2">
                <div class="col-12">
                    <input type="text" class="form-control mb-2" name="relativeName" placeholder="Họ và tên" required>
                </div>
                <div class="col-6">
                    <input type="text" class="form-control" name="idNumber" placeholder="Số CCCD" required pattern="[0-9]+">
                </div>
                <div class="col-6">
                    <select class="form-select" name="relationship" required>
                         <option value="" disabled selected>Quan hệ</option>
                         <option value="Cha/Mẹ">Cha/Mẹ</option>
                         <option value="Vợ/Chồng">Vợ/Chồng</option>
                         <option value="Anh/Chị/Em">Anh/Chị/Em</option>
                         <option value="Con">Con</option>
                         <option value="Khác">Khác</option>
                    </select>
                </div>
            </div>
        </div>
    `;
    container.appendChild(div);
}

function removeRelative(btn) {
    btn.closest('.relative-item').remove();
    relativeCount--;
}

function submitRegistration(e) {
    e.preventDefault();
    
    const manualUnitName = document.getElementById('manualUnitName').value.trim(); // New manual unit name input
    const manualSoldier = document.getElementById('manualSoldierName').value.trim();
    const representativePhone = document.getElementById('representativePhone').value.trim();
    const province = document.getElementById('province').value.trim();

    // Validation
    if (!manualUnitName) { alert("Vui lòng nhập tên đơn vị"); return; }
    if (!manualSoldier) { alert("Vui lòng nhập tên quân nhân"); return; }
    if (!representativePhone) { alert("Vui lòng nhập SĐT đại diện"); return; }
    if (!/^\d+$/.test(representativePhone)) { alert("SĐT đại diện phải là số"); return; }
    if (!province) { alert("Vui lòng nhập Tỉnh/Thành phố"); return; }

    // Gather relatives
    const relativeItems = document.querySelectorAll('.relative-item');
    const relatives = [];
    let hasError = false;

    relativeItems.forEach(item => {
        if(hasError) return;
        const name = item.querySelector('[name="relativeName"]').value.trim();
        const idNum = item.querySelector('[name="idNumber"]').value.trim();
        const rel = item.querySelector('[name="relationship"]').value;

        if(!name || !idNum || !rel) {
            alert("Vui lòng điền đầy đủ thông tin người thăm");
            hasError = true;
            return;
        }
        if(!/^\d+$/.test(idNum)) {
            alert(`CCCD của ${name} phải là số`);
            hasError = true;
            return;
        }

        relatives.push({ name: name, idNumber: idNum, relationship: rel });
    });

    if (hasError) return;

    if (relatives.length === 0) {
        alert("Vui lòng nhập ít nhất 1 người thăm");
        return;
    }

    showLoading(true);

    const data = {
        manualUnitName: manualUnitName, // New manual unit name input
        manualSoldierName: manualSoldier,
        representativePhone: representativePhone,
        province: province,
        relatives: relatives
    };
    
    fetch('/api/v1/public/registrations', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
    .then(response => {
        if(response.ok) return response.json();
        throw new Error('Có lỗi xảy ra');
    })
    .then(result => {
        showStatusView(representativePhone);
    })
    .catch(err => {
        alert(err.message);
    })
    .finally(() => showLoading(false));
}

// --- Status & Lookup Logic ---

function openLookupModal() {
    const modal = new bootstrap.Modal(document.getElementById('lookupModal'));
    modal.show();
}

function performLookup() {
    const phone = document.getElementById('lookupPhone').value;
    if(!phone) {
        alert('Vui lòng nhập số điện thoại');
        return;
    }
    
    bootstrap.Modal.getInstance(document.getElementById('lookupModal')).hide();
    showLoading(true);
    
    fetch(`/api/v1/public/registrations/search?phone=${phone}`)
        .then(res => {
            if(!res.ok) throw new Error('Không tìm thấy hồ sơ');
            return res.json();
        })
        .then(data => {
            if (data && data.length > 0) {
                showStatusView(phone);
            } else {
                throw new Error('Không tìm thấy hồ sơ');
            }
        })
        .catch(err => {
            alert(err.message);
        })
        .finally(() => showLoading(false));
}

function showStatusView(phone) {
    document.getElementById('introSection').style.display = 'none';
    document.getElementById('formSection').style.display = 'none';
    document.getElementById('statusSection').style.display = 'block';
    
    pollStatus(phone);
    pollingInterval = setInterval(() => pollStatus(phone), 5000);
}

function pollStatus(phone) {
    fetch(`/api/v1/public/registrations/search?phone=${phone}`)
    .then(res => res.json())
    .then(data => {
        if (data && data.length > 0) {
            updateStatusUI(data[0]);
        }
    })
    .catch(err => console.error("Polling error", err));
}

function updateStatusUI(data) {
    const iconContainer = document.getElementById('statusIconContainer');
    const title = document.getElementById('statusTitle');
    const msg = document.getElementById('statusMessage');
    const pollingInd = document.getElementById('pollingIndicator');
    const noteSection = document.getElementById('adminNoteSection');
    
    document.getElementById('detailCode').textContent = `REG-${data.id}`;
    document.getElementById('detailRelative').textContent = data.relative_name || data.relativeName || '...'; 
    document.getElementById('detailSoldier').textContent = data.soldierName || data.manual_soldier_name;
    document.getElementById('detailUnit').textContent = data.unitName || data.manual_unit_name || 'N/A';

    if (data.status === 'PENDING') {
        iconContainer.innerHTML = '<div class="spinner-border text-warning" style="width: 4rem; height: 4rem;" role="status"></div>';
        title.className = 'fw-bold text-warning';
        title.textContent = 'Đang chờ duyệt';
        msg.textContent = 'Hồ sơ của bạn đang chờ đơn vị kiểm tra. Vui lòng giữ liên lạc.';
        pollingInd.style.display = 'block';
        noteSection.style.display = 'none';

        // Add Cancel Button
        if (!document.getElementById('btnCancelReg')) {
            const btnCancel = document.createElement('button');
            btnCancel.id = 'btnCancelReg';
            btnCancel.className = 'btn btn-outline-danger w-100 mt-2';
            btnCancel.innerHTML = '<i class="fas fa-times-circle"></i> Hủy đăng ký';
            btnCancel.onclick = function() { cancelRegistration(data.id); };
            document.getElementById('statusSection').querySelector('.card-body').appendChild(btnCancel);
        }
    } 
    else {
        // Remove Cancel Button if not pending
        const existingBtn = document.getElementById('btnCancelReg');
        if(existingBtn) existingBtn.remove();
        
        if (data.status === 'APPROVED') {
        iconContainer.innerHTML = '<i class="fas fa-check-circle text-success" style="font-size: 5rem;"></i>';
        title.className = 'fw-bold text-success';
        title.textContent = 'Đã được chấp thuận!';
        msg.textContent = 'Chúc mừng! Đơn vị đã đồng ý cho bạn vào thăm. Vui lòng mang theo giấy tờ tùy thân.';
        pollingInd.style.display = 'none';
        noteSection.style.display = 'none';
    } 
    else if (data.status === 'REJECTED') {
        iconContainer.innerHTML = '<i class="fas fa-times-circle text-danger" style="font-size: 5rem;"></i>';
        title.className = 'fw-bold text-danger';
        title.textContent = 'Đã bị từ chối';
        msg.textContent = 'Rất tiếc, yêu cầu của bạn chưa được chấp thuận lần này.';
        pollingInd.style.display = 'none';
        
        if(data.note) {
            noteSection.style.display = 'block';
            document.getElementById('detailNote').textContent = data.note;
        }
    }
    else if (data.status === 'CANCELLED') {
        iconContainer.innerHTML = '<i class="fas fa-ban text-secondary" style="font-size: 5rem;"></i>';
        title.className = 'fw-bold text-secondary';
        title.textContent = 'Đã hủy';
        msg.textContent = 'Bạn đã hủy đơn đăng ký này.';
        pollingInd.style.display = 'none';
        noteSection.style.display = 'none';
    }
}

function cancelRegistration(id) {
    if(!confirm('Bạn có chắc muốn hủy đơn đăng ký này không?')) return;
    
    showLoading(true);
    fetch(`/api/v1/public/registrations/${id}/cancel`, {
        method: 'PUT'
    })
    .then(res => {
        if(res.ok) {
            pollStatus(document.getElementById('representativePhone').value || document.getElementById('lookupPhone').value);
        } else {
            alert('Không thể hủy đơn. Có thể đơn đã được duyệt.');
        }
    })
    .catch(err => alert('Lỗi kết nối'))
    .finally(() => showLoading(false));
}

function showLoading(isLoading) {
    document.getElementById('loadingOverlay').style.display = isLoading ? 'flex' : 'none';
}