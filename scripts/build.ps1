$ErrorActionPreference = 'Stop'
Push-Location (Split-Path $PSScriptRoot -Parent)
try {
    & mvn test
    if ($LASTEXITCODE -ne 0) { throw 'Maven tests failed.' }
    $jdkJar = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin/jar.exe' } else { 'jar' }
    $jarTemp = (Resolve-Path target).Path
    & $jdkJar "-J-Djava.io.tmpdir=$jarTemp" --create --file target/hotel-management-1.0.jar --main-class hotel.Main -C target/classes .
    if ($LASTEXITCODE -ne 0) { throw 'JAR creation failed.' }
    Write-Output 'Built target/hotel-management-1.0.jar'
} finally {
    Pop-Location
}
