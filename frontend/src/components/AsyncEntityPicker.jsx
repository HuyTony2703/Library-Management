import { X } from "lucide-react";
import { useEffect, useId, useMemo, useRef, useState } from "react";

const defaultKey = (option) => option.value;
const defaultLabel = (option) => option.label;
const defaultCode = (option) => option.code ?? option.value;
const defaultMetadata = (option) => {
    if (!option.metadata) return "";
    if (typeof option.metadata === "string") return option.metadata;
    return Object.values(option.metadata)
        .filter((item) => item !== null && item !== undefined && item !== "")
        .join(" · ");
};

export default function AsyncEntityPicker({
    value,
    onChange,
    loadOptions,
    multiple = false,
    minQueryLength = 2,
    debounceMs = 300,
    placeholder = "Nhập để tìm kiếm...",
    disabled = false,
    required = false,
    inputName,
    ariaLabel,
    getOptionKey = defaultKey,
    getOptionLabel = defaultLabel,
    getOptionCode = defaultCode,
    getOptionMetadata = defaultMetadata,
    isOptionExact = (option) => Boolean(option.exactMatch),
    requireExactMatchOnEnter = false,
    loadExactOption,
    autoFocus = false,
    exactNotFoundMessage = "Không tìm thấy mã khớp chính xác"
}) {
    const listboxId = useId();
    const inputRef = useRef(null);
    const activeRequestRef = useRef(null);
    const [query, setQuery] = useState(() => !multiple && value ? getOptionLabel(value) : "");
    const [options, setOptions] = useState([]);
    const [optionsQuery, setOptionsQuery] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [open, setOpen] = useState(false);
    const [highlightedIndex, setHighlightedIndex] = useState(0);
    const [retryKey, setRetryKey] = useState(0);
    const selectedOptions = useMemo(
        () => multiple ? (Array.isArray(value) ? value : []) : (value ? [value] : []),
        [multiple, value]
    );
    const trimmedQuery = query.trim();
    const canSearch = trimmedQuery.length >= minQueryLength;
    const queryMatchesSelection = !multiple
        && selectedOptions[0]
        && query === getOptionLabel(selectedOptions[0]);
    const searchEnabled = canSearch && !queryMatchesSelection;
    const waitingForQuery = searchEnabled && optionsQuery !== trimmedQuery;
    const visibleOptions = searchEnabled && !waitingForQuery ? options : [];

    useEffect(() => {
        if (!searchEnabled || disabled) return undefined;

        const controller = new AbortController();
        activeRequestRef.current?.abort();
        activeRequestRef.current = controller;
        const timer = window.setTimeout(async () => {
            if (controller.signal.aborted) return;
            setLoading(true);
            setError("");
            try {
                const result = await loadOptions(trimmedQuery, { signal: controller.signal });
                if (!controller.signal.aborted) {
                    setOptions(Array.isArray(result) ? result : []);
                    setOptionsQuery(trimmedQuery);
                    setHighlightedIndex(0);
                }
            } catch (requestError) {
                if (!controller.signal.aborted) {
                    setOptions([]);
                    setOptionsQuery(trimmedQuery);
                    setError(requestError?.message || "Không tải được kết quả");
                }
            } finally {
                if (!controller.signal.aborted) setLoading(false);
            }
        }, debounceMs);

        return () => {
            window.clearTimeout(timer);
            controller.abort();
        };
    }, [debounceMs, disabled, loadOptions, retryKey, searchEnabled, trimmedQuery]);

    function selectOption(option) {
        if (multiple) {
            const optionKey = getOptionKey(option);
            if (!selectedOptions.some((item) => getOptionKey(item) === optionKey)) {
                onChange([...selectedOptions, option]);
            }
            setQuery("");
            setOpen(true);
            inputRef.current?.focus();
            return;
        }

        onChange(option);
        setQuery(getOptionLabel(option));
        setOpen(false);
    }

    function removeOption(option) {
        const optionKey = getOptionKey(option);
        const next = selectedOptions.filter((item) => getOptionKey(item) !== optionKey);
        onChange(multiple ? next : null);
        if (!multiple) setQuery("");
    }

    async function runExactSearch() {
        const controller = new AbortController();
        activeRequestRef.current?.abort();
        activeRequestRef.current = controller;
        setLoading(true);
        setError("");
        try {
            const result = loadExactOption
                ? await loadExactOption(trimmedQuery, { signal: controller.signal })
                : await loadOptions(trimmedQuery, { signal: controller.signal });
            if (controller.signal.aborted) return;
            const nextOptions = loadExactOption
                ? (result ? [result] : [])
                : (Array.isArray(result) ? result : []);
            setOptions(nextOptions);
            setOptionsQuery(trimmedQuery);
            setHighlightedIndex(0);
            const exactOption = nextOptions.find((option) => isOptionExact(option, trimmedQuery));
            if (exactOption) {
                selectOption(exactOption);
            } else if (requireExactMatchOnEnter) {
                setError(exactNotFoundMessage);
                setOpen(true);
            } else if (nextOptions.length > 0) {
                selectOption(nextOptions[0]);
            }
        } catch (requestError) {
            if (!controller.signal.aborted) {
                setOptions([]);
                setOptionsQuery(trimmedQuery);
                setError(requestError?.message || "Không tải được kết quả");
            }
        } finally {
            if (!controller.signal.aborted) setLoading(false);
        }
    }

    function handleKeyDown(event) {
        if (event.key === "ArrowDown" && visibleOptions.length) {
            event.preventDefault();
            setOpen(true);
            setHighlightedIndex((current) => (current + 1) % visibleOptions.length);
        } else if (event.key === "ArrowUp" && visibleOptions.length) {
            event.preventDefault();
            setOpen(true);
            setHighlightedIndex((current) => (current - 1 + visibleOptions.length) % visibleOptions.length);
        } else if (event.key === "Enter" && open && visibleOptions.length) {
            event.preventDefault();
            const exactOption = visibleOptions.find((option) => isOptionExact(option, trimmedQuery));
            if (exactOption) {
                selectOption(exactOption);
            } else if (requireExactMatchOnEnter) {
                setError(exactNotFoundMessage);
            } else {
                selectOption(visibleOptions[highlightedIndex] || visibleOptions[0]);
            }
        } else if (event.key === "Enter" && open && searchEnabled) {
            event.preventDefault();
            runExactSearch();
        } else if (event.key === "Escape") {
            setOpen(false);
        } else if (event.key === "Backspace" && multiple && !query && selectedOptions.length) {
            removeOption(selectedOptions[selectedOptions.length - 1]);
        }
    }

    const showDropdown = open && !disabled && !queryMatchesSelection;

    return (
        <div className={`async-entity-picker${disabled ? " is-disabled" : ""}${showDropdown ? " is-open" : ""}`}>
            <div className="async-entity-picker-control">
                {multiple && selectedOptions.map((option) => (
                    <span className="async-entity-picker-chip" key={getOptionKey(option)}>
                        {getOptionLabel(option)}
                        <button type="button" onClick={() => removeOption(option)} aria-label={`Bỏ chọn ${getOptionLabel(option)}`} disabled={disabled}>
                            <X size={14} />
                        </button>
                    </span>
                ))}
                <input
                    ref={inputRef}
                    name={inputName}
                    value={query}
                    onChange={(event) => {
                        setQuery(event.target.value);
                        setError("");
                        if (!multiple && selectedOptions.length) onChange(null);
                        setOpen(true);
                    }}
                    onFocus={() => setOpen(true)}
                    onBlur={() => window.setTimeout(() => setOpen(false), 100)}
                    onKeyDown={handleKeyDown}
                    placeholder={selectedOptions.length ? "" : placeholder}
                    disabled={disabled}
                    autoFocus={autoFocus}
                    required={required && selectedOptions.length === 0}
                    role="combobox"
                    aria-label={ariaLabel || placeholder}
                    aria-autocomplete="list"
                    aria-expanded={showDropdown}
                    aria-controls={listboxId}
                    aria-activedescendant={showDropdown && visibleOptions[highlightedIndex]
                        ? `${listboxId}-${highlightedIndex}`
                        : undefined}
                />
                {!multiple && selectedOptions[0] && (
                    <button className="async-entity-picker-clear" type="button" onClick={() => removeOption(selectedOptions[0])} aria-label="Bỏ lựa chọn" disabled={disabled}>
                        <X size={16} />
                    </button>
                )}
            </div>

            {showDropdown && (
                <div className="async-entity-picker-menu" id={listboxId} role="listbox" aria-multiselectable={multiple || undefined}>
                    {!searchEnabled && <div className="async-entity-picker-state">Nhập ít nhất {minQueryLength} ký tự để tìm kiếm</div>}
                    {waitingForQuery && !loading && <div className="async-entity-picker-state" role="status">Đang tìm kiếm...</div>}
                    {loading && <div className="async-entity-picker-state" role="status">Đang tìm kiếm...</div>}
                    {!loading && !waitingForQuery && error && (
                        <div className="async-entity-picker-state async-entity-picker-error" role="alert">
                            <span>{error}</span>
                            <button type="button" onMouseDown={(event) => event.preventDefault()} onClick={() => setRetryKey((key) => key + 1)}>Thử lại</button>
                        </div>
                    )}
                    {!loading && !error && searchEnabled && !waitingForQuery && visibleOptions.length === 0 && (
                        <div className="async-entity-picker-state">Không tìm thấy kết quả phù hợp</div>
                    )}
                    {!loading && !error && visibleOptions.map((option, index) => {
                        const optionKey = getOptionKey(option);
                        const selected = selectedOptions.some((item) => getOptionKey(item) === optionKey);
                        return (
                            <button
                                id={`${listboxId}-${index}`}
                                className={`async-entity-picker-option${index === highlightedIndex ? " is-highlighted" : ""}`}
                                type="button"
                                role="option"
                                aria-selected={selected}
                                key={optionKey}
                                onMouseDown={(event) => event.preventDefault()}
                                onMouseEnter={() => setHighlightedIndex(index)}
                                onClick={() => selectOption(option)}
                            >
                                <span className="async-entity-picker-label">{getOptionLabel(option)}</span>
                                <span className="async-entity-picker-code">{getOptionCode(option)}</span>
                                {getOptionMetadata(option) && <span className="async-entity-picker-metadata">{getOptionMetadata(option)}</span>}
                            </button>
                        );
                    })}
                </div>
            )}
        </div>
    );
}
