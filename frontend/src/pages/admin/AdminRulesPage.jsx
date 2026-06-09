import { useEffect, useState } from "react";
import { RefreshCcw, Rocket, Plus } from "lucide-react";
import { adminApi } from "../../api/adminApi";
import PageHeader from "../../components/PageHeader";
import DataTable from "../../components/DataTable";
import StatusBadge from "../../components/StatusBadge";
import { useToast } from "../../components/ToastProvider";

export default function AdminRulesPage() {
  const toast = useToast();

  const [current, setCurrent] = useState(null);
  const [history, setHistory] = useState([]);
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);

  const [form, setForm] = useState({
    maPhienBan: "QD_V2",
    tenPhienBan: "Quy định v2",
    maNhanVienThayDoi: "NV_ADMIN",
    ghiChu: "Tạo phiên bản quy định mới",

    maThamSo: "TS_QD_V2",
    tuoiToiThieu: 18,
    tuoiToiDa: 55,
    thoiHanTheTheoThang: 6,
    khoangCachNamXuatBan: 8,
    soNgayNhacTruocHan: 3,
    soNgayGiuDatTruoc: 2,
    mucPhatTreMoiNgay: 2000,

    giaGoiTheoNhomJson: JSON.stringify(
      [
        {
          maGiaGoi: "GG_SV_THUONG_V2",
          maGoiThanhVien: "GOI_THUONG",
          maNhomDocGia: "NHOM_SINHVIEN",
          giaTien: 0,
          thoiHanGoiTheoNgay: 180
        }
      ],
      null,
      2
    ),

    quyDinhGoiJson: JSON.stringify(
      [
        {
          maQuyDinhGoi: "QDG_THUONG_V2",
          maGoiThanhVien: "GOI_THUONG",
          soSachMuonToiDa: 5,
          soLanGiaHanToiDa: 2
        }
      ],
      null,
      2
    ),

    quyDinhMuonTheoTheLoaiJson: JSON.stringify(
      [
        {
          maQuyDinhMuon: "QDM_THUONG_MANGA_V2",
          maGoiThanhVien: "GOI_THUONG",
          maTheLoai: "TL_MANGA",
          soNgayMuon: 6,
          soNgayGiaHanMoiLan: 3
        }
      ],
      null,
      2
    )
  });

  function updateField(field, value) {
    setForm((prev) => ({
      ...prev,
      [field]: value
    }));
  }

  async function loadData() {
    try {
      const [currentRule, historyRules] = await Promise.all([
        adminApi.getCurrentRule(),
        adminApi.getRuleHistory()
      ]);

      setCurrent(currentRule);
      setHistory(Array.isArray(historyRules) ? historyRules : []);
    } catch (err) {
      toast.error(err.message || "Không tải được quy định");
    }
  }

  useEffect(() => {
    loadData();
  }, []);

  function buildPayload() {
    return {
      maPhienBan: form.maPhienBan,
      tenPhienBan: form.tenPhienBan,
      maNhanVienThayDoi: form.maNhanVienThayDoi,
      ghiChu: form.ghiChu,
      thamSo: {
        maThamSo: form.maThamSo,
        tuoiToiThieu: Number(form.tuoiToiThieu),
        tuoiToiDa: Number(form.tuoiToiDa),
        thoiHanTheTheoThang: Number(form.thoiHanTheTheoThang),
        khoangCachNamXuatBan: Number(form.khoangCachNamXuatBan),
        soNgayNhacTruocHan: Number(form.soNgayNhacTruocHan),
        soNgayGiuDatTruoc: Number(form.soNgayGiuDatTruoc),
        mucPhatTreMoiNgay: Number(form.mucPhatTreMoiNgay)
      },
      giaGoiTheoNhom: JSON.parse(form.giaGoiTheoNhomJson || "[]"),
      quyDinhGoi: JSON.parse(form.quyDinhGoiJson || "[]"),
      quyDinhMuonTheoTheLoai: JSON.parse(form.quyDinhMuonTheoTheLoaiJson || "[]")
    };
  }

  async function createRule(e) {
    e.preventDefault();

    setLoading(true);

    try {
      const payload = buildPayload();
      const data = await adminApi.createRule(payload);

      setResult(data);
      toast.success("Tạo phiên bản quy định thành công");
      await loadData();
    } catch (err) {
      toast.error(err.message || "Tạo quy định thất bại. Kiểm tra lại JSON hoặc mã bị trùng.");
    } finally {
      setLoading(false);
    }
  }

  async function activateRule(maPhienBan) {
    if (!window.confirm(`Áp dụng phiên bản ${maPhienBan}? Phiên bản đang áp dụng hiện tại sẽ bị ngừng áp dụng.`)) {
      return;
    }

    setLoading(true);

    try {
      const data = await adminApi.activateRule(maPhienBan);
      setResult(data);
      toast.success("Áp dụng quy định thành công");
      await loadData();
    } catch (err) {
      toast.error(err.message || "Áp dụng quy định thất bại");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <PageHeader
        eyebrow="Admin"
        title="Quy định hệ thống"
        description="Tạo phiên bản quy định mới và áp dụng cho các nghiệp vụ phát sinh sau thời điểm áp dụng."
        right={
          <button className="soft-button" onClick={loadData}>
            <RefreshCcw size={17} />
            Tải lại
          </button>
        }
      />

      <div className="panel">
        <div className="panel-title">
          <h2>Quy định đang áp dụng</h2>
          {current && <StatusBadge value={current.trangThai} />}
        </div>

        {current ? (
          <div className="rule-summary">
            <p><b>Mã phiên bản:</b> {current.maPhienBan}</p>
            <p><b>Tên:</b> {current.tenPhienBan}</p>
            <p><b>Mức phạt trễ/ngày:</b> {Number(current.thamSo?.mucPhatTreMoiNgay || 0).toLocaleString()}đ</p>
            <p><b>Tuổi:</b> {current.thamSo?.tuoiToiThieu} - {current.thamSo?.tuoiToiDa}</p>
            <p><b>Hạn thẻ:</b> {current.thamSo?.thoiHanTheTheoThang} tháng</p>
          </div>
        ) : (
          <p>Chưa có quy định đang áp dụng.</p>
        )}
      </div>

      <div className="form-layout">
        <form className="panel form-panel" onSubmit={createRule}>
          <div className="panel-title">
            <h2>Tạo phiên bản mới</h2>
            <Plus size={20} />
          </div>

          <div className="form-grid-2">
            <div className="form-row">
              <label>Mã phiên bản</label>
              <input value={form.maPhienBan} onChange={(e) => updateField("maPhienBan", e.target.value)} />
            </div>

            <div className="form-row">
              <label>Tên phiên bản</label>
              <input value={form.tenPhienBan} onChange={(e) => updateField("tenPhienBan", e.target.value)} />
            </div>
          </div>

          <div className="form-grid-2">
            <div className="form-row">
              <label>Nhân viên thay đổi</label>
              <input value={form.maNhanVienThayDoi} onChange={(e) => updateField("maNhanVienThayDoi", e.target.value)} />
            </div>

            <div className="form-row">
              <label>Mã tham số</label>
              <input value={form.maThamSo} onChange={(e) => updateField("maThamSo", e.target.value)} />
            </div>
          </div>

          <div className="form-grid-2">
            <div className="form-row">
              <label>Tuổi tối thiểu</label>
              <input type="number" value={form.tuoiToiThieu} onChange={(e) => updateField("tuoiToiThieu", e.target.value)} />
            </div>

            <div className="form-row">
              <label>Tuổi tối đa</label>
              <input type="number" value={form.tuoiToiDa} onChange={(e) => updateField("tuoiToiDa", e.target.value)} />
            </div>
          </div>

          <div className="form-grid-2">
            <div className="form-row">
              <label>Thời hạn thẻ theo tháng</label>
              <input type="number" value={form.thoiHanTheTheoThang} onChange={(e) => updateField("thoiHanTheTheoThang", e.target.value)} />
            </div>

            <div className="form-row">
              <label>Khoảng cách năm xuất bản</label>
              <input type="number" value={form.khoangCachNamXuatBan} onChange={(e) => updateField("khoangCachNamXuatBan", e.target.value)} />
            </div>
          </div>

          <div className="form-grid-2">
            <div className="form-row">
              <label>Số ngày nhắc trước hạn</label>
              <input type="number" value={form.soNgayNhacTruocHan} onChange={(e) => updateField("soNgayNhacTruocHan", e.target.value)} />
            </div>

            <div className="form-row">
              <label>Số ngày giữ đặt trước</label>
              <input type="number" value={form.soNgayGiuDatTruoc} onChange={(e) => updateField("soNgayGiuDatTruoc", e.target.value)} />
            </div>
          </div>

          <div className="form-row">
            <label>Mức phạt trễ mỗi ngày</label>
            <input type="number" value={form.mucPhatTreMoiNgay} onChange={(e) => updateField("mucPhatTreMoiNgay", e.target.value)} />
          </div>

          <div className="form-row">
            <label>Giá gói theo nhóm độc giả JSON</label>
            <textarea rows={8} value={form.giaGoiTheoNhomJson} onChange={(e) => updateField("giaGoiTheoNhomJson", e.target.value)} />
          </div>

          <div className="form-row">
            <label>Quy định gói JSON</label>
            <textarea rows={8} value={form.quyDinhGoiJson} onChange={(e) => updateField("quyDinhGoiJson", e.target.value)} />
          </div>

          <div className="form-row">
            <label>Quy định mượn theo thể loại JSON</label>
            <textarea rows={8} value={form.quyDinhMuonTheoTheLoaiJson} onChange={(e) => updateField("quyDinhMuonTheoTheLoaiJson", e.target.value)} />
          </div>

          <div className="form-row">
            <label>Ghi chú</label>
            <textarea value={form.ghiChu} onChange={(e) => updateField("ghiChu", e.target.value)} />
          </div>

          <button className="primary-button" disabled={loading}>
            Tạo phiên bản quy định
          </button>
        </form>

        <div className="panel preview-panel">
          <h2>Kết quả</h2>
          <pre>{result ? JSON.stringify(result, null, 2) : "Chưa có dữ liệu"}</pre>
        </div>
      </div>

      <div className="panel">
        <div className="panel-title">
          <h2>Lịch sử phiên bản quy định</h2>
          <span>{history.length} phiên bản</span>
        </div>

        <DataTable
          data={history}
          columns={[
            { key: "maPhienBan", title: "Mã phiên bản" },
            { key: "tenPhienBan", title: "Tên phiên bản" },
            {
              key: "mucPhat",
              title: "Phạt/ngày",
              render: (row) => `${Number(row.thamSo?.mucPhatTreMoiNgay || 0).toLocaleString()}đ`
            },
            {
              key: "trangThai",
              title: "Trạng thái",
              render: (row) => <StatusBadge value={row.trangThai} />
            },
            {
              key: "actions",
              title: "Thao tác",
              render: (row) =>
                row.trangThai === "Đang áp dụng" ? (
                  <span>Đang áp dụng</span>
                ) : (
                  <button className="soft-button" onClick={() => activateRule(row.maPhienBan)}>
                    <Rocket size={15} />
                    Áp dụng
                  </button>
                )
            }
          ]}
        />
      </div>
    </div>
  );
}
