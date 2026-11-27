#!/bin/bash
# SWUpdate post-install script ─ included in every .swu package
#
# Updates /boot/cmdline.txt so that the RPi firmware boots the newly
# written slot on the next reboot.  The boot partition is remounted
# read-write for the edit, then restored to read-only.

set -euo pipefail

TARGET_SLOT=$(cat /run/swupdate-target-slot)
TARGET_DEV=$(cat  /run/swupdate-target-dev)

echo "[swupdate post-install] activating slot ${TARGET_SLOT} (${TARGET_DEV})"

# ── Resolve the PARTUUID of the target partition ──────────────────────────────
TARGET_PARTUUID=$(blkid -s PARTUUID -o value "${TARGET_DEV}")
if [[ -z "${TARGET_PARTUUID}" ]]; then
    echo "[swupdate post-install] ERROR: cannot read PARTUUID from ${TARGET_DEV}" >&2
    exit 1
fi

# ── Toggle root= in /boot/cmdline.txt ────────────────────────────────────────
mount -o remount,rw /boot
sed -i "s|root=PARTUUID=[a-fA-F0-9-]*|root=PARTUUID=${TARGET_PARTUUID}|" \
    /boot/cmdline.txt
mount -o remount,ro /boot

echo "[swupdate post-install] cmdline.txt → root=PARTUUID=${TARGET_PARTUUID}"

# ── Clean up temporary files and symlink ─────────────────────────────────────
rm -f /dev/swupdate-inactive \
      /run/swupdate-target-slot \
      /run/swupdate-target-dev
