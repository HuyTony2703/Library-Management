import { useEffect, useState } from "react";
import { RefreshCcw, Rocket, Plus } from "lucide-react";
import { adminApi } from "../../api/adminApi";
import PageHeader from "../../components/PageHeader";
import DataTable from "../../components/DataTable";
import StatusBadge from "../../components/StatusBadge";
import { useToast } from "../../components/ToastProvider";
import { displayCode, formatMoney } from "../../utils/displayUtils";

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
    maGiaGoi: "GG_SV_THUONG_V2",
    maGoiThanhVienGia: "GOI_THUONG",
    maNhomDocGia: "NHOM_SINHVIEN",
    giaTien: 0,
    thoiHanGoiTheoNgay: 180,
    maQuyDinhGoi: "QDG_THUONG_V2",
    maGoiThanhVienQuyDinh: "GOI_THUONG",
    soSachMuonToiDa: 5,
    soLanGiaHanToiDa: 2,
    maQuyDinhMuon: "QDM_THUONG_MANGA_V2",
    maGoiThanhVienMuon: "GOI_THUONG",
    maTheLoai: "TL_MANGA",
    soNgayMuon: 6,
    soNgayGiaHanMoiLan: 3
  });

  function updateField(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }));
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
      giaGoiTheoNhom: [
        {
          maGiaGoi: form.maGiaGoi,
          maGoiThanhVien: form.maGoiThanhVienGia,
          maNhomDocGia: form.maNhomDocGia,
          giaTien: Number(form.giaTien),
          thoiHanGoiTheoNgay: Number(form.thoiHanGoiTheoNgay)
        }
      ],
      quyDinhGoi: [
        {
          maQuyDinhGoi: form.maQuyDinhGoi,
          maGoiThanhVien: form.maGoiThanhVienQuyDinh,
          soSachMuonToiDa: Number(form.soSachMuonToiDa),
          soLanGiaHanToiDa: Number(form.soLanGiaHanToiDa)
        }
      ],
      quyDinhMuonTheoTheLoai: [
        {
          maQuyDinhMuon: form.maQuyDinhMuon,
          maGoiThanhVien: form.maGoiThanhVienMuon,
          maTheLoai: form.maTheLoai,
          soNgayMuon: Number(form.soNgayMuon),
          soNgayGiaHanMoiLan: Number(form.soNgayGiaHanMoiLan)
        }
      ]
    };
  }

  async function createRule(e) {
    e.preventDefault();
    setLoading(true);

    try {
      const data = await adminApi.createRule(buildPayload());
      setResult(data);
      toast.success("Tạo phiên bản quy định thành công");
      await loadData();
    } catch (err) {
      toast.error(err.message || "Tạo quy định thất bại. Kiểm tra lại mã bị trùng hoặc dữ liệu không hợp lệ.");
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
          <RuleSummary rule={current} />
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

          <SectionTitle title="Thông tin phiên bản" />
          <div className="form-grid-2">
            <TextField label="Mã phiên bản" value={form.maPhienBan} onChange={(value) => updateField("maPhienBan", value)} />
            <TextField label="Tên phiên bản" value={form.tenPhienBan} onChange={(value) => updateField("tenPhienBan", value)} />
          </div>
          <div className="form-grid-2">
            <TextField label="Nhân viên thay đổi" value={form.maNhanVienThayDoi} onChange={(value) => updateField("maNhanVienThayDoi", value)} />
            <TextField label="Mã tham số" value={form.maThamSo} onChange={(value) => updateField("maThamSo", value)} />
          </div>

          <SectionTitle title="Tham số hệ thống" />
          <div className="form-grid-3">
            <NumberField label="Tuổi tối thiểu" value={form.tuoiToiThieu} onChange={(value) => updateField("tuoiToiThieu", value)} />
            <NumberField label="Tuổi tối đa" value={form.tuoiToiDa} onChange={(value) => updateField("tuoiToiDa", value)} />
            <NumberField label="Hạn thẻ theo tháng" value={form.thoiHanTheTheoThang} onChange={(value) => updateField("thoiHanTheTheoThang", value)} />
          </div>
          <div className="form-grid-3">
            <NumberField label="Khoảng cách năm xuất bản" value={form.khoangCachNamXuatBan} onChange={(value) => updateField("khoangCachNamXuatBan", value)} />
            <NumberField label="Ngày nhắc trước hạn" value={form.soNgayNhacTruocHan} onChange={(value) => updateField("soNgayNhacTruocHan", value)} />
            <NumberField label="Ngày giữ đặt trước" value={form.soNgayGiuDatTruoc} onChange={(value) => updateField("soNgayGiuDatTruoc", value)} />
          </div>
          <NumberField label="Mức phạt trễ mỗi ngày" value={form.mucPhatTreMoiNgay} onChange={(value) => updateField("mucPhatTreMoiNgay", value)} />

          <SectionTitle title="Giá gói theo nhóm độc giả" />
          <div className="form-grid-3">
            <TextField label="Mã giá gói" value={form.maGiaGoi} onChange={(value) => updateField("maGiaGoi", value)} />
            <TextField label="Gói thành viên" value={form.maGoiThanhVienGia} onChange={(value) => updateField("maGoiThanhVienGia", value)} />
            <TextField label="Nhóm độc giả" value={form.maNhomDocGia} onChange={(value) => updateField("maNhomDocGia", value)} />
          </div>
          <div className="form-grid-2">
            <NumberField label="Giá tiền" value={form.giaTien} onChange={(value) => updateField("giaTien", value)} />
            <NumberField label="Thời hạn gói theo ngày" value={form.thoiHanGoiTheoNgay} onChange={(value) => updateField("thoiHanGoiTheoNgay", value)} />
          </div>

          <SectionTitle title="Quy định gói" />
          <div className="form-grid-2">
            <TextField label="Mã quy định gói" value={form.maQuyDinhGoi} onChange={(value) => updateField("maQuyDinhGoi", value)} />
            <TextField label="Gói thành viên" value={form.maGoiThanhVienQuyDinh} onChange={(value) => updateField("maGoiThanhVienQuyDinh", value)} />
          </div>
          <div className="form-grid-2">
            <NumberField label="Số sách mượn tối đa" value={form.soSachMuonToiDa} onChange={(value) => updateField("soSachMuonToiDa", value)} />
            <NumberField label="Số lần gia hạn tối đa" value={form.soLanGiaHanToiDa} onChange={(value) => updateField("soLanGiaHanToiDa", value)} />
          </div>

          <SectionTitle title="Quy định mượn theo thể loại" />
          <div className="form-grid-3">
            <TextField label="Mã quy định mượn" value={form.maQuyDinhMuon} onChange={(value) => updateField("maQuyDinhMuon", value)} />
            <TextField label="Gói thành viên" value={form.maGoiThanhVienMuon} onChange={(value) => updateField("maGoiThanhVienMuon", value)} />
            <TextField label="Thể loại" value={form.maTheLoai} onChange={(value) => updateField("maTheLoai", value)} />
          </div>
          <div className="form-grid-2">
            <NumberField label="Số ngày mượn" value={form.soNgayMuon} onChange={(value) => updateField("soNgayMuon", value)} />
            <NumberField label="Số ngày gia hạn mỗi lần" value={form.soNgayGiaHanMoiLan} onChange={(value) => updateField("soNgayGiaHanMoiLan", value)} />
          </div>

          <div className="form-row">
            <label>Ghi chú</label>
            <textarea value={form.ghiChu} onChange={(e) => updateField("ghiChu", e.target.value)} />
          </div>

          <button className="primary-button" disabled={loading}>
            Tạo phiên bản quy định
          </button>
        </form>

        <RuleResultPanel result={result} />
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
            { key: "mucPhat", title: "Phạt/ngày", render: (row) => formatMoney(row.thamSo?.mucPhatTreMoiNgay) },
            { key: "trangThai", title: "Trạng thái", render: (row) => <StatusBadge value={row.trangThai} /> },
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

function RuleSummary({ rule }) {
  return (
    <div className="result-stack">
      <div className="result-grid">
        <ResultItem label="Mã phiên bản" value={rule.maPhienBan} />
        <ResultItem label="Tên" value={rule.tenPhienBan} />
        <ResultItem label="Mức phạt trễ/ngày" value={formatMoney(rule.thamSo?.mucPhatTreMoiNgay)} />
        <ResultItem label="Tuổi độc giả" value={`${rule.thamSo?.tuoiToiThieu ?? "-"} - ${rule.thamSo?.tuoiToiDa ?? "-"}`} />
        <ResultItem label="Hạn thẻ" value={`${rule.thamSo?.thoiHanTheTheoThang ?? "-"} tháng`} />
      </div>
      <RuleDetailTables rule={rule} />
    </div>
  );
}

function RuleResultPanel({ result }) {
  return (
    <div className="panel preview-panel">
      <h2>Kết quả quy định</h2>

      {!result ? (
        <p className="muted-text">Chưa có dữ liệu</p>
      ) : (
        <div className="result-stack">
          <RuleSummary rule={result} />
        </div>
      )}
    </div>
  );
}

function RuleDetailTables({ rule }) {
  return (
    <div className="result-stack">
      <DataTable
        data={rule.giaGoiTheoNhom || []}
        columns={[
          { key: "maGiaGoi", title: "Mã giá gói" },
          { key: "maGoiThanhVien", title: "Gói", render: (row) => displayCode(row.maGoiThanhVien) },
          { key: "maNhomDocGia", title: "Nhóm độc giả", render: (row) => displayCode(row.maNhomDocGia) },
          { key: "giaTien", title: "Giá tiền", render: (row) => formatMoney(row.giaTien) },
          { key: "thoiHanGoiTheoNgay", title: "Thời hạn" }
        ]}
      />
      <DataTable
        data={rule.quyDinhGoi || []}
        columns={[
          { key: "maQuyDinhGoi", title: "Mã quy định" },
          { key: "maGoiThanhVien", title: "Gói", render: (row) => displayCode(row.maGoiThanhVien) },
          { key: "soSachMuonToiDa", title: "Sách tối đa" },
          { key: "soLanGiaHanToiDa", title: "Gia hạn tối đa" }
        ]}
      />
      <DataTable
        data={rule.quyDinhMuonTheoTheLoai || []}
        columns={[
          { key: "maQuyDinhMuon", title: "Mã quy định" },
          { key: "maGoiThanhVien", title: "Gói", render: (row) => displayCode(row.maGoiThanhVien) },
          { key: "maTheLoai", title: "Thể loại", render: (row) => displayCode(row.maTheLoai) },
          { key: "soNgayMuon", title: "Ngày mượn" },
          { key: "soNgayGiaHanMoiLan", title: "Ngày gia hạn/lần" }
        ]}
      />
    </div>
  );
}

function TextField({ label, value, onChange }) {
  return (
    <div className="form-row">
      <label>{label}</label>
      <input value={value} onChange={(e) => onChange(e.target.value)} />
    </div>
  );
}

function NumberField({ label, value, onChange }) {
  return (
    <div className="form-row">
      <label>{label}</label>
      <input type="number" value={value} onChange={(e) => onChange(e.target.value)} />
    </div>
  );
}

function SectionTitle({ title }) {
  return <h3 className="form-section-title">{title}</h3>;
}

function ResultItem({ label, value }) {
  return (
    <div className="result-item">
      <span>{label}</span>
      <strong>{value || "-"}</strong>
    </div>
  );
}
