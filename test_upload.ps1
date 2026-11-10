Add-Type -AssemblyName System.Net.Http

$cookies = New-Object System.Net.CookieContainer
$handler = New-Object System.Net.Http.HttpClientHandler -Property @{
    CookieContainer = $cookies
    AllowAutoRedirect = $true
    MaxAutomaticRedirections = 10
}
$client = New-Object System.Net.Http.HttpClient -ArgumentList $handler

# Step 1: Register
Write-Host "=== Step 1: Register ==="
$regDict = New-Object 'System.Collections.Generic.Dictionary[string,string]'
$regDict.Add("username", "uploadtest2")
$regDict.Add("password", "123456")
$regDict.Add("nickname", "UploadTester2")
$regContent = New-Object System.Net.Http.FormUrlEncodedContent -ArgumentList @(,$regDict.GetEnumerator())
$regResp = $client.PostAsync("http://localhost:8080/book-floating/register", $regContent).Result
Write-Host "Register: $($regResp.StatusCode)"

# Step 2: Login
Write-Host ""
Write-Host "=== Step 2: Login ==="
$loginDict = New-Object 'System.Collections.Generic.Dictionary[string,string]'
$loginDict.Add("username", "uploadtest2")
$loginDict.Add("password", "123456")
$loginContent = New-Object System.Net.Http.FormUrlEncodedContent -ArgumentList @(,$loginDict.GetEnumerator())
$loginResp = $client.PostAsync("http://localhost:8080/book-floating/login", $loginContent).Result
Write-Host "Login: $($loginResp.StatusCode) -> $($loginResp.RequestMessage.RequestUri)"

# Step 3: Verify login by accessing personal.jsp
Write-Host ""
Write-Host "=== Step 3: Verify login session ==="
$personalResp = $client.GetAsync("http://localhost:8080/book-floating/personal.jsp").Result
$personalBody = $personalResp.Content.ReadAsStringAsync().Result
if ($personalBody -match 'UploadTester2') {
    Write-Host "PASS: Session OK, showing nickname UploadTester2"
} else {
    Write-Host "CHECK: personal.jsp loaded"
}

# Step 4: Upload avatar
Write-Host ""
Write-Host "=== Step 4: Upload avatar ==="
$multipart = New-Object System.Net.Http.MultipartFormDataContent
$fileBytes = [System.IO.File]::ReadAllBytes("C:\Users\HW\Desktop\图书漂流\test_avatar.png")
$fileContent = New-Object System.Net.Http.ByteArrayContent -ArgumentList @(,$fileBytes)
$fileContent.Headers.ContentType = [System.Net.Http.Headers.MediaTypeHeaderValue]::new("image/png")
$multipart.Add($fileContent, "avatarFile", "test_avatar.png")
$uploadResp = $client.PostAsync("http://localhost:8080/book-floating/uploadAvatar", $multipart).Result
Write-Host "Upload: $($uploadResp.StatusCode) -> $($uploadResp.RequestMessage.RequestUri)"
$uploadBody = $uploadResp.Content.ReadAsStringAsync().Result

if ($uploadBody -match '当前头像') {
    Write-Host "PASS: Avatar uploaded and displayed!"
    if ($uploadBody -match 'src="([^"]*static/avatar/[^"]*)"') {
        Write-Host "Avatar path: $($Matches[1])"
    }
} elseif ($uploadBody -match '请选择图片') {
    Write-Host "FAIL: Upload said 'please select image'"
} elseif ($uploadResp.RequestMessage.RequestUri -match 'login') {
    Write-Host "FAIL: Redirected to login (session lost)"
} else {
    Write-Host "Response (first 300 chars): $($uploadBody.Substring(0, [Math]::Min(300, $uploadBody.Length)))"
}

$client.Dispose()
