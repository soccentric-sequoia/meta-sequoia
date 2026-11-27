SUMMARY = "Initialise /home on first boot after the persistent home partition is mounted"
DESCRIPTION = "On first boot the empty ext4 home partition is mounted at /home. This one-shot service creates user homes and populates them from /etc/skel."

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://sequoia-home-init.sh \
    file://sequoia-home-init.service \
"

S = "${WORKDIR}"

inherit systemd

SYSTEMD_SERVICE:${PN} = "sequoia-home-init.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

RDEPENDS:${PN} = "bash util-linux parted e2fsprogs-resize2fs"

do_install() {
    install -d ${D}${libdir}/sequoia-home-init
    install -m 0755 ${WORKDIR}/sequoia-home-init.sh \
                    ${D}${libdir}/sequoia-home-init/sequoia-home-init.sh

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/sequoia-home-init.service \
                    ${D}${systemd_system_unitdir}/sequoia-home-init.service
}

FILES:${PN} += " \
    ${libdir}/sequoia-home-init/ \
    ${systemd_system_unitdir}/sequoia-home-init.service \
"
