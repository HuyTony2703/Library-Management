import { libraryApi } from "../api/libraryApi";

export async function resolveExportResult(initialResult, onQueued) {
    if (initialResult?.status === "READY") return initialResult;
    if (initialResult?.status !== "QUEUED" || !initialResult.jobId) {
        throw new Error(initialResult?.message || "Export không hợp lệ");
    }
    onQueued?.(initialResult);
    for (let attempt = 0; attempt < 60; attempt += 1) {
        await delay(1500);
        const job = await libraryApi.exportJob(initialResult.jobId);
        if (job.status === "READY") return job;
        if (job.status === "FAILED") throw new Error(job.message || "Export thất bại");
    }
    throw new Error("Export đang xử lý quá lâu, vui lòng thử lại sau");
}

export function downloadCsvExport(result) {
    if (!result?.content) throw new Error("File export chưa sẵn sàng");
    const blob = new Blob([result.content], { type: result.mediaType || "text/csv;charset=UTF-8" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = result.filename || "export.csv";
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
}

function delay(ms) {
    return new Promise((resolve) => window.setTimeout(resolve, ms));
}
