import { useEffect, useMemo, useState } from "react";

const defaultConfig = {
    host: "localhost",
    port: "1433",
    database: "QuanLyThuVien",
    authMode: "windows",
    username: "",
    password: ""
};

function getElectronApi() {
    return window.electronAPI?.isDesktop ? window.electronAPI : null;
}

export default function DesktopBootGate({ children }) {
    const electronApi = useMemo(() => getElectronApi(), []);
    const [config, setConfig] = useState(defaultConfig);
    const [hasSavedConfig, setHasSavedConfig] = useState(false);
    const [ready, setReady] = useState(!electronApi);
    const [loading, setLoading] = useState(Boolean(electronApi));
    const [message, setMessage] = useState("Dang kiem tra cau hinh database...");
    const [error, setError] = useState("");

    useEffect(() => {
        if (!electronApi) {
            return;
        }

        let active = true;

        async function boot() {
            try {
                const savedConfig = await electronApi.getDatabaseConfig();

                if (!active) {
                    return;
                }

                if (!savedConfig) {
                    setHasSavedConfig(false);
                    setLoading(false);
                    setMessage("");
                    return;
                }

                setConfig({ ...defaultConfig, ...savedConfig });
                setHasSavedConfig(true);
                await startWithConfig({ ...defaultConfig, ...savedConfig });
            } catch (err) {
                setError(err?.message || "Khong doc duoc cau hinh database.");
                setLoading(false);
            }
        }

        boot();

        return () => {
            active = false;
        };
    }, [electronApi]);

    async function startWithConfig(nextConfig) {
        setLoading(true);
        setError("");
        setMessage("Dang khoi dong backend va ket noi SQL Server...");

        const result = await electronApi.startBackend(nextConfig);

        if (!result?.ok) {
            setError(result?.message || "Backend khong khoi dong duoc.");
            setLoading(false);
            setReady(false);
            return;
        }

        setReady(true);
        setLoading(false);
    }

    async function handleSubmit(event) {
        event.preventDefault();

        const nextConfig = {
            ...config,
            host: config.host.trim() || "localhost",
            port: config.port.trim() || "1433",
            database: config.database.trim() || "QuanLyThuVien",
            username: config.authMode === "sql" ? config.username.trim() : "",
            password: config.authMode === "sql" ? config.password : ""
        };

        if (nextConfig.authMode === "sql" && (!nextConfig.username || !nextConfig.password)) {
            setError("Vui long nhap username va password cua SQL Server.");
            return;
        }

        await electronApi.saveDatabaseConfig(nextConfig);
        setHasSavedConfig(true);
        await startWithConfig(nextConfig);
    }

    if (ready) {
        return children;
    }

    return (
        <main className="setup-screen">
            <form className="setup-card" onSubmit={handleSubmit}>
                <div className="setup-header">
                    <span>LibraDesk</span>
                    <h1>Cau hinh SQL Server</h1>
                    <p>
                        Thong tin nay chi can nhap lan dau tren may nay. App se luu cau hinh de tu
                        dong ket noi o cac lan mo sau.
                    </p>
                </div>

                <div className="form-grid-2">
                    <label className="form-row">
                        <span>Server</span>
                        <input
                            value={config.host}
                            onChange={(event) => setConfig({ ...config, host: event.target.value })}
                            placeholder="localhost"
                            disabled={loading}
                        />
                    </label>

                    <label className="form-row">
                        <span>Port</span>
                        <input
                            value={config.port}
                            onChange={(event) => setConfig({ ...config, port: event.target.value })}
                            placeholder="1433"
                            disabled={loading}
                        />
                    </label>
                </div>

                <label className="form-row">
                    <span>Database</span>
                    <input
                        value={config.database}
                        onChange={(event) => setConfig({ ...config, database: event.target.value })}
                        placeholder="QuanLyThuVien"
                        disabled={loading}
                    />
                </label>

                <div className="setup-auth-mode">
                    <button
                        type="button"
                        className={config.authMode === "windows" ? "selected" : ""}
                        onClick={() => setConfig({ ...config, authMode: "windows" })}
                        disabled={loading}
                    >
                        Windows Authentication
                    </button>
                    <button
                        type="button"
                        className={config.authMode === "sql" ? "selected" : ""}
                        onClick={() => setConfig({ ...config, authMode: "sql" })}
                        disabled={loading}
                    >
                        SQL Server Login
                    </button>
                </div>

                {config.authMode === "sql" && (
                    <div className="form-grid-2">
                        <label className="form-row">
                            <span>Username</span>
                            <input
                                value={config.username}
                                onChange={(event) => setConfig({ ...config, username: event.target.value })}
                                placeholder="sa"
                                disabled={loading}
                            />
                        </label>

                        <label className="form-row">
                            <span>Password</span>
                            <input
                                type="password"
                                value={config.password}
                                onChange={(event) => setConfig({ ...config, password: event.target.value })}
                                placeholder="SQL password"
                                disabled={loading}
                            />
                        </label>
                    </div>
                )}

                {message && <div className="setup-message">{message}</div>}
                {error && <div className="setup-error">{error}</div>}

                <div className="setup-actions">
                    {hasSavedConfig && (
                        <button
                            type="button"
                            className="ghost-button"
                            onClick={() => startWithConfig(config)}
                            disabled={loading}
                        >
                            Thu lai
                        </button>
                    )}
                    <button type="submit" className="primary-button" disabled={loading}>
                        {loading ? "Dang ket noi..." : "Luu va ket noi"}
                    </button>
                </div>
            </form>
        </main>
    );
}
