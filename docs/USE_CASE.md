1. Yêu cầu người dùng
   Người dùng chính của hệ thống là thân nhân quân nhân muốn đăng ký vào thăm chiến sĩ tại đơn vị. Họ thường tiếp cận hệ thống bằng cách quét mã QR dán tại đơn vị hoặc nhận qua Zalo/đường link, dùng điện thoại là chủ yếu, nên giao diện cần tối ưu cho mobile, font chữ to, ít thao tác, không yêu cầu tài khoản đăng nhập. Người dùng chỉ cần thực hiện một lần nhập thông tin đơn giản: chọn/nhập tên quân nhân và đơn vị, nhập tên người thân, quan hệ, số điện thoại, tỉnh/thành, tuần dự kiến đến thăm, sau đó gửi đăng ký và chờ cán bộ đơn vị phê duyệt; mọi hướng dẫn phải rõ ràng, ngắn gọn, tiếng Việt dễ hiểu.

Về mặt trải nghiệm, landing page cần hiển thị rõ quy trình 3–4 bước (quét QR → điền form → chờ duyệt → đến thăm) để người không rành công nghệ vẫn làm được. Sau khi gửi form, hệ thống nên hiển thị thông báo xác nhận và hướng dẫn tiếp theo (ví dụ: “Giữ lại màn hình này hoặc tin nhắn xác nhận, liên hệ số điện thoại đơn vị nếu cần thay đổi thời gian”), có thể gửi thêm SMS/Zalo nếu sau này mở rộng. Thông tin cá nhân phải được cam kết bảo mật, chỉ dùng cho mục đích quản lý thăm gặp, và mọi nội dung đều cần tuân thủ quy định bảo mật, không yêu cầu những dữ liệu nhạy cảm không cần thiết.​

2. Chức năng phía người thân (landing page public)
   Màn hướng dẫn / giới thiệu: hiển thị tiêu đề “Quét mã QR để đăng ký thăm quân nhân”, 4 bước minh họa giống các hình bạn gửi (mở camera/Zalo, quét mã, điền form, chờ duyệt). Trang này đồng thời chứa khung form bên dưới hoặc nút “Bắt đầu đăng ký” cuộn xuống form để giảm rối mắt.

Form đăng ký thăm: gồm các trường tên quân nhân (chọn từ danh sách hoặc gõ, map với bảng soldier), đơn vị (dropdown từ bảng unit), tên người thân, quan hệ, số điện thoại, tỉnh/thành, chọn tuần thăm (Tuần 1–4) và nút “Gửi đăng ký”; khi submit, hệ thống tạo bản ghi mới trong relative (nếu số điện thoại chưa tồn tại) và visit_registration (trạng thái mặc định PENDING).

Trang/khung xác nhận: sau khi gửi thành công, landing page hiển thị mã đăng ký hoặc thông tin đã gửi, trạng thái “Chờ duyệt” và một số lưu ý khi đến đơn vị (thời gian, trang phục, mang giấy tờ tùy thân…). Nội dung ngắn gọn, không yêu cầu người dùng phải nhớ thêm tài khoản hay mật khẩu, chỉ cần nhớ số điện thoại/mã đăng ký nếu sau này cần tra cứu.​

3. Chức năng phía cán bộ đơn vị
   Màn danh sách đăng ký: một trang nội bộ (có thể bảo vệ bằng tài khoản admin_user) hiển thị bảng danh sách các lượt đăng ký từ visit_registration, có filter theo đơn vị, tuần, tỉnh, trạng thái; mỗi dòng hiển thị tên quân nhân, đơn vị, người thân, quan hệ, số điện thoại, tỉnh, tuần, trạng thái hiện tại.

Màn duyệt đăng ký: khi nhấn vào một dòng, cán bộ có thể xem chi tiết, sau đó bấm “Chấp nhận” hoặc “Từ chối”, nhập ghi chú (ví dụ: “Đề nghị đổi sang tuần 2 vì đơn vị bận huấn luyện tuần 1”) và lưu; hệ thống cập nhật status, note, approved_at, và nếu cần có thể kích hoạt gửi tin nhắn thông báo cho người thân sau này.

Thống kê thăm quân nhân: một trang đơn giản tổng hợp dữ liệu từ visit_registration để hiển thị bảng “Tỉnh / Tuần 1–4 / Ghi chú” giống mockup của bạn, có thể thêm tổng số lượt thăm theo: tỉnh, tuần, đơn vị, trạng thái; chức năng này không cần bảng riêng, chỉ là các truy vấn GROUP BY trên dữ liệu hiện có, phục vụ báo cáo nhanh cho chỉ huy về tình hình thăm gặp từng đợt.​