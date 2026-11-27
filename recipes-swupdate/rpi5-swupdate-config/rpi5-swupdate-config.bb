SUMMARY     = "SWUpdate runtime configuration and A/B OTA helper scripts for RPi5"
DESCRIPTION = "Installs swupdate.cfg, slot-detection / boot-slot scripts, a \
               systemd drop-in that enables the Mongoose web UI on port 8080, \
               and a one-shot boot-check service that logs the active slot."
LICENSE     = "MIT"
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

# Only declare the service unit installed by THIS recipe.
# swupdate.service is owned by the swupdate package; we configure it via a
# drop-in (swupdate.service.d/webui.conf) which does not need to be listed here.
SYSTEMD_SERVICE:${PN}     = "swupdate-bootcheck.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

# swupdate and bash are required at runtime; util-linux provides findmnt/blkid
RDEPENDS:${PN} = "swupdate bash util-linux"

do_install() {
    # ── swupdate main config ────────────────────────────────────────────────
    install -d ${D}${sysconfdir}/swupdate
    install -m 0644 ${WORKDIR}/swupdate.cfg \
                    ${D}${sysconfdir}/swupdate/swupdate.cfg

    # ── A/B slot helper scripts (also shipped in .swu packages) ────────────
    install -d ${D}${libdir}/swupdate
    install -m 0755 ${WORKDIR}/pre-install.sh \
                    ${D}${libdir}/swupdate/pre-install.sh
    install -m 0755 ${WORKDIR}/post-install.sh \
                    ${D}${libdir}/swupdate/post-install.sh
    install -m 0755 ${WORKDIR}/swupdate-bootcheck.sh \
                    ${D}${libdir}/swupdate/swupdate-bootcheck.sh

    # ── systemd units ───────────────────────────────────────────────────────
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/swupdate-bootcheck.service \
                    ${D}${systemd_system_unitdir}/swupdate-bootcheck.service

    # Drop-in: enable Mongoose web UI and point at our config file
    install -d ${D}${systemd_system_unitdir}/swupdate.service.d
    install -m 0644 ${WORKDIR}/swupdate-webui.conf \
                    ${D}${systemd_system_unitdir}/swupdate.service.d/webui.conf
}

FILES:${PN} += " \
    ${sysconfdir}/swupdate/ \
    ${libdir}/swupdate/ \
    ${systemd_system_unitdir}/swupdate-bootcheck.service \
    ${systemd_system_unitdir}/swupdate.service.d/ \
"
