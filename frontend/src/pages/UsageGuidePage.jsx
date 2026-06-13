import {
    ArrowLeftRight,
    BarChart3,
    BookCopy,
    BookOpen,
    ClipboardList,
    CreditCard,
    MessageSquare,
    Settings,
    ShieldCheck,
    UserCog,
    UsersRound
} from "lucide-react";
import PageHeader from "../components/PageHeader";
import { useAuth } from "../context/AuthContext";
import { isAdmin } from "../utils/roleUtils";

const staffGuide = [
    {
        icon: BookOpen,
        title: "Quản lý đầu sách",
        steps: [
            "Vào Đầu sách để thêm hoặc cập nhật thông tin sách, tác giả, thể loại và nhà xuất bản.",
            "Kiểm tra mã đầu sách trước khi tạo mới để tránh trùng dữ liệu.",
            "Sau khi lưu đầu sách, chuyển sang Cuốn sách để nhập từng bản vật lý trong kho."
        ]
    },
    {
        icon: BookCopy,
        title: "Quản lý cuốn sách",
        steps: [
            "Vào Cuốn sách để theo dõi tình trạng từng bản: sẵn sàng, đang mượn, hỏng hoặc mất.",
            "Cập nhật vị trí và trạng thái khi sách được nhập kho, chuyển kệ hoặc ngừng phục vụ.",
            "Chỉ cho mượn các cuốn đang ở trạng thái có thể phục vụ."
        ]
    },
    {
        icon: UsersRound,
        title: "Quản lý độc giả",
        steps: [
            "Vào Độc giả để tạo hồ sơ, gia hạn thẻ và kiểm tra trạng thái tài khoản.",
            "Đối chiếu nhóm độc giả, hạn thẻ và công nợ trước khi lập phiếu mượn.",
            "Khóa hoặc mở tài khoản theo đúng quy định vận hành của thư viện."
        ]
    },
    {
        icon: ArrowLeftRight,
        title: "Mượn sách",
        steps: [
            "Vào Mượn sách, chọn độc giả và các cuốn sách cần lập phiếu.",
            "Hệ thống kiểm tra hạn thẻ, số lượng mượn tối đa và tình trạng cuốn sách.",
            "Xác nhận phiếu mượn sau khi dữ liệu hợp lệ."
        ]
    },
    {
        icon: ClipboardList,
        title: "Trả sách",
        steps: [
            "Vào Trả sách, tìm phiếu mượn hoặc độc giả cần trả.",
            "Ghi nhận tình trạng sách khi trả để hệ thống tính trễ hạn hoặc hỏng mất nếu có.",
            "Hoàn tất phiếu trả rồi kiểm tra khoản nợ phát sinh."
        ]
    },
    {
        icon: CreditCard,
        title: "Thu tiền",
        steps: [
            "Vào Thu tiền để xem các khoản nợ còn lại của độc giả.",
            "Chọn khoản cần thu, nhập phương thức thanh toán và xác nhận số tiền.",
            "Sau khi thu, kiểm tra lại trạng thái công nợ của độc giả."
        ]
    },
    {
        icon: MessageSquare,
        title: "Kiểm duyệt bình luận",
        steps: [
            "Vào Kiểm duyệt bình luận để xem nội dung độc giả gửi cho đầu sách.",
            "Duyệt các bình luận phù hợp và từ chối nội dung không hợp lệ.",
            "Theo dõi trạng thái xử lý để giữ nội dung hiển thị nhất quán."
        ]
    },
    {
        icon: Settings,
        title: "Cài đặt tài khoản",
        steps: [
            "Vào Cài đặt để cập nhật thông tin cá nhân và đổi mật khẩu.",
            "Thiết lập giao diện, thông báo và các tùy chọn sử dụng trên thiết bị hiện tại.",
            "Đăng xuất khi bàn giao máy hoặc kết thúc ca trực."
        ]
    }
];

const adminGuide = [
    ...staffGuide,
    {
        icon: BarChart3,
        title: "Báo cáo hệ thống",
        steps: [
            "Vào Báo cáo hệ thống để xem tổng hợp công nợ, sách đang mượn, mượn theo thể loại và trả trễ.",
            "Chọn kỳ báo cáo phù hợp trước khi đối chiếu số liệu vận hành.",
            "Dùng báo cáo để kiểm tra tình trạng tồn đọng và hiệu quả phục vụ."
        ]
    },
    {
        icon: ShieldCheck,
        title: "Quy định hệ thống",
        steps: [
            "Vào Quy định hệ thống để tạo phiên bản quy định mới khi chính sách thư viện thay đổi.",
            "Kiểm tra tuổi độc giả, hạn thẻ, số ngày mượn, số lần gia hạn và mức phạt trước khi áp dụng.",
            "Chỉ một phiên bản quy định nên ở trạng thái đang áp dụng tại một thời điểm."
        ]
    },
    {
        icon: UserCog,
        title: "Tài khoản thủ thư",
        steps: [
            "Vào Tài khoản thủ thư để tạo, cập nhật, khóa hoặc đặt lại mật khẩu cho nhân viên.",
            "Gắn đúng thông tin nhân viên và trạng thái tài khoản trước khi bàn giao đăng nhập.",
            "Thu hồi hoặc khóa tài khoản khi nhân viên không còn phụ trách hệ thống."
        ]
    }
];

export default function UsageGuidePage() {
    const { user } = useAuth();
    const adminUser = isAdmin(user);
    const guideItems = adminUser ? adminGuide : staffGuide;

    return (
        <div>
            <PageHeader
                eyebrow="Guide"
                title="Hướng dẫn sử dụng"
                description={adminUser
                    ? "Các bước thao tác chính dành cho quản trị viên khi vận hành LibraDesk."
                    : "Các bước thao tác chính dành cho thủ thư khi phục vụ mượn trả và quản lý dữ liệu."}
            />

            <div className="guide-grid">
                {guideItems.map((item) => {
                    const Icon = item.icon;

                    return (
                        <section className="guide-card" key={item.title}>
                            <div className="guide-card-icon">
                                <Icon size={22} />
                            </div>
                            <h2>{item.title}</h2>
                            <ol>
                                {item.steps.map((step) => (
                                    <li key={step}>{step}</li>
                                ))}
                            </ol>
                        </section>
                    );
                })}
            </div>
        </div>
    );
}
