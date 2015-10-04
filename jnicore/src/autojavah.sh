#!/bin/sh
export ProjectPath=$(cd "../../$(dirname "$1")"; pwd)
echo $ProjectPath

export TargetClassName="com.inno.sdk.core.AppSecurity"

export SourceFile="${ProjectPath}/app/src/main/java"
export TargetPath="${ProjectPath}/jnicore/src/main/jni"

cd "${SourceFile}"
javah -d ${TargetPath} -classpath "${SourceFile}" "${TargetClassName}"
echo -d ${TargetPath} -classpath "${SourceFile}" "${TargetClassName}"
