#!/bin/bash
# pre-install.sh — SWUpdate pre-install hook for Sequoia (Intel x86 / GRUB EFI A/B)
#
# Determines the currently-active rootfs slot by reading the filesystem LABEL
# of the live root device, derives the inactive slot, then:
#   1. Creates /dev/swupdate-inactive → inactive rootfs block device
#   2. Writes /run/swupdate-target-slot   (a or b)
#   3. Writes /run/swupdate-target-dev    (e.g. /dev/sda3)
#
# The sw-description handler writes the new rootfs to /dev/swupdate-inactive.
# post-install.sh reads the saved variables to relabel and update grub.cfg.

set -euo pipefail

log() { logger -t swupdate-preinstall "$*"; echo "$*" >&2; }

# ── 1. Find the current root device ─────────────────────────────────────────
ACTIVE_DEV=$(findmnt -n -o SOURCE /)
if [[ -z "${ACTIVE_DEV}" ]]; then
    log "ERROR: cannot determine current root device"
    exit 1
fi
log "Active root device: ${ACTIVE_DEV}"

# ── 2. Determine active slot by filesystem LABEL ─────────────────────────────
ACTIVE_LABEL=$(blkid -s LABEL -o value "${ACTIVE_DEV}" 2>/dev/null || true)

if [[ -z "${ACTIVE_LABEL}" ]]; then
    # Fallback: try PARTLABEL from partition table
    ACTIVE_LABEL=$(lsblk -no PARTLABEL "${ACTIVE_DEV}" 2>/dev/null || true)
fi

log "Active label: ${ACTIVE_LABEL}"

case "${ACTIVE_LABEL}" in
    rootfs-a)
        INACTIVE_SLOT=b
        ;;
    rootfs-b)
        INACTIVE_SLOT=a
        ;;
    *)
        log "ERROR: unexpected root label '${ACTIVE_LABEL}' — expected rootfs-a or rootfs-b"
        exit 1
        ;;
esac

log "Inactive slot: ${INACTIVE_SLOT}"

# ── 3. Find the inactive rootfs block device by LABEL then PARTLABEL ─────────
INACTIVE_DEV=$(blkid -t "LABEL=rootfs-${INACTIVE_SLOT}" -o device 2>/dev/null || true)

if [[ -z "${INACTIVE_DEV}" ]]; then
    # Fallback: locate by partition label (GPT)
    INACTIVE_DEV=$(lsblk -rno NAME,PARTLABEL | \
                   awk -v label="rootfs-${INACTIVE_SLOT}" '$2==label {print "/dev/"$1}' | \
                   head -1)
fi

if [[ -z "${INACTIVE_DEV}" ]]; then
    log "ERROR: cannot find block device for inactive slot rootfs-${INACTIVE_SLOT}"
    exit 1
fi

log "Inactive rootfs device: ${INACTIVE_DEV}"

# ── 4. Publish results for SWUpdate handler and post-install ─────────────────
ln -sf "${INACTIVE_DEV}" /dev/swupdate-inactive
echo "${INACTIVE_SLOT}" > /run/swupdate-target-slot
echo "${INACTIVE_DEV}"  > /run/swupdate-target-dev

log "pre-install complete: inactive slot=${INACTIVE_SLOT} dev=${INACTIVE_DEV}"
