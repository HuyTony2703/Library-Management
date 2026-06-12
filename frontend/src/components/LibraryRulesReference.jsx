const BUSINESS_RULES = [
    {
        code: "QĐ1",
        title: "Lập thẻ độc giả",
        points: [
            "Độc giả bắt buộc có tài khoản đăng nhập và mỗi tài khoản chỉ có một vai trò.",
            "Nhóm độc giả gồm học sinh, sinh viên, giáo viên và khác. Nhóm chỉ ảnh hưởng đến giá gói thành viên.",
            "Tuổi tối thiểu, tuổi tối đa và thời hạn thẻ lấy từ quy định hệ thống, admin có thể thay đổi."
        ]
    },
    {
        code: "QĐ2",
        title: "Tiếp nhận sách mới",
        points: [
            "Một đầu sách có thể có nhiều tác giả, nhiều thể loại và nhiều cuốn sách vật lý.",
            "Mỗi cuốn sách có mã riêng, có thể gắn mã vạch hoặc mã QR.",
            "Chỉ tiếp nhận sách có năm xuất bản trong khoảng cho phép; trị giá sách dùng khi xử lý phạt mất hoặc hỏng.",
            "Trạng thái cuốn sách gồm: Sẵn có, Đang được mượn, Đang được đặt trước, Bị mất, Bị hỏng, Ngừng lưu thông."
        ]
    },
    {
        code: "QĐ4",
        title: "Cho mượn sách",
        points: [
            "Chỉ cho mượn khi thẻ còn hạn, độc giả không có nợ và không có sách quá hạn chưa trả.",
            "Cuốn sách phải ở trạng thái Sẵn có hoặc đang được đặt trước cho chính độc giả đó.",
            "Số sách tối đa phụ thuộc vào gói thành viên; số ngày mượn phụ thuộc vào gói và thể loại.",
            "Hạn trả thực tế là ngày nhỏ hơn giữa ngày mượn cộng số ngày mượn và ngày hết hạn thẻ trừ 1 ngày."
        ]
    },
    {
        code: "QĐ5",
        title: "Nhận trả sách",
        points: [
            "Mỗi cuốn trong phiếu mượn có thể được trả riêng, hệ thống hỗ trợ trả từng phần.",
            "Tiền phạt trễ bằng số ngày trễ nhân mức phạt mỗi ngày.",
            "Nếu sách hỏng hoặc mất, thủ thư nhập tiền phạt thủ công.",
            "Sau khi trả, trạng thái cuốn sách cập nhật thành Sẵn có, Bị hỏng hoặc Bị mất theo tình trạng thực tế."
        ]
    },
    {
        code: "QĐ6",
        title: "Lập phiếu thu tiền",
        points: [
            "Phiếu thu dùng để thu tiền phạt hoặc tiền mua gói thành viên.",
            "Khi thu tiền phạt, số tiền thu không được vượt tổng số tiền độc giả đang nợ.",
            "Hệ thống có thể tự trừ vào khoản nợ cũ nhất hoặc cho thủ thư chọn khoản nợ cụ thể.",
            "Mỗi khoản nợ lưu số tiền phát sinh, số tiền đã thanh toán và số tiền còn lại."
        ]
    },
    {
        code: "QĐ7",
        title: "Quản lý gói thành viên",
        points: [
            "Độc giả có thể đăng ký, gia hạn, nâng cấp hoặc hạ cấp gói thành viên.",
            "Giá gói phụ thuộc vào nhóm độc giả; quyền mượn phụ thuộc vào gói thành viên.",
            "Mỗi lần thay đổi gói phải lưu vào lịch sử gói thành viên."
        ]
    },
    {
        code: "QĐ8",
        title: "Đặt trước sách",
        points: [
            "Độc giả đặt trước theo đầu sách, không đặt theo từng cuốn vật lý ngay từ đầu.",
            "Khi có cuốn phù hợp ở chi nhánh được chọn, hệ thống có thể gán cuốn cụ thể để giữ chỗ.",
            "Số ngày giữ chỗ lấy từ quy định hiện hành và admin có thể thay đổi.",
            "Trạng thái đặt trước gồm: Đang chờ, Đã giữ chỗ, Đã mượn, Đã hủy, Đã hết hạn."
        ]
    },
    {
        code: "QĐ10",
        title: "Đánh giá và bình luận sách",
        points: [
            "Độc giả đánh giá và bình luận theo đầu sách, không theo từng cuốn vật lý.",
            "Mỗi độc giả chỉ nên có một đánh giá chính cho một đầu sách.",
            "Bình luận hiển thị ngay sau khi đăng, nhưng admin hoặc thủ thư có thể ẩn hoặc xóa khi vi phạm.",
            "Trạng thái bình luận gồm: Hiển thị, Đã ẩn, Đã xóa."
        ]
    },
    {
        code: "QĐ11",
        title: "Thay đổi quy định",
        points: [
            "Admin có thể thay đổi tuổi lập thẻ, thời hạn thẻ, khoảng năm xuất bản, số sách mượn tối đa, số ngày mượn, mức phạt, giá gói, số ngày giữ chỗ và số ngày nhắc hạn.",
            "Khi thay đổi quy định, hệ thống lưu phiên bản mới thay vì chỉ ghi đè dữ liệu cũ."
        ]
    },
    {
        code: "QĐ12",
        title: "Gửi thông báo",
        points: [
            "Hệ thống gửi thông báo trong app và có thể gửi email cho độc giả.",
            "Loại thông báo gồm sách sắp đến hạn trả, sách đã quá hạn trả, phát sinh tiền phạt, sách đặt trước đã có, mua hoặc gia hạn gói thành viên thành công, gói thành viên sắp hết hạn, và tài khoản hoặc thẻ độc giả thay đổi trạng thái.",
            "Cần lưu lịch sử thông báo, trạng thái gửi email, số lần thử gửi và thời điểm gửi email cuối cùng để tránh gửi trùng."
        ]
    }
];

const RULE_SUMMARY = [
    ["QĐ1", "Lập thẻ độc giả, tài khoản, nhóm độc giả, thời hạn thẻ"],
    ["QĐ2", "Tiếp nhận sách, đầu sách, cuốn sách, trạng thái sách"],
    ["QĐ4", "Điều kiện mượn sách, hạn trả, số sách tối đa"],
    ["QĐ5", "Trả sách, trả từng phần, phạt trễ, phạt hỏng hoặc mất"],
    ["QĐ6", "Thu tiền phạt hoặc tiền mua gói"],
    ["QĐ7", "Đăng ký, gia hạn, nâng cấp hoặc hạ cấp gói thành viên"],
    ["QĐ8", "Đặt trước sách, giữ chỗ, hết hạn giữ chỗ"],
    ["QĐ10", "Đánh giá, bình luận, ẩn hoặc xóa bình luận vi phạm"],
    ["QĐ11", "Admin thay đổi tham số quy định và lưu phiên bản"],
    ["QĐ12", "Gửi thông báo trong app và email"]
];

export default function LibraryRulesReference({ className = "" }) {
    return (
        <section className={`panel rules-reference ${className}`.trim()}>
            <div className="panel-title">
                <div>
                    <p className="eyebrow">Business rules</p>
                    <h2>Danh sách quy định nghiệp vụ</h2>
                </div>
                <span>{BUSINESS_RULES.length} quy định</span>
            </div>

            <div className="rules-reference-grid">
                {BUSINESS_RULES.map((rule) => (
                    <article className="rules-reference-card" key={rule.code}>
                        <div className="rules-reference-card-title">
                            <strong>{rule.code}</strong>
                            <h3>{rule.title}</h3>
                        </div>
                        <ul>
                            {rule.points.map((point) => (
                                <li key={point}>{point}</li>
                            ))}
                        </ul>
                    </article>
                ))}
            </div>

            <div className="rules-reference-summary">
                <h3>Tóm tắt</h3>
                <div className="settings-matrix-card">
                    <table className="settings-matrix">
                        <thead>
                            <tr>
                                <th>Mã quy định</th>
                                <th>Nội dung chính</th>
                            </tr>
                        </thead>
                        <tbody>
                            {RULE_SUMMARY.map(([code, text]) => (
                                <tr key={code}>
                                    <td>{code}</td>
                                    <td>{text}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </section>
    );
}
