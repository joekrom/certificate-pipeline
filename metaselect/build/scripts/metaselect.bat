@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  metaselect startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Add default JVM options here. You can also use JAVA_OPTS and METASELECT_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:init
@rem Get command-line arguments, handling Windows variants

if not "%OS%" == "Windows_NT" goto win9xME_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\metaselect-1.0-SNAPSHOT.jar;%APP_HOME%\lib\converterservices-0.13.2.jar;%APP_HOME%\lib\gson-2.8.7.jar;%APP_HOME%\lib\spark-core-2.8.0.jar;%APP_HOME%\lib\logback-classic-1.2.3.jar;%APP_HOME%\lib\slf4j-api-1.7.25.jar;%APP_HOME%\lib\logback-core-1.2.3.jar;%APP_HOME%\lib\Saxon-HE-9.9.1-1.jar;%APP_HOME%\lib\fop-2.3.jar;%APP_HOME%\lib\batik-transcoder-1.10.jar;%APP_HOME%\lib\batik-extension-1.10.jar;%APP_HOME%\lib\batik-bridge-1.10.jar;%APP_HOME%\lib\batik-script-1.10.jar;%APP_HOME%\lib\batik-anim-1.10.jar;%APP_HOME%\lib\batik-svg-dom-1.10.jar;%APP_HOME%\lib\batik-gvt-1.10.jar;%APP_HOME%\lib\batik-parser-1.10.jar;%APP_HOME%\lib\batik-svggen-1.10.jar;%APP_HOME%\lib\batik-awt-util-1.10.jar;%APP_HOME%\lib\batik-dom-1.10.jar;%APP_HOME%\lib\batik-css-1.10.jar;%APP_HOME%\lib\xmlgraphics-commons-2.3.jar;%APP_HOME%\lib\pdfbox-tools-2.0.15.jar;%APP_HOME%\lib\pdfbox-debugger-2.0.15.jar;%APP_HOME%\lib\pdfbox-2.0.15.jar;%APP_HOME%\lib\commons-fileupload-1.4.jar;%APP_HOME%\lib\httpclient-4.5.8.jar;%APP_HOME%\lib\poi-ooxml-4.0.1.jar;%APP_HOME%\lib\poi-4.0.1.jar;%APP_HOME%\lib\commons-codec-1.11.jar;%APP_HOME%\lib\commons-net-3.6.jar;%APP_HOME%\lib\commons-io-2.6.jar;%APP_HOME%\lib\jsch-0.1.54.jar;%APP_HOME%\lib\json-20171018.jar;%APP_HOME%\lib\commons-email-1.5.jar;%APP_HOME%\lib\jetty-webapp-9.4.12.v20180830.jar;%APP_HOME%\lib\websocket-server-9.4.12.v20180830.jar;%APP_HOME%\lib\jetty-servlet-9.4.12.v20180830.jar;%APP_HOME%\lib\jetty-security-9.4.12.v20180830.jar;%APP_HOME%\lib\jetty-server-9.4.12.v20180830.jar;%APP_HOME%\lib\websocket-servlet-9.4.12.v20180830.jar;%APP_HOME%\lib\fontbox-2.0.15.jar;%APP_HOME%\lib\commons-logging-1.2.jar;%APP_HOME%\lib\batik-ext-1.10.jar;%APP_HOME%\lib\avalon-framework-impl-4.3.1.jar;%APP_HOME%\lib\avalon-framework-api-4.3.1.jar;%APP_HOME%\lib\poi-ooxml-schemas-4.0.1.jar;%APP_HOME%\lib\commons-compress-1.18.jar;%APP_HOME%\lib\curvesapi-1.05.jar;%APP_HOME%\lib\httpcore-4.4.11.jar;%APP_HOME%\lib\javax.mail-1.5.6.jar;%APP_HOME%\lib\javax.servlet-api-3.1.0.jar;%APP_HOME%\lib\websocket-client-9.4.12.v20180830.jar;%APP_HOME%\lib\jetty-client-9.4.12.v20180830.jar;%APP_HOME%\lib\jetty-http-9.4.12.v20180830.jar;%APP_HOME%\lib\websocket-common-9.4.12.v20180830.jar;%APP_HOME%\lib\jetty-io-9.4.12.v20180830.jar;%APP_HOME%\lib\jetty-xml-9.4.12.v20180830.jar;%APP_HOME%\lib\websocket-api-9.4.12.v20180830.jar;%APP_HOME%\lib\batik-xml-1.10.jar;%APP_HOME%\lib\batik-util-1.10.jar;%APP_HOME%\lib\xml-apis-ext-1.3.04.jar;%APP_HOME%\lib\commons-collections4-4.2.jar;%APP_HOME%\lib\commons-math3-3.6.1.jar;%APP_HOME%\lib\xmlbeans-3.0.2.jar;%APP_HOME%\lib\activation-1.1.jar;%APP_HOME%\lib\jetty-util-9.4.12.v20180830.jar;%APP_HOME%\lib\xalan-2.7.2.jar;%APP_HOME%\lib\serializer-2.7.2.jar;%APP_HOME%\lib\xml-apis-1.3.04.jar;%APP_HOME%\lib\batik-constants-1.10.jar;%APP_HOME%\lib\batik-i18n-1.10.jar

@rem Execute metaselect
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %METASELECT_OPTS%  -classpath "%CLASSPATH%" de.axxepta.metaselect.Certificate %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable METASELECT_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%METASELECT_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
