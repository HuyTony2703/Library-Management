import RandomBookSection from "../../components/reader/RandomBookSection";

export default function ReaderRecommendationsPage() {
    return (
        <div>
            <div className="reader-home-header">
                <small>Random books</small>
                <h1>Gợi ý sách</h1>
                <p>Khám phá ngẫu nhiên các đầu sách đang hoạt động trong thư viện.</p>
            </div>

            <RandomBookSection limit={6} />
        </div>
    );
}
