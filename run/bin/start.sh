#!/usr/bin/env bash

###  ------------------------------- ###
###  Helper methods for BASH scripts ###
###  ------------------------------- ###

die() {
  echo "$@" 1>&2
  exit 1
}

realpath () {
(
  TARGET_FILE="$1"
  CHECK_CYGWIN="$2"

  cd "$(dirname "$TARGET_FILE")"
  TARGET_FILE=$(basename "$TARGET_FILE")

  COUNT=0
  while [ -L "$TARGET_FILE" -a $COUNT -lt 100 ]
  do
      TARGET_FILE=$(readlink "$TARGET_FILE")
      cd "$(dirname "$TARGET_FILE")"
      TARGET_FILE=$(basename "$TARGET_FILE")
      COUNT=$(($COUNT + 1))
  done

  if [ "$TARGET_FILE" == "." -o "$TARGET_FILE" == ".." ]; then
    cd "$TARGET_FILE"
    TARGET_FILEPATH=
  else
    TARGET_FILEPATH=/$TARGET_FILE
  fi

  # make sure we grab the actual windows path, instead of cygwin's path.
  if [[ "x$CHECK_CYGWIN" == "x" ]]; then
    echo "$(pwd -P)/$TARGET_FILE"
  else
    echo $(cygwinpath "$(pwd -P)/$TARGET_FILE")
  fi
)
}

# TODO - Do we need to detect msys?

# Uses uname to detect if we're in the odd cygwin environment.
is_cygwin() {
  local os=$(uname -s)
  case "$os" in
    CYGWIN*) return 0 ;;
    *)  return 1 ;;
  esac
}

# This can fix cygwin style /cygdrive paths so we get the
# windows style paths.
cygwinpath() {
  local file="$1"
  if is_cygwin; then
    echo $(cygpath -w $file)
  else
    echo $file
  fi
}

# Make something URI friendly
make_url() {
  url="$1"
  local nospaces=${url// /%20}
  if is_cygwin; then
    echo "/${nospaces//\\//}"
  else
    echo "$nospaces"
  fi
}

# This crazy function reads in a vanilla "linux" classpath string (only : are separators, and all /),
# and returns a classpath with windows style paths, and ; separators.
fixCygwinClasspath() {
  OLDIFS=$IFS
  IFS=":"
  read -a classpath_members <<< "$1"
  declare -a fixed_members
  IFS=$OLDIFS
  for i in "${!classpath_members[@]}"
  do
    fixed_members[i]=$(realpath "${classpath_members[i]}" "fix")
  done
  IFS=";"
  echo "${fixed_members[*]}"
  IFS=$OLDIFS
}

# Fix the classpath we use for cygwin.
fix_classpath() {
  cp="$1"
  if is_cygwin; then
    echo "$(fixCygwinClasspath "$cp")"
  else
    echo "$cp"
  fi
}
# Detect if we should use JAVA_HOME or just try PATH.
get_java_cmd() {
  if [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
    echo "$JAVA_HOME/bin/java"
  else
    echo "java"
  fi
}

echoerr () {
  echo 1>&2 "$@"
}
vlog () {
  [[ $verbose || $debug ]] && echoerr "$@"
}
dlog () {
  [[ $debug ]] && echoerr "$@"
}
execRunner () {
  # print the arguments one to a line, quoting any containing spaces
  [[ $verbose || $debug ]] && echo "# Executing command line:" && {
    for arg; do
      if printf "%s\n" "$arg" | grep -q ' '; then
        printf "\"%s\"\n" "$arg"
      else
        printf "%s\n" "$arg"
      fi
    done
    echo ""
  }

  # we use "exec" here for our pids to be accurate.
  exec "$@"
}
addJava () {
  dlog "[addJava] arg = '$1'"
  java_args+=( "$1" )
}
addApp () {
  dlog "[addApp] arg = '$1'"
  app_commands+=( "$1" )
}
addResidual () {
  dlog "[residual] arg = '$1'"
  residual_args+=( "$1" )
}
addDebugger () {
  addJava "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=$1"
}

require_arg () {
  local type="$1"
  local opt="$2"
  local arg="$3"
  if [[ -z "$arg" ]] || [[ "${arg:0:1}" == "-" ]]; then
    die "$opt requires <$type> argument"
  fi
}
is_function_defined() {
  declare -f "$1" > /dev/null
}

# Attempt to detect if the script is running via a GUI or not
# TODO - Determine where/how we use this generically
detect_terminal_for_ui() {
  [[ ! -t 0 ]] && [[ "${#residual_args}" == "0" ]] && {
    echo "true"
  }
  # SPECIAL TEST FOR MAC
  [[ "$(uname)" == "Darwin" ]] && [[ "$HOME" == "$PWD" ]] && [[ "${#residual_args}" == "0" ]] && {
    echo "true"
  }
}

# Processes incoming arguments and places them in appropriate global variables.  called by the run method.
process_args () {
  local no_more_snp_opts=0
  while [[ $# -gt 0 ]]; do
    case "$1" in
             --) shift && no_more_snp_opts=1 && break ;;
       -h|-help) usage; exit 1 ;;
    -v|-verbose) verbose=1 && shift ;;
      -d|-debug) debug=1 && shift ;;

    -no-version-check) no_version_check=1 && shift ;;

           -mem) echo "!! WARNING !! -mem option is ignored. Please use -J-Xmx and -J-Xms" && shift 2 ;;
     -jvm-debug) require_arg port "$1" "$2" && addDebugger $2 && shift 2 ;;

          -main) custom_mainclass="$2" && shift 2 ;;

     -java-home) require_arg path "$1" "$2" && jre=`eval echo $2` && java_cmd="$jre/bin/java" && shift 2 ;;

 -D*|-agentlib*) addJava "$1" && shift ;;
            -J*) addJava "${1:2}" && shift ;;
              *) addResidual "$1" && shift ;;
    esac
  done

  if [[ no_more_snp_opts ]]; then
    while [[ $# -gt 0 ]]; do
      addResidual "$1" && shift
    done
  fi

  is_function_defined process_my_args && {
    myargs=("${residual_args[@]}")
    residual_args=()
    process_my_args "${myargs[@]}"
  }
}

# Actually runs the script.
run() {
  # TODO - check for sane environment

  # process the combined args, then reset "$@" to the residuals
  process_args "$@"
  set -- "${residual_args[@]}"
  argumentCount=$#

  #check for jline terminal fixes on cygwin
  if is_cygwin; then
    stty -icanon min 1 -echo > /dev/null 2>&1
    addJava "-Djline.terminal=jline.UnixTerminal"
    addJava "-Dsbt.cygwin=true"
  fi

  # check java version
  if [[ ! $no_version_check ]]; then
    java_version_check
  fi

  if [ -n "$custom_mainclass" ]; then
    mainclass=("$custom_mainclass")
  else
    mainclass=("${app_mainclass[@]}")
  fi

  # Now we check to see if there are any java opts on the environment. These get listed first, with the script able to override them.
  if [[ "$JAVA_OPTS" != "" ]]; then
    java_opts="${JAVA_OPTS}"
  fi

  # run sbt
  execRunner "$java_cmd" \
    ${java_opts[@]} \
    "${java_args[@]}" \
    -cp "$(fix_classpath "$app_classpath")" \
    "${mainclass[@]}" \
    "${app_commands[@]}" \
    "${residual_args[@]}"

  local exit_code=$?
  if is_cygwin; then
    stty icanon echo > /dev/null 2>&1
  fi
  exit $exit_code
}

# Loads a configuration file full of default command line options for this script.
loadConfigFile() {
  cat "$1" | sed '/^\#/d'
}

# Now check to see if it's a good enough version
# TODO - Check to see if we have a configured default java version, otherwise use 1.6
java_version_check() {
  readonly java_version=$("$java_cmd" -version 2>&1 | awk -F '"' '/version/ {print $2}')
  if [[ "$java_version" == "" ]]; then
    echo
    echo No java installations was detected.
    echo Please go to http://www.java.com/getjava/ and download
    echo
    exit 1
  elif [[ ! "$java_version" > "1.6" ]]; then
    echo
    echo The java installation you have is not up to date
    echo $app_name requires at least version 1.6+, you have
    echo version $java_version
    echo
    echo Please go to http://www.java.com/getjava/ and download
    echo a valid Java Runtime and install before running $app_name.
    echo
    exit 1
  fi
}

###  ------------------------------- ###
###  Start of customized settings    ###
###  ------------------------------- ###
usage() {
 cat <<EOM
Usage: $script_name [options]

  -h | -help         print this message
  -v | -verbose      this runner is chattier
  -d | -debug        set sbt log level to debug
  -no-version-check  Don't run the java version check.
  -main <classname>  Define a custom main class
  -jvm-debug <port>  Turn on JVM debugging, open at the given port.

  # java version (default: java from PATH, currently $(java -version 2>&1 | grep version))
  -java-home <path>         alternate JAVA_HOME

  # jvm options and output control
  JAVA_OPTS          environment variable, if unset uses "$java_opts"
  -Dkey=val          pass -Dkey=val directly to the java runtime
  -J-X               pass option -X directly to the java runtime
                     (-J is stripped)

  # special option
  --                 To stop parsing built-in commands from the rest of the command-line.
                     e.g.) enabling debug and sending -d as app argument
                     \$ ./start-script -d -- -d

In the case of duplicated or conflicting options, basically the order above
shows precedence: JAVA_OPTS lowest, command line options highest except "--".
EOM
}

###  ------------------------------- ###
###  Main script                     ###
###  ------------------------------- ###

declare -a residual_args
declare -a java_args
declare -a app_commands
declare -r real_script_path="$(realpath "$0")"
declare -r app_home="$(realpath "$(dirname "$real_script_path")")"
# TODO - Check whether this is ok in cygwin...
declare -r lib_dir="$(realpath "${app_home}/../lib")"
declare -a app_mainclass=("com.example.ParkingS.Application")

declare -r script_conf_file="${app_home}/../conf/application.ini"
declare -r app_classpath="$lib_dir/default.parkings-0.1.jar:$lib_dir/org.scala-lang.scala-library-2.12.7.jar:$lib_dir/org.springframework.boot.spring-boot-starter-web-1.5.17.RELEASE.jar:$lib_dir/org.springframework.boot.spring-boot-starter-1.5.17.RELEASE.jar:$lib_dir/org.springframework.boot.spring-boot-1.5.17.RELEASE.jar:$lib_dir/org.springframework.spring-core-4.3.20.RELEASE.jar:$lib_dir/org.springframework.spring-context-4.3.20.RELEASE.jar:$lib_dir/org.springframework.spring-aop-4.3.20.RELEASE.jar:$lib_dir/org.springframework.spring-beans-4.3.20.RELEASE.jar:$lib_dir/org.springframework.spring-expression-4.3.20.RELEASE.jar:$lib_dir/org.springframework.boot.spring-boot-autoconfigure-1.5.17.RELEASE.jar:$lib_dir/org.springframework.boot.spring-boot-starter-logging-1.5.17.RELEASE.jar:$lib_dir/ch.qos.logback.logback-classic-1.1.11.jar:$lib_dir/ch.qos.logback.logback-core-1.1.11.jar:$lib_dir/org.slf4j.jcl-over-slf4j-1.7.25.jar:$lib_dir/org.slf4j.slf4j-api-1.7.25.jar:$lib_dir/org.slf4j.jul-to-slf4j-1.7.25.jar:$lib_dir/org.slf4j.log4j-over-slf4j-1.7.25.jar:$lib_dir/org.springframework.boot.spring-boot-starter-tomcat-1.5.17.RELEASE.jar:$lib_dir/org.apache.tomcat.embed.tomcat-embed-core-8.5.34.jar:$lib_dir/org.apache.tomcat.tomcat-annotations-api-8.5.34.jar:$lib_dir/org.apache.tomcat.embed.tomcat-embed-el-8.5.34.jar:$lib_dir/org.apache.tomcat.embed.tomcat-embed-websocket-8.5.34.jar:$lib_dir/org.hibernate.hibernate-validator-5.3.6.Final.jar:$lib_dir/javax.validation.validation-api-1.1.0.Final.jar:$lib_dir/com.fasterxml.jackson.core.jackson-databind-2.9.6.jar:$lib_dir/com.fasterxml.jackson.core.jackson-annotations-2.9.0.jar:$lib_dir/com.fasterxml.jackson.core.jackson-core-2.9.6.jar:$lib_dir/org.springframework.spring-web-4.3.20.RELEASE.jar:$lib_dir/org.springframework.spring-webmvc-4.3.20.RELEASE.jar:$lib_dir/org.yaml.snakeyaml-1.17.jar:$lib_dir/org.hibernate.hibernate-core-5.3.7.Final.jar:$lib_dir/org.jboss.logging.jboss-logging-3.3.2.Final.jar:$lib_dir/javax.persistence.javax.persistence-api-2.2.jar:$lib_dir/org.javassist.javassist-3.23.1-GA.jar:$lib_dir/net.bytebuddy.byte-buddy-1.8.17.jar:$lib_dir/antlr.antlr-2.7.7.jar:$lib_dir/org.jboss.spec.javax.transaction.jboss-transaction-api_1.2_spec-1.1.1.Final.jar:$lib_dir/org.jboss.jandex-2.0.5.Final.jar:$lib_dir/com.fasterxml.classmate-1.3.4.jar:$lib_dir/javax.activation.javax.activation-api-1.2.0.jar:$lib_dir/org.dom4j.dom4j-2.1.1.jar:$lib_dir/org.hibernate.common.hibernate-commons-annotations-5.0.4.Final.jar:$lib_dir/org.hibernate.hibernate-java8-5.3.7.Final.jar"

# java_cmd is overrode in process_args when -java-home is used
declare java_cmd=$(get_java_cmd)

# if configuration files exist, prepend their contents to $@ so it can be processed by this runner
[[ -f "$script_conf_file" ]] && set -- $(loadConfigFile "$script_conf_file") "$@"

run "$@"
