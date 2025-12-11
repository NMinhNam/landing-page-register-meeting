# Bản Ghi Phát Hành - Phiên bản 1.0.0

**Ngày phát hành:** 02/12/2025
**Dự án:** Hệ thống Đăng ký thăm Quân nhân (`dang_ky_tham_quan_nhan`)

Đây là bản phát hành đầu tiên (phiên bản 1.0.0) của hệ thống Đăng ký thăm Quân nhân, bao gồm việc phân tích yêu cầu, thiết kế kiến trúc, triển khai các API backend, xây dựng giao diện người dùng cơ bản và cung cấp các công cụ hỗ trợ kiểm thử.

---

## 1. Tổng quan Dự án & Tài liệu

*   **Phân tích Dự án**: Tiến hành phân tích cấu trúc dự án hiện có, tệp `pom.xml`, `HELP.md` và `application.properties`.
*   **Tạo `GEMINI.md`**: Tổng hợp thông tin dự án, giới thiệu công nghệ sử dụng, hướng dẫn xây dựng và cấu hình cơ bản.
*   **Phân tích Yêu cầu**: Nghiên cứu kỹ lưỡng tệp `USE_CASE.md` (mô tả yêu cầu người dùng) và `init.sql` (thiết kế cơ sở dữ liệu).
*   **Tạo `API_SPEC.md`**: Xây dựng tài liệu đặc tả API RESTful chi tiết cho cả hai phân hệ (Public và Admin), bao gồm các endpoint, phương thức HTTP, request/response body.

---

## 2. Triển khai Backend (Spring Boot & MyBatis Plus)

*   **Cấu hình (`application.yaml`)**:
    *   Chuyển đổi và cấu hình ứng dụng từ `application.properties` sang `application.yaml`.
    *   Thiết lập các thuộc tính cho máy chủ (server), nguồn dữ liệu (MariaDB), Thymeleaf và MyBatis Plus.
*   **Kiến trúc**: Áp dụng kiến trúc dựa trên tính năng (Feature-Driven Architecture), tổ chức mã nguồn theo các module chức năng chính (e.g., `unit`, `soldier`, `visit`, `auth`).
*   **Thành phần dùng chung**: Tạo lớp `PageRequest.java` trong `common/models` để phục vụ các yêu cầu phân trang.
*   **Triển khai API Public (Dành cho Người thân)**:
    *   **Module Đơn vị (`unit`)**: Entity (`Unit`), Mapper (`UnitMapper`), Service (`UnitService`), Controller (`PublicUnitController`) hỗ trợ lấy danh sách đơn vị (`GET /public/units`).
    *   **Module Quân nhân (`soldier`)**: Entity (`Soldier`), Mapper (`SoldierMapper` với truy vấn tìm kiếm động), Service (`SoldierService`), Controller (`PublicSoldierController`) hỗ trợ tìm kiếm quân nhân (`GET /public/soldiers`).
    *   **Module Đăng ký thăm (`visit` - Public)**: Entity (`Relative`, `VisitRegistration`), DTOs (`RegistrationRequest`), Mappers (`RelativeMapper`, `VisitRegistrationMapper`), Service (`VisitService` quản lý logic tạo/cập nhật người thân và đăng ký thăm), Controller (`PublicVisitController`) hỗ trợ gửi đăng ký (`POST /public/registrations`) và tra cứu trạng thái (`GET /public/registrations/search`).
*   **Triển khai API Admin (Dành cho Cán bộ)**:
    *   **Module Xác thực (`auth`)**: Entity (`AdminUser`), Mapper (`AdminUserMapper`), DTOs (`LoginRequest`), Service (`AuthService` với logic đăng nhập cơ bản), Controller (`AuthController`) hỗ trợ đăng nhập (`POST /auth/login`).
    *   **Module Đăng ký thăm (`visit` - Admin)**: Nâng cấp `VisitRegistrationMapper` (bổ sung truy vấn tìm kiếm, thống kê cho Admin), cập nhật `VisitService` (bổ sung `updateStatus`, `getStats`), tạo Controller (`AdminVisitController`) hỗ trợ:
        *   Lấy danh sách đăng ký (`GET /admin/registrations`).
        *   Cập nhật trạng thái đăng ký (`PUT /admin/registrations/{id}/status`).
        *   Xem thống kê báo cáo (`GET /admin/stats`).

---

## 3. Công cụ hỗ trợ kiểm thử

*   **Dữ liệu mẫu (`scritp_db/mock_data.sql`)**: Tạo script SQL chứa dữ liệu mẫu để khởi tạo các bảng `unit`, `soldier`, `admin_user`, `relative`, `visit_registration`, giúp dễ dàng kiểm thử các chức năng API.
*   **Postman Collection (`postman/QuanNhan_Collection.json`)**: Cung cấp một tệp Postman Collection với các request được cấu hình sẵn cho tất cả các API đã triển khai (cả Public và Admin), thuận tiện cho việc kiểm thử thủ công.

---

## 4. Triển khai Giao diện Người dùng (Frontend - Thymeleaf & Bootstrap)

*   **Bộ điều khiển Trang chủ (`HomeController.java`)**: Tạo và cập nhật `HomeController` để phục vụ các trang HTML tĩnh.
*   **Tệp CSS chung (`static/css/style.css`)**: Cung cấp các định nghĩa CSS cơ bản, tập trung vào thiết kế mobile-first.
*   **Giao diện Public (Người thân)**:
    *   **`templates/index.html`**: Trang đích chính, bao gồm phần giới thiệu 4 bước, form đăng ký thăm, cơ chế tìm kiếm quân nhân/đơn vị, và màn hình xác nhận đăng ký thành công. Thiết kế tối ưu cho trải nghiệm di động với Bootstrap 5.
    *   **`static/js/public.js`**: Mã JavaScript xử lý các tương tác trên `index.html`, bao gồm gọi AJAX đến các API backend để lấy dữ liệu đơn vị/quân nhân và gửi form đăng ký.
*   **Giao diện Admin (Cán bộ)**:
    *   **`templates/admin/login.html`**: Trang đăng nhập dành cho cán bộ quản lý, với giao diện đơn giản, màu sắc nhẹ nhàng.
    *   **`templates/admin/dashboard.html`**: Trang tổng quan cho phép cán bộ xem danh sách đăng ký, áp dụng các bộ lọc (tuần, trạng thái), xem các thẻ thống kê nhanh, và sử dụng modal để duyệt/từ chối đăng ký.
    *   **`static/js/admin.js`**: Mã JavaScript xử lý logic đăng nhập, kiểm tra xác thực (token), tải dữ liệu danh sách đăng ký, cập nhật trạng thái và hiển thị thống kê.

---

**Kết luận:** Hệ thống đã được phát triển hoàn chỉnh từ backend API đến giao diện người dùng cơ bản cho cả hai phân hệ chính. Các công cụ kiểm thử cũng đã được cung cấp để hỗ trợ quá trình phát triển và kiểm tra.
