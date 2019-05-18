DESCRIPTION = "Dotnet from source-build"
HOMEPAGE = "http://dot.net"
LICENSE = "MIT"
SECTION = "devel"

SRC_URI = "\
  gitsm://github.com/dotnet/source-build;branch=release/2.2 \
  file://0001-core-setup.patch \
  file://0002-coreclr.patch \
  file://0003-corefx.patch \
"
SRCREV="75c9106dcadb39015675f163fbca814da486665d"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=9fc642ff452b28d62ab19b7eea50dfb9"

S = "${WORKDIR}/git"

DEPENDS = "clang-native lldb libunwind gettext icu openssl util-linux cmake-native lttng-ust ca-certificates-native krb5 curl"
RDEPENDS_${PN} = "libicuuc libicui18n lttng-ust libcurl libuv libssl"

INSANE_SKIP_${PN} += "staticdev file-rdeps textrel"
WARN_QA_remove = "libdir"
SKIP_FILEDEPS_${PN} = "1"

do_fix_target_name() {
	sed -i s/arm-linux-gnueabihf/${TARGET_SYS}/g ${S}/cross/arm/toolchain.cmake
	sed -i s/arm-linux-gnueabihf/${TARGET_SYS}/g ${S}/src/coreclr/cross/toolchain.cmake
	sed -i s/arm-linux-gnueabihf/${TARGET_SYS}/g ${S}/src/corefx/cross/arm/toolchain.cmake
	sed -i s/arm-linux-gnueabihf/${TARGET_SYS}/g ${S}/src/core-setup/cross/arm/toolchain.cmake

  sed -i s/arm-linux-gnueabi/${TARGET_SYS}/g ${S}/cross/arm/toolchain.cmake
	sed -i s/arm-linux-gnueabi/${TARGET_SYS}/g ${S}/src/coreclr/cross/toolchain.cmake
	sed -i s/arm-linux-gnueabi/${TARGET_SYS}/g ${S}/src/corefx/cross/arm/toolchain.cmake
	sed -i s/arm-linux-gnueabi/${TARGET_SYS}/g ${S}/src/core-setup/cross/arm/toolchain.cmake
}

addtask fix_target_name after do_patch before do_configure

BUILD_CONFIGURATION = "Release"

do_compile() {
	cd ${S}
	unset bindir
	export CURL_CA_BUNDLE="${STAGING_ETCDIR_NATIVE}/ssl/certs/ca-certificates.crt"
	export SOURCE_BUILD_SKIP_SUBMODULE_CHECK=1
	export ArmEnvironmentVariables="ROOTFS_DIR=${STAGING_DIR_HOST}"
	export ROOTFS_DIR=${STAGING_DIR_HOST}
	export GCC_TOOLCHAIN=${STAGING_BINDIR_TOOLCHAIN}
	export PARALLEL_MAKEINST="${PARALLEL_MAKEINST}"
	export VersionUserName=meta-aspnet
	export SkipEnsurePackagesCreated=true
	./build.sh /p:Platform=armel /p:TargetRid=linux-arm /p:Configuration=${BUILD_CONFIGURATION} /p:SkipGenerateRootFs=true
}

do_install() {
    install -d ${D}${datadir}/dotnet
    install -d ${D}${bindir}

    cp -dr ${S}/src/core-setup/Bin/obj/*-arm.${BUILD_CONFIGURATION}/combined-framework-host/* ${D}${datadir}/dotnet/
    ln -sf ../share/dotnet/dotnet ${D}${bindir}/dotnet
    # https://github.com/dotnet/coreclr/issues/19025
    echo "" > ${D}${datadir}/dotnet/shared/Microsoft.NETCore.App/${PV}/libcoreclrtraceptprovider.so
}

FILES_${PN} = "${datadir}/dotnet ${bindir}/dotnet"
