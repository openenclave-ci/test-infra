// Common openenclave jenkins functions

/** Checkout openenclave, along with merged pull request. If master is instead passed in, don't check out branch
  * as this is being ran as a validation of master or as a reverse integration test on the test-infra repo.
**/
void checkout( String PULL_NUMBER="master" ) {
    if (isUnix()) {
        sh  """
            git config --global core.compression 0 && \
            rm -rf openenclave && \
            git clone --recursive --depth 1 https://github.com/openenclave/openenclave && \
            cd openenclave && \
            git fetch origin +refs/pull/*/merge:refs/remotes/origin/pr/*
            if [ '${PULL_NUMBER}' != 'master' ]
            then
                echo 'checking out  ${PULL_NUMBER}'
                git checkout origin/pr/${PULL_NUMBER}
            fi
            """
    }
    else {
        bat """
            git config --global core.compression 0 && \
            (if exist openenclave rmdir /s/q openenclave) && \
            git clone --recursive --depth 1 https://github.com/openenclave/openenclave && \
            cd openenclave && \
            git fetch origin +refs/pull/*/merge:refs/remotes/origin/pr/*
            if NOT ${PULL_NUMBER}==master git checkout origin/pr/${PULL_NUMBER}
            """
    }
}

/* installOpenEnclavePrereqs
*  Runs set up configuration configuration on vanilla vm to allow e2e variant checks
*/
void installOpenEnclavePrereqs() {
    dir ('openenclave') {
        if (isUnix()) {
            sh  """
                sudo bash scripts/ansible/install-ansible.sh
                # Run ACC Playbook
                for i in 1 2 3 4 5
                do
                    sudo \$(which ansible-playbook) scripts/ansible/oe-contributors-acc-setup.yml && break
                    sleep 60
                done
                """
        }
        else {
            powershell returnStatus: true, script: '.\\scripts\\install-windows-prereqs.ps1 -InstallPath C:/oe_prereqs -LaunchConfiguration SGX1FLC -DCAPClientType Azure'
        }
    }
}

// Get C Compiler Flags
def getCCompiler(String COMPILER="clang-8"){
    switch(COMPILER) {
        case "gcc":
            return "gcc"
        case "clang-8":
            return "clang-8"
        case "clang-7":
            return "clang-7"
        default:
            return "clang-8"
    }
}

// Get CXX Compiler Flags
def getCXXCompiler(String COMPILER="clang-8"){
    switch(COMPILER) {
        case "gcc":
            return "gcc"
        case "clang-8":
            return "clang++-8"
        case "clang-7":
            return "clang++-7"
        default:
            return "clang++-8"
    }
}
/** Build openenclave based on build config, compiler and platform
**/
def cmakeBuildopenenclave( String BUILD_CONFIG="Release", String COMPILER="clang-7", String EXTRA_CMAKE_ARGS ="") {
    dir ('openenclave/build') {
        if (isUnix()) {

            def c_compiler = getCCompiler("${COMPILER}")
            def cpp_compiler = getCXXCompiler("${COMPILER}")
            
            printDebug()

            withEnv(["CC=${c_compiler}","CXX=${cpp_compiler}"]) {
                sh  """
                    cmake .. -G Ninja -DCMAKE_BUILD_TYPE=${BUILD_CONFIG} ${EXTRA_CMAKE_ARGS}
                    ninja -v
                    ctest --output-on-failure --timeout
                    """
            }
        } else {
            bat """
                vcvars64.bat x64 && \
                cmake.exe .. -G Ninja -DCMAKE_BUILD_TYPE=${BUILD_CONFIG} ${EXTRA_CMAKE_ARGS} && \
                ninja.exe && \
                ctest.exe -V -C ${BUILD_CONFIG} --output-on-failure
                """
        }
    }
}

// Common build and package functionality.
def openenclavepackageInstall( String BUILD_CONFIG="Release", String COMPILER="clang-7", String EXTRA_CMAKE_ARGS ="") {
    dir ('openenclave/build') {
        if (isUnix()) {
            
            printDebug()

            def c_compiler = getCCompiler("${COMPILER}")
            def cpp_compiler = getCXXCompiler("${COMPILER}")

            withEnv(["CC=${c_compiler}","CXX=${cpp_compiler}"]) {
                //Missing lvi sample test case
                sh  """
                    sudo ninja -v package
                    sudo ninja -v install
                    cp -r /opt/openenclave/share/openenclave/samples ~/
                    cd ~/samples
                    . /opt/openenclave/share/openenclave/openenclaverc
                    for i in *; do
                        if [ -d \${i} ]; then
                            cd \${i}
                            mkdir build
                            cd build
                            cmake ..
                            make
                            make run
                            cd ../..
                        fi
                    done
                    """
            }
        } else {
            //Missing lvi sample test case
            bat """
                vcvars64.bat x64 && \
                cpack.exe -D CPACK_NUGET_COMPONENT_INSTALL=ON -DCPACK_COMPONENTS_ALL=OEHOSTVERIFY && \
                cpack.exe && \
                (if exist C:\\oe rmdir /s/q C:\\oe) && \
                nuget.exe install open-enclave -Source %cd% -OutputDirectory C:\\oe -ExcludeVersion && \
                set CMAKE_PREFIX_PATH=C:\\oe\\open-enclave\\openenclave\\lib\\openenclave\\cmake && \
                cd C:\\oe\\open-enclave\\openenclave\\share\\openenclave\\samples && \
                setlocal enabledelayedexpansion && \
                for /d %%i in (*) do (
                    cd C:\\oe\\open-enclave\\openenclave\\share\\openenclave\\samples\\"%%i"
                    mkdir build
                    cd build
                    cmake .. -G Ninja -DNUGET_PACKAGE_PATH=C:\\oe_prereqs -DLVI_MITIGATION=OFF || exit /b %errorlevel%
                    ninja || exit /b %errorlevel%
                    ninja run || exit /b %errorlevel%
                )
                """
        }
    }
}

// Check CI flows https://github.com/openenclave/openenclave/blob/master/scripts/check-ci
def checkCI() {
    dir ('openenclave') {
        if (isUnix()) {
            try {
                    // This is a hack, migrating docker image repos and we just need the short hand 
                    // linux version for compatability with legacy repo.
                    def lin_version = "${params.LINUX_VERSION}" == "Ubuntu-1604" ? "16.04" : "18.04"
                    def task =  """
                                echo 'bug here, revisit before go live'
                                #./scripts/check-ci
                                """
                    Run("cross", task)

                } catch (Exception e) {
                    // Do something with the exception 
                    error "Program failed, please read logs..."
                }
        }
    }
}

def AArch64GNUBuild( String BUILD_CONFIG="Release") {
    // This is a hack, migrating docker image repos and we just need the short hand 
    // linux version for compatability with legacy repo.
    def lin_version = "${params.LINUX_VERSION}" == "Ubuntu-1604" ? "16.04" : "18.04"

    def task =  """
                cmake ${WORKSPACE}/openenclave/                                             \
                    -G Ninja                                                                \
                    -DCMAKE_BUILD_TYPE=${BUILD_CONFIG}                                      \
                    -DCMAKE_TOOLCHAIN_FILE=${WORKSPACE}/openenclave/cmake/arm-cross.cmake   \
                    -DOE_TA_DEV_KIT_DIR=/devkits/vexpress-qemu_armv8a/export-ta_arm64       \
                    -Wdev
                ninja -v
                """
    ContainerRun("oeciteam/oetools-full-${lin_version}", "cross", task, "--cap-add=SYS_PTRACE")
}

// This is copy and pasted from the deprecated openenclave-ci repo and is used for multiphase tests, we should consider cleaning this up and refactoring 
def ContainerRun(String imageName, String compiler, String task, String runArgs="") {
    def image = docker.image(imageName)
    image.pull()
    image.inside(runArgs) {
        dir("${WORKSPACE}/openenclave/build") {
            Run(compiler, task)
        }
    }
}

def Run(String compiler, String task) {
    def c_compiler = getCCompiler("${compiler}")
    def cpp_compiler = getCXXCompiler("${compiler}")
    
    withEnv(["CC=${c_compiler}","CXX=${cpp_compiler}"]) {
        runTask(task);
    }
}

def runTask(String task) {
    dir("${WORKSPACE}/build") {
        sh """#!/usr/bin/env bash
                set -o errexit
                set -o pipefail
                source /etc/profile
                ${task}
            """
    }
}

def printDebug(){
    sh  """
        printenv
        """
}

// Clean up environment, do not fail on error.
def cleanup() {
    if (isUnix()) {
        try {
                sh  """
                    sudo rm -rf openenclave || rm -rf openenclave || echo 'Workspace is clean'
                    sudo rm -rf /opt/openenclave || rm -rf /opt/openenclave || echo 'Workspace is clean'
                    sudo rm -rf ~/samples || rm -rf ~/samples || echo 'Workspace is clean'
                    """
            } catch (Exception e) {
                // Do something with the exception 
                error "Program failed, please read logs..."
            } 
    }
}

return this