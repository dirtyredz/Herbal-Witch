[CmdletBinding()]
param([Parameter(Mandatory)][string]$WorkspaceRoot,[string]$Ref='HEAD')
$ErrorActionPreference='Stop'
$worktree=(Resolve-Path (Join-Path $PSScriptRoot '../../../..')).Path
& (Join-Path $WorkspaceRoot 'tools/anu-test.ps1') -Mod HerbalWitch -Ref $Ref -Path $worktree -Tasks 'magicToolsRestartTest' -Wait
exit $LASTEXITCODE
