SUMMARY = "Sequoia A/B-aware GRUB configuration for Intel x86 (LattePanda)"
DESCRIPTION = "Stages an A/B GRUB config that boots rootfs-a or rootfs-b by \
filesystem label. Picked up by the bootimg-efi wic plugin via GRUB_CFG."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://grub.cfg \
"

S = "${WORKDIR}"

# Only needed at image build time (wic), not installed into the rootfs.
inherit nopackages

do_install() {
    install -d ${D}${datadir}/grub-efi
    install -m 0644 ${WORKDIR}/grub.cfg ${D}${datadir}/grub-efi/grub.cfg
}

FILES:${PN} = "${datadir}/grub-efi/grub.cfg"
