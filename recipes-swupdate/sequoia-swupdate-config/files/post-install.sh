#!/bin/bash
# post-install.sh — SWUpdate post-install hook for Sequoia (Intel x86 / GRUB EFI A/B)
#
# After SWUpdate has written the new rootfs image to the inactive partition:
#   1. Re-labels the inactive partition ext4 filesystem to rootfs-<slot>
#      so the new rootfs keeps the expected LABEL after the swap.
#   2. Locates the EFI partition, mounts it rw at /boot/efi.
#   3. Replaces the rootfs label reference in grub.cfg so GRUB boots from
#      the new slot on next reboot.
#   4. Unmounts the EFI partition, cleans up /run artefacts.

set -euo pipefail

log() { logger -t swupdate-postinstall "$*"; echo "$*" >&2; }

# ── 1. Read slot/device saved by pre-install ──────────────────────────────────
if [[ ! -f /run/swupdate-target-slot || ! -f /run/swupdate-target-dev ]]; then
    log "ERROR: /run/swupdate-target-slot or /run/swupdate-target-dev missing"
    exit 1
fi

TARGET_SLOT=$(cat /run/swupdate-target-slot)
TARGET_DEV=$(cat  /run/swupdate-target-dev)

log "Target slot: ${TARGET_SLOT}  device: ${TARGET_DEV}"

# ── 2. Relabel the inactive partition ────────────────────────────────────────
log "Relabelling ${TARGET_DEV} → rootfs-${TARGET_SLOT}"
e2label "${TARGET_DEV}" "rootfs-${TARGET_SLOT}"

# ── 3. Find the EFI partition ────────────────────────────────────────────────
EFI_DEV=$(blkid -t "LABEL=efi" -o device 2>/dev/null || true)
if [[ -z "${EFI_DEV}" ]]; then
    EFI_DEV=$(blkid -t "PARTLABEL=efi" -o device 2>/dev/null || true)
fi
if [[ -z "${EFI_DEV}" ]]; then
    # Last resort: look for the first vfat partition on the same disk
    DISK=$(lsblk -ndo PKNAME "${TARGET_DEV}" 2>/dev/null || \
           echo "${TARGET_DEV}" | sed 's/p\?[0-9]\+$//')
    EFI_DEV=$(lsblk -rno NAME,FSTYPE "/dev/${DISK}" 2>/dev/null | \
              awk '$2=="vfat"{print "/dev/"$1}' | head -1)
fi

if [[ -z "${EFI_DEV}" ]]; then
    log "ERROR: cannot locate EFI partition"
    exit 1
fi
log "EFI partition: ${EFI_DEV}"

# ── 4. Mount EFI partition rw and update grub.cfg ────────────────────────────
EFI_MOUNT=/boot/efi
GRUB_CFG="${EFI_MOUNT}/EFI/BOOT/grub.cfg"

mkdir -p "${EFI_MOUNT}"
if mountpoint -q "${EFI_MOUNT}"; then
    # Already mounted — remount rw
    mount -o remount,rw "${EFI_MOUNT}"
    REMOUNTED=1
else
    mount -t vfat "${EFI_DEV}" "${EFI_MOUNT}"
    REMOUNTED=0
fi

if [[ ! -f "${GRUB_CFG}" ]]; then
    log "WARNING: ${GRUB_CFG} not found — cannot update boot target"
else
    log "Updating grub.cfg: new slot = rootfs-${TARGET_SLOT}"
    sed -i "s|rootfs-[ab]|rootfs-${TARGET_SLOT}|g" "${GRUB_CFG}"
fi

# ── 5. Unmount / remount ro ───────────────────────────────────────────────────
if [[ "${REMOUNTED}" -eq 1 ]]; then
    mount -o remount,ro "${EFI_MOUNT}"
else
    umount "${EFI_MOUNT}"
fi

# ── 6. Cleanup ────────────────────────────────────────────────────────────────
rm -f /dev/swupdate-inactive
rm -f /run/swupdate-target-slot
rm -f /run/swupdate-target-dev

log "post-install complete: system will boot from rootfs-${TARGET_SLOT} after reboot"
