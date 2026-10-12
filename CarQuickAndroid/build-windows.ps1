$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$gradleVersion = '8.9'
$javaPath = "$env:ProgramFiles\Android\Android Studio\jbr"
if (-not $env:JAVA_HOME -and (Test-Path $javaPath)) { $env:JAVA_HOME = $javaPath }
if (-not $env:JAVA_HOME) { throw '請安裝 Android Studio，並設定 JAVA_HOME 指向 JDK 17 或 Android Studio 的 jbr。' }
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
if (-not $env:ANDROID_HOME) { $env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk" }
if (-not (Test-Path $env:ANDROID_HOME)) { throw '請先在 Android Studio 安裝 Android SDK。' }
$folder = Join-Path $PSScriptRoot '.build-tools'
New-Item -ItemType Directory -Force $folder | Out-Null
$gradle = Join-Path $folder "gradle-$gradleVersion\bin\gradle.bat"
if (-not (Test-Path $gradle)) {
 $url = "https://services.gradle.org/distributions/gradle-$gradleVersion-bin.zip"
 $zip = Join-Path $folder 'gradle.zip'
 Invoke-WebRequest $url -OutFile $zip
 $expected = (Invoke-RestMethod "$url.sha256").Trim()
 if ((Get-FileHash $zip -Algorithm SHA256).Hash.ToLower() -ne $expected.ToLower()) { throw 'Gradle 校驗失敗' }
 Expand-Archive $zip -DestinationPath $folder -Force
}
& $gradle --no-daemon assembleDebug
if ($LASTEXITCODE -ne 0) { throw '編譯失敗，請保留錯誤訊息。' }
Write-Host 'APK 已產生：app\build\outputs\apk\debug\app-debug.apk'
