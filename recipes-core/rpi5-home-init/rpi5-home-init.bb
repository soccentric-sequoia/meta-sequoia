SUMMARY = "Initialise /home on first boot after the persistent home partition is mounted"
DESCRIPTION = "\
On first boot the empty ext4 home partition (p4) is mounted at /home, hiding \
the skeleton copy that useradd placed in the rootfs.  This one-shot service \
creates /home/rpi with correct ownership and populates it from /etc/skel so \
the user can log in immediately."

LICENSE     = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://rpi5-home-init.sh \
    file://rpi5-home-init.service \
"

S = "${WORKDIR}"

inherit systemd

SYSTEMD_SERVICE:${PN}     = "rpi5-home-init.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

RDEPENDS:${PN} = "bash util-linux parted e2fsprogs-resize2fs"

do_install() {
    install -d ${D}${libdir}/rpi5-home-init
    install -m 0755 ${WORKDIR}/rpi5-home-init.sh \
                    ${D}${libdir}/rpi5-home-init/rpi5-home-init.sh

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/rpi5-home-init.service \
                    ${D}${systemd_system_unitdir}/rpi5-home-init.service
}

FILES:${PN} += " \
    ${libdir}/rpi5-home-init/ \
    ${systemd_system_unitdir}/rpi5-home-init.service \
"
