function init() {
    console.log('Initializing... SCRIPT LOADED');
    loadProvinces();
    loadUnits();

    // Event listeners
    const btnAddRelative = document.getElementById('btnAddRelative');
    console.log('btnAddRelative found:', btnAddRelative);
    if (btnAddRelative) {
        btnAddRelative.addEventListener('click', function() {
            console.log('btnAddRelative clicked!');
            addRelative();
        });
    }
    
    const registrationForm = document.getElementById('registrationForm');
    console.log('registrationForm found:', registrationForm);
    if (registrationForm) {
        registrationForm.addEventListener('submit', function(e) {
            console.log('Form submitted!');
            submitRegistration(e);
        });
    }

    const btnOpenLookup = document.getElementById('btnOpenLookup');
    console.log('btnOpenLookup found:', btnOpenLookup);
    if (btnOpenLookup) {
        btnOpenLookup.addEventListener('click', function() {
            console.log('btnOpenLookup clicked!');
            openLookupModal();
        });
    }

    const btnPerformLookup = document.getElementById('btnPerformLookup');
    console.log('btnPerformLookup found:', btnPerformLookup);
    if (btnPerformLookup) {
        btnPerformLookup.addEventListener('click', function() {
            console.log('btnPerformLookup clicked!');
            performLookup();
        });
    }
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
} else {
    init();
}

let pollingInterval;
let relativeCount = 1;

function showToast(message, type = 'danger') {
    const toastEl = document.getElementById('liveToast');
    const toastMessage = document.getElementById('toastMessage');
    
    // Set color based on type
    toastEl.classList.remove('bg-danger', 'bg-success', 'bg-warning', 'bg-info');
    if (type === 'success') toastEl.classList.add('bg-success');
    else if (type === 'warning') toastEl.classList.add('bg-warning', 'text-dark');
    else if (type === 'info') toastEl.classList.add('bg-info');
    else toastEl.classList.add('bg-danger');

    toastMessage.textContent = message;
    
    const toast = new bootstrap.Toast(toastEl, { delay: 3000 });
    toast.show();
}

function submitRegistrationForm(event) {
    console.log('submitRegistrationForm called via onclick!');
    event.preventDefault();
    
    const unitName = document.getElementById('manualUnitName').value.trim();
    const soldierName = document.getElementById('manualSoldierName').value.trim();
    const representativePhone = document.getElementById('representativePhone').value.trim();
    const province = document.getElementById('province').value.trim();

    console.log('Form data:', { unitName, soldierName, representativePhone, province });

    // Validation
    if (!unitName) { showToast("Vui lòng nhập tên đơn vị"); return; }
    if (!soldierName) { showToast("Vui lòng nhập tên quân nhân"); return; }
    if (!representativePhone) { showToast("Vui lòng nhập SĐT đại diện"); return; }
    if (!/^\d+$/.test(representativePhone)) { showToast("SĐT đại diện phải là số"); return; }
    if (!province) { showToast("Vui lòng nhập Tỉnh/Thành phố"); return; }

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
            showToast("Vui lòng điền đầy đủ thông tin người thăm");
            hasError = true;
            return;
        }
        if(!/^\d+$/.test(idNum)) {
            showToast(`CCCD của ${name} phải là số`);
            hasError = true;
            return;
        }

        relatives.push({ name: name, idNumber: idNum, relationship: rel });
    });

    if (hasError) return;

    if (relatives.length === 0) {
        showToast("Vui lòng nhập ít nhất 1 người thăm");
        return;
    }

    showLoading(true);

    const data = {
        manualUnitName: unitName,
        manualSoldierName: soldierName,
        representativePhone: representativePhone,
        province: province,
        relatives: relatives
    };

    console.log('Sending data:', data);
    
    fetch('/api/v1/public/registrations', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
    .then(response => {
        console.log('Registration response:', response);
        if(response.ok) return response.json();
        throw new Error('Có lỗi xảy ra khi gửi đăng ký');
    })
    .then(result => {
        console.log('Registration success:', result);
        showToast("Đăng ký thành công!", "success");
        showStatusView(representativePhone);
    })
    .catch(err => {
        console.error('Registration error:', err);
        showToast(err.message);
    })
    .finally(() => showLoading(false));
}

let cachedUnits = [];

function loadUnits() {
    console.log('[DEBUG] Calling Units API: /api/v1/public/units');
    fetch('/api/v1/public/units')
        .then(response => {
            if (!response.ok) throw new Error('Cannot load units');
            return response.json();
        })
        .then(data => {
            console.log('[DEBUG] Units Data:', data);
            cachedUnits = data || [];
            
            const parentSelect = document.getElementById('parentUnitSelect');
            const childSelect = document.getElementById('manualUnitName');
            
            if (!parentSelect || !childSelect) return;
            
            // 1. Populate Parent Units (Roots)
            parentSelect.innerHTML = '<option value="">-- Chọn Cấp trên --</option>';
            
            // Filter units with no parentId (Roots)
            const roots = cachedUnits.filter(u => !u.parentId);
            roots.sort((a, b) => a.name.localeCompare(b.name));
            
            roots.forEach(unit => {
                const option = document.createElement('option');
                option.value = unit.id; // Use ID for filtering children
                option.textContent = unit.name;
                parentSelect.appendChild(option);
            });
            
            // 2. Add Event Listener
            parentSelect.addEventListener('change', function() {
                const parentId = this.value;
                childSelect.innerHTML = '<option value="">-- Chọn Đơn vị --</option>';
                childSelect.disabled = true;
                
                if (parentId) {
                    // Filter children
                    const children = cachedUnits.filter(u => u.parentId == parentId);
                    
                    if (children.length > 0) {
                        children.sort((a, b) => a.name.localeCompare(b.name));
                        children.forEach(unit => {
                            const option = document.createElement('option');
                            option.value = unit.name; // Value is NAME (as backend expects manualUnitName)
                            option.textContent = unit.name;
                            childSelect.appendChild(option);
                        });
                        childSelect.disabled = false;
                    } else {
                        // Edge case: Parent has no children, maybe allow selecting parent itself?
                        // For now, just show message
                         const option = document.createElement('option');
                         option.text = "Không có đơn vị trực thuộc";
                         childSelect.add(option);
                    }
                }
            });

            console.log('[DEBUG] 2-Level Units Dropdown initialized');
        })
        .catch(err => console.error('[DEBUG] Error loading units:', err));
}

function loadSoldiers(unitId) {
    // Function removed - no longer needed since users input soldier name manually
}

function loadProvinces() {
    console.log('Loading provinces...');
    fetch('/data/provinces.json')
        .then(response => {
            console.log('Provinces fetch response:', response);
            if (!response.ok) {
                throw new Error('Cannot load provinces: ' + response.status);
            }
            return response.json();
        })
        .then(data => {
            console.log('Provinces data loaded:', data.length, 'items');
            const select = document.getElementById('province');
            console.log('Province select element found:', select);
            
            if (!select) {
                console.error('Province select element not found!');
                return;
            }
            
            // Sort by name alphabetically
            data.sort((a, b) => a.name.localeCompare(b.name));
            data.forEach(p => {
                const option = document.createElement('option');
                option.value = p.name;
                option.textContent = p.name;
                select.appendChild(option);
            });
            console.log('Provinces populated successfully');
        })
        .catch(err => {
            console.error('Error loading provinces:', err);
            // Fallback: add some common provinces manually
            const select = document.getElementById('province');
            if (select) {
                const provinces = ['Hà Nội', 'TP. Hồ Chí Minh', 'Đà Nẵng', 'Hải Phòng', 'Cần Thơ', 
                                 'Bắc Ninh', 'Bình Dương', 'Đồng Nai', 'Vĩnh Phúc', 'Hưng Yên'];
                provinces.forEach(p => {
                    const option = document.createElement('option');
                    option.value = p;
                    option.textContent = p;
                    select.appendChild(option);
                });
                console.log('Fallback provinces added');
            }
        });
}

function addRelative() {
    if (relativeCount >= 3) {
        showToast("Tối đa 3 người thăm.", "warning");
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
                         <option value="Ông">Ông</option>
                         <option value="Bà">Bà</option>
                         <option value="Cha">Cha</option>
                         <option value="Mẹ">Mẹ</option>
                         <option value="Vợ">Vợ</option>
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
    
    const unitName = document.getElementById('manualUnitName').value.trim();
    const soldierName = document.getElementById('manualSoldierName').value.trim();
    const representativePhone = document.getElementById('representativePhone').value.trim();
    const province = document.getElementById('province').value.trim();

    // Validation
    if (!unitName) { alert("Vui lòng nhập tên đơn vị"); return; }
    if (!soldierName) { alert("Vui lòng nhập tên quân nhân"); return; }
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
        manualUnitName: unitName,
        manualSoldierName: soldierName,
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
    console.log('openLookupModal called - checking bootstrap...');
    
    // Check if Bootstrap is available
    if (typeof bootstrap === 'undefined') {
        console.error('Bootstrap is not loaded!');
        alert('Bootstrap không được load. Vui lòng làm mới trang.');
        return;
    }
    
    console.log('Bootstrap available:', typeof bootstrap);
    const modalElement = document.getElementById('lookupModal');
    console.log('Modal element found:', modalElement);
    
    try {
        const modal = new bootstrap.Modal(modalElement);
        console.log('Modal created:', modal);
        modal.show();
    } catch (error) {
        console.error('Error creating modal:', error);
        alert('Không thể mở dialog tra cứu: ' + error.message);
    }
}

function performLookup() {
    const phone = document.getElementById('lookupPhone').value;
    console.log('performLookup called with phone:', phone);
    
    if(!phone) {
        alert('Vui lòng nhập số điện thoại');
        return;
    }
    
    console.log('Hiding modal...');
    const modalInstance = bootstrap.Modal.getInstance(document.getElementById('lookupModal'));
    console.log('Modal instance:', modalInstance);
    
    if (modalInstance) {
        modalInstance.hide();
    }
    
    showLoading(true);
    
    console.log('Fetching registration data...');
    fetch(`/api/v1/public/registrations/search?phone=${phone}`)
        .then(res => {
            console.log('Response status:', res.status);
            if(!res.ok) throw new Error('Không tìm thấy hồ sơ');
            return res.json();
        })
        .then(data => {
            console.log('Data received:', data);
            if (data && data.length > 0) {
                // Sort to get newest first
                data.sort((a, b) => b.id - a.id);
                showStatusView(phone);
            } else {
                throw new Error('Không tìm thấy hồ sơ');
            }
        })
        .catch(err => {
            console.error('Lookup error:', err);
            alert(err.message);
        })
        .finally(() => showLoading(false));
}

function showStatusView(phone) {
    document.getElementById('introSection').style.display = 'none';
    document.getElementById('formSection').style.display = 'none';
    document.getElementById('statusSection').style.display = 'block';
    
    pollStatus(phone);
    if (pollingInterval) clearInterval(pollingInterval);
    pollingInterval = setInterval(() => pollStatus(phone), 5000);
}

function resetToForm() {
    if (pollingInterval) clearInterval(pollingInterval);
    document.getElementById('statusSection').style.display = 'none';
    document.getElementById('introSection').style.display = 'block';
    document.getElementById('formSection').style.display = 'block';
    // Optional: Scroll to form
    document.getElementById('formSection').scrollIntoView({ behavior: 'smooth' });
}

function pollStatus(phone) {
    fetch(`/api/v1/public/registrations/search?phone=${phone}`)
    .then(res => res.json())
    .then(data => {
        if (data && data.length > 0) {
            // Sort to ensure we see the latest registration
            data.sort((a, b) => b.id - a.id);
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
    
    // Clear dynamic buttons first
    const btnContainer = document.getElementById('statusSection').querySelector('.card-body');
    const existingCancel = document.getElementById('btnCancelReg');
    if (existingCancel) existingCancel.remove();
    const existingReReg = document.getElementById('btnReRegister');
    if (existingReReg) existingReReg.remove();

    if (data.status === 'PENDING') {
        iconContainer.innerHTML = '<div class="spinner-border text-warning" style="width: 4rem; height: 4rem;" role="status"></div>';
        title.className = 'fw-bold text-warning';
        title.textContent = 'Đang chờ duyệt';
        msg.textContent = 'Hồ sơ của bạn đang chờ đơn vị kiểm tra. Vui lòng giữ liên lạc.';
        pollingInd.style.display = 'block';
        noteSection.style.display = 'none';

        // Add Cancel Button
        const btnCancel = document.createElement('button');
        btnCancel.id = 'btnCancelReg';
        btnCancel.className = 'btn btn-outline-danger w-100 mt-2';
        btnCancel.innerHTML = '<i class="fas fa-times-circle"></i> Hủy đăng ký';
        btnCancel.onclick = function () {
            cancelRegistration(data.id);
        };
        btnContainer.appendChild(btnCancel);
        
    } else {
        pollingInd.style.display = 'none';

        if (data.status === 'APPROVED') {
            iconContainer.innerHTML = '<i class="fas fa-check-circle text-success" style="font-size: 5rem;"></i>';
            title.className = 'fw-bold text-success';
            title.textContent = 'Đã được chấp thuận!';
            msg.textContent = 'Chúc mừng! Đơn vị đã đồng ý cho bạn vào thăm. Vui lòng mang theo giấy tờ tùy thân.';
            noteSection.style.display = 'none';
        } else if (data.status === 'REJECTED') {
            iconContainer.innerHTML = '<i class="fas fa-times-circle text-danger" style="font-size: 5rem;"></i>';
            title.className = 'fw-bold text-danger';
            title.textContent = 'Đã bị từ chối';
            msg.textContent = 'Rất tiếc, yêu cầu của bạn chưa được chấp thuận lần này.';

            // Always show note section for REJECTED
            noteSection.style.display = 'block';
            const noteText = data.note || 'Không có lý do cụ thể từ đơn vị.';
            document.getElementById('detailNote').textContent = noteText;
            
            // Add Re-Register Button
            const btnReReg = document.createElement('button');
            btnReReg.id = 'btnReRegister';
            btnReReg.className = 'btn btn-primary w-100 mt-3';
            btnReReg.innerHTML = '<i class="fas fa-redo"></i> Đăng ký lại / Sửa thông tin';
            btnReReg.onclick = resetToForm;
            btnContainer.appendChild(btnReReg);

        } else if (data.status === 'CANCELLED') {
            iconContainer.innerHTML = '<i class="fas fa-ban text-secondary" style="font-size: 5rem;"></i>';
            title.className = 'fw-bold text-secondary';
            title.textContent = 'Đã hủy';
            msg.textContent = 'Bạn đã hủy đơn đăng ký này.';
            noteSection.style.display = 'none';
            
             // Add Re-Register Button for Cancelled too
            const btnReReg = document.createElement('button');
            btnReReg.id = 'btnReRegister';
            btnReReg.className = 'btn btn-primary w-100 mt-3';
            btnReReg.innerHTML = '<i class="fas fa-redo"></i> Đăng ký lại';
            btnReReg.onclick = resetToForm;
            btnContainer.appendChild(btnReReg);
        }
    }
}

function cancelRegistration(id) {
    if (!confirm('Bạn có chắc muốn hủy đơn đăng ký này không?')) return;

        showLoading(true);
        fetch(`/api/v1/public/registrations/${id}/cancel`, {
            method: 'PUT'
        })
            .then(res => {
                if (res.ok) {
                    showToast('Đã hủy đơn thành công', 'info');
                    pollStatus(document.getElementById('representativePhone').value || document.getElementById('lookupPhone').value);
                } else {
                    showToast('Không thể hủy đơn. Có thể đơn đã được duyệt.');
                }
            })
            .catch(err => showToast('Lỗi kết nối'))
            .finally(() => showLoading(false));
    }

    function showLoading(isLoading) {
    document.getElementById('loadingOverlay').style.display = isLoading ? 'flex' : 'none';
}