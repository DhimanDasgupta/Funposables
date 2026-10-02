#!/usr/bin/env bash
#
# Thorough Build Script for Funposables Android Project
#
# Usage:
#   ./build.sh [options]
#
# Run './build.sh --help' for details on available flags.
#

set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Color formatting
BOLD='\033[1m'
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Default options
DO_CLEAN=false
RUN_TESTS=true
RUN_LINT=true
RUN_STABILITY=true
BUILD_APKS=true
BUILD_BUNDLES=false
VARIANT="all" # debug, release, all
EXTRA_GRADLE_ARGS=()

# Status tracking
TOTAL_START_TIME=$(date +%s)
STEP_RESULTS=()

log_header() {
  echo -e "\n${BOLD}${BLUE}======================================================================${NC}"
  echo -e "${BOLD}${BLUE} $1 ${NC}"
  echo -e "${BOLD}${BLUE}======================================================================${NC}\n"
}

log_step() {
  echo -e "${BOLD}${CYAN}==> $1${NC}"
}

log_success() {
  echo -e "${BOLD}${GREEN}✔ $1${NC}"
}

log_warning() {
  echo -e "${BOLD}${YELLOW}⚠ $1${NC}"
}

log_error() {
  echo -e "${BOLD}${RED}✖ $1${NC}"
}

show_help() {
  cat << EOF
Thorough Build Script for Funposables

Usage:
  ./build.sh [OPTIONS] [-- [GRADLE_ARGS...]]

Options:
  -c, --clean            Clean build directories before running tasks
  -v, --variant <name>   Target build variant: 'debug', 'release', or 'all' (default: all)
      --skip-tests       Skip running unit tests
      --skip-lint        Skip running Android Lint analysis
      --skip-stability   Skip Compose stability metrics generation
  -b, --bundle           Build Android App Bundle (.aab) in addition to APK
      --quick            Fast build: skips tests, lint, stability reports, and release APK
  -h, --help             Show this help message and exit

Examples:
  ./build.sh                        # Run full build: clean/build, test, lint, stability, apks
  ./build.sh --clean --variant debug # Clean and build debug variant with tests and lint
  ./build.sh --bundle               # Full build including App Bundles
  ./build.sh --skip-lint            # Run full build skipping lint
  ./build.sh -- --info              # Pass extra arguments to Gradle
EOF
}

# Parse command-line arguments
while [[ $# -gt 0 ]]; do
  case "$1" in
    -c|--clean)
      DO_CLEAN=true
      shift
      ;;
    -v|--variant)
      if [[ -n "${2:-}" && "$2" != -* ]]; then
        VARIANT="$(echo "$2" | tr '[:upper:]' '[:lower:]')"
        shift 2
      else
        log_error "Error: --variant requires an argument ('debug', 'release', or 'all')"
        exit 1
      fi
      ;;
    --skip-tests)
      RUN_TESTS=false
      shift
      ;;
    --skip-lint)
      RUN_LINT=false
      shift
      ;;
    --skip-stability)
      RUN_STABILITY=false
      shift
      ;;
    -b|--bundle)
      BUILD_BUNDLES=true
      shift
      ;;
    --quick)
      RUN_TESTS=false
      RUN_LINT=false
      RUN_STABILITY=false
      VARIANT="debug"
      shift
      ;;
    -h|--help)
      show_help
      exit 0
      ;;
    --)
      shift
      while [[ $# -gt 0 ]]; do
        EXTRA_GRADLE_ARGS+=("$1")
        shift
      done
      break
      ;;
    *)
      log_warning "Unrecognized option '$1'. Passing as argument to Gradle."
      EXTRA_GRADLE_ARGS+=("$1")
      shift
      ;;
  esac
done

# Validate variant
if [[ "$VARIANT" != "debug" && "$VARIANT" != "release" && "$VARIANT" != "all" ]]; then
  log_error "Invalid variant '$VARIANT'. Must be 'debug', 'release', or 'all'."
  exit 1
fi

# Verify Gradle wrapper
if [[ ! -f "./gradlew" ]]; then
  log_error "Gradle wrapper './gradlew' not found in $SCRIPT_DIR!"
  exit 1
fi

chmod +x ./gradlew

# Step execution helper
run_step() {
  local step_name="$1"
  shift
  local start_time
  start_time=$(date +%s)
  
  log_step "$step_name"
  echo -e "${CYAN}Executing: ./gradlew ${*}${NC}\n"

  if ./gradlew "$@"; then
    local duration=$(($(date +%s) - start_time))
    log_success "$step_name succeeded (${duration}s)\n"
    STEP_RESULTS+=("${GREEN}✔${NC} $step_name (${duration}s)")
    return 0
  else
    local duration=$(($(date +%s) - start_time))
    log_error "$step_name FAILED (${duration}s)\n"
    STEP_RESULTS+=("${RED}✖${NC} $step_name (${duration}s)")
    return 1
  fi
}

log_header "Funposables Build Pipeline"
echo "Project Directory : $SCRIPT_DIR"
echo "Target Variant    : $VARIANT"
echo "Run Clean         : $DO_CLEAN"
echo "Run Tests         : $RUN_TESTS"
echo "Run Lint          : $RUN_LINT"
echo "Compose Stability : $RUN_STABILITY"
echo "Build Bundles     : $BUILD_BUNDLES"
if [[ ${#EXTRA_GRADLE_ARGS[@]} -gt 0 ]]; then
  echo "Extra Gradle Args : ${EXTRA_GRADLE_ARGS[*]}"
fi
echo ""

# Helper to invoke run_step with optional extra gradle args
invoke_gradle_step() {
  local step_name="$1"
  shift
  local tasks=("$@")
  if [[ ${#EXTRA_GRADLE_ARGS[@]} -gt 0 ]]; then
    run_step "$step_name" "${tasks[@]}" "${EXTRA_GRADLE_ARGS[@]}"
  else
    run_step "$step_name" "${tasks[@]}"
  fi
}

# 1. Clean Stage
if [[ "$DO_CLEAN" == "true" ]]; then
  if ! invoke_gradle_step "Clean Project" clean; then
    log_error "Build failed during clean stage."
    exit 1
  fi
fi

# 2. Unit Tests Stage
if [[ "$RUN_TESTS" == "true" ]]; then
  TASKS_TEST=()
  case "$VARIANT" in
    debug)
      TASKS_TEST+=("testDebugUnitTest")
      ;;
    release)
      TASKS_TEST+=("testReleaseUnitTest")
      ;;
    all)
      TASKS_TEST+=("test")
      ;;
  esac

  if ! invoke_gradle_step "Run Unit Tests" "${TASKS_TEST[@]}"; then
    log_error "Unit tests failed!"
    exit 1
  fi
fi

# 3. Android Lint Stage
if [[ "$RUN_LINT" == "true" ]]; then
  TASKS_LINT=()
  case "$VARIANT" in
    debug)
      TASKS_LINT+=("lintDebug")
      ;;
    release)
      TASKS_LINT+=("lintRelease")
      ;;
    all)
      TASKS_LINT+=("lint")
      ;;
  esac

  if ! invoke_gradle_step "Run Android Lint" "${TASKS_LINT[@]}"; then
    log_error "Lint check failed!"
    exit 1
  fi
fi

# 4. Compose Stability Report Stage
if [[ "$RUN_STABILITY" == "true" ]]; then
  TASKS_STABILITY=()
  case "$VARIANT" in
    debug)
      TASKS_STABILITY+=("composeStabilityDebug")
      ;;
    release)
      TASKS_STABILITY+=("composeStabilityRelease")
      ;;
    all)
      TASKS_STABILITY+=("composeStability")
      ;;
  esac

  if ! invoke_gradle_step "Generate Compose Stability Reports" "${TASKS_STABILITY[@]}"; then
    log_error "Compose stability report generation failed!"
    exit 1
  fi
fi

# 5. Assemble APKs Stage
if [[ "$BUILD_APKS" == "true" ]]; then
  TASKS_ASSEMBLE=()
  case "$VARIANT" in
    debug)
      TASKS_ASSEMBLE+=("assembleDebug")
      ;;
    release)
      TASKS_ASSEMBLE+=("assembleRelease")
      ;;
    all)
      TASKS_ASSEMBLE+=("assemble")
      ;;
  esac

  if ! invoke_gradle_step "Assemble APKs" "${TASKS_ASSEMBLE[@]}"; then
    log_error "Assembly failed!"
    exit 1
  fi
fi

# 6. Build App Bundles Stage (Optional)
if [[ "$BUILD_BUNDLES" == "true" ]]; then
  TASKS_BUNDLE=()
  case "$VARIANT" in
    debug)
      TASKS_BUNDLE+=("bundleDebug")
      ;;
    release)
      TASKS_BUNDLE+=("bundleRelease")
      ;;
    all)
      TASKS_BUNDLE+=("bundle")
      ;;
  esac

  if ! invoke_gradle_step "Build App Bundles" "${TASKS_BUNDLE[@]}"; then
    log_error "App bundle creation failed!"
    exit 1
  fi
fi

TOTAL_DURATION=$(($(date +%s) - TOTAL_START_TIME))

log_header "Build Summary"
for res in "${STEP_RESULTS[@]}"; do
  echo -e "  $res"
done
echo -e "\nTotal Duration: ${BOLD}${TOTAL_DURATION}s${NC}\n"

# Output artifact locations if available
echo -e "${BOLD}Artifacts & Reports:${NC}"
if [[ -d "app/build/outputs/apk" ]]; then
  echo -e "  ${CYAN}APKs:${NC}"
  find app/build/outputs/apk -type f -name "*.apk" 2>/dev/null | while read -r apk; do
    echo "    - $apk"
  done
fi

if [[ -d "app/build/outputs/bundle" ]]; then
  echo -e "  ${CYAN}Bundles:${NC}"
  find app/build/outputs/bundle -type f -name "*.aab" 2>/dev/null | while read -r aab; do
    echo "    - $aab"
  done
fi

if [[ -d "app/build/reports/tests" ]]; then
  echo -e "  ${CYAN}Test Reports:${NC}"
  echo "    - file://$SCRIPT_DIR/app/build/reports/tests/testDebugUnitTest/index.html"
fi

if [[ -d "app/build/reports" ]]; then
  echo -e "  ${CYAN}Lint Reports:${NC}"
  find app/build/reports -type f -name "lint-results*.html" 2>/dev/null | while read -r lint; do
    echo "    - file://$SCRIPT_DIR/$lint"
  done
fi

if [[ -d "app/build/reports/compose-stability" ]]; then
  echo -e "  ${CYAN}Compose Stability Reports:${NC}"
  find app/build/reports/compose-stability -type f -name "*.txt" 2>/dev/null | while read -r stab; do
    echo "    - $stab"
  done
fi

echo -e "\n${BOLD}${GREEN}BUILD FINISHED SUCCESSFULLY!${NC}\n"
exit 0
