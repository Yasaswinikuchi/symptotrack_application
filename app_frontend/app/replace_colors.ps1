Get-ChildItem -Path 'c:/Users/yasaswini kuchi/Downloads/SymtoTrack_Source/app_frontend/app/src/main/res/layout' -Filter *.xml -Recurse |
ForEach-Object {
    $file = $_.FullName
    (Get-Content -Raw $file) -replace '#64748B','@color/text_secondary' |
    ForEach-Object { $_ -replace '#0F172A','@color/text_primary_dark' } |
    ForEach-Object { $_ -replace '#1E293B','@color/divider_gray' } |
    ForEach-Object { $_ -replace '#FFFFFF','@color/card_white' } |
    ForEach-Object { $_ -replace '#EFF6FF','@color/background_light_alt' } |
    ForEach-Object { $_ -replace '#FEF2F2','@color/background_error' } |
    ForEach-Object { $_ -replace '#DC2626','@color/error_red_alt' } |
    ForEach-Object { $_ -replace '#F59E0B','@color/accent_orange_alt' } |
    ForEach-Object { $_ -replace '#8B5CF6','@color/accent_purple' } |
    ForEach-Object { $_ -replace '#34D399','@color/accent_green_alt' } |
    ForEach-Object { $_ -replace '#38BDF8','@color/accent_cyan' } |
    ForEach-Object { $_ -replace '#A78BFA','@color/accent_purple_light' } |
    ForEach-Object { $_ -replace '#2E6CEB','@color/brand_blue' } |
    ForEach-Object { $_ -replace '#F4F7F9','@color/background_light' } |
    ForEach-Object { $_ -replace '#F9FAFB','@color/input_bg' } |
    ForEach-Object { $_ -replace '#22C55E','@color/accent_green' } |
    ForEach-Object { $_ -replace '#F97316','@color/accent_orange' } |
    ForEach-Object { $_ -replace '#EF4444','@color/error_red' } |
    ForEach-Object { $_ -replace '#6B7280','@color/text_secondary' } |
    ForEach-Object { $_ -replace '#9CA3AF','@color/text_hint' } |
    ForEach-Object { $_ -replace '#E5E7EB','@color/border_gray' } |
    ForEach-Object { $_ -replace '#E2E8F0','@color/border_light' } |
    ForEach-Object { $_ -replace '#111827','@color/background_dark' } | Set-Content $file
}
