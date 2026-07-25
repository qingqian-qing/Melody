#!/bin/sh
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar
exec java -Xmx768m -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
