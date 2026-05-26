SUMMARY     = "Sequoia Intel — A/B-aware GRUB EFI configuration"
DESCRIPTION = "Installs a custom grub.cfg into the staging sysroot at \
${datadir}/grub-efi/grub.cfg so that the wic bootimg-efi source plugin \
picks it up (via GRUB_CFG = \"${STAGING_DATADIR}/grub-efi/grub.cfg\" in \
local.conf) instead of auto-generating a UUID-based config. \
\
The config uses 'search --label rootfs-a' so that the A/B OTA \
post-install.sh can activate a new slot with a simple sed replacement \
of the label name, without needing to look up PARTUUIDs or filesystem \
UUIDs that may change after a rootfs write."

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://grub.cfg"

S = "${WORKDIR}"

do_install() {
    install -d ${D}${datadir}/grub-efi
    install -m 0644 ${WORKDIR}/grub.cfg ${D}${datadir}/grub-efi/grub.cfg
}

FILES:${PN} = "${datadir}/grub-efi/grub.cfg"

# Make the grub.cfg available in the sysroot so do_image_wic can find it via
# GRUB_CFG = "${STAGING_DATADIR}/grub-efi/grub.cfg".
# The caller (core-image-full-cmdline.bbappend) adds:
#   do_image_wic[depends] += "sequoia-grub-cfg:do_populate_sysroot"
SYSROOT_DIRS += "${datadir}/grub-efi"
