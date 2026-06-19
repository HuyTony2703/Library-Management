export default function PageHeader({ eyebrow, title, description, right, className = "" }) {
    return (
        <div className={`page-header${className ? ` ${className}` : ""}`}>
            <div>
                {eyebrow && <div className="eyebrow">{eyebrow}</div>}
                <h1>{title}</h1>
                {description && <p>{description}</p>}
            </div>

            {right && <div className="page-header-actions">{right}</div>}
        </div>
    );
}
