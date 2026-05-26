# Sequoia Intel — wic dependency on the custom GRUB config recipe.
#
# The sequoia-grub-cfg recipe installs a label-based A/B-aware grub.cfg to
# ${datadir}/grub-efi/grub.cfg in the sysroot.  bootimg-efi wic plugin picks
# it up via GRUB_CFG = "${STAGING_DATADIR}/grub-efi/grub.cfg" (set in
# kas/common-sequoia.yaml).  This dependency ensures it is staged before
# do_image_wic runs.
do_image_wic[depends] += "sequoia-grub-cfg:do_populate_sysroot"

# ── root filesystem expansion on first boot ───────────────────────────────────
IMAGE_INSTALL:append = " expand-rootfs"
