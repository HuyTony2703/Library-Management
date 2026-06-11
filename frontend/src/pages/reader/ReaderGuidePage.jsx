import {
    Bell,
    BookmarkPlus,
    CircleHelp,
    Crown,
    RefreshCcw,
    Search
} from "lucide-react";
import { Link } from "react-router-dom";

export default function ReaderGuidePage() {
    return (
        <div>
            <div className="reader-home-header">
                <small>Guide</small>
                <h1>Hướng dẫn sử dụng</h1>
                <p>Các bước cơ bản dành cho độc giả khi sử dụng LibraDesk.</p>
            </div>

            <div className="guide-grid">
                <GuideCard
                    icon={Search}
                    title="Tra cứu sách"
                    steps={[
                        "Vào mục Tra cứu sách.",
                        "Nhập tên sách, mã sách, tác giả hoặc thể loại.",
                        "Bấm vào sách để xem chi tiết đầu sách và danh sách cuốn sách.",
                        "Kiểm tra trạng thái cuốn sách trước khi đặt trước hoặc mượn."
                    ]}
                />

                <GuideCard
                    icon={BookmarkPlus}
                    title="Đặt trước sách"
                    steps={[
                        "Vào trang chi tiết đầu sách.",
                        "Bấm Đặt trước đầu sách nếu muốn xếp hàng chờ.",
                        "Bấm Đặt cuốn này nếu muốn giữ đúng một cuốn cụ thể.",
                        "Theo dõi trạng thái đặt trước trong mục Đặt trước."
                    ]}
                />

                <GuideCard
                    icon={RefreshCcw}
                    title="Gia hạn sách"
                    steps={[
                        "Vào mục Sách đang mượn.",
                        "Chọn sách cần gia hạn.",
                        "Bấm Gia hạn.",
                        "Hệ thống sẽ kiểm tra nợ, số lần gia hạn và quy định hiện tại."
                    ]}
                />

                <GuideCard
                    icon={Crown}
                    title="Mua gói độc giả"
                    steps={[
                        "Vào mục Gói độc giả.",
                        "Xem gói hiện tại và các gói có thể mua.",
                        "Chọn gói phù hợp.",
                        "Xác nhận phương thức thanh toán để kích hoạt gói."
                    ]}
                />

                <GuideCard
                    icon={Bell}
                    title="Theo dõi thông báo"
                    steps={[
                        "Bấm chuông thông báo trên thanh trên cùng.",
                        "Xem các nhắc hạn, đặt trước, gia hạn và mua gói.",
                        "Đánh dấu đã đọc từng thông báo hoặc toàn bộ thông báo.",
                        "Quay lại trang liên quan nếu cần xử lý tiếp."
                    ]}
                />

                <GuideCard
                    icon={CircleHelp}
                    title="Quy định thư viện"
                    steps={[
                        "Vào mục Quy định để xem dữ liệu đang áp dụng.",
                        "Mở phần Quy định phạt trong trang Quy định để xem công thức phạt trả trễ.",
                        "Nếu có thắc mắc, liên hệ thủ thư tại quầy hỗ trợ.",
                        "Luôn kiểm tra hạn trả trong mục Sách đang mượn."
                    ]}
                />
            </div>

            <div className="guide-link-row">
                <Link to="/reader/rules">Xem quy định hiện hành</Link>
                <Link to="/reader/rules#penalty-rules">Xem cách tính phạt</Link>
            </div>
        </div>
    );
}

function GuideCard({ icon: Icon, title, steps }) {
    return (
        <article className="guide-card">
            <div className="guide-icon">
                <Icon size={24} />
            </div>
            <h3>{title}</h3>
            <ol>
                {steps.map((step) => (
                    <li key={step}>{step}</li>
                ))}
            </ol>
        </article>
    );
}
