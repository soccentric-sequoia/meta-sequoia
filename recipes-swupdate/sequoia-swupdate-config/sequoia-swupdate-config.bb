SUMMARY = "SWUpdate configuration for Sequoia OS (Intel x86 / GRUB EFI A/B)"
DESCRIPTION = "\
Installs the swupdate daemon config, the pre/post install hooks, the \
boot-slot detection service, and the web-UI nginx config for Sequoia. \
The pre-install hook detects the active rootfs by filesystem LABEL and \
creates the /dev/swupdate-inactive symlink used by sw-description. \
The post-install hook relabels the newly-written partition and patches \
/boot/efi/EFI/BOOT/grub.cfg so GRUB boots from the new slot."

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://swupdate.cfg \
    file://pre-install.sh \
    file://post-install.sh \
    file://swupdate-bootcheck.sh \
    file://swupdate-bootcheck.service \
    file://swupdate-webui.conf \
"

S = "${WORKDIR}"

inherit systemd

SYSTEMD_SERVICE:${PN}     = "swupdate-bootcheck.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

RDEPENDS:${PN} = "bash swupdate e2fsprogs util-linux"

do_install() {
    # ── SWUpdate main daemon config ───────────────────────────────────────────
    install -d ${D}${sysconfdir}
    install -m 0644 ${WORKDIR}/swupdate.cfg ${D}${sysconfdir}/swupdate.cfg

    # ── A/B hook scripts ──────────────────────────────────────────────────────
    install -d ${D}${libdir}/sequoia-swupdate
    install -m 0755 ${WORKDIR}/pre-install.sh \
                    ${D}${libdir}/sequoia-swupdate/pre-install.sh
    install -m 0755 ${WORKDIR}/post-install.sh \
                    ${D}${libdir}/sequoia-swupdate/post-install.sh
    install -m 0755 ${WORKDIR}/swupdate-bootcheck.sh \
                    ${D}${libdir}/sequoia-swupdate/swupdate-bootcheck.sh

    # ── Systemd service for boot-slot detection ───────────────────────────────
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/swupdate-bootcheck.service \
                    ${D}${systemd_system_unitdir}/swupdate-bootcheck.service

    # ── Nginx web-UI config ───────────────────────────────────────────────────
    install -d ${D}${sysconfdir}/nginx/conf.d
    install -m 0644 ${WORKDIR}/swupdate-webui.conf \
                    ${D}${sysconfdir}/nginx/conf.d/swupdate-webui.conf
}

FILES:${PN} += " \
    ${sysconfdir}/swupdate.cfg \
    ${libdir}/sequoia-swupdate/ \
    ${systemd_system_unitdir}/swupdate-bootcheck.service \
    ${sysconfdir}/nginx/conf.d/swupdate-webui.conf \
"
