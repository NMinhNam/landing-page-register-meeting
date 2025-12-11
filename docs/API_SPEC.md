# Tài Liệu Thiết Kế API RESTful
**Dự án:** Đăng Ký Thăm Quân Nhân
**Phiên bản:** 1.0

Tài liệu này mô tả danh sách các API cần thiết cho hệ thống, được thiết kế theo chuẩn RESTful, ưu tiên sự đơn giản và gọn nhẹ.

---

## 1. Quy ước chung
- **Base URL:** `/api/v1`
- **Format:** JSON
- **Date Format:** ISO 8601 (YYYY-MM-DDTHH:mm:ss)
- **HTTP Status:**
    - `200 OK`: Thành công.
    - `201 Created`: Tạo mới thành công.
    - `400 Bad Request`: Lỗi dữ liệu đầu vào.
    - `401 Unauthorized`: Chưa đăng nhập (cho Admin).
    - `404 Not Found`: Không tìm thấy tài nguyên.
    - `500 Internal Server Error`: Lỗi hệ thống.

---

## 2. Public API (Dành cho Người thân - Không cần đăng nhập)

Nhóm API phục vụ việc hiển thị form và gửi yêu cầu đăng ký.

### 2.1. Lấy danh sách Đơn vị
*   **Mô tả:** Lấy danh sách các đơn vị để hiển thị dropdown chọn.
*   **Method:** `GET`
*   **Endpoint:** `/public/units`
*   **Response:**
    ```json
    [
      { "id": 1, "name": "Tiểu đoàn 1", "parentId": null },
      { "id": 2, "name": "Đại đội 1", "parentId": 1 }
    ]
    ```

### 2.2. Tìm kiếm Quân nhân
*   **Mô tả:** Tìm quân nhân theo tên hoặc mã, lọc theo đơn vị.
*   **Method:** `GET`
*   **Endpoint:** `/public/soldiers`
*   **Query Params:**
    - `unitId` (optional): ID đơn vị.
    - `keyword` (optional): Tên hoặc mã quân nhân.
*   **Response:**
    ```json
    [
      { "id": 101, "name": "Nguyễn Văn A", "code": "QN001", "unitName": "Đại đội 1" }
    ]
    ```

### 2.3. Đăng ký thăm
*   **Mô tả:** Gửi thông tin đăng ký thăm gặp.
*   **Method:** `POST`
*   **Endpoint:** `/public/registrations`
*   **Body:**
    ```json
    {
      "soldierId": 101,
      "relativeName": "Trần Thị B",
      "relativePhone": "0987654321",
      "relationship": "MẸ",
      "province": "Hà Nội",
      "visitWeek": 1
    }
    ```
*   **Response (201 Created):**
    ```json
    {
      "registrationCode": "REG-12345",
      "message": "Đăng ký thành công, vui lòng chờ duyệt."
    }
    ```

### 2.4. Tra cứu trạng thái đăng ký
*   **Mô tả:** Kiểm tra trạng thái đơn bằng mã đăng ký hoặc số điện thoại.
*   **Method:** `GET`
*   **Endpoint:** `/public/registrations/search`
*   **Query Params:** `phone` hoặc `code`
*   **Response:**
    ```json
    {
      "soldierName": "Nguyễn Văn A",
      "visitWeek": 1,
      "status": "PENDING", // PENDING, APPROVED, REJECTED
      "note": "Chờ chỉ huy duyệt"
    }
    ```

---

## 3. Admin API (Dành cho Cán bộ - Cần Authentication)

### 3.1. Đăng nhập
*   **Mô tả:** Xác thực cán bộ quản lý.
*   **Method:** `POST`
*   **Endpoint:** `/auth/login`
*   **Body:** `{ "username": "admin", "password": "***" }`
*   **Response:** `{ "token": "eyJhbGci..." }`

### 3.2. Lấy danh sách đăng ký (Quản lý)
*   **Mô tả:** Xem danh sách các lượt đăng ký, hỗ trợ bộ lọc.
*   **Method:** `GET`
*   **Endpoint:** `/admin/registrations`
*   **Query Params:**
    - `unitId`, `week`, `province`, `status`
    - `page`, `size` (phân trang)
*   **Response:**
    ```json
    {
      "data": [
        {
          "id": 50,
          "soldierName": "Nguyễn Văn A",
          "relativeName": "Trần Thị B",
          "status": "PENDING",
          "visitWeek": 1,
          "createdAt": "2025-12-02T10:00:00"
        }
      ],
      "total": 1
    }
    ```

### 3.3. Duyệt / Từ chối đăng ký
*   **Mô tả:** Cập nhật trạng thái cho một lượt đăng ký.
*   **Method:** `PUT`
*   **Endpoint:** `/admin/registrations/{id}/status`
*   **Body:**
    ```json
    {
      "status": "APPROVED", // hoặc REJECTED
      "note": "Đồng ý cho thăm"
    }
    ```

### 3.4. Xem thống kê báo cáo
*   **Mô tả:** Lấy số liệu tổng hợp để hiển thị bảng thống kê.
*   **Method:** `GET`
*   **Endpoint:** `/admin/stats`
*   **Query Params:** `week` (optional)
*   **Response:**
    ```json
    {
      "byProvince": [
        { "province": "Hà Nội", "count": 15 },
        { "province": "Nam Định", "count": 8 }
      ],
      "byStatus": {
        "pending": 5,
        "approved": 20,
        "rejected": 2
      }
    }
    ```
