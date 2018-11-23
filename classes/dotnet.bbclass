inherit dotnet-environment

DEPENDS +=" mono-native"
RDEPENDS_${PN}+=" mono libsystemnative libcurl libgssapi-krb5"

BUILD="${WORKDIR}/build"
PACKAGES = "${PN}-dbg ${PN}"

FILES_${PN} = "/opt/${PN}"
FILES_${PN}-dbg = "/opt/${PN}/*.pdb"

# Some packages (EF for sqlite, Kestrel) have *.so files bundle.
# This uses a sledge hammer aproach to fix the build
PACKAGEBUILDPKGD_remove = "split_and_strip_files"
INSANE_SKIP_${PN} = "file-rdeps arch staticdev"

TARGET_FRAMEWORK ?= "net471"
MSBUILD_EXTRA_ARGS ?= ""

do_configure () {
    dotnet restore ${MSBUILD_EXTRA_ARGS}
}

do_compile () {
    rm -rf ${BUILD}
    dotnet publish -c Release -o ${BUILD} -f ${TARGET_FRAMEWORK} ${MSBUILD_EXTRA_ARGS}
    if [$TARGET_FRAMEWORK = "net471"]; then
        rm ${BUILD}/System.Diagnostics.Tracing.dll
    fi
}

do_install () {
    install -d ${D}/opt/${PN}
    cp -dRf ${BUILD}/* ${D}/opt/${PN}
    if [$TARGET_FRAMEWORK = "net471"]; then
        find ${D}/opt/${PN} -type d -exec chmod 755 {} +
        find ${D}/opt/${PN} -type f -exec chmod 644 {} +
        chmod 755 ${D}/opt/${PN}/*.exe
    fi
}

