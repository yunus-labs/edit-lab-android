param(
    [string]$Sdk = $env:ANDROID_HOME,
    [string]$BuildTools = '35.0.0',
    [string]$SigningKey = '',
    [string]$KeyAlias = 'edit-lab'
)
$ErrorActionPreference = 'Stop'
function Invoke-BuildTool($Program, [string[]]$ToolArgs) {
    & $Program @ToolArgs
    if ($LASTEXITCODE -ne 0) { throw "Build tool failed: $Program" }
}
$tools = Join-Path $Sdk "build-tools/$BuildTools"
$androidJar = Join-Path $Sdk 'platforms/android-36/android.jar'
$src = Join-Path $PSScriptRoot 'app/src/main'
$build = Join-Path $PSScriptRoot 'build'
$classes = Join-Path $build 'classes'
New-Item -ItemType Directory -Force -Path $build,$classes | Out-Null

Invoke-BuildTool "$tools/aapt2.exe" @('compile','--dir',"$src/res",'-o',"$build/res.zip")
Invoke-BuildTool "$tools/aapt2.exe" @('link','-o',"$build/resources.apk",'-I',$androidJar,'--manifest',"$src/AndroidManifest.xml",'-A',"$src/assets","$build/res.zip")
$java = Join-Path $env:JAVA_HOME 'bin/javac.exe'
$jar = Join-Path $env:JAVA_HOME 'bin/jar.exe'
Invoke-BuildTool $java @('--release','8','-encoding','UTF-8','-classpath',$androidJar,'-d',$classes,"$src/java/com/yunuskhanlabs/editlab/MainActivity.java")
Invoke-BuildTool $jar @('cf',"$build/classes.jar",'-C',$classes,'.')
Invoke-BuildTool "$tools/d8.bat" @('--release','--min-api','23','--lib',$androidJar,'--output',$build,"$build/classes.jar")
Push-Location $build
try { Invoke-BuildTool $jar @('uf','resources.apk','classes.dex') } finally { Pop-Location }
Invoke-BuildTool "$tools/zipalign.exe" @('-f','-p','4',"$build/resources.apk","$build/Edit-Lab-unsigned.apk")

if ($SigningKey) {
    $apk = "$build/Edit-Lab.apk"
    $sign = @('sign','--ks',$SigningKey,'--ks-key-alias',$KeyAlias,'--out',$apk)
    if ($env:EDIT_LAB_KEY_PASSWORD) { $sign += @('--ks-pass','env:EDIT_LAB_KEY_PASSWORD') }
    Invoke-BuildTool "$tools/apksigner.bat" ($sign + "$build/Edit-Lab-unsigned.apk")
    Invoke-BuildTool "$tools/apksigner.bat" @('verify','--verbose',$apk)
    Invoke-BuildTool "$tools/zipalign.exe" @('-c','-p','4',$apk)
}
