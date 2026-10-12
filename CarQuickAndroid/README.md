## GitHub 雲端編譯（不用安裝 Android Studio）
1. 在 GitHub 建立新 repository，例如 car-quick-android。可設 Private，勾選 Add a README。
2. 解壓縮本 ZIP。進入 CarQuickAndroid，把「裡面的所有檔案與資料夾」上傳到 repository 根目錄。不要直接上傳 ZIP，也不要再包一層 CarQuickAndroid。
3. 確認根目錄有 settings.gradle、build.gradle、app；確認 .github/workflows/build-apk.yml 也已上傳。
4. 如果 Windows 沒有顯示 .github，開啟檔案總管的顯示隱藏項目；或在 GitHub 用 Add file → Create new file，檔名輸入 .github/workflows/build-apk.yml，貼上本專案同名檔案內容。
5. Commit changes 後進入 Actions → Build Android APK。首次上傳會自動執行，也可按 Run workflow。
6. 出現綠色勾勾後，開啟該次執行，向下找 Artifacts → car-quick-android-test-apk。
7. 下載並解壓縮，取出 app-debug.apk，傳到 Android 手機安裝。

目前只是已準備的 workflow，尚未在你的 GitHub 執行成功。若出現紅色叉叉，傳 Build debug APK 步驟錯誤給我。
GitHub Actions 依帳號配額計算；此流程不要求 Apple 或 Google Play 開發者帳號。
不同 GitHub runner 可能產生不同 debug 簽署金鑰；安裝新版遇到簽署不符時，需先移除舊版（會清除設定）。穩定版再設定固定金鑰。

---

# Android 車訊快捷助手 0.1 原始碼測試版

Windows 可編譯，不需付費開發者帳號。此 ZIP 不是 APK。
目前環境沒有 Android SDK / Gradle，尚未編譯，也未在 Android LINE 實機測試。

## 已實作
- 自訂車訊；快捷鍵盤以每行「分類|車訊」設定，點選輸入、刪除、傳送、切换其他鍵盤。
- 無障礙懸浮鈕：車訊、收回、拖曳。
- 車訊功能採 A：讀取最後一則文字訊息，包含自己發出的，加上換行和設定車訊。
- 初始採預覽後傳送；確認實機正確後，可在 App 開啟「一鍵直接傳送」。
- 收回鈕：長按自己的訊息，尋找 LINE 的「收回」選項；不使用「刪除」。
- 無網路權限、不保存對話內容。只在按下按鈕時擷取文字，無障礙事件只用來控制懸浮鈕顯示。

## Windows 編譯與安裝
1. 安裝 Android Studio。首次啟動安裝 Android SDK，在 SDK Manager 安裝 API 35、Build Tools 及接受授權。
2. 解壓縮後，對資料夾內空白處按右鍵開啟 PowerShell。
3. 執行 `powershell -ExecutionPolicy Bypass -File .\build-windows.ps1`。指令僅對本次程序放行腳本。
4. 腳本下載 Gradle 8.9 並核對 SHA256，編譯產生 `app\build\outputs\apk\debug\app-debug.apk`。
5. 将 APK 傳到 Android 手機，允許该來源安裝 App，安裝測試。
6. Debug APK 由 Gradle 自動用本機 debug key 簽署；不用购买開發者帳號。換電腦或換簽署可能需要先移除舊版。

## 初次操作
1. 在 App 設定自己的車訊，儲存。保留「一鍵直接傳送」關閉，先驗證。
2. 開啟無障礙設定，啟用 LINE 車訊懸浮助手。側載 App 若被 Android 限制，需先在系統的 App 資訊按裝置提示允許受限制的設定。
3. 開啟自己的 LINE 測試聊天室，滑到最底部，輸入框保持空白。
4. 長按「車訊」懸浮鈕：從清單選擇真正的訊息文字，完成 LINE 版本欄位校正，不選人名、時間、輸入框或標題。
5. 點車訊，檢查預覽是否為最後一則完整訊息加車訊。確認後傳送。
6. 確認自己的訊息位於右側後，才在 App 啟用右側辨識設定。點收回做測試。
7. 啟用快捷鍵盤需另外到系統鍵盤設定啟用，再選擇車訊快捷鍵盤。

## 重要實機限制／尚未完成的相容性
LINE 的 UI 結構、訊息 ID 及選單依版本與語言改變，本版不是已驗證可用的 APK。
本版以手動校正的訊息 ID、清單末列與畫面位置辨識，不是 LINE 官方訊息 API。
如果訊息欄位沒有 resource ID、畫面無可辨識的 RecyclerView/ListView、無法确认已滑到底或传送按鈕不是支援的文字，會停止，需要依實機調整。

擷取僅支援最後一列可辨識的文字訊息；最後一則是圖片、貼圖、語音、系統通知或畫面未到底時，會停止，不能聲稱取得真正最後訊息。
收回会从当前可见訊息選擇最靠下的右側文字訊息；後續每一列必須可確認為別人的文字訊息，否則停止。自己的文字位置需在右半邊，長文字横跨中線可能不支援。自己的最新訊息不在畫面內、或後方有圖片／貼圖／系統列時，本版會停止，不能保证完整辨識。
LINE 不提供收回、訊息超過其限制、需付費權限、或確認視窗無法辨識時不會自動繞過。可能需要手動完成 LINE 確認視窗。
只能報告已操作按鈕，不能確認 LINE 伺服器是否完成發送／收回。

## 必測
- 最後一則別人／自己發出的文字，長文字及換行。
- 最後一則圖片／貼圖，應停止，不取前一則文字。
- 自己最新文字位於末列或後方有別人文字时收回；後方有無法辨識的非文字列時應停止。
- 已有草稿不得覆蓋；校正錯誤、更新 LINE、滑到歷史訊息、快速連點。
- LINE 無收回選項／確認視窗，不能誤點刪除。
- 不在 LINE 时隱藏、關閉服務后移除、重新啟動。

技術文件：https://developer.android.com/guide/topics/ui/accessibility/service
