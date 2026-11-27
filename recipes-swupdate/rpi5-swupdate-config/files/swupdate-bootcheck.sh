#!/bin/bash
# swupdate-bootcheck — runs once per boot, logs the active slot.
#
# In a full rollback implementation an initramfs-based boot counter
# (stored in /boot/tries_remaining) would decrement on each boot attempt
# and restore the old cmdline.txt when it reaches zero.  This script
# represents the "boot confirmed" step that would reset that counter.

set -euo pipefail

ACTIVE_DEV=$(findmnt -n -o SOURCE / 2>/dev/null || echo "unknown")
case "${ACTIVE_DEV}" in
    /dev/mmcblk0p2) SLOT=a ;;
    /dev/mmcblk0p3) SLOT=b ;;
    *)              SLOT=unknown ;;
esac

logger -t swupdate-bootcheck "Boot confirmed: slot=${SLOT} dev=${ACTIVE_DEV}"
echo "slot=${SLOT}" > /run/swupdate-active-slot
