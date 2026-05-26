#!/bin/bash
# swupdate-bootcheck.sh — detect active slot at boot and publish it to /run.
#
# Reads the filesystem LABEL of the live root partition; maps label to slot
# letter (a or b).  Falls back to GPT PARTLABEL if the ext4 label is absent.
# Writes slot=<a|b> to /run/swupdate-active-slot for other services.

set -euo pipefail

log() { logger -t swupdate-bootcheck "$*"; }

ROOT_DEV=$(findmnt -n -o SOURCE /)
if [[ -z "${ROOT_DEV}" ]]; then
    log "ERROR: cannot determine root device"
    exit 1
fi

FS_LABEL=$(blkid -s LABEL -o value "${ROOT_DEV}" 2>/dev/null || true)
if [[ -z "${FS_LABEL}" ]]; then
    FS_LABEL=$(lsblk -no PARTLABEL "${ROOT_DEV}" 2>/dev/null || true)
fi

log "Root device ${ROOT_DEV} label=${FS_LABEL}"

case "${FS_LABEL}" in
    rootfs-a) SLOT=a ;;
    rootfs-b) SLOT=b ;;
    *)
        log "WARNING: unrecognised label '${FS_LABEL}' — defaulting to slot a"
        SLOT=a
        ;;
esac

mkdir -p /run
echo "slot=${SLOT}" > /run/swupdate-active-slot
log "Active slot: ${SLOT}"
