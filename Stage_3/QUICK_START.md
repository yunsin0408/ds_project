# Quick Start Guide

## 快速開始指南

### 第一步：設置 Python 環境

```bash
./setup_python.sh
```

For Windows in git bash(啟動虛擬環境，若尚未創建虛擬環境請參考`README.md`):
```bash
source .venv/Scripts/activate
```

這會創建 `.venv/` 虛擬環境並安裝 `youtube-transcript-api`。

### 第二步：編譯並運行

```bash
./compile_and_run.sh
```

或者手動執行：

```bash
# 編譯
javac *.java

# 運行
java Main
```

### 測試 YouTube 功能

單獨測試 YouTube 字幕抓取：

```bash
# 使用 Java
javac YouTubeTranscriptFetcher.java
java YouTubeTranscriptFetcher

# 或直接使用 Python
source .venv/bin/activate
python fetch_youtube_transcript.py "a4cyMAIyWIQ"
deactivate
```

### 修改分析內容

編輯 `Main.java`：

```java
// 修改 URL 列表
urls.add("https://www.example.com");
urls.add("https://youtu.be/VIDEO_ID");

// 修改關鍵詞
keywords.add("NewKeyword");

// 修改權重
int score = (keyword1Count * weight1) + 
            (keyword2Count * weight2) + ...
```

### 常見問題

**Q: YouTube 顯示 "IP blocked"？**  
A: YouTube 暫時封鎖了過多的請求。等待 5-10 分鐘後重試。

**Q: Python 環境問題？**  
A: 刪除 `.venv/` 並重新運行 `./setup_python.sh`

**Q: Java 編譯錯誤？**  
A: 確保安裝了 JDK 8 或以上版本：
```bash
java -version
javac -version
```

### 系統需求

- Java JDK 8+
- Python 3.8+
- 網路連接（用於抓取網頁和 YouTube 字幕）

### 輸出格式

程序會輸出排名結果，格式如下：

```
====================================================
                Ranking Result
====================================================
# 排名 | URL | SCORE = 總分 | Counts = {關鍵詞: 次數, ...}
```

分數計算方式：
- ISO: 權重 4
- Standard: 權重 3
- Sustain: 權重 2
- Certificate: 權重 1

### 技術架構

```
Java 主程序
    ↓
WebAnalyzer (檢測 URL 類型)
    ↓
    ├─→ HTMLFetcher → TextPreprocessor → WordCounter (網頁)
    └─→ YouTubeTranscriptFetcher → Python 腳本 → WordCounter (YouTube)
```


