$files = Get-ChildItem -Path "c:/Users/yasaswini kuchi/Downloads/SymtoTrack_Source/app_frontend/app/src/main/res/layout" -Filter *.xml -Recurse
foreach ($file in $files) {
    $path = $file.FullName
    $content = Get-Content -Raw $path
    $content = $content -replace '#16A34A', '@color/green_600'
    $content = $content -replace '#0EA5E9', '@color/sky_500'
    $content = $content -replace '#B91C1C', '@color/red_700'
    Set-Content -Path $path -Value $content
}
Write-Host "Final replacement completed."
