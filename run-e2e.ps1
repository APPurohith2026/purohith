$ErrorActionPreference='Stop'
$Root=Split-Path -Parent $MyInvocation.MyCommand.Path
& (Join-Path $Root 'verify-project.ps1') -SkipBackendTests -SkipInstall
